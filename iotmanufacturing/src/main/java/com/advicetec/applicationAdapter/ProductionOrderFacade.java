package com.advicetec.applicationAdapter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.configuration.SystemConstants;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.TimeInterval;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.Symbol;
import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.MeasuringState;
import com.advicetec.measuredentitity.StateInterval;
import com.advicetec.core.AttributeValue;
import com.advicetec.persistence.MeasureAttributeValueCache;
import com.advicetec.persistence.StateIntervalCache;
import com.advicetec.persistence.StatusStore;


/**
 * This class is a Production Order facade.
 * It allows other classes to access the functionality
 * from the production order without exposing all its methods.
 *   
 * @author maldofer
 *
 */
public final class ProductionOrderFacade {


	static Logger logger = LogManager.getLogger(ProductionOrderFacade.class.getName());
	
	
	/**
	 * Production order object for which this facade is created.
	 */
	private ProductionOrder pOrder;
	
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
	 */
	public ProductionOrderFacade(ProductionOrder pOrder, String productionRateId, String unit1PerCycles, String unit2PerCycles, String actualProductionCountId) 
	{
		this.pOrder = pOrder;
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

	/**
	 * @return Returns the reference to the production order.
	 */
	public ProductionOrder getProductionOrder() {
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
			attMap.put(attName,new TreeMap<LocalDateTime, String>());
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
			internalMap = internalMap.tailMap(oldest);
		}
	}
	
	
	/**
	 * Deletes all state intervals registered previous to oldest
	 * 
	 * @param oldest : data and time establishing the date limit to delete the state intervals.
	 */
	public void deleteOldStates(LocalDateTime oldest){
		statesMap = (SortedMap<LocalDateTime, String>) statesMap.tailMap(oldest);
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

		ArrayList<AttributeValue> ret = getByIntervalByAttributeName(attrName, from, to);

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
	public List<AttributeValue> getLastNbyAttributeName(String attrName, int n){
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
	public void importSymbols(Map<String, Symbol> symbolMap, AttributeOrigin origin) throws Exception {
		status.importSymbols(symbolMap, origin);
	}

	/**
	 * The STATUS is the collection of attributes defined for a measured entity. This method returns the list 
	 * of attributes associated to the measured entity(production order).
	 * 
	 * @return List of attributes.
	 */
	public Collection<Attribute> getStatus(){
		return status.getStatus();
	}
	
	/**
	 * Returns the measured Entity status. 
	 * @return json array
	 */
	public JSONArray getStatusJSON(){
		return new JSONArray(getStatusValues());
	}
	
	/**
	 * This method register a new value for all attributes within valueMap The string in the map corresponds to 
	 * the attribute name and the value corresponds to the new measure attribute value to insert.
	 * 
	 * @param valueMap Map with tuples attribute name, value to be registered.
	 */
	public void importAttributeValues(Map<String, ASTNode> valueMap) {
		importAttributeValues(valueMap,this.pOrder.getId(),this.pOrder.getType());

	}

	/**
	 * The STATUS is the collection of attributes defined for a measured entity. This method returns the list 
	 * of measured attributes values associated to the measured entity(production order).

	 * @return Current values of all attributes registered for the measured entity.
	 */
	public Collection<AttributeValue> getStatusValues(){
		return status.getAttributeValues();
	}

	/**
	 * Register a new state interval in the measured entity.
	 * 
	 * @param status : Measured entity state during the interval. 
	 * @param reasonCode : Reason code for that state
	 * @param interval : date from and to when the interval happens.
	 */
	public void registerInterval(MeasuringState status, ReasonCode reasonCode, TimeInterval interval)
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
	private boolean isAttribute(String attrName) {
		
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
	public String getJsonStatesByInterval(LocalDateTime from, LocalDateTime to){

		ArrayList<StateInterval> intervals = getStatesByInterval(from, to);

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
	public ArrayList<StateInterval> getStatesByInterval(LocalDateTime from, LocalDateTime to){
		
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
	public void storeAllStateIntervals(){
		
		logger.debug("in storeAllStateIntervals");
		
		LocalDateTime oldest = stateCache.getOldestTime();
				
		deleteOldStates(oldest);
		
		logger.debug("After deleting all states");
		
		ArrayList<String> keys = new ArrayList<String>();
		keys.addAll(statesMap.values());
		
		logger.debug("before bulk commit");
		
		for (String key :keys){
			logger.debug("key to delete:" + key);
		}
		
		stateCache.bulkCommit(keys);
		
		logger.debug("finish storeAllStateIntervals");
	}

	/***
	 * Command to make the cache to store all measured attribute values into the database and clean itself.
	 */
	public void storeAllMeasuredAttributeValues(){

		logger.debug("in storeAllMeasuredAttributeValues");

		LocalDateTime oldest = attValueCache.getOldestTime();
		
		deleteOldValues(oldest);

		ArrayList<String> keys = new ArrayList<String>();
		
		for(SortedMap<LocalDateTime, String> internalMap : attMap.values()){
			// replace the map with the last entries. 
			keys.addAll(internalMap.values());
		}
		attValueCache.bulkCommit(keys);

		logger.debug("finish storeAllMeasuredAttributeValues");

	}
	
	/**
	 * Starts the production order, whenever the production order was in schedule down or operating 
	 */
	public void start()
	{
		if (this.pOrder.getCurrentState() != MeasuringState.OPERATING) {  
			TimeInterval tInterval= new TimeInterval(this.pOrder.getCurrentStatDateTime(), LocalDateTime.now()); 
			registerInterval(this.pOrder.getCurrentState(), this.pOrder.getCurrentReason(), tInterval);
			this.pOrder.startInterval(MeasuringState.OPERATING, null);
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
			this.pOrder.startInterval(MeasuringState.UNSCHEDULEDOWN, null);
		}
		
		logger.debug("Finish production order stop ");
	}
	
}

