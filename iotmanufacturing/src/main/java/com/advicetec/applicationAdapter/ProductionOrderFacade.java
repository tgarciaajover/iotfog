package com.advicetec.applicationAdapter;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;


import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.aggregation.oee.OEEAggregationCalculator;
import com.advicetec.aggregation.oee.OEEAggregationManager;
import com.advicetec.aggregation.oee.OverallEquipmentEffectiveness;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.configuration.SystemConstants;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeOrigin;
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
import com.advicetec.core.AttributeValue;
import com.advicetec.core.EntityFacade;
import com.advicetec.persistence.MeasureAttributeValueCache;
import com.advicetec.persistence.StateIntervalCache;
import com.advicetec.persistence.StatusStore;
import com.advicetec.utils.PeriodUtils;
import com.advicetec.utils.PredefinedPeriod;
import com.advicetec.utils.PredefinedPeriodType;


/**
 * This class is a Production Order facade.
 * It allows other classes to access the functionality
 * from the production order without exposing all its methods.
 *   
 * @author maldofer
 *
 */
public final class ProductionOrderFacade implements EntityFacade {


	static Logger logger = LogManager.getLogger(ProductionOrderFacade.class.getName());
	
	
	/**
	 * Production order object for which this facade is created.
	 */
	private ExecutedEntity pOrder;
	
	/**
	 * Keeps an in-memory entity status (The status is the set of variables register for the production order)
	 */
	private StatusStore status;
	
	/**
	* The first string in the outer map corresponds to the attribute name, datetime in the inner map corresponds to
	* the exact time when the variable change and the key assigned to this change within the cache.
	 */
	private Map<String,SortedMap<LocalDateTime,String>> attMap;
	
	/**
	 * Reference to the cache where we store temporarily the measured attribute values.
	 */
	private MeasureAttributeValueCache attValueCache;
	
	/**
	 * This map stores endTime, startTime of every state
	 * A state is created when a stop or ready condition show up.  
	 */
	private SortedMap<LocalDateTime,String> statesMap;

	/**
	 * Reference to the cache where we store temporarily state intervals.
	 */
	private StateIntervalCache stateCache;
	
	/**
	 * Production rate field.
	 * Form this field we are going to take the production rate. 
	 */
	private String productionRateId;
	
	/**
	 * Field that establishes the conversion Product Unit 1 / Cycle
	 */
	private String unit1PerCycles;
	
	/**
	 * Field that establishes the conversion Product Unit 2 / Cycle
	 */
	private String unit2PerCycles;
	
	/**
	 * Production cycle or cycle count registered from the sensor. 
	 */
	private String actualProductionCountId;

	/**
	 *  This field establishes how often we have to remove the cache entries (seconds). 
	 */
	private Integer purgeFacadeCacheMapEntries;
		
	
	/**
	 * SQL to select a set of AttributeValue given owner id and type, attribute
	 * value name, and time range. 
	 *  
	 */
	final private static String sqlMeasureAttributeValueRangeSelect = "select timestamp, value_decimal, value_datetime, value_string, value_int, value_boolean, value_date, value_time from measuredattributevalue where id_owner = ? and owner_type = ? and attribute_name = ? and timestamp >= ? and timestamp <= ?";  

	/**
	 * Column name from the query
	 */
	final private static String timestamp = "timestamp";


	/**
	 * Constructor for the object.
	 * 
	 * @param pOrder			: production order that is the base for the facade.
	 * @param productionRateId	: production rate field. It is assumed part of the production order object and used to evaluate the actual rate of the machine.
	 * @param unit1PerCycles	: field used to convert from cycles to a first product unit.
	 * @param unit2PerCycles	: field used to convert from cycles to a second product unit.
	 * @param actualProductionCountId : actual rate registered from the sensor.
	 * @param purgeFacadeCacheMapEntries	how often we have to purge cache entry references.
	 *
	 * @throws PropertyVetoException 
	 */
	public ProductionOrderFacade(ExecutedEntity pOrder, String productionRateId, 
								  String unit1PerCycles, String unit2PerCycles, 
								    String actualProductionCountId, Integer purgeFacadeCacheMapEntries) throws PropertyVetoException 
	{
		this.pOrder = pOrder;
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
	
		PurgeFacadeCacheMapsEvent purgeEvent = new PurgeFacadeCacheMapsEvent(pOrder.getId(), pOrder.getType());
		purgeEvent.setRepeated(true);
		purgeEvent.setMilliseconds(this.purgeFacadeCacheMapEntries * 1000);
		
		try {
			
			EventManager.getInstance().getDelayedQueue().put(new DelayEvent(purgeEvent, purgeEvent.getMilliseconds())  );
			logger.debug("Purge Event has been scheduled for measured entity:" + pOrder.getId());

		} catch (InterruptedException e) {
			logger.error("Error creating the purge event in the queue for measured entity:" + pOrder.getId());
			e.printStackTrace();
		}
		
	}

	/**
	 * @return Returns the reference to the production order.
	 */
	public ExecutedEntity getProductionOrder() {
		return pOrder;
	}

	/**
	 * Sets the production order for which this facade is being created.
	 * @param pOrder production order object.
	 */
	public void setProductionOrder(ProductionOrder pOrder) {
		this.pOrder = pOrder;
	}

	/**
	 * Gets the measured entity type of the production order.
	 * @return
	 */
	public MeasuredEntityType getType(){
		return pOrder.getType();
	}

	/**
	 * Sets a new attribute.
	 * @param attribute to add.
	 * @throws Exception If the new type of the attribute does not match the 
	 * previous type.
	 */
	public synchronized void setAttribute(Attribute attribute) throws Exception{
		// returns the previous value
		status.setAttribute(attribute);
	}

	/**
	 * Sets or updates an attribute value into the status and store it.
	 * @param attrValue The attribute value to set in the production order.
	 */
	public synchronized void setAttributeValue(AttributeValue attrValue){
		
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
	 * Gets the production rate field 
	 * @return The field that establishes the production rate.
	 */
	public String getProductionRateId() {
		return productionRateId;
	}

	/**
	 * Sets the production rate field 
	 * @param productionRateId production rate field to set.
	 */
	public void setProductionRateId(String productionRateId) {
		this.productionRateId = productionRateId;
	}

	/**
	 * Gets the conversion unit 1 from cycle to product's unit
	 * @return conversion unit 1 from cycle to product's unit
	 */
	public String getConversion1() {
		return unit1PerCycles;
	}

	/**
	 * Sets the conversion unit 1 from cycle to product's unit
	 * @param unit1PerCycles : conversion unit 1 from cycle to product's unit
	 */
	public void setConversion1(String unit1PerCycles) {
		this.unit1PerCycles = unit1PerCycles;
	}

	/**
	 * Gets the conversion unit 2 from cycle to product's unit
	 * @return  : conversion unit 2 from cycle to product's unit
	 */
	public String getConversion2() {
		return unit2PerCycles;
	}

	/**
	 * Sets the conversion unit 2 from cycle to product's unit
	 * @param unit2PerCycles conversion unit 2 from cycle to product's unit
	 */
	public void setConversion2(String unit2PerCycles) {
		this.unit2PerCycles = unit2PerCycles;
	}
		
	/**
	 * Gets the most recent measured attribute value from the attribute with name attName
	 * Returns the newest Measured Attribute Value for a given Attribute name.
	 * @param attName Attribute name for which we want to know the latest attribute value.
	 * @return The last measure attribute value registered.
	 */
	public AttributeValue getNewestByAttributeName(String attName){
		return status.getAttributeValueByName(attName);
	}

	/**
	 * Returns the current state of the executed entity.
	 *  
	 * @return  If there is not entity assigned return undefined.
	 */    
	public synchronized MeasuringState getCurrentState(){
    	 if (this.pOrder == null){
    		 return MeasuringState.UNDEFINED;
    	 } else {
    		 return this.pOrder.getCurrentState();
    	 }
     }
	
	/**
	 * Inserts a new measure attribute value from the map valueMap. The map has as key the name of the attribute and as value 
	 * the measure of the attribute value to insert. 
	 * 
	 * @param valueMap : Map with tuple attribute name, value 
	 * @param parent : the production Order of
	 * @param parentType Type of measure entity, in this case production order.
	 */
	public void importAttributeValues(Map<String, ASTNode> valueMap, Integer parent, MeasuredEntityType parentType) {

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
	 * Adds to the STATUS of the measuring entity a new Attribute Value.
	 * 
	 * @param att The Attribute
	 * @param value The Value
	 * @param parent Id of the measured entity
	 * @param parentType Type of measured entity.
	 */
	public void setAttributeValue(Attribute att, Object value,Integer parent, MeasuredEntityType parentType) {

		logger.debug("inserting attribute value -attr:" + att.getName() + " value:" + value.toString() );
		setAttributeValue(new AttributeValue(att.getName(), att, value, parent, parentType));

	}	

	/**
	 * Returns a list of Measured Attribute Values for the given interval.
	 * 
	 * @param attrName Attribute name.
	 * @param from Time from.
	 * @param to Time to.
	 * @return List of measured attribute values registered to the interval [from, to]
	 */
	public ArrayList<AttributeValue> getByIntervalByAttributeName(
			String attrName, LocalDateTime from, LocalDateTime to){
		
		LocalDateTime oldest = attValueCache.getOldestTime();
		
		if(!attMap.containsKey(attrName)){
			logger.error("attribute is not in facade");
			return new ArrayList<AttributeValue>();  
		}
		SortedMap<LocalDateTime,String> internalMap = attMap.get(attrName);
		
		// all values are in the cache
		if(oldest.isBefore(from)){
			SortedMap<LocalDateTime,String> subMap = internalMap.subMap(from, to);
			Collection<String> keys = subMap.values();
			String[] keyArray = keys.toArray( new String[keys.size()]);
			return getFromCache(keyArray);
			
		} else if(oldest.isAfter(to)){
			
			// get all values from database
			return attValueCache.getFromDatabase(this.pOrder.getId(),this.pOrder.getType(),
					status.getAttribute(attrName),from, oldest);
		} else {
			SortedMap<LocalDateTime,String> subMap = 
					internalMap.subMap(oldest, to);
			
			Collection<String> keys = subMap.values();
			String[] keyArray = keys.toArray( new String[keys.size()]);
			
			ArrayList<AttributeValue> newList = attValueCache.getFromDatabase(this.pOrder.getId(),this.pOrder.getType(),
							status.getAttribute(attrName),from, oldest);
			newList.addAll(getFromCache(keyArray));
			
			return newList;
		}
	}

	/**
	 * Deletes all attribute values registered previous to oldest 
	 * @param oldest : data and time establishing the date limit to delete the measure attribute values
	 */
	public void deleteOldValues(LocalDateTime oldest){
		for(SortedMap<LocalDateTime, String> internalMap : attMap.values()){
			// replace the map with the last entries. 
			Set<LocalDateTime> keysToDelete = internalMap.headMap(oldest).keySet();
			for (LocalDateTime datetime : keysToDelete){
				internalMap.remove(datetime);
			}
		}
	}
	
	
	/**
	 * Deletes all state intervals registered previous to oldest
	 * 
	 * @param oldest : data and time establishing the date limit to delete the state intervals.
	 */
	public void deleteOldStates(LocalDateTime oldest){
		Set<LocalDateTime> keysToDelete = statesMap.headMap(oldest).keySet();
		for (LocalDateTime datetime : keysToDelete){
			statesMap.remove(datetime);
		}
	}
	
		
	/**
	 * Returns a Json list of Measured Attribute Values for the given interval. 
	 * 
	 * @param attrName Attribute name.
	 * @param from Time from.
	 * @param to Time to.
	 * @return Json list of measured attribute values registered to the interval [from, to]
	 */
	public String getByIntervalByAttributeNameJSON(
			String attrName, LocalDateTime from, LocalDateTime to){

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
		} else {
			for (int i = keyArray.length - n - 1; i < keyArray.length; i++) {
				maValues.add(attValueCache.getFromCache(keyArray[i]));
			}
			return maValues;
		}
	}


	/**
	 * Returns a list of Measured Attribute Values, from the cache which keys are in keyArray.
	 * @param keyArray Keys of the measured attributes to get from the cache
	 * @return Measured Attribute Value List.
	 */
	private ArrayList<AttributeValue> getFromCache(String[] keyArray){
		ArrayList<AttributeValue> maValues = new ArrayList<AttributeValue>();
		for (String key : keyArray) {
			maValues.add(attValueCache.getFromCache(key));
		}
		return maValues;
	}

	/**
	 * Creates attributes and units from the symbolMap. the string key on the map corresponds to the attribute name or unit.
	 * The object (symbol) corresponds to the information of the attribute or unit to be added.
	 * 
	 * @param symbolMap Map with attributes or units to be inserted. 
	 * @param origin : there are two origins language transformation or behavior 
	 * @throws Exception excepts if some symbol could not be inserted.
	 */
	public synchronized void importSymbols(Map<String, Symbol> symbolMap, AttributeOrigin origin) throws Exception {
		status.importSymbols(symbolMap, origin);
	}

	/**
	 * The STATUS is the collection of attributes defined for a measured entity. This method returns the list 
	 * of attributes associated to the measured entity(production order).
	 * 
	 * @return List of attributes.
	 */
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
	
	/**
	 * This method register a new value for all attributes within valueMap The string in the map corresponds to 
	 * the attribute name and the value corresponds to the new measure attribute value to insert.
	 * 
	 * @param valueMap Map with tuples attribute name, value to be registered.
	 */
	public synchronized void importAttributeValues(Map<String, ASTNode> valueMap) {
		importAttributeValues(valueMap,this.pOrder.getId(),this.pOrder.getType());

	}

	/**
	 * The STATUS is the collection of attributes defined for a measured entity. This method returns the list 
	 * of measured attributes values associated to the measured entity(production order).

	 * @return Current values of all attributes registered for the measured entity.
	 */
	public synchronized Collection<AttributeValue> getStatusValues(){
		return status.getAttributeValues();
	}

	/**
	 * Register a new state interval in the measured entity.
	 * 
	 * @param status : Measured entity state during the interval. 
	 * @param reasonCode : Reason code for that state
	 * @param interval : date from and to when the interval happens.
	 */
	public synchronized void registerInterval(MeasuringState status, ReasonCode reasonCode, TimeInterval interval)
	{
		
		Double rate = null;
		Double conversion1 = null;
		Double conversion2 = null;
		Double actualRate = null; 
		
		if (this.pOrder.getCurrentState() == MeasuringState.OPERATING) {
			AttributeValue attrValue = this.pOrder.getAttributeValue(this.productionRateId);
			if (attrValue != null){
				rate = (Double) attrValue.getValue();
			} else {
				rate = new Double(0.0);
			}
		} else {
			rate = new Double(0.0);
		}

		if (this.pOrder.getCurrentState() == MeasuringState.OPERATING) {
			AttributeValue attrValue = this.pOrder.getAttributeValue(this.unit1PerCycles);
			if (attrValue != null){
				conversion1 = (Double) attrValue.getValue();
			} else {
				conversion1 = new Double(0.0);
			}
		} else {
			conversion1 = new Double(0.0);
		}

		if (this.pOrder.getCurrentState() == MeasuringState.OPERATING) {
			AttributeValue attrValue = this.pOrder.getAttributeValue(this.unit2PerCycles);
			if (attrValue != null){
				conversion2 = (Double) attrValue.getValue();
			} else {
				conversion2 = new Double(0.0);
			}
		} else {
			conversion2 = new Double(0.0);
		}

		if (this.pOrder.getCurrentState() == MeasuringState.OPERATING) {
			// Verifies that the actual production count id field is an attribute in the measuring entity
			if (!isAttribute(actualProductionCountId)) {
				logger.error("The given attribute: " + this.actualProductionCountId + " does not exists as attribute in the measuring entity");
				actualRate = new Double(0.0);
			} else {
			
				ArrayList<AttributeValue> list = getByIntervalByAttributeName(actualProductionCountId, interval.getStart(), interval.getEnd());
				
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
				actualRate = new Double(sum * 60 / seconds); 
			}
		} else {
			actualRate = new Double(0.0);
		}
		
		logger.debug("we are going to register the production order interval");
		
		StateInterval stateInterval = new StateInterval(status, reasonCode, interval, this.pOrder.getId(), this.pOrder.getType(), this.pOrder.getId(), this.pOrder.getType().getValue(), this.pOrder.getCanonicalKey(), rate, conversion1, conversion2, actualRate, new Double(0));
		stateInterval.setKey(this.pOrder.getId()+stateInterval.getKey());
		// key in the map and the cache must be consistent
		statesMap.put(interval.getStart(),stateInterval.getKey());
		StateIntervalCache.getInstance().storeToCache(stateInterval);
		
	}


	/**
	 * This method verifies if an attribute with name attrName is registered for the measured entity.
	 * 
	 * @param attrName  Attribute name to verify.
	 * @return True if an attribute with name attrName is registered, false otherwise.
	 */
	private synchronized boolean isAttribute(String attrName) {
		
		Attribute att = status.getAttribute(attrName);

		if( att != null )
			return true;
		else
			return false;
	}

	/**
	 * Returns the list of states assumed by the measured entity during the interval [from, to]
	 * 
	 * @param from start of the interval
	 * @param to   end of the interval
	 * @return String in Json format representing the list of states assumed by the measured entity.
	 */
	public synchronized String getJsonStatesByInterval(LocalDateTime from, LocalDateTime to){

		List<StateInterval> intervals = getStatesByInterval(from, to);

		ObjectMapper mapper = new ObjectMapper();
		String jsonText=null;

		try {

			jsonText = mapper. writeValueAsString(intervals);

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
	 * Returns the list of state intervals between two datetimes.
	 * @param from Beginning time
	 * @param to Ending time.
	 * @return List of state intervals.
	 */
	public synchronized List<StateInterval> getStatesByInterval(LocalDateTime from, LocalDateTime to){
		
		ArrayList<StateInterval> list = new ArrayList<StateInterval>();
		LocalDateTime oldest = stateCache.getOldestTime();
		
		// all values are in the cache
		if(oldest.isBefore(from)){
			SortedMap<LocalDateTime, String> subMap = statesMap.subMap(from, to);
			for (Map.Entry<LocalDateTime, String> entry : subMap.entrySet()) {
				list.add(stateCache.getFromCache(entry.getValue()));
			}
		}else if(oldest.isAfter(to)){
			// all values are in the database 
			list.addAll(stateCache.getFromDatabase(this.pOrder.getId(),this.pOrder.getType(),from,to));
		}else{
			
		     // get from cache
			SortedMap<LocalDateTime, String> subMap = statesMap.subMap(oldest, to);
			for (Map.Entry<LocalDateTime, String> entry : subMap.entrySet()) {
				list.add(stateCache.getFromCache(entry.getValue()));
			}
			// get from database
			list.addAll(stateCache.getFromDatabase(this.pOrder.getId(),this.pOrder.getType(),from,oldest));
		}
		return list;
	}

	/**
	 * Command to make the cache store all intervals into the database and clean itself.
	 */
	public synchronized void storeAllStateIntervals(){
		
		logger.debug("in storeAllStateIntervals");
		
		LocalDateTime oldest = stateCache.getOldestTime();
				
		deleteOldStates(oldest);
		
		logger.debug("After deleting all states");
			
		stateCache.bulkCommit(new ArrayList<String>(statesMap.values()));
		
		logger.debug("finish storeAllStateIntervals");
	}

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
	 * Command to make the cache to store all measured attribute values into the database and clean itself.
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
			reasons.addAll(stateCache.getDownTimeReasonsByInterval(this.getProductionOrder().getId(),
																	this.getProductionOrder().getType(),
																	this.getProductionOrder().getCanonicalKey(),
																	from,to).values());
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
			Map<Integer, DowntimeReason> temp2 = stateCache.getDownTimeReasonsByInterval(this.getProductionOrder().getId(),
																						   this.getProductionOrder().getType(),
																						   	  this.getProductionOrder().getCanonicalKey(),from,oldest);
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
						if(getProductionOrder().getType() == MeasuredEntityType.MACHINE){
							ExecutedEntity pOrder = (ExecutedEntity) getProductionOrder();
							reason = new DowntimeReason(pOrder.getCanonicalKey(), 
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
					if ((this.pOrder.getCurrentState() != MeasuringState.OPERATING) || (this.pOrder.startNewInterval())) {
						LocalDateTime localDateTime = LocalDateTime.now();
						TimeInterval interval = new TimeInterval(this.pOrder.getCurrentStatDateTime(), localDateTime);
						this.registerInterval(this.pOrder.getCurrentState(), this.pOrder.getCurrentReason(), interval);
						this.pOrder.startInterval(localDateTime, MeasuringState.OPERATING, null);
					}
				} else if (newState == 1){
					
					if ((this.pOrder.getCurrentState() != MeasuringState.SCHEDULEDOWN) || (this.pOrder.startNewInterval())) {
						LocalDateTime localDateTime = LocalDateTime.now();
						TimeInterval interval = new TimeInterval(this.pOrder.getCurrentStatDateTime(), localDateTime);
						this.registerInterval(this.pOrder.getCurrentState(), this.pOrder.getCurrentReason(), interval);
						this.pOrder.startInterval(localDateTime, MeasuringState.SCHEDULEDOWN, null);						
					}
					
				} else if (newState == 2){
					
					if ((this.pOrder.getCurrentState() != MeasuringState.UNSCHEDULEDOWN) || (this.pOrder.startNewInterval())) {
						LocalDateTime localDateTime = LocalDateTime.now();
						TimeInterval interval = new TimeInterval(this.pOrder.getCurrentStatDateTime(), localDateTime);
						this.registerInterval(this.pOrder.getCurrentState(), this.pOrder.getCurrentReason(), interval);
						this.pOrder.startInterval(localDateTime, MeasuringState.UNSCHEDULEDOWN, null);						
					}
					
				} else {
					logger.error("The new state is being set to undefined, which is incorrect");
				}	
				
			}
		}

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
															this.getProductionOrder().getId(), 
															this.getProductionOrder().getType(), 
															periodTmp.getKey(), parQueryFrom, parQueryTo);
				
				if (oeesHour.size() == 0) {
					logger.error("The aggregation interval could not be calculated predefined Period:" + parQueryFrom );
				} else {
					oees.addAll(oeesHour);
				}
				
			} else if ( period.getType() == PredefinedPeriodType.HOUR ){
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getProductionOrder().getId(), 
										this.getProductionOrder().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					logger.debug("calculating oee for hour");
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateHour(this.getProductionOrder().getId(), 
															 this.getProductionOrder().getType(), 
															   period.getLocalDateTime(), false, false));
				}
			} else if ( period.getType() == PredefinedPeriodType.DAY ) {
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getProductionOrder().getId(), 
										this.getProductionOrder().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateDay(this.getProductionOrder().getId(), 
															this.getProductionOrder().getType(), 
															  period.getLocalDateTime(), false, false));
				}
				
			} else if ( period.getType() == PredefinedPeriodType.MONTH ) {
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getProductionOrder().getId(), this.getProductionOrder().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateMonth(this.getProductionOrder().getId(), 
															   this.getProductionOrder().getType(), 
															     period.getLocalDateTime(), false, false));
				}
				
			} else if ( period.getType() == PredefinedPeriodType.YEAR )  {
				
				OverallEquipmentEffectiveness oee2 = oeeAggregation.getOeeAggregationContainer().
						getPeriodOEE(this.getProductionOrder().getId(), this.getProductionOrder().getType(), period);
				if (oee2 !=null) {
					oees.add(oee2);
				} else {
					OEEAggregationCalculator oeeCalculator = new OEEAggregationCalculator();
					oees.addAll(oeeCalculator.calculateYear(this.getProductionOrder().getId(), 
															  this.getProductionOrder().getType(), 
															    period.getLocalDateTime(), false, false));
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
	 * Starts the production order, whenever the production order was in schedule down or operating 
	 */
	public void start()
	{
		if (this.pOrder.getCurrentState() != MeasuringState.OPERATING) {  
			TimeInterval tInterval= new TimeInterval(this.pOrder.getCurrentStatDateTime(), LocalDateTime.now()); 
			registerInterval(this.pOrder.getCurrentState(), this.pOrder.getCurrentReason(), tInterval);
			this.pOrder.startInterval(LocalDateTime.now(), MeasuringState.OPERATING, null);
		}
		
	}
	
	/**
	 * Stops the production order, whenever the production order was in schedule down or operating 
	 */
	public void stop()
	
	{
		logger.debug("Stopping the production order");
		
		if (this.pOrder.getCurrentState() != MeasuringState.UNSCHEDULEDOWN) {
			
			logger.debug("Registering the final interval for the production order");
			
			TimeInterval tInterval= new TimeInterval(this.pOrder.getCurrentStatDateTime(), LocalDateTime.now()); 
			registerInterval(this.pOrder.getCurrentState(), this.pOrder.getCurrentReason(), tInterval);
			this.pOrder.startInterval(LocalDateTime.now(), MeasuringState.UNSCHEDULEDOWN, null);
		}
		
		logger.debug("Finish production order stop ");
	}

	public synchronized AttributeValue getExecutedObjectAttribute(String attributeId){
		return null;
	}
}

