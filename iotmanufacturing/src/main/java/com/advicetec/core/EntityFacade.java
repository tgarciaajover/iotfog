package com.advicetec.core;


import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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
import com.advicetec.core.EntityFacade;
import com.advicetec.core.TimeInterval;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.eventprocessor.PurgeFacadeCacheMapsEvent;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.Symbol;
import com.advicetec.measuredentitity.DowntimeReason;
import com.advicetec.measuredentitity.ExecutedEntity;
import com.advicetec.measuredentitity.Machine;
import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.MeasuringState;
import com.advicetec.measuredentitity.StateInterval;
import com.advicetec.persistence.MeasureAttributeValueCache;
import com.advicetec.persistence.StateIntervalCache;
import com.advicetec.persistence.StatusStore;
import com.advicetec.utils.PeriodUtils;
import com.advicetec.utils.PredefinedPeriod;
import com.advicetec.utils.PredefinedPeriodType;


/**
 * This class is a Entity facade.
 * 
 * It allows the language processor to access the measured entity functionality
 * without exposing all its methods.
 *   
 * @author maldofer
 *
 */
public abstract class EntityFacade  {


	static Logger logger = LogManager.getLogger(EntityFacade.class.getName());

	/**
	 *  entity for which this facade is built.  
	 */
	protected Entity entity;
	
	/**
	 * Keeps the in-memory entity status, the status corresponds to the current value of all attributes. 
	 */
	protected StatusStore status;
	
	/**
	 * References to measured attributes value stored in the cache. 
	 * For each attribute, we store the keys being used in the cache ordered by the datetime when they were inserted.
	 */
	protected Map<String,SortedMap<LocalDateTime,String>> attMap;
	
	protected MeasureAttributeValueCache attValueCache;
	
	/**
	 * This map maintains references to states instances stored in the state cache.
	 * 
	 * It maintains the references ordered by datetime.    
	 */
	protected SortedMap<LocalDateTime,String> statesMap;

	/**
	 *  Reference to the state cache where intervals are stored.
	 */
	protected StateIntervalCache stateCache;
	
	/**
	 *  This field establishes how often we have to remove the cache entries (seconds). 
	 */
	protected Integer purgeFacadeCacheMapEntries;
	
	/**
	 * Constructor for the class.
	 *  
	 * @param entity						Entity for which we are building the facade
	 * @param purgeFacadeCacheMapEntries	how often we have to purge cache entry references.
	 */
	public EntityFacade(Entity entity, Integer purgeFacadeCacheMapEntries)
	{
		logger.debug("EntityFacade Start " + String.valueOf(entity.getId()));
		logger.debug("Creating entity facade");
		
		this.entity = entity;
		this.status = new StatusStore();
		this.attValueCache= MeasureAttributeValueCache.getInstance();

		
		this.attMap = new ConcurrentHashMap<String,SortedMap<LocalDateTime,String>>();
		this.statesMap = new ConcurrentSkipListMap<LocalDateTime,String>();
		
		this.stateCache = StateIntervalCache.getInstance();
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
		logger.debug("EntityFacade End " + String.valueOf(entity.getId()));
		logger.debug("Finish Creating entity facade");
		
	}

	/**
	 * Gets the measured entity
	 * 
	 * @return  measured entity
	 */
	public synchronized Entity getEntity() {
		logger.debug("getEntity called");
		return entity;
	}

	/**
	 * Sets the entity referenced by this facade.
	 * @param entity	measured entity being referenced. 
	 */
	public synchronized void setEntity(Entity entity) {
		logger.debug("setEntity Start " + String.valueOf(entity.getId()));
		this.entity = entity;
		logger.debug("setEntity End " + String.valueOf(entity.getId()));
	}

	/**
	 * Gets the measured entity type
	 * 
	 * @return	measure entity type
	 */
	public synchronized MeasuredEntityType getType(){
		logger.debug("getType called");
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
		logger.debug("setAttribute Start");
		status.setAttribute(attribute);
		logger.debug("setAttribute End");
	}

	/**
	 * Sets or updates the attribute value into the status and store.
	 * 
	 * @param attrValue attribute value to update.
	 */
	public synchronized void setAttributeValue(AttributeValue attrValue)
	{
		logger.debug("setAttributeValue Start");
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
		logger.debug("setAttributeValue End");
	}

	/**
	 * Returns the newest Measured Attribute Value for a given Attribute name.
	 * 
	 * @param attName attribute name that the user wants to return its newest value
	 * 
	 * @return Last measure attribute value created.
	 */
	public synchronized AttributeValue getNewestByAttributeName(String attName){
		logger.debug("getNewestByAttributeName called");
		return status.getAttributeValueByName(attName);
	}

	
	/**
	 * Imports all attribute values in valueMap into the cache and status. 
	 * This method is used whenever the language finishes and an update is required to the measured entity status. 
	 * 
	 * @param valueMap 		Values to be imported
	 */
	public synchronized void importAttributeValues(Map<String, ASTNode> valueMap) {
		
		logger.debug("importAttributeValues Start");
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
						setAttributeValue(att, node.asBoolean());
						break;

					case INT:
						setAttributeValue(att, node.asInterger());
						break;

					case DOUBLE:
						setAttributeValue(att, node.asDouble());
						break;

					case STRING:
						setAttributeValue(att, node.asString());
						break;

					case DATETIME:
						setAttributeValue(att, node.asDateTime());
						break;

					case DATE:
						setAttributeValue(att, node.asDate());
						break;

					case TIME:
						setAttributeValue(att, node.asTime());
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
		logger.debug("importAttributeValues End");
	}


	/**
	 * Adds to the STATUS a new Attribute Value.
	 * 
	 * @param att 			The Attribute
	 * @param value 		The measure attribute value to add
	 * @param parent 		Id of the measured entity
	 * @param parentType 	Type of measured entity.
	 */
	public synchronized void setAttributeValue(Attribute att, Object value) {
		logger.debug("setAttributeValue Start");
		logger.debug("inserting attribute value -attr:" + att.getName() + " value:" + value.toString() );
		setAttributeValue(new AttributeValue(att.getName(), att, value, getEntity().getId(), getEntity().getType()));
		logger.debug("setAttributeValue End");
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
		logger.debug("getByIntervalByAttributeName Start");
		LocalDateTime oldest = attValueCache.getOldestTime();

		
		logger.debug("getByIntervalByAttributeValue from:" + from + " to:" + to);
		if(!attMap.containsKey(attrName)){
			logger.debug("attribute:"+attrName+" is not in facade");
			List<AttributeValue> newList = new ArrayList<AttributeValue>();
			logger.debug("getByIntervalByAttributeName End");
			return newList;
		}
		SortedMap<LocalDateTime,String> internalMap = attMap.get(attrName);

		// all values are in the cache
		if(oldest.isBefore(from)){
			SortedMap<LocalDateTime,String> subMap = internalMap.subMap(from, to);
			Collection<String> keys = subMap.values();
			String[] keyArray = keys.toArray( new String[keys.size()]);
			List<AttributeValue> newList = getFromCache(keyArray);
			logger.debug("getByIntervalByAttributeName End");
			return newList;
			
		} else if(oldest.isAfter(to)) {
			// get all values from database
			List<AttributeValue> newList = attValueCache.getFromDatabase(entity.getId(),entity.getType(),
					status.getAttribute(attrName),from, oldest);
			logger.debug("getByIntervalByAttributeName End");
			return newList;
		} else {
			SortedMap<LocalDateTime,String> subMap = 
					internalMap.subMap(oldest, to);

			Collection<String> keys = subMap.values();
			String[] keyArray = keys.toArray( new String[keys.size()]);

			ArrayList<AttributeValue> newList = attValueCache.getFromDatabase(entity.getId(),entity.getType(),
							status.getAttribute(attrName),from, oldest);
			newList.addAll(getFromCache(keyArray));
			logger.debug("getByIntervalByAttributeName End");
			return newList;
		}
	}
	
	/**
	 * Return the temporality of rates from a measured entity.
	 *
	 * @return a string with the temporality of rates.
	 */
	public synchronized String getTemporalityRate(){
		logger.debug("in getTemporalityRate");
		String temporality = attValueCache.getTemporality(entity.getId());
		return temporality;
	}
	
	/**
	 * Return the variable name of actual rate.
	 *
	 * @return Variable name.
	 */
	public synchronized String getActualRateVariableName(){
		logger.debug("in getActualRateVariableName");
		String variableName = attValueCache.getVariableName(entity.getId());
		return variableName;
	}


	/**
	 * Deletes old measure attribute values from attribute value map.
	 * 
	 * @param oldest
	 */
	public synchronized void deleteOldValues(LocalDateTime oldest){
		logger.debug("deleteOldValues Start");
		for(SortedMap<LocalDateTime, String> internalMap : attMap.values()){
			// replace the map with the last entries. 
			Set<LocalDateTime> keysToDelete = internalMap.headMap(oldest).keySet();
			for (LocalDateTime datetime : keysToDelete){
				internalMap.remove(datetime);
			}
		}
		logger.debug("deleteOldValues End");
	}

	/**
	 * Deletes old states from the state map.  
	 * 
	 * @param oldest this corresponds to the oldest state that must be maintained on the map. 
	 */
	public synchronized void deleteOldStates(LocalDateTime oldest){
		logger.debug("deleteOldStates Start");
		Set<LocalDateTime> keysToDelete = statesMap.headMap(oldest).keySet();
		for (LocalDateTime datetime : keysToDelete){
			statesMap.remove(datetime);
		}
		logger.debug("deleteOldStates End");
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
		logger.debug("getByIntervalByAttributeNameJSON Start");
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
		logger.debug("getByIntervalByAttributeNameJSON End");
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
		logger.debug("getLastNbyAttributeName Start");
		ArrayList<AttributeValue> maValues = new ArrayList<AttributeValue>();

		if(!attMap.containsKey(attrName)){
			logger.debug("getLastNbyAttributeName End");
			return null;
		}

		SortedMap<LocalDateTime,String> internalMap = attMap.get(attrName);
		String[] keyArray = (String[]) internalMap.values().toArray();
		if(n>= internalMap.size()){
			maValues = getFromCache(keyArray);
			logger.debug("getLastNbyAttributeName End");
			return maValues;
		} else{
			for (int i = keyArray.length - n - 1; i < keyArray.length; i++) {
				maValues.add(attValueCache.getFromCache(keyArray[i]));
			}
			logger.debug("getLastNbyAttributeName End");
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
		logger.debug("getFromCache Start");
		ArrayList<AttributeValue> maValues = new ArrayList<AttributeValue>();
		for (String key : keyArray) {
			AttributeValue value = attValueCache.getFromCache(key);
			if (value == null){
				logger.error("measure attribute with key:" + key + " not found in cache");
			} else {
				maValues.add(value);
			}
				
			
		}
		logger.debug("getFromCache End");
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
		logger.debug("importSymbols Start");
		status.importSymbols(symbolMap, origin);
		logger.debug("importSymbols End");
	}

	/**
	 * Gets the collection of attributes registered in the status.
	 * 
	 * @return set of attributes registered in the measured entity. 
	 */
	public synchronized Collection<Attribute> getStatus(){
		logger.debug("getStatus called");
		return status.getStatus();
	}

	/**
	 * Returns the measured Entity status in a JSON object.
	 *  
	 * @return json array
	 */
	public synchronized JSONArray getStatusJSON(){
		logger.debug("getStatusJSON called");
		return new JSONArray(getStatusValues());
	}


	public synchronized Collection<AttributeValue> getStatusValues(){
		logger.debug("getStatusValues called");
		return status.getAttributeValues();
	}


	/**
	 * Verifies if a particular attribute belongs to the measure entity status.
	 * 
	 * @param attrName  attribute name to verify 
	 * @return			true if the attribute name is registered, false otherwise.
	 */
	public synchronized boolean isAttribute(String attrName) {
		logger.debug("isAttribute Start");
		Attribute att = status.getAttribute(attrName);

		if( att != null ) {
			logger.debug("isAttribute End");
			return true;
		}
		else {
			logger.debug("isAttribute End");
			return false;
		}
	}

	/**
	 * Returns an array of states of this measured entity for the given dates.
	 * 
	 * @param from 		Start date.
	 * @param to 		End date.
	 * @return 			String representation of the json information.
	 */
	public synchronized String getJsonStatesByInterval(LocalDateTime from, LocalDateTime to){
		logger.debug("getJsonStatesByInterval Start");
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
		
		logger.debug("getJsonStatesByInterval End");
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
		logger.debug("getJsonStates Start");
		logger.debug("getJsonStates" + " from: " + from.toString() + " to: " + to.toString());
		JSONArray array = null;
		String cannonicalMachine = "";
			
		cannonicalMachine = entity.getCanonicalKey();

		List<StateInterval> intervals = getStatesByInterval(from, to);
		array = new JSONArray();
		for (StateInterval interval : intervals) {
			if (interval != null)
			{
				List<AttributeValue> valuesEmp;
				List<AttributeValue> valuesEng;
				String emp;
				String eng;
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

				jsob.append("executedObject", interval.getRelatedObject().toString());
				jsob.append("executedObjectType", interval.getRelatedObjectType().toString());
				jsob.append("executedObjectCanonical", interval.getExecutedObjectCanonical());
				valuesEmp = this.getByIntervalByAttributeName("empacadora", interval.getInterval().getStart(), interval.getInterval().getEnd());
				if (valuesEmp.size() == 0) {
					emp = "N";
				} else {
					emp = "Y";
				}
				valuesEng = this.getByIntervalByAttributeName("gasto_energia", interval.getInterval().getStart(), interval.getInterval().getEnd());
				if (valuesEng.size() == 0) {
					eng = "N";
				} else {
					eng = "Y";
				}
				jsob.append("withPacking", emp);
				jsob.append("wasteEnergy", eng);
				jsob.append("productionRate", interval.getProductionRate());
				jsob.append("actualProductionRate", interval.getActualProductionRate());
				array.put(jsob);
			}
		}
		logger.debug("getJsonStates End");
		return array;
	}

	
	
	public synchronized double getEntityAvailability(LocalDateTime from){
		
		PredefinedPeriod predefinedPeriod = new PredefinedPeriod(from.getYear(), from.getMonthValue(), from.getDayOfMonth(), from.getHour(), from.getMinute());
		double availability = 0;
		logger.info(predefinedPeriod.getKey());
		OEEAggregationManager oeeAggregation = OEEAggregationManager.getInstance();
		availability = oeeAggregation.getOeeAggregationContainer().getPeriodAvailabilityOEE(this.getEntity().getId(), this.getEntity().getType(), predefinedPeriod);
		return availability;
	}
	
	/**
	 * Recalculate the OEE for the measured entity within the interval given as parameter  
	 * 
	 * @param dttmFrom		start date time 
	 * @param dttmTo		end date time
	 * @param reqInterval	specifies the granularity required for the response.
	 */
	public synchronized void calculateOverallEquipmentEffectiveness(String company, String location, String plant, 
			String machineGroup, String machineId, String year, String month, String day, String productionOrder) {
		OEEAggregationManager oeeAggregation = OEEAggregationManager.getInstance();
		
		oeeAggregation.getOeeAggregationContainer().calculatePeriodOEE(company, location, plant, machineGroup, machineId, 
				year, month, day, productionOrder, "N");		
	}
	
	public synchronized LocalDateTime getBeginDttmCurrentShift(){
		logger.debug("getBeginDttmCurrentShift Start");
		LocalDateTime beginpivot = LocalDate.now().atTime(7, 0);
		LocalDateTime endpivot = LocalDate.now().atTime(19, 0);
		
		if(LocalDateTime.now().isBefore(beginpivot)){
			return beginpivot.minusHours(12);
		}
		if(LocalDateTime.now().isAfter(beginpivot) && LocalDateTime.now().isBefore(endpivot)){
			return beginpivot;			
		}
		if(LocalDateTime.now().isAfter(endpivot)){
			return endpivot;
		}
		if(LocalDateTime.now().isEqual(beginpivot)){
			return beginpivot.minusHours(12);
		}
		if(LocalDateTime.now().isEqual(endpivot)){
			return beginpivot;			
		}
		logger.debug("getBeginDttmCurrentShift End");
		return null;
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
		logger.debug("getJsonTrend Start");
		JSONArray array = null;
		String cannonicalMachine ="";
			
		cannonicalMachine = entity.getCanonicalKey();

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
		logger.debug("getJsonTrend End");
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
		logger.debug("statesByInterval Start");
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
		logger.debug("statesByInterval End");
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
		logger.debug("getStatesByInterval Start");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		String formattedFrom = from.format(formatter);
		String formattedTo = to.format(formatter);
		
		List<StateInterval> list = new ArrayList<StateInterval>();
		LocalDateTime oldest = stateCache.getOldestTime();
		LocalDateTime current = LocalDateTime.now();

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
		logger.debug("getStatesByInterval End");
		return list;
	}


	/**
	 * Commands the cache to store all intervals in the database and cleans the cache.
	 * 
	 * This method is used when the measured facade must be removed because the measured entity is deleted.
	 */
	public synchronized void storeAllStateIntervals(){
		logger.debug("storeAllStateIntervals Start");
		//logger.debug("storeAllStateIntervals purge map facade");
		LocalDateTime oldest = stateCache.getOldestTime();
		
		deleteOldStates(oldest);
		
		stateCache.bulkCommit(new ArrayList<String>(statesMap.values()));
		logger.debug("storeAllStateIntervals End");
	}
	
	
	/**
	 * Method to remove internal references for both caches that are out of date.
	 */
	
	public synchronized void removeOldCacheReferences()
	{
		logger.debug("removeOldCacheReferences Start");
		// Remove References from the attribute cache.
		LocalDateTime oldestAttr = attValueCache.getOldestTime();		
		deleteOldValues(oldestAttr);

		
		// Delete References from the state cache.
		LocalDateTime oldestState = stateCache.getOldestTime();		
		deleteOldStates(oldestState);
		logger.debug("removeOldCacheReferences End");
	}
	
	/***
	 * Command to order the cache to store all measured attribute values associated to the measured 
	 * entity into the database and cleans itself. 
	 */
	public synchronized void storeAllMeasuredAttributeValues(){
		logger.debug("storeAllMeasuredAttributeValues Start");
		
		//logger.debug("in storeAllMeasuredAttributeValues");
		
		LocalDateTime oldest = attValueCache.getOldestTime();
		
		deleteOldValues(oldest);

		ArrayList<String> keys = new ArrayList<String>();
		
		for(SortedMap<LocalDateTime, String> internalMap : attMap.values()){
			// replace the map with the last entries. 
			keys.addAll(internalMap.values());
		}
		
		attValueCache.bulkCommit(keys);
		
		//logger.debug("it is going out from storeAllMeasuredAttributeValues");
		logger.debug("storeAllMeasuredAttributeValues End");
	}

	
	/**
	 * Returns the Entity Status into a XML document.
	 * 
	 * @return
	 * @throws ParserConfigurationException
	 * @throws JAXBException
	 */
	public Document getXmlStatus() throws ParserConfigurationException, JAXBException{
		logger.debug("getXmlStatus called");
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
	public abstract JSONArray getJsonDowntimeReasons(LocalDateTime from, LocalDateTime to); 

	/**
	 * Get the list of downtime reasons within an interval.
	 * 
	 * @param from	start datetime 
	 * @param to	end datetime
	 * 
	 * @return	list of downtime reasons.
	 */
	protected List<DowntimeReason> getDowntimeReasons(LocalDateTime from,	LocalDateTime to){
		logger.debug("getDowntimeReasons Start");
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
			reasons.addAll(stateCache.getDownTimeReasonsByInterval(this.entity.getId(), this.entity.getType(),this.entity.getCanonicalKey(), from,to).values());
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
			Map<Integer, DowntimeReason> temp2 = stateCache.getDownTimeReasonsByInterval(this.entity.getId(), this.entity.getType(),
																						    this.entity.getCanonicalKey(),from,oldest);
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
		logger.debug("getDowntimeReasons End");
		return reasons;
	}
	
	
	/**
	 * 
	 * @param list
	 * @return
	 */
	protected abstract Map<Integer,DowntimeReason> sumarizeDowntimeReason(List<StateInterval> list);
				
	
	private List<OverallEquipmentEffectiveness> getOEESSegments(LocalDateTime dttmFrom, LocalDateTime dttmTo, String reqInterval){
		logger.debug("getOEESSegments Start");
		List<OverallEquipmentEffectiveness> oees = new ArrayList<OverallEquipmentEffectiveness>();
		OEEAggregationManager oeeAggregation = OEEAggregationManager.getInstance();
		// Bring different predefined periods required
		List<PredefinedPeriod> periods = PeriodUtils.getPredefinedPeriods( dttmFrom, dttmTo, reqInterval );
		//logger.debug("Number of elements to calculate in the final list:" + periods.size());
		
		// loop through the different intervals and calculate total schedule downtime, availability loss, etc..
		for (int i = 0; i < periods.size(); i++)
		{
					
			PredefinedPeriod period = periods.get(i);
			
			//logger.debug("Period Key: " + period.getKey().toString());
			
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
				
				List<OverallEquipmentEffectiveness> oeesHour = oeeAggregation.getOeeAggregationContainer().intervalsByMinute(
															this.getEntity().getId(), this.getEntity().getType(), periodTmp.getKey(), parQueryFrom, parQueryTo);
				
				if (oeesHour.size() == 0) {
					logger.error("The aggregation interval could not be calculated predefined Period:" + parQueryFrom );
				} else {
					oees.addAll(oeesHour);
				}
				
			} else if ( period.getType() == PredefinedPeriodType.HOUR ){
				//logger.debug(period.getKey());
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					//logger.debug("Exist Period");
					oees.add(oee2);
				} else {
					//logger.debug("Don't Exist Period");
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateHour(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, false));
				}
			} else if ( period.getType() == PredefinedPeriodType.MINUTE ){
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateMinute(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, false));
				}
			} else if ( period.getType() == PredefinedPeriodType.DAY ) {
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateDay(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, false));
				}
				
			} else if ( period.getType() == PredefinedPeriodType.MONTH ) {
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateMonth(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, false));
				}
				
			} else if ( period.getType() == PredefinedPeriodType.YEAR )  {
				
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateYear(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, false));
				}
									
			} else {
				logger.error("Invalid Predefined Period type:" + period.getType().getName());			
			}			
		}
			
		logger.debug("Number of elements in the final list:" + oees.size());
		logger.debug("getOEESSegments End");
		return oees;
	}
	
	/**
	 * Recalculate the OEE for the measured entity within the interval given as parameter  
	 * 
	 * @param dttmFrom		start date time 
	 * @param dttmTo		end date time
	 * @param reqInterval	specifies the granularity required for the response.
	 */
	public synchronized void recalculateOverallEquipmentEffectiveness(LocalDateTime dttmFrom, LocalDateTime dttmTo, String reqInterval) {
		logger.debug("recalculateOverallEquipmentEffectiveness Start");
        // Bring different predefined periods required
		List<PredefinedPeriod> periods = PeriodUtils.getPredefinedPeriods( dttmFrom, dttmTo, reqInterval ); 
		OEEAggregationManager oeeAggregation = OEEAggregationManager.getInstance();
		List<OverallEquipmentEffectiveness> oees = new ArrayList<OverallEquipmentEffectiveness>();
		//logger.debug("Number of elements to calculate in the final list:" + periods.size());
		// loop through the different intervals and calculate total schedule down time, availability loss, etc..
		for (int i = 0; i < periods.size(); i++)
		{
			PredefinedPeriod period = periods.get(i);
			//logger.debug("Period Type:" + period.getType().getName());
			if (period.getType() == PredefinedPeriodType.INT_LT_HOUR)
			{
				//logger.debug(period.getKey());
				// Search for intervals in the requested hour.
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
				String parQueryFrom = formatter.format(period.getCalendarFrom().getTime());
				String parQueryTo = formatter.format(period.getCalendarTo().getTime());
				PredefinedPeriod periodTmp = new PredefinedPeriod(period.getCalendarFrom().get(Calendar.YEAR), 
						period.getCalendarFrom().get(Calendar.MONTH) +1,
						period.getCalendarFrom().get(Calendar.DAY_OF_MONTH),
						period.getCalendarFrom().get(Calendar.HOUR_OF_DAY)); 
				List<OverallEquipmentEffectiveness> oeesHour = oeeAggregation.getOeeAggregationContainer().intervalsByMinute(this.getEntity().getId(), this.getEntity().getType(), periodTmp.getKey(), parQueryFrom, parQueryTo);				
				if (oeesHour.size() == 0) {
					logger.error("The aggregation interval could not be calculated predefined Period:" + parQueryFrom );
				} else {
					oees.addAll(oeesHour);
				}
			} else if ( period.getType() == PredefinedPeriodType.HOUR ){
				//logger.debug(period.getKey());
				OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
				oees.addAll(oeeCalculator.calculateHour(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, true));
			} else if ( period.getType() == PredefinedPeriodType.MINUTE ){
				//logger.debug(period.getKey());
				OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
				oees.addAll(oeeCalculator.calculateMinute(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, true));
			} else if ( period.getType() == PredefinedPeriodType.DAY ) {
				//logger.debug(period.getKey());
				OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
				oees.addAll(oeeCalculator.calculateDay(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, true));				
			} else if ( period.getType() == PredefinedPeriodType.MONTH ) {
				//logger.debug(period.getKey());
				OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
				oees.addAll(oeeCalculator.calculateMonth(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, true));				
			} else if ( period.getType() == PredefinedPeriodType.YEAR )  {
				//logger.debug(period.getKey());
				OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
				oees.addAll(oeeCalculator.calculateYear(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, true));
			} else {
				logger.error("Invalid Predefined Period type:" + period.getType().getName());			
			}
		}
		logger.debug("Number of elements in the final recalculated list:" + oees.size());
		logger.debug("recalculateOverallEquipmentEffectiveness End");
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
		logger.debug("getOverallEquipmentEffectiveness Start");
        // Bring different predefined periods required
		List<PredefinedPeriod> periods = PeriodUtils.getPredefinedPeriods( dttmFrom, dttmTo, reqInterval ); 
		
		OEEAggregationManager oeeAggregation = OEEAggregationManager.getInstance();
		List<OverallEquipmentEffectiveness> oees = new ArrayList<OverallEquipmentEffectiveness>();
		
		//logger.debug("Number of elements to calculate in the final list:" + periods.size());
		
		// loop through the different intervals and calculate total schedule downtime, availability loss, etc..
		for (int i = 0; i < periods.size(); i++)
		{
					
			PredefinedPeriod period = periods.get(i);
			
			//logger.debug("Period Type:" + period.getType().getName());
			
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
				
				List<OverallEquipmentEffectiveness> oeesHour = oeeAggregation.getOeeAggregationContainer().intervalsByMinute(
															this.getEntity().getId(), this.getEntity().getType(), periodTmp.getKey(), parQueryFrom, parQueryTo);
				
				if (oeesHour.size() == 0) {
					logger.error("The aggregation interval could not be calculated predefined Period:" + parQueryFrom );
				} else {
					oees.addAll(oeesHour);
				}
				
			} else if ( period.getType() == PredefinedPeriodType.HOUR ){
				//logger.debug(period.getKey());
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					//logger.debug("Exist Period: " + period.getKey().toString());
					oees.add(oee2);
				} else {
					//logger.debug("Don't Exist Period: " + period.getKey().toString());
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateHour(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, false));
				}
			} else if ( period.getType() == PredefinedPeriodType.MINUTE ){
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateMinute(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, false));
				}
			} else if ( period.getType() == PredefinedPeriodType.DAY ) {
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateDay(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, false));
				}
				
			} else if ( period.getType() == PredefinedPeriodType.MONTH ) {
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateMonth(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, false));
				}
				
			} else if ( period.getType() == PredefinedPeriodType.YEAR )  {
				
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateYear(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), true, false));
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
		logger.debug("getOverallEquipmentEffectiveness End");
		return array;
	}

	/**
	 * This method returns a JSON Array with those attributes marked as trend in the language.
	 */
	public synchronized JSONArray getJsonAttributeTrend() {
		logger.debug("getJsonAttributeTrend Start");
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
		logger.debug("getJsonAttributeTrend End");
		return array;
	}

		
}

