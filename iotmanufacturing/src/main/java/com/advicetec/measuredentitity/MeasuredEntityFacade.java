package com.advicetec.measuredentitity;

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
import java.util.SortedMap;
import java.util.TreeMap;

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

import com.advicetec.aggregation.oee.OEEAggregationCalculator;
import com.advicetec.aggregation.oee.OEEAggregationManager;
import com.advicetec.aggregation.oee.OverallEquipmentEffectiveness;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.TimeInterval;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.Symbol;
import com.advicetec.persistence.DowntimeReason;
import com.advicetec.persistence.MeasureAttributeValueCache;
import com.advicetec.persistence.StateIntervalCache;
import com.advicetec.persistence.StatusStore;
import com.advicetec.utils.PeriodUtils;
import com.advicetec.utils.PredefinedPeriod;
import com.advicetec.utils.PredefinedPeriodType;


/**
 * This class is a MeasuredEntity facade.
 * It allows the language processor to access some functionality
 * from the MeasuredEntity without expose all its methods.
 *   
 * @author maldofer
 *
 */
public final class MeasuredEntityFacade {


	static Logger logger = LogManager.getLogger(MeasuredEntityFacade.class.getName());

	private MeasuredEntity entity;
	// Keeps an in-memory entity status
	private StatusStore status;
	private Map<String,SortedMap<LocalDateTime,String>> attMap;
	private MeasureAttributeValueCache attValueCache;
	
	// This map stores endTime, startTime,
	private TreeMap<LocalDateTime,String> statesMap;
	private StateIntervalCache stateCache;
	
	// This field is the attribute name for the expected production rate in minutes.
	private String productionRateId;
	
	// This field establishes the conversion Product Unit 1 / Cycle
	private String unit1PerCycles;
	
	// This field establishes the conversion Product Unit 2 / Cycle
	private String unit2PerCycles;
	
	// This field is the attribute name for the production counter.
	private String actualProductionCountId;

	public MeasuredEntityFacade(MeasuredEntity entity, String productionRateId, 
								 String unit1PerCycles, String unit2PerCycles,  String actualProductionCountId) 
	{
		this.entity = entity;
		this.status = new StatusStore();
		this.attValueCache= MeasureAttributeValueCache.getInstance();
		this.attMap = new HashMap<String,SortedMap<LocalDateTime,String>>();
		this.statesMap = new TreeMap<LocalDateTime,String>();
		this.stateCache = StateIntervalCache.getInstance();
		this.productionRateId = productionRateId;
		this.unit1PerCycles = unit1PerCycles;
		this.unit2PerCycles = unit2PerCycles;
		this.actualProductionCountId = actualProductionCountId;
	}

	public MeasuredEntity getEntity() {
		return entity;
	}

	public synchronized void setEntity(MeasuredEntity entity) {
		this.entity = entity;
	}

	public synchronized MeasuredEntityType getType(){
		return entity.getType();
	}

	/**
	 * Sets an Attribute.
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
	 * @param attrValue
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
			attMap.put(attName,new TreeMap<LocalDateTime, String>());
			internalMap = attMap.get(attName);
		}
		internalMap.put(mav.getTimeStamp(), mav.getKey());
	}

	/**
	 * Returns the newest Measured Attribute Value for a given Attribute name.
	 * @param attName
	 * @return
	 */
	public synchronized AttributeValue getNewestByAttributeName(String attName){
		return status.getAttributeValueByName(attName);
	}

	/**
	 * Returns the current state of the measured entity. 
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
	 * 
	 * @param valueMap 
	 * @param parent Identificator from the MeasuredEntity
	 * @param parentType Type of the Measured Entity.
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
	 * @param att The Attribute
	 * @param value The Value
	 * @param parent Id of the measured entity
	 * @param parentType Type of measured entity.
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

			ArrayList<AttributeValue> newList = attValueCache.
					getFromDatabase(entity.getId(),entity.getType(),
							status.getAttribute(attrName),from, oldest);
			newList.addAll(getFromCache(keyArray));

			return newList;
		}
	}



	/**
	 * Deletes old values from attribute value map.
	 * @param oldest
	 */
	public synchronized void deleteOldValues(LocalDateTime oldest){
		for(SortedMap<LocalDateTime, String> internalMap : attMap.values()){
			// replace the map with the last entries. 
			internalMap = internalMap.tailMap(oldest);
		}
	}


	public synchronized void deleteOldStates(LocalDateTime oldest){
		statesMap = (TreeMap<LocalDateTime, String>) statesMap.tailMap(oldest, true);
	}

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
	 * @param keyArray
	 * @return
	 */
	private synchronized ArrayList<AttributeValue> getFromCache(String[] keyArray){
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

	public synchronized void importSymbols(Map<String, Symbol> symbolMap, AttributeOrigin origin) throws Exception {
		status.importSymbols(symbolMap, origin);
	}

	public synchronized Collection<Attribute> getStatus(){
		return status.getStatus();
	}

	/**
	 * Returns the measured Entity status. 
	 * @return json array
	 */
	public synchronized JSONArray getStatusJSON(){
		return new JSONArray(getStatusValues());
	}


	public synchronized void importAttributeValues(Map<String, ASTNode> valueMap) {
		importAttributeValues(valueMap,entity.getId(),entity.getType());

	}

	public synchronized Collection<AttributeValue> getStatusValues(){
		return status.getAttributeValues();
	}

    /**
     * Actual rate and rate are assumed in minutes. 
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

	// Verifies if a particular attribute belongs to the measure entity status.  
	private  boolean isAttribute(String attrName) {

		Attribute att = status.getAttribute(attrName);

		if( att != null )
			return true;
		else
			return false;
	}

	/**
	 * Returns array of States of this MeasuredEntity for the given dates.
	 * @param from Start date.
	 * @param to End date.
	 * @return String with the json information.
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
	 * @param from Start date.
	 * @param to End date.
	 * @return Json Array of states.
	 */
	public synchronized JSONArray getJsonStates(LocalDateTime from, LocalDateTime to){
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
	 * @param trendVar
	 * @param from
	 * @param to
	 * @return
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
	 * Returns the json representation of 
	 * @param interval
	 * @return
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
	 * @param from Beginning time
	 * @param to Ending time.
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
			
		     // get from cache
			SortedMap<LocalDateTime, String> subMap = statesMap.subMap(oldest, to);
			for (Map.Entry<LocalDateTime, String> entry : subMap.entrySet()) {
				list.add(stateCache.getFromCache(entry.getValue()));
			}
			// get from database
			list.addAll(stateCache.getFromDatabase(entity.getId(),entity.getType(),from,oldest));
		}
		return list;
	}


	/**
	 * Commands to the cache to store all intervals into the database 
	 * and clean the cache.
	 */
	public synchronized void storeAllStateIntervals(){
		LocalDateTime oldest = stateCache.getOldestTime();
		
		deleteOldStates(oldest);
		
		stateCache.bulkCommit(new ArrayList<String>(statesMap.values()));
	}
	
	/***
	 * Commands to store all measured attribute values into the database
	 * and clean the cache. 
	 */
	public void storeAllMeasuredAttributeValues(){
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
	 * @return
	 * @throws ParserConfigurationException
	 * @throws JAXBException
	 */
	public Document getXmlStatus() throws ParserConfigurationException, JAXBException{
		return status.toXml();
	}


	public synchronized JSONArray getJsonDowntimeReasons(LocalDateTime from,	LocalDateTime to) 
	{
		logger.info("In getJsonDowntimeReasons + from:" + from.toString() + " to:" + to.toString());
		
		JSONArray array = null;
		String cannonicalMachine ="";
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

	private List<DowntimeReason> getDowntimeReasons(LocalDateTime from,	LocalDateTime to){
	
		List<StateInterval> list = new ArrayList<StateInterval>();
		LocalDateTime oldest = stateCache.getOldestTime();

		List<DowntimeReason> reasons = new ArrayList<DowntimeReason>();
		
		// all values are in the cache
		if(oldest.isBefore(from))
		{
			logger.info("downtime reason cache only");	
			SortedMap<LocalDateTime, String> subMap = statesMap.subMap(from, to);
			for (Map.Entry<LocalDateTime, String> entry : subMap.entrySet()) {
				list.add(stateCache.getFromCache(entry.getValue()));
			}
			reasons.addAll(sumarizeDowntimeReason(list).values());
		} 
		else if(oldest.isAfter(to))
		{
			logger.info("downtime reason database only");
			// all values are in the database 
			reasons.addAll(stateCache.getDownTimeReasonsByInterval(this.entity,from,to).values());
		} 
		else 
		{
			logger.info("downtime reason mixed cache - database");
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
		logger.info("num Reasons:" + reasons.size());
		return reasons;
	}
	
	
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
	
	public void addExecutedObject(ExecutedEntity executedEntity)
	{
		this.entity.addExecutedEntity(executedEntity);
		
		ExecutedEntityChange();
	}
	
	public void stopExecutedObjects()
	{
		this.entity.stopExecuteEntities();
	}
	
	public void removeExecutedObject(Integer id)
	{
		this.entity.removeExecutedEntity(id);
		
		ExecutedEntityChange();
	}

	public AttributeValue getExecutedObjectAttribute(String attributeId)
	{
		return this.entity.getAttributeFromExecutedObject(attributeId);
	}

	public void setCurrentState(Map<String, ASTNode> symbolMap) {

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
	
	public synchronized void ExecutedEntityChange(){
		
		LocalDateTime localDateTime = LocalDateTime.now();
		TimeInterval interval = new TimeInterval(this.entity.getCurrentStatDateTime(), localDateTime);
		this.registerInterval(this.entity.getCurrentState(), this.entity.getCurrentReason(), interval);
		this.entity.startInterval(localDateTime, this.entity.getCurrentState(), null);						

	}

	public synchronized JSONArray getOverallEquipmentEffectiveness(LocalDateTime dttmFrom, LocalDateTime dttmTo, String reqInterval) {
				
        // Bring different predefined periods required
		List<PredefinedPeriod> periods = PeriodUtils.getPredefinedPeriods( dttmFrom, dttmTo, reqInterval ); 
		
		OEEAggregationManager oeeAggregation = OEEAggregationManager.getInstance();
		List<OverallEquipmentEffectiveness> oees = new ArrayList<OverallEquipmentEffectiveness>();
		
		logger.info("Number of elements to calculate in the final list:" + periods.size());
		
		// loop through the different intervals and calculate total schedule downtime, availability loss, etc..
		for (int i = 0; i < periods.size(); i++)
		{
					
			PredefinedPeriod period = periods.get(i);
			
			logger.info("Period Type:" + period.getType().getName());
			
			if (period.getType() == PredefinedPeriodType.INT_LT_HOUR)
			{
				
				// Search for intervals in the requested hour.
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
				String parQueryFrom = formatter.format(period.getCalendar().getTime());
				String parQueryTo = formatter.format(period.getCalendarTo().getTime());
				
				PredefinedPeriod periodTmp = new PredefinedPeriod(period.getCalendar().get(Calendar.YEAR), 
						period.getCalendar().get(Calendar.MONTH) +1,
						period.getCalendar().get(Calendar.DAY_OF_MONTH),
						period.getCalendar().get(Calendar.HOUR_OF_DAY)); 
				
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
					logger.info("calculating oee for hour");
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateHour(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), false));
				}
			} else if ( period.getType() == PredefinedPeriodType.DAY ) {
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateDay(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), false));
				}
				
			} else if ( period.getType() == PredefinedPeriodType.MONTH ) {
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateMonth(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), false));
				}
				
			} else if ( period.getType() == PredefinedPeriodType.YEAR )  {
				
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getEntity().getId(), this.getEntity().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateYear(this.getEntity().getId(), this.getEntity().getType(), period.getLocalDateTime(), false));
				}
									
			} else {
				logger.error("Invalid Predefined Period type:" + period.getType().getName());			
			}			
		}
			
		logger.info("Number of elements in the final list:" + oees.size());

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
			
			logger.info("oee" + Double.toString(oeeValue));
			
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
		
		logger.info("In getJsonAttributeTrend");
		
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

	public boolean updateStateInterval(String startDttmStr, ReasonCode reasonCode) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		LocalDateTime startDttm = LocalDateTime.parse(startDttmStr, formatter);
		
		logger.info("reasoncode id:" + reasonCode.getId() + " descr:" + reasonCode.getDescription() + " startDttm:" + startDttm );
		
		boolean ret = false;
		
		if (this.entity.getCurrentStatDateTime().equals(startDttm)){
			
			logger.info("Updating the current state interval");
			
			this.entity.setCurrentReasonCode(reasonCode);
			ret = true;
		} else {

			logger.info("Updating the past state interval");
			
			LocalDateTime oldest = stateCache.getOldestTime();
			
			logger.info("oldest" + oldest.format(formatter));
			
			// all values are in the cache
			if(oldest.isBefore(startDttm))
			{
				logger.info("the datetime given is before");
				String stateKey = statesMap.get(startDttm);
				logger.info("State key found:" + stateKey);
				ret = stateCache.updateCacheStateInterval(stateKey, reasonCode);
			} else if(oldest.isAfter(startDttm)){
				logger.info("the datetime given is after");
				// all values are in the database 
				ret = stateCache.updateStateInterval(this.entity.getId(), this.entity.getType(), startDttm, reasonCode);				
			}
		}
		
		return ret;
	}
}

