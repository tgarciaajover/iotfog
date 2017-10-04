package com.advicetec.measuredentitity;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.aggregation.oee.OEEAggregationCalculator;
import com.advicetec.aggregation.oee.OEEAggregationManager;
import com.advicetec.aggregation.oee.OverallEquipmentEffectiveness;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.TimeInterval;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.eventprocessor.PurgeFacadeCacheMapsEvent;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.Symbol;
import com.advicetec.persistence.MeasureAttributeValueCache;
import com.advicetec.persistence.StateIntervalCache;
import com.advicetec.persistence.StatusStore;
import com.advicetec.utils.PeriodUtils;
import com.advicetec.utils.PredefinedPeriod;
import com.advicetec.utils.PredefinedPeriodType;


/**
 * This class is a MeasuredEntity facade.
 * 
 * It allows the language processor to access the measured entity functionality
 * without exposing all its methods.
 *   
 * @author maldofer
 *
 */
public final class MeasuredEntityFacade {


	static Logger logger = LogManager.getLogger(MeasuredEntityFacade.class.getName());

	/**
	 *  Measure entity for which this facade is built.  
	 */
	private MeasuredEntity entity;
	
	/**
	 * Keeps the in-memory entity status, the status corresponds to the current value of all attributes. 
	 */
	private StatusStore status;
	
	/**
	 * References to measured attributes value stored in the cache. 
	 * For each attribute, we store the keys being used in the cache ordered by the datetime when they were inserted.
	 *    
	 */
	private Map<String,SortedMap<LocalDateTime,String>> attMap;
	private MeasureAttributeValueCache attValueCache;
	
	/**
	 * This map maintains references to states instances stored in the state cache.
	 * 
	 * It maintains the references ordered by datetime.    
	 */
	private SortedMap<LocalDateTime,String> statesMap;
	
	/**
	 *  Reference to the state cache where intervals are stored.
	 */
	private StateIntervalCache stateCache;
	
	/**
	 *  This field is the attribute name for the expected production rate in minutes. 
	 */
	private String productionRateId;
	
	/**
	 * This field establishes the conversion Product Unit 1 / Cycle 
	 */
	private String unit1PerCycles;
	
	/**
	 *  This field establishes the conversion Product Unit 2 / Cycle 
	 */
	private String unit2PerCycles;
	
	/**
	 *  This field is the attribute name for the production counter. 
	 */
	private String actualProductionCountId;	
	
	/**
	 *  This field establishes how often we have to remove the cache entries (seconds). 
	 */
	private Integer purgeFacadeCacheMapEntries;
	
	/**
	 * Constructor for the class.
	 *  
	 * @param entity						measure entity for which we are building the facade
	 * @param productionRateId				field used to represent the production rate 
	 * @param unit1PerCycles				field used to represent the units produced per cycle (conversion 1) 
	 * @param unit2PerCycles				field used to represent the units produced per cycle (conversion 2)
	 * @param actualProductionCountId		field used to maintain the actual production count
	 * @param purgeFacadeCacheMapEntries	how often we have to purge cache entry references.
	 */
	public MeasuredEntityFacade(MeasuredEntity entity, String productionRateId, 
								 String unit1PerCycles, String unit2PerCycles,  String actualProductionCountId, Integer purgeFacadeCacheMapEntries)
	{
		this.entity = entity;
		this.status = new StatusStore();
		this.attValueCache= MeasureAttributeValueCache.getInstance();
		
		this.attMap = new ConcurrentHashMap<String,SortedMap<LocalDateTime,String>>();
		this.statesMap = new ConcurrentSkipListMap<LocalDateTime,String>();
		
		this.stateCache = StateIntervalCache.getInstance();
		this.productionRateId = productionRateId;
		this.unit1PerCycles = unit1PerCycles;
		this.unit2PerCycles = unit2PerCycles;
		this.actualProductionCountId = actualProductionCountId;
		this.purgeFacadeCacheMapEntries = purgeFacadeCacheMapEntries;
		
		PurgeFacadeCacheMapsEvent purgeEvent = new PurgeFacadeCacheMapsEvent(entity.getId(), entity.getType());
		purgeEvent.setRepeated(true);
		purgeEvent.setMilliseconds(this.purgeFacadeCacheMapEntries * 1000);
		
		try {
			
			EventManager.getInstance().getDelayedQueue().put(new DelayEvent(purgeEvent, purgeEvent.getMilliseconds())  );
			logger.debug("Purge Event has been scheduled for measured entity:" + entity.getId());

		} catch (InterruptedException e) {
			logger.error("Error creating the purge event in the queue for measured entity:" + entity.getId());
			e.printStackTrace();
		}
		
	}

	/**
	 * Gets the measured entity
	 * 
	 * @return  measured entity
	 */
	public synchronized MeasuredEntity getEntity() {
		return entity;
	}

	/**
	 * Sets the entity referenced by this facade.
	 * @param entity	measured entity being referenced. 
	 */
	public synchronized void setEntity(MeasuredEntity entity) {
		this.entity = entity;
	}

	/**
	 * Gets the measured entity type
	 * 
	 * @return	measure entity type
	 */
	public synchronized MeasuredEntityType getType(){
		return entity.getType();
	}

	/**
	 * Sets an Attribute.
	 * 
	 * @param attribute
	 * @throws Exception If the new type of the attribute does not match the 
	 * previous type.
	 */
	public synchronized void setAttribute(Attribute attribute) throws Exception{
		// returns the previous value
		status.setAttribute(attribute);
	}

	/**
	 * Sets or updates the attribute value into the status and store.
	 * 
	 * @param attrValue attribute value to update.
	 */
	public synchronized void setAttributeValue(AttributeValue attrValue)
	{

		MeasuredAttributeValue mav = new MeasuredAttributeValue(attrValue.getAttr(), attrValue.getValue(),
				attrValue.getGenerator(), attrValue.getGeneratorType(), LocalDateTime.now());
		// stores this attributeValue into cache
		attValueCache.cacheStore(mav);
		// stores this value into status
		status.setAttributeValue(attrValue);


		// The key for a measuredAttributeValue is the name of the attribute plus the timestamp
		// The key for an attributeValue is the name of the attribute. 
		String attName = mav.getAttr().getName();
		SortedMap<LocalDateTime, String> internalMap = attMap.get(attName);
		if(internalMap == null){
			attMap.put(attName,new ConcurrentSkipListMap<LocalDateTime, String>());
			internalMap = attMap.get(attName);
		}
		internalMap.put(mav.getTimeStamp(), mav.getKey());
	}

	/**
	 * Returns the newest Measured Attribute Value for a given Attribute name.
	 * 
	 * @param attName attribute name that the user wants to return its newest value
	 * 
	 * @return Last measure attribute value created.
	 */
	public synchronized AttributeValue getNewestByAttributeName(String attName){
		return status.getAttributeValueByName(attName);
	}

	/**
	 * Returns the current state of the measured entity.
	 *  
	 * @return  If there is not entity assigned return undefined.
	 */    
	public synchronized MeasuringState getCurrentState(){
    	 if (this.entity == null){
    		 return MeasuringState.UNDEFINED;
    	 } else {
    		 return this.entity.getCurrentState();
    	 }
     }
	
	/**
	 * Imports all attribute values in valueMap into the cache and status. 
	 * This method is used whenever the language finishes and an update is required to the measured entity status. 
	 * 
	 * @param valueMap 		Values to be imported
	 * @param parent 		Measured Entity identificator to update
	 * @param parentType 	Type of the Measured Entity.
	 */
	public synchronized void importAttributeValues(Map<String, ASTNode> valueMap, Integer parent, MeasuredEntityType parentType) {

		logger.debug("entering importAttributeValues" + valueMap.size() + " attribute status count:" + status.getAttributeSize()); 

		for ( String attrName : valueMap.keySet()){

			logger.debug("importAttributeValue:" + attrName);

			Attribute att = status.getAttribute(attrName);

			if( att != null )
			{

				ASTNode node = valueMap.get(att.getName());

				if (node.isVOID()){
					logger.warn("The attribute:" + att.getName() +" is declared but it is not initialized !!!");
				} 
				else 
				{ 
					switch(att.getType()){
					case BOOLEAN:
						setAttributeValue(att, node.asBoolean(), parent, parentType);
						break;

					case INT:
						setAttributeValue(att, node.asInterger(), parent, parentType);
						break;

					case DOUBLE:
						setAttributeValue(att, node.asDouble(), parent, parentType);
						break;

					case STRING:
						setAttributeValue(att, node.asString(), parent, parentType);
						break;

					case DATETIME:
						setAttributeValue(att, node.asDateTime(), parent, parentType);
						break;

					case DATE:
						setAttributeValue(att, node.asDate(), parent, parentType);
						break;

					case TIME:
						setAttributeValue(att, node.asTime(), parent, parentType);
						break;

					default:
						logger.error("The type:" + att.getType().getName() + " of the attribute value:" + att.getName() + " is not supported");
						break;
					}
				}
			} else {
				logger.error("The attribute: " + attrName + " is not in the status");
			}

		}
	}


	/**
	 * Adds to the STATUS a new Attribute Value.
	 * 
	 * @param att 			The Attribute
	 * @param value 		The measure attribute value to add
	 * @param parent 		Id of the measured entity
	 * @param parentType 	Type of measured entity.
	 */
	public synchronized void setAttributeValue(Attribute att, Object value,Integer parent, MeasuredEntityType parentType) {

		logger.debug("inserting attribute value -attr:" + att.getName() + " value:" + value.toString() );
		setAttributeValue(new AttributeValue(att.getName(), att, value, parent, parentType));

	}	

	/**
	 * Returns a list of attribute values for a given interval.
	 * 
	 * @param attrName Attribute name.
	 * @param from Time from.
	 * @param to Time to.
	 * @return
	 */
	public synchronized List<AttributeValue> getByIntervalByAttributeName(
			String attrName, LocalDateTime from, LocalDateTime to){

		LocalDateTime oldest = attValueCache.getOldestTime();

		
		logger.debug("getByIntervalByAttributeValue from:" + from + " to:" + to);
		if(!attMap.containsKey(attrName)){
			logger.error("attribute:"+attrName+" is not in facade");
			return new ArrayList<AttributeValue>();
		}
		SortedMap<LocalDateTime,String> internalMap = attMap.get(attrName);

		// all values are in the cache
		if(oldest.isBefore(from)){
			SortedMap<LocalDateTime,String> subMap = internalMap.subMap(from, to);
			Collection<String> keys = subMap.values();
			String[] keyArray = keys.toArray( new String[keys.size()]);
			return getFromCache(keyArray);
			
		} else if(oldest.isAfter(to)) {
			// get all values from database
			return attValueCache.getFromDatabase(entity.getId(),entity.getType(),
					status.getAttribute(attrName),from, oldest);
		} else {
			SortedMap<LocalDateTime,String> subMap = 
					internalMap.subMap(oldest, to);

			Collection<String> keys = subMap.values();
			String[] keyArray = keys.toArray( new String[keys.size()]);

			ArrayList<AttributeValue> newList = attValueCache.getFromDatabase(entity.getId(),entity.getType(),
							status.getAttribute(attrName),from, oldest);
			newList.addAll(getFromCache(keyArray));

			return newList;
		}
	}


	/**
	 * Deletes old measure attribute values from attribute value map.
	 * 
	 * @param oldest
	 */
	public synchronized void deleteOldValues(LocalDateTime oldest){
		for(SortedMap<LocalDateTime, String> internalMap : attMap.values()){
			// replace the map with the last entries. 
			Set<LocalDateTime> keysToDelete = internalMap.headMap(oldest).keySet();
			for (LocalDateTime datetime : keysToDelete){
				internalMap.remove(datetime);
			}
		}
	}

	/**
	 * Deletes old states from the state map.  
	 * 
	 * @param oldest this corresponds to the oldest state that must be maintained on the map. 
	 */
	public synchronized void deleteOldStates(LocalDateTime oldest){
		Set<LocalDateTime> keysToDelete = statesMap.headMap(oldest).keySet();
		for (LocalDateTime datetime : keysToDelete){
			statesMap.remove(datetime);
		}
		
	}

	/**
	 * Generates a Json object with all the attribute values stored during the time interval defined by from and to.
	 *    
	 * @param attrName  Attribute name for which we want to return the values
	 * @param from		start of the time interval
	 * @param to		end of the time interval
	 * @return			values stored for the attribute during the time interval given as parameter. 
	 */
	public synchronized String getByIntervalByAttributeNameJSON(String attrName, LocalDateTime from, LocalDateTime to){

		List<AttributeValue> ret = getByIntervalByAttributeName(attrName, from, to);

		ObjectMapper mapper = new ObjectMapper();
		String jsonText=null;

		try {
			jsonText = mapper. writeValueAsString(ret);

		} catch (JsonGenerationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return jsonText;
	}

	/**
	 * Returns a list with the last N attribute values for a given
	 * attribute name.
	 * 
	 * @param attrName The name of the attribute.
	 * @param n The number of values for the given attribute.
	 * @return The list of Measured Attribute Values. NULL if there are not 
	 * values for the given attribute name.
	 * 
	 */
	public synchronized List<AttributeValue> getLastNbyAttributeName(String attrName, int n){
		ArrayList<AttributeValue> maValues = new ArrayList<AttributeValue>();

		if(!attMap.containsKey(attrName)){
			return null;
		}

		SortedMap<LocalDateTime,String> internalMap = attMap.get(attrName);
		String[] keyArray = (String[]) internalMap.values().toArray();
		if(n>= internalMap.size()){
			return getFromCache(keyArray);
		}
		else{
			for (int i = keyArray.length - n - 1; i < keyArray.length; i++) {
				maValues.add(attValueCache.getFromCache(keyArray[i]));
			}
			return maValues;
		}
	}


	/**
	 * Returns a list of Measured Attribute Values, from the cache,
	 * 
	 * @param keyArray 	array with the keys to retrieve
	 * @return			Measure attribute values from the cache.
	 */
	private ArrayList<AttributeValue> getFromCache(String[] keyArray){
		ArrayList<AttributeValue> maValues = new ArrayList<AttributeValue>();
		for (String key : keyArray) {
			AttributeValue value = attValueCache.getFromCache(key);
			if (value == null){
				logger.error("measure attribute with key:" + key + " not found in cache");
			} else {
				maValues.add(value);
			}
				
			
		}
		return maValues;
	}

	/**
	 * Import all attribute definitions in symbolMap into the measure entity status.
	 * 
	 * @param symbolMap		Map with the symbols to import
	 * @param origin		Identifies the origin of these symbols: behavior, transformation.
	 * @throws Exception
	 */
	public synchronized void importSymbols(Map<String, Symbol> symbolMap, AttributeOrigin origin) throws Exception {
		status.importSymbols(symbolMap, origin);
	}

	/**
	 * Gets the collection of attributes registered in the status.
	 * 
	 * @return set of attributes registered in the measured entity. 
	 */
	public synchronized Collection<Attribute> getStatus(){
		return status.getStatus();
	}

	/**
	 * Returns the measured Entity status in a JSON object.
	 *  
	 * @return json array
	 */
	public synchronized JSONArray getStatusJSON(){
		return new JSONArray(getStatusValues());
	}


	/**
	 * Facade method to import attribute values from the language to the measured entity.
	 *  
	 * @param valueMap map to import 
	 */
	public synchronized void importAttributeValues(Map<String, ASTNode> valueMap) {
		importAttributeValues(valueMap,entity.getId(),entity.getType());

	}

	public synchronized Collection<AttributeValue> getStatusValues(){
		return status.getAttributeValues();
	}

	/**
	 * Registers a new interval in the measured entity
	 * 
	 * @param status		new status for the measured entity 
	 * @param reasonCode	reason code of the new status
	 * @param interval		Time interval in which the measure entity remains in the status defined.
	 */
	public synchronized void registerInterval(MeasuringState status, ReasonCode reasonCode, TimeInterval interval)
	{
		
		// Obtains the current executed entity being processed.
		ExecutedEntity executedEntity = this.entity.getCurrentExecutedEntity();
				
		// search the production rate in the actual production job, if not defined then search on the measured entity. 
		Double rate = this.entity.getProductionRate(this.productionRateId);
		Double conversion1 = this.entity.getConversion1(this.unit1PerCycles);
		Double conversion2 = this.entity.getConversion2(this.unit2PerCycles);
		
		if (rate == null)
			rate = new Double(0.0); // No rate defined. 
		
		if (conversion1 == null) {
			conversion1 = new Double(0.0); // No conversion defined
		}
		
		if (conversion2 == null) {
			conversion2 = new Double(0.0); // No conversion defined
		}
		
		Double actualRate = null; 
		
		// Verifies that the actual production count id field is an attribute in the measuring entity
		if (!isAttribute(actualProductionCountId)) {
			logger.error("The given attribute: " + this.actualProductionCountId + " does not exists as attribute in the measuring entity");
			actualRate = new Double(0.0);
		} else {
		
			List<AttributeValue> list = getByIntervalByAttributeName(actualProductionCountId, interval.getStart(), interval.getEnd());
			
			double sum = 0;
			// Calculates the actual rate as the sum(count) / Interval.duration (minutes)
			for (AttributeValue attributeValue : list) 
			{
				
				MeasuredAttributeValue measvalue = (MeasuredAttributeValue) attributeValue;
				
				if ((measvalue.getValue() instanceof Double) || (measvalue.getValue() instanceof Integer)){
					if (measvalue.getValue() instanceof Double)
						sum = sum + (Double) measvalue.getValue();
					else
						sum = sum + (Integer) measvalue.getValue();
				} else {
					logger.error("The production count attribute: " + actualProductionCountId + " parametrized is not of type Double or Integer");
					break;
				}
			}
			LocalDateTime tempDateTime = LocalDateTime.from( interval.getStart() );
			long seconds = tempDateTime.until( interval.getEnd(), ChronoUnit.SECONDS);
			actualRate = new Double(sum * 60 / seconds);    // The actual rate is in cycle over minutes. 
		}
			
		StateInterval stateInterval = null;
		if (executedEntity != null){
			stateInterval = new StateInterval(status, reasonCode, interval, entity.getId(), entity.getType(), executedEntity.getId(), executedEntity.getType().getValue(),executedEntity.getCanonicalKey(), rate, conversion1, conversion2, actualRate, new Double(0));
		}
		else{
			stateInterval = new StateInterval(status, reasonCode, interval, entity.getId(), entity.getType(), 0, 0, "", rate, conversion1, conversion2, actualRate, new Double(0));
		}
		stateInterval.setKey(entity.getId()+stateInterval.getKey());
		// key in the map and the cache must be consistent
		statesMap.put(interval.getStart(),stateInterval.getKey());
		StateIntervalCache.getInstance().storeToCache(stateInterval);
		
	}

	/**
	 * Verifies if a particular attribute belongs to the measure entity status.
	 * 
	 * @param attrName  attribute name to verify 
	 * @return			true if the attribute name is registered, false otherwise.
	 */
	private synchronized boolean isAttribute(String attrName) {

		Attribute att = status.getAttribute(attrName);

		if( att != null )
			return true;
		else
			return false;
	}

	/**
	 * Returns an array of states of this measured entity for the given dates.
	 * 
	 * @param from 		Start date.
	 * @param to 		End date.
	 * @return 			String representation of the json information.
	 */
	public synchronized String getJsonStatesByInterval(LocalDateTime from, LocalDateTime to){

		List<StateInterval> intervals = getStatesByInterval(from, to);

		ObjectMapper mapper = new ObjectMapper();
		String jsonText=null;

		try {

			jsonText = mapper.writeValueAsString(intervals);

		} catch (JsonGenerationException e) {
			logger.error("Cannot trasform to json object");
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return jsonText;

	}

	/**
	 * Returns the json array with the format:<br>
	 * [ ... {"machine":machine,"status":sts,"startDttm":start,"endDttm":end,"reason":reason}...]
	 * 
	 * @param from 	Start date.
	 * @param to 	End date.
	 * @return Json Array of states.
	 * @throws PropertyVetoException 
	 */
	public synchronized JSONArray getJsonStates(LocalDateTime from, LocalDateTime to) {
		logger.debug("getJsonStates" + " from: " + from.toString() + " to: " + to.toString());
		JSONArray array = null;
		String cannonicalMachine = "";
		try {
			cannonicalMachine = MeasuredEntityManager.getInstance()
					.getCanonicalById(entity.getId());
			List<StateInterval> intervals = getStatesByInterval(from, to);
			array = new JSONArray();
			for (StateInterval interval : intervals) {
				if (interval != null)
				{
					//logger.info(interval.toString());
					// create the json object
					JSONObject jsob = new JSONObject();
					jsob.append("machine", cannonicalMachine);
					jsob.append("status", interval.getState().getName());
					jsob.append("startDttm", interval.getInterval().getStart().toString());
					jsob.append("endDttm", interval.getInterval().getEnd().toString());
					if (interval.getReason() != null)
						jsob.append("reason", interval.getReason().getDescription());
					else 
						jsob.append("reason", null);
					// adding the jsonObject to array
					
					jsob.append("executedObject", interval.getExecutedObject().toString());
					jsob.append("executedObjectType", interval.getExecutedObjectType().toString());
					jsob.append("executedObjectCanonical", interval.getExecutedObjectCanonical());
					array.put(jsob);
				}
			}
		} catch (SQLException e) {
			logger.error("Cannot get the cannonical machine:"+entity.getId());
			e.printStackTrace();
		}
		return array;
	}

	/**
	 * Returns Json array with the format:<br>
	 * [ ... {"machine":machine,"variable":var,"dttmStamp":stamp,"variableValue":val}...]
	 * 
	 * @param trendVar	name of the attribute marked as trend 
	 * @param from		start datetime 
	 * @param to		end datetime
	 * @return			Json array with the format specified. The array maintains measure attribute values.  
	 * @throws PropertyVetoException 
	 */
	public synchronized JSONArray getJsonTrend(String trendVar,LocalDateTime from, LocalDateTime to){
		JSONArray array = null;
		String cannonicalMachine ="";

		try {
			cannonicalMachine = MeasuredEntityManager.getInstance()
					.getCanonicalById(entity.getId());
			
			List<AttributeValue> valList = getByIntervalByAttributeName(trendVar, from, to);
			array = new JSONArray();
			for (AttributeValue attValue : valList) {
				if(attValue instanceof MeasuredAttributeValue){
					MeasuredAttributeValue mAttValue = (MeasuredAttributeValue) attValue;
					// create the json object
					JSONObject jsob = new JSONObject();
					jsob.append("machine",cannonicalMachine);
					jsob.append("variable", trendVar);
					jsob.append("dttmStamp", mAttValue.getTimeStamp().toString());
					jsob.append("variableValue", mAttValue.getValue());
					// adding jsonObject to JsonArray
					array.put(jsob);
				}
			}
		} catch (SQLException e) {
			logger.error("Cannot get the cannonical machine:"+entity.getId());
			e.printStackTrace();
		}
		return array;
	}

	/**
	 * Returns the Json representation of a set of states that belong to the interval given as parameter.
	 * 
	 * @param interval  start and end datetime
	 * 
	 * @return	json array in string.
	 */
	public synchronized String statesByInterval(TimeInterval interval){
		List<StateInterval> intervals = getStatesByInterval(interval.getStart(), interval.getEnd());

		ObjectMapper mapper = new ObjectMapper();
		String jsonText=null;

		try {

			jsonText = mapper. writeValueAsString(intervals);

		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return jsonText;
	}


	/**
	 * Returns the list of intervals between two datetimes.
	 * 
	 * @param from 		Begin time
	 * @param to 		End time.
	 * 
	 * @return List of intervals.
	 */
	public synchronized List<StateInterval> getStatesByInterval(LocalDateTime from, LocalDateTime to){

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		String formattedFrom = from.format(formatter);
		String formattedTo = to.format(formatter);
		logger.info("getStatesByInterval from:" + formattedFrom + " to:" + formattedTo );
		
		List<StateInterval> list = new ArrayList<StateInterval>();
		LocalDateTime oldest = stateCache.getOldestTime();

		// all values are in the cache
		if(oldest.isBefore(from)){
			SortedMap<LocalDateTime, String> subMap = statesMap.subMap(from, to);
			for (Map.Entry<LocalDateTime, String> entry : subMap.entrySet()) {
				list.add(stateCache.getFromCache(entry.getValue()));
			}
		} else if(oldest.isAfter(to)){
			// all values are in the database 
			list.addAll(stateCache.getFromDatabase(entity.getId(),entity.getType(),from,to));
		} else {

			// get from database
			list.addAll(stateCache.getFromDatabase(entity.getId(),entity.getType(),from,oldest));

		     // get from cache
			SortedMap<LocalDateTime, String> subMap = statesMap.subMap(oldest, to);
			for (Map.Entry<LocalDateTime, String> entry : subMap.entrySet()) {
				list.add(stateCache.getFromCache(entry.getValue()));
			}


		}
		return list;
	}


	/**
	 * Commands the cache to store all intervals in the database and cleans the cache.
	 * 
	 * This method is used when the measured facade must be removed because the measured entity is deleted.
	 */
	public synchronized void storeAllStateIntervals(){
		LocalDateTime oldest = stateCache.getOldestTime();
		
		deleteOldStates(oldest);
		
		stateCache.bulkCommit(new ArrayList<String>(statesMap.values()));
	}
	
	
	/**
	 * Method to remove internal references for both caches that are out of date.
	 */
	public synchronized void removeOldCacheReferences()
	{
		// Remove References from the attribute cache.
		LocalDateTime oldestAttr = attValueCache.getOldestTime();		
		deleteOldValues(oldestAttr);

		
		// Delete References from the state cache.
		LocalDateTime oldestState = stateCache.getOldestTime();		
		deleteOldStates(oldestState);
		
	}
	/***
	 * Command to order the cache to store all measured attribute values associated to the measured 
	 * entity into the database and cleans itself. 
	 */
	public synchronized void storeAllMeasuredAttributeValues(){
		LocalDateTime oldest = attValueCache.getOldestTime();
		
		deleteOldValues(oldest);

		ArrayList<String> keys = new ArrayList<String>();
		
		for(SortedMap<LocalDateTime, String> internalMap : attMap.values()){
			// replace the map with the last entries. 
			keys.addAll(internalMap.values());
		}
		attValueCache.bulkCommit(keys);
	}

	
	/**
	 * Returns the Entity Status into a XML document.
	 * 
	 * @return
	 * @throws ParserConfigurationException
	 * @throws JAXBException
	 */
	public Document getXmlStatus() throws ParserConfigurationException, JAXBException{
		return status.toXml();
	}

	
	/**
	 * Gets registered downtown reasons in an interval. The response is a json array with downtime reason 
	 * fulfilling the date interval.
	 * 
	 * @param from	start datetime 
	 * @param to	end datetime
	 * 
	 * @return  list of downtime reasons.
	 */
	public synchronized JSONArray getJsonDowntimeReasons(LocalDateTime from,	LocalDateTime to) 
	{
		logger.debug("In getJsonDowntimeReasons + from:" + from.toString() + " to:" + to.toString());
		
		JSONArray array = null;
		List<DowntimeReason> list = getDowntimeReasons(from, to);

		array = new JSONArray();
		for (DowntimeReason reason : list) {
			// create the json object
			JSONObject jsob = new JSONObject();
			jsob.append("machine",reason.getMachine());
			jsob.append("reason",reason.getReason());
			jsob.append("reasonDescr",reason.getReasonDescr());
			jsob.append("ocurrences", reason.getOccurrences());
			jsob.append("durationMinutes", reason.getDurationMinutos());
			// adding jsonObject to JsonArray
			array.put(jsob);
		}
		return array;
	}

	/**
	 * Get the list of downtime reasons within an interval.
	 * 
	 * @param from	start datetime 
	 * @param to	end datetime
	 * 
	 * @return	list of downtime reasons.
	 */
	private List<DowntimeReason> getDowntimeReasons(LocalDateTime from,	LocalDateTime to){
	
		List<StateInterval> list = new ArrayList<StateInterval>();
		LocalDateTime oldest = stateCache.getOldestTime();

		List<DowntimeReason> reasons = new ArrayList<DowntimeReason>();
		
		// all values are in the cache
		if(oldest.isBefore(from))
		{
			logger.debug("downtime reason cache only");	
			SortedMap<LocalDateTime, String> subMap = statesMap.subMap(from, to);
			for (Map.Entry<LocalDateTime, String> entry : subMap.entrySet()) {
				list.add(stateCache.getFromCache(entry.getValue()));
			}
			reasons.addAll(sumarizeDowntimeReason(list).values());
		} 
		else if(oldest.isAfter(to))
		{
			logger.debug("downtime reason database only");
			// all values are in the database 
			reasons.addAll(stateCache.getDownTimeReasonsByInterval(this.entity,from,to).values());
		} 
		else 
		{
			logger.debug("downtime reason mixed cache - database");
		     // get from cache
			SortedMap<LocalDateTime, String> subMap = statesMap.subMap(oldest, to);
			for (Map.Entry<LocalDateTime, String> entry : subMap.entrySet()) {
				list.add(stateCache.getFromCache(entry.getValue()));
			}
			Map<Integer, DowntimeReason> temp = sumarizeDowntimeReason(list);
			
			// get from database
			Map<Integer, DowntimeReason> temp2 = stateCache.getDownTimeReasonsByInterval(this.entity,from,oldest);
			for (Integer k : temp.keySet()) {
				if(temp2.containsKey(k)){
					DowntimeReason dtr = temp2.remove(k);
					temp.get(k).setOccurrences(dtr.getOccurrences() + temp.get(k).getOccurrences());
					temp.get(k).setMinDuration(dtr.getDurationMinutos() + temp.get(k).getDurationMinutos());
				}
			}
			reasons.addAll(temp.values());
			reasons.addAll(temp2.values());
		}

		logger.debug("num Reasons:" + reasons.size());
		return reasons;
	}
	
	
	/**
	 * 
	 * @param list
	 * @return
	 */
	private Map<Integer,DowntimeReason> sumarizeDowntimeReason(List<StateInterval> list) {
		Map<Integer,DowntimeReason> map = new HashMap<Integer, DowntimeReason>();
		DowntimeReason reason = null;
		for (StateInterval interval : list) {
			// all operatives have null
			if (interval == null){
				logger.error("the interval in the downtime reason is null" );
			} else {
				if(interval.getReason() != null){
					if(map.containsKey(interval.getReason().getId())){
						reason = map.get(interval.getReason().getId());
						reason.setMinDuration(reason.getDurationMinutos() + interval.getDurationMin());
						reason.setOccurrences(reason.getOccurrences() + 1);
					}else{
						if(getEntity().getType() == MeasuredEntityType.MACHINE){
							Machine machine = (Machine) getEntity();
							reason = new DowntimeReason(machine.getCannonicalMachineId(), 
									interval.getReason().getCannonicalReasonId(), 
									interval.getReason().getDescription(), 
									1, 
									interval.getDurationMin());
							map.put(interval.getReason().getId(), reason);
						}
					}
				}
			}
		}
		return map;
	}
	
	/**
	 * Adds a new executed object to the measured entity. 
	 * This is equivalent to saying that the executing object is being processed in this measured entity
	 * 
	 * @param executedEntity  executed object to add.
	 */
	public synchronized void addExecutedObject(ExecutedEntity executedEntity)
	{
		this.entity.addExecutedEntity(executedEntity);
		
		ExecutedEntityChange();
	}
	
	/**
	 * Stops all executed object that were previously executed in this measured entity. 
	 */
	public synchronized void stopExecutedObjects()
	{
		this.entity.stopExecuteEntities();
	}
	
	/**
	 * Stops an executed object that was previously executed in this measured entity.
	 * 
	 * @param id identifier of the executed object to remove.
	 */
	public synchronized void removeExecutedObject(Integer id)
	{
		this.entity.removeExecutedEntity(id);
		
		ExecutedEntityChange();
	}

	/**
	 * Get an attribute value defined in an executed object.
	 * 
	 * @param attributeId	attribute name for which we want to obtain its value
	 * 
	 * @return	attribute value registered.
	 */
	public synchronized AttributeValue getExecutedObjectAttribute(String attributeId)
	{
		return this.entity.getAttributeFromExecutedObject(attributeId);
	}

	/**
	 * Updates the state of the measure entity taking as parameter 
	 * the measured attributes resulting from a transformation or behavior execution 
	 * 
	 * @param symbolMap	symbols generated by the transformation or behavior execution.
	 */
	public synchronized void setCurrentState(Map<String, ASTNode> symbolMap) {

		for (Map.Entry<String, ASTNode> entry : symbolMap.entrySet()) 
		{
			if(entry.getKey().compareTo("state") == 0 ){
				ASTNode node = entry.getValue();
				Integer newState = node.asInterger();
				 
				if (newState == 0){
					if ((this.entity.getCurrentState() != MeasuringState.OPERATING) || (this.entity.startNewInterval())) {
						LocalDateTime localDateTime = LocalDateTime.now();
						TimeInterval interval = new TimeInterval(this.entity.getCurrentStatDateTime(), localDateTime);
						this.registerInterval(this.entity.getCurrentState(), this.entity.getCurrentReason(), interval);
						this.entity.startInterval(localDateTime, MeasuringState.OPERATING, null);
					}
				} else if (newState == 1){
					
					if ((this.entity.getCurrentState() != MeasuringState.SCHEDULEDOWN) || (this.entity.startNewInterval())) {
						LocalDateTime localDateTime = LocalDateTime.now();
						TimeInterval interval = new TimeInterval(this.entity.getCurrentStatDateTime(), localDateTime);
						this.registerInterval(this.entity.getCurrentState(), this.entity.getCurrentReason(), interval);
						this.entity.startInterval(localDateTime, MeasuringState.SCHEDULEDOWN, null);						
					}
					
				} else if (newState == 2){
					
					if ((this.entity.getCurrentState() != MeasuringState.UNSCHEDULEDOWN) || (this.entity.startNewInterval())) {
						LocalDateTime localDateTime = LocalDateTime.now();
						TimeInterval interval = new TimeInterval(this.entity.getCurrentStatDateTime(), localDateTime);
						this.registerInterval(this.entity.getCurrentState(), this.entity.getCurrentReason(), interval);
						this.entity.startInterval(localDateTime, MeasuringState.UNSCHEDULEDOWN, null);						
					}
					
				} else {
					logger.error("The new state is being set to undefined, which is incorrect");
				}	
				
			}
		}

	}
	
	/**
	 * Change the executing entity being processed in the measure entity. Registers the corresponding interval representing the change. 
	 */
	public synchronized void ExecutedEntityChange(){
		
		LocalDateTime localDateTime = LocalDateTime.now();
		TimeInterval interval = new TimeInterval(this.entity.getCurrentStatDateTime(), localDateTime);
		this.registerInterval(this.entity.getCurrentState(), this.entity.getCurrentReason(), interval);
		this.entity.startInterval(localDateTime, this.entity.getCurrentState(), null);						

	}

	/**
	 * Gets the OEE for the measured entity within the interval given as parameter  
	 * 
	 * @param dttmFrom		start datetime 
	 * @param dttmTo		end datetime
	 * @param reqInterval	specifies the granurality required for the response.
	 * 
	 * @return	Array of OEEs calculated with the granurality defined by reqInterval.  
	 */
	public synchronized JSONArray getOverallEquipmentEffectiveness(LocalDateTime dttmFrom, LocalDateTime dttmTo, String reqInterval) {
				
        // Bring different predefined periods required
		List<PredefinedPeriod> periods = PeriodUtils.getPredefinedPeriods( dttmFrom, dttmTo, reqInterval ); 
		
		OEEAggregationManager oeeAggregation = OEEAggregationManager.getInstance();
		List<OverallEquipmentEffectiveness> oees = new ArrayList<OverallEquipmentEffectiveness>();
		
		logger.debug("Number of elements to calculate in the final list:" + periods.size());
		
		// loop through the different intervals and calculate total schedule downtime, availability loss, etc..
		for (int i = 0; i < periods.size(); i++)
		{
					
			PredefinedPeriod period = periods.get(i);
			
			logger.debug("Period Type:" + period.getType().getName());
			
			if (period.getType() == PredefinedPeriodType.INT_LT_HOUR)
			{
				
				// Search for intervals in the requested hour.
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
				String parQueryFrom = formatter.format(period.getCalendarFrom().getTime());
				String parQueryTo = formatter.format(period.getCalendarTo().getTime());
				
				PredefinedPeriod periodTmp = new PredefinedPeriod(period.getCalendarFrom().get(Calendar.YEAR), 
						period.getCalendarFrom().get(Calendar.MONTH) +1,
						period.getCalendarFrom().get(Calendar.DAY_OF_MONTH),
						period.getCalendarFrom().get(Calendar.HOUR_OF_DAY)); 
				
				List<OverallEquipmentEffectiveness> oeesHour = oeeAggregation.getOeeAggregationContainer().intervalsByHour(
															this.getEntity().getId(), this.getEntity().getType(), periodTmp.getKey(), parQueryFrom, parQueryTo);
				
				if (oeesHour.size() == 0) {
					logger.error("The aggregation interval could not be calculated predefined Period:" + parQueryFrom );
				} else {
					oees.addAll(oeesHour);
				}
				
			} else if ( period.getType() == PredefinedPeriodType.HOUR ){
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					logger.debug("calculating oee for hour");
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateHour(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), false, false));
				}
			} else if ( period.getType() == PredefinedPeriodType.DAY ) {
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateDay(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), false, false));
				}
				
			} else if ( period.getType() == PredefinedPeriodType.MONTH ) {
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateMonth(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), false, false));
				}
				
			} else if ( period.getType() == PredefinedPeriodType.YEAR )  {
				
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateYear(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), false, false));
				}
									
			} else {
				logger.error("Invalid Predefined Period type:" + period.getType().getName());			
			}			
		}
			
		logger.debug("Number of elements in the final list:" + oees.size());

		JSONArray array = null;
		array = new JSONArray();
		for (OverallEquipmentEffectiveness oee : oees) {
			// create the json object
			JSONObject jsob = new JSONObject();
			jsob.append("start_dttm", oee.getStartDttm());
			jsob.append("end_dttm", oee.endDttm());
			jsob.append("available_time",oee.getAvailableTime());
			jsob.append("productive_time",oee.getProductiveTime());
			jsob.append("qty_sched_to_produce",oee.getQtySchedToProduce());
			jsob.append("qty_produced",oee.getQtyProduced());
			jsob.append("qty_defective",oee.getQtyDefective());
			
			double part1 = 0;
			double part2 = 0;
			double part3 = 0;
			
			if (oee.getAvailableTime() != 0) {
				part1 = (oee.getProductiveTime() / oee.getAvailableTime());
			} else {
				part1 = 1;
			}
			
			if (oee.getQtySchedToProduce() != 0) {
				part2 = (oee.getQtyProduced() / oee.getQtySchedToProduce() );
			} else {
				part2 = 1;
			}
			
			if (oee.getQtyProduced() != 0) {
				part3 = ((oee.getQtyProduced() - oee.getQtyDefective()) / oee.getQtyProduced() );
			} else {
				part3 = 1;
			}
			
			double oeeValue =  part1 * part2 * part3;   
			oeeValue = oeeValue * 100; 
			
			logger.debug("oee" + Double.toString(oeeValue));
			
			jsob.append("oee", new Double(oeeValue));
			
			// adding jsonObject to JsonArray
			array.put(jsob);
		}
		
		return array;
	}

	/**
	 * This method returns a JSON Array with those attributes marked as trend in the language.
	 */
	public synchronized JSONArray getJsonAttributeTrend() {
		
		logger.debug("In getJsonAttributeTrend");
		
		List<Attribute> trendAttributes = status.getTrendAttributes();
		JSONArray array = null;
		array = new JSONArray();
		for (Attribute attribute : trendAttributes) {
			JSONObject jsob = new JSONObject();
			jsob.append("name", attribute.getName());
			jsob.append("type", attribute.getType());
			jsob.append("unit", attribute.getUnit());
			jsob.append("origin", attribute.getOrigin());
			// adding jsonObject to JsonArray
			array.put(jsob);			
		}
		return array;
	}

	/**
	 * Update a previously defined stat, assigning its reason code. 
	 *  
	 * @param startDttmStr	start datetime when the interval to update started - this value works as a key for the interval states. 
	 * @param reasonCode	Reason code to assign.
	 * 	
	 * @return	true if the intervals was found and updated, false otherwise.
	 */
	public synchronized boolean updateStateInterval(String startDttmStr, ReasonCode reasonCode) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		LocalDateTime startDttm = LocalDateTime.parse(startDttmStr, formatter);
		
		logger.debug("reasoncode id:" + reasonCode.getId() + " descr:" + reasonCode.getDescription() + " startDttm:" + startDttm );
		
		boolean ret = false;
		
		if (this.entity.getCurrentStatDateTime().equals(startDttm)){
			
			logger.debug("Updating the current state interval");
			
			this.entity.setCurrentReasonCode(reasonCode);
			ret = true;
		} else {

			logger.debug("Updating the past state interval");
			
			LocalDateTime oldest = stateCache.getOldestTime();
			
			logger.debug("oldest" + oldest.format(formatter));
			
			// all values are in the cache
			if(oldest.isBefore(startDttm))
			{
				logger.debug("the datetime given is before");
				String stateKey = statesMap.get(startDttm);
				logger.debug("State key found:" + stateKey);
				ret = stateCache.updateCacheStateInterval(stateKey, reasonCode);
			} else if(oldest.isAfter(startDttm)){
				logger.debug("the datetime given is after");
				// all values are in the database 
				ret = stateCache.updateStateInterval(this.entity.getId(), this.entity.getType(), startDttm, reasonCode);				
			}
		}
		
		return ret;
	}
	
}

