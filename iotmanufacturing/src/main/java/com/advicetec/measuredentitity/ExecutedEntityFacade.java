package com.advicetec.measuredentitity;

import java.beans.PropertyVetoException;
import java.io.IOException;
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
import com.advicetec.applicationAdapter.ProductionOrder;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.configuration.SystemConstants;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.TimeInterval;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.eventprocessor.PurgeFacadeCacheMapsEvent;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.Symbol;
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
public final class ExecutedEntityFacade extends EntityFacade {


	static Logger logger = LogManager.getLogger(ExecutedEntityFacade.class.getName());
		
	/**
	 * Production rate field.
	 * Form this field we are going to take the production rate. 
	 */
	protected String productionRateId;
	
	/**
	 * Field that establishes the conversion Product Unit 1 / Cycle
	 */
	protected String unit1PerCycles;
	
	/**
	 * Field that establishes the conversion Product Unit 2 / Cycle
	 */
	protected String unit2PerCycles;
	
	/**
	 * Production cycle or cycle count registered from the sensor. 
	 */
	protected String actualProductionCountId;
		
	
	/**
	 * It maintains the measured entity reference where the executed entity is being processed. 
	 */
	protected Map<Integer, MeasuredEntityFacade> processedOn;  
	
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
	public ExecutedEntityFacade(ExecutedEntity pOrder, String productionRateId, 
								  String unit1PerCycles, String unit2PerCycles, 
								    String actualProductionCountId, Integer purgeFacadeCacheMapEntries) throws PropertyVetoException 
	{
		super(pOrder,purgeFacadeCacheMapEntries);
		logger.debug("ExecutedEntityFacade Start " + String.valueOf(pOrder.getId()));
		this.productionRateId = productionRateId;
		this.unit1PerCycles = unit1PerCycles; 
		this.unit2PerCycles = unit2PerCycles;
		this.actualProductionCountId = actualProductionCountId;
	
		this.processedOn = new ConcurrentSkipListMap<Integer,MeasuredEntityFacade>();
		
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
		logger.debug("ExecutedEntityFacade End " + String.valueOf(pOrder.getId()));
	}

	/**
	 * Gets the production rate field 
	 * @return The field that establishes the production rate.
	 */
	public String getProductionRateId() {
		logger.debug("getProductionRateId called");
		return productionRateId;
	}

	/**
	 * Sets the production rate field 
	 * @param productionRateId production rate field to set.
	 */
	public void setProductionRateId(String productionRateId) {
		logger.debug("setProductionRateId Start");
		this.productionRateId = productionRateId;
		logger.debug("setProductionRateId End");
	}

	/**
	 * Gets the conversion unit 1 from cycle to product's unit
	 * @return conversion unit 1 from cycle to product's unit
	 */
	public String getConversion1() {
		logger.debug("getConversion1 called");
		return unit1PerCycles;
	}

	/**
	 * Sets the conversion unit 1 from cycle to product's unit
	 * @param unit1PerCycles : conversion unit 1 from cycle to product's unit
	 */
	public void setConversion1(String unit1PerCycles) {
		logger.debug("setConversion1 Start");
		this.unit1PerCycles = unit1PerCycles;
		logger.debug("setConversion1 End");
	}

	/**
	 * Gets the conversion unit 2 from cycle to product's unit
	 * @return  : conversion unit 2 from cycle to product's unit
	 */
	public String getConversion2() {
		logger.debug("getConversion2 called");
		return unit2PerCycles;
	}

	/**
	 * Sets the conversion unit 2 from cycle to product's unit
	 * @param unit2PerCycles conversion unit 2 from cycle to product's unit
	 */
	public void setConversion2(String unit2PerCycles) {
		logger.debug("setConversion2 Start");
		this.unit2PerCycles = unit2PerCycles;
		logger.debug("setConversion2 End");
	}
		
	/**
	 * Creates a new interval from the measuring state, reason code and data in the executed entity
	 * 
	 * @param status		new status for the measured entity 
	 * @param reasonCode	reason code of the new status
	 * @param interval		Time interval in which the measure entity remains in the status defined.
	 * 
	 * @return new state interval
	 */
	private StateInterval BuildStateInterval(MeasuringState status, ReasonCode reasonCode, TimeInterval interval, Integer measuringEntityId)
	{
		logger.debug("BuildStateInterval Start " + String.valueOf(measuringEntityId));
		//logger.debug("In registerInterval:" + status.getName());
		
		Double rate = null;
		Double conversion1 = null;
		Double conversion2 = null;
		Double actualRate = null;
		
		MeasuringState currentStatus = ((ExecutedEntity)this.getEntity()).getCurrentState(measuringEntityId); 
		
		MeasuredEntityFacade measuringEntityFacade = this.processedOn.get(measuringEntityId);
		
		if (currentStatus == MeasuringState.OPERATING) {
			AttributeValue attrValue = this.entity.getAttributeValue(this.productionRateId);
			if (attrValue != null) {
				rate = (Double) attrValue.getValue();
			} else {
				rate = new Double(0.0);
			}
		} else {
			rate = new Double(0.0);
		}

		if (currentStatus == MeasuringState.OPERATING) {
			AttributeValue attrValue = this.entity.getAttributeValue(this.unit1PerCycles);
			if (attrValue != null) {
				conversion1 = (Double) attrValue.getValue();
			} else {
				conversion1 = new Double(0.0);
			}
		} else {
			conversion1 = new Double(0.0);
		}

		if (currentStatus == MeasuringState.OPERATING) {
			AttributeValue attrValue = this.entity.getAttributeValue(this.unit2PerCycles);
			if (attrValue != null) {
				conversion2 = (Double) attrValue.getValue();
			} else {
				conversion2 = new Double(0.0);
			}
		} else {
			conversion2 = new Double(0.0);
		}

		if (currentStatus == MeasuringState.OPERATING) {
			
			if (measuringEntityFacade == null) {
				logger.error("The the measured entity: " + measuringEntityId + " is not registered in the executed object" + this.getEntity().getId());
				actualRate = new Double(0.0);
			} else {
			
				
				// Verifies that the actual production count id field is an attribute in the measuring entity
				if (!measuringEntityFacade.isAttribute(actualProductionCountId)) {
					
					logger.error("The given attribute: " + this.actualProductionCountId + 
									" does not exists as attribute in the measuring entity " + measuringEntityFacade.getEntity().getId());
					
					actualRate = new Double(0.0);
				} else {
				
					List<AttributeValue> list = measuringEntityFacade.getByIntervalByAttributeName(actualProductionCountId, interval.getStart(), interval.getEnd());
					
					//logger.debug("Registering interval - number of rows:" + list.size() );
					
					double sum = 0;
					// Calculates the actual rate as the sum(count) / Interval.duration (minutes)
					for (AttributeValue attributeValue : list) 
					{
						
						MeasuredAttributeValue measvalue = (MeasuredAttributeValue) attributeValue;
						
						if ((measvalue.getValue() instanceof Double) || (measvalue.getValue() instanceof Integer)){
							if (measvalue.getValue() instanceof Double) {
								sum = sum + (Double) measvalue.getValue();
							} else {
								sum = sum + (Integer) measvalue.getValue();
							}
						} else {
							logger.error("The production count attribute: " + actualProductionCountId + " parametrized is not of type Double or Integer");
							break;
						}
					}
					
					//logger.debug("The production count attribute: " + actualProductionCountId + ": " + sum );
					
					LocalDateTime tempDateTime = LocalDateTime.from( interval.getStart() );
					long seconds = tempDateTime.until( interval.getEnd(), ChronoUnit.SECONDS);
					double tmp = 0;
					if (seconds > 0) {
						tmp = (sum * 60) / seconds;
					}
						
					actualRate = new Double(tmp);    // The actual rate is in cycle over minutes. 
					
				}
			}
		} else {
			actualRate = new Double(0.0);
		}
		
		logger.debug("we are going to register the production order interval");
		
		StateInterval stateInterval = null;
		if (measuringEntityFacade != null) {
			stateInterval = new StateInterval(status, reasonCode, interval ,getEntity().getId(), 
												getEntity().getType(), measuringEntityFacade.getEntity().getId(), 
												measuringEntityFacade.getEntity().getType().getValue(), 
												getEntity().getCanonicalKey(), 
												rate, conversion1, conversion2, actualRate, new Double(0));
		} else {
			// Sets as the related object the measured entity 
			stateInterval = new StateInterval(status, reasonCode, interval,getEntity().getId(), 
					  getEntity().getType(), 0, 0, 
					  getEntity().getCanonicalKey(), 
					  rate, conversion1, conversion2, actualRate, new Double(0));			
		}
		
		stateInterval.setKey(this.getEntity().getId()+ ":" + stateInterval.getKey());
		
		logger.debug("BuildStateInterval End " + String.valueOf(measuringEntityId));
		return stateInterval;
		
	}
	
	/**
	 * Register a new state interval in the executed entity.
	 * 
	 * @param status : 			Measured entity state during the interval. 
	 * @param reasonCode : 		Reason code for that state
	 * @param interval : 		date from and to when the interval happens.
	 * @param measuringEntityId Measured entity identifier where the executed entiti is being processed.
	 */
	public synchronized void registerInterval(MeasuringState status, ReasonCode reasonCode, TimeInterval interval, Integer measuringEntityId)
	{
		logger.debug("registerInterval Start " + String.valueOf(measuringEntityId));
		
		StateInterval stateInterval = BuildStateInterval(status, reasonCode, interval, measuringEntityId);
		
		// key in the map and the cache must be consistent
		statesMap.put(interval.getStart(),stateInterval.getKey());
		StateIntervalCache.getInstance().storeToCache(stateInterval);
		
		logger.debug("registerInterval End " + String.valueOf(measuringEntityId));
		
	}

	/**
	 *  Registers a measuredEntity where its being processed.
	 *   
	 * @param entityFacade  Facade to the measured entity where the executed entity is being produced. 
	 */
	public synchronized void addMeasuredEntity(MeasuredEntityFacade entityFacade)
	{
		logger.debug("addMeasuredEntity Start " + String.valueOf(entityFacade.getEntity().getId()));
		this.processedOn.put(entityFacade.getEntity().getId(), entityFacade);
		logger.debug("addMeasuredEntity End " + String.valueOf(entityFacade.getEntity().getId()));
	}

	/**
	 * Deletes a measured entity from the list of machines where the executed entity is being processed. 
	 * 
	 * @param measuredEntityId  Identifier to be removed.
	 */
	public synchronized boolean deleteMeasuredEntity(Integer measuredEntityId)
	{
		logger.debug("deleteMeasuredEntity Start " + String.valueOf(measuredEntityId));
		if (this.processedOn.remove(measuredEntityId) != null) {
			logger.debug("deleteMeasuredEntity End " + String.valueOf(measuredEntityId));
			return true;
		}
		else {
			logger.debug("deleteMeasuredEntity End " + String.valueOf(measuredEntityId));
			return false;
		}
	}
	
	
	public synchronized MeasuredEntityFacade getMeasuredEntity(Integer measuredEntityId)
	{
		logger.debug("getMeasuredEntity call");
		return this.processedOn.get(measuredEntityId); 
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
 		logger.debug("getJsonDowntimeReasons Start");
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
		logger.debug("getJsonDowntimeReasons End");
		return array;
	}


	/**
	 * 
	 * @param list
	 * @return
	 */
	protected Map<Integer,DowntimeReason> sumarizeDowntimeReason(List<StateInterval> list) {
		logger.debug("sumarizeDowntimeReason Start");
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
					} else {
						if(getEntity().getType() == MeasuredEntityType.JOB){
							
							reason = new DowntimeReason(getEntity().getCanonicalKey(), 
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
		logger.debug("sumarizeDowntimeReason End");
		return map;
	}

	
	/**
	 * Starts the production order, whenever the production order was in schedule down or operating. 
	 * 
	 * The production order is said to be in operation when it is executed in any measured entity.
	 */
	public synchronized void start(Integer measuredEntityId)
	{
		logger.debug("start Start " + String.valueOf(measuredEntityId));
		if (((ExecutedEntity)getEntity()).getCurrentState(measuredEntityId) != MeasuringState.UNDEFINED) {
			
			// This case happens when the executed object has already been processing in the executed entity and it is going to be reprocessed.
			
			TimeInterval tInterval = new TimeInterval(((ExecutedEntity)getEntity()).getCurrentStatDateTime(measuredEntityId), 
													LocalDateTime.now());
		
			registerInterval(((ExecutedEntity)getEntity()).getCurrentState(measuredEntityId), 
						 ((ExecutedEntity)getEntity()).getCurrentReason(measuredEntityId), 
						 tInterval, measuredEntityId);
		} 
		
		((ExecutedEntity) getEntity()).startInterval(measuredEntityId, LocalDateTime.now(), MeasuringState.OPERATING, null);

		logger.debug("start End " + String.valueOf(measuredEntityId));
	}
	
	/**
	 * Stops the production order, whenever the production order was in schedule down or operating
	 * 
	 * The production order is said to be in operation when it is executed in any measured entity. 
	 * 
	 * This means that only the production order is stopped when is not in process in all measured entities.
	 */
	public synchronized void stop(Integer measuringEntityId)
	
	{
		logger.debug("stop Start " + String.valueOf(measuringEntityId));
		logger.debug("Stopping the production order");

		if ( this.processedOn.containsKey(measuringEntityId) ) {
			
			//logger.debug("Registering the final interval for the production order");

			TimeInterval tInterval= new TimeInterval( ((ExecutedEntity)getEntity()).getCurrentStatDateTime(measuringEntityId), 
													  LocalDateTime.now());
			
			registerInterval(((ExecutedEntity) getEntity()).getCurrentState(measuringEntityId), 
							 ((ExecutedEntity) getEntity()).getCurrentReason(measuringEntityId), 
							 tInterval, measuringEntityId);
			
			((ExecutedEntity) getEntity()).startInterval(measuringEntityId, LocalDateTime.now(), MeasuringState.UNSCHEDULEDOWN, null);
		
		}

		logger.debug("Finish production order stop ");
		logger.debug("stop End " + String.valueOf(measuringEntityId));
	}

	/**
	 * Gets the current value attribute from the measured entity registered as parameter  
	 * 
	 * @param attributeId        requested attribute 
	 * @param measuredEntityId	 measured Entity Identifier
	 * 
	 * @return the current value or null
	 */
	public synchronized AttributeValue getProcessEntityAttribute(String attributeId, Integer measuredEntityId) {
		logger.debug("getProcessEntityAttribute Start " + String.valueOf(measuredEntityId));
		MeasuredEntityFacade measuredEntityFacade = this.processedOn.get(measuredEntityId);
		
		if (measuredEntityFacade != null) {
			logger.debug("getProcessEntityAttribute End " + String.valueOf(measuredEntityId));
			return measuredEntityFacade.getNewestByAttributeName(attributeId);
			
		} else {
			logger.debug("getProcessEntityAttribute End " + String.valueOf(measuredEntityId));
			return null;
		}
	}

	/**
	 * Returns whether or not the executed entity is being processed in any measured entity.
	 * 
	 * @return true 	- it is processed in any measured entity
	 * 		   false  	- Otherwise.
	 */
	public boolean isProcessed() 
	{
		logger.debug("isProcessed Start");
		if (this.processedOn.size() > 0) {
			logger.debug("isProcessed End");
			return true;
		
		} else {
			logger.debug("isProcessed End");
			return false;
		}
	}

	/**
	 * Returns the current state of the measured entity.
	 *  
	 * @return  If there is not entity assigned return undefined.
	 */    
	public synchronized MeasuringState getCurrentState(Integer measuredEntityId){
		logger.debug("getCurrentState Start " + String.valueOf(measuredEntityId));
    	 if (this.entity == null){
    		 logger.debug("getCurrentState End " + String.valueOf(measuredEntityId));
    		 return MeasuringState.UNDEFINED;
    	 } else {
    		 logger.debug("getCurrentState End " + String.valueOf(measuredEntityId));
    		 return ((ExecutedEntity) this.getEntity()).getCurrentState(measuredEntityId);
    	 }
     }

	private void changeState(Integer measuredEntityId, MeasuringState newState, boolean persistReason) {
		logger.debug("changeState Start " + String.valueOf(measuredEntityId));
		LocalDateTime localDateTime = LocalDateTime.now();
		
		// Creates the time interval, from the last status change to now.
		TimeInterval interval = new TimeInterval(((ExecutedEntity) this.getEntity()).getCurrentStatDateTime(measuredEntityId), localDateTime);
		
		// Registers the interval in the Measured Entity
		this.registerInterval(((ExecutedEntity) this.getEntity()).getCurrentState(measuredEntityId), 
							  ((ExecutedEntity) this.getEntity()).getCurrentReason(measuredEntityId), 
							  interval, measuredEntityId);
		
		//Starts a new Interval depending of new state and origin.
		if (persistReason) {
			((ExecutedEntity) this.getEntity()).startInterval(measuredEntityId, localDateTime, newState, ((ExecutedEntity) this.getEntity()).getCurrentReason(measuredEntityId));
		} else {
			((ExecutedEntity) this.getEntity()).startInterval(measuredEntityId, localDateTime, newState, null);
		}
		logger.debug("changeState End " + String.valueOf(measuredEntityId));
		//logger.debug ("current state:" + (((ExecutedEntity) this.getEntity()).getCurrentState(measuredEntityId)));
	}

	/**
	 * Updates the state of the entity taking as parameter 
	 * the measured attributes resulting from a transformation or behavior execution 
	 * 
	 * @param symbolMap				symbols generated by the transformation or behavior execution.
	 * @param measuredEntityId 	This parameter corresponds to the measured entity where the behavior is begin run.
	 * 								It is required only for executed entities needing to register intervals on particular machines.
	 */
	public synchronized void setCurrentState(Map<String, ASTNode> symbolMap, Integer measuredEntityId) {
		logger.debug("setCurrentState Start " + String.valueOf(measuredEntityId));
		logger.debug("In set current state");
		
		for (Map.Entry<String, ASTNode> entry : symbolMap.entrySet()) 
		{
			if(entry.getKey().compareTo("state") == 0 ){
				ASTNode node = entry.getValue();
				
				MeasuringState newState = node.asMeasuringState();
				
				logger.debug("In set current state - new state:" + newState.getName());
				 
				if (newState == MeasuringState.OPERATING){
					
					if ( (((ExecutedEntity) this.getEntity()).getCurrentState(measuredEntityId) != MeasuringState.OPERATING) || 
							(((ExecutedEntity) this.getEntity()).startNewInterval(measuredEntityId)) ) {
						
						changeState(measuredEntityId, MeasuringState.OPERATING, false);
						
					}
					
				} else if (newState == MeasuringState.SCHEDULEDOWN){
					
					if ((((ExecutedEntity) this.getEntity()).getCurrentState(measuredEntityId) != MeasuringState.SCHEDULEDOWN) || 
							(((ExecutedEntity) this.getEntity()).startNewInterval(measuredEntityId))) {
						
						changeState(measuredEntityId, MeasuringState.SCHEDULEDOWN, false);
												
					}
					
				} else if (newState == MeasuringState.UNSCHEDULEDOWN){
					
					if ((((ExecutedEntity) this.getEntity()).getCurrentState(measuredEntityId) != MeasuringState.UNSCHEDULEDOWN) || 
							(((ExecutedEntity) this.getEntity()).startNewInterval(measuredEntityId))) {
						
						changeState(measuredEntityId, MeasuringState.UNSCHEDULEDOWN, true);
						
					}
					
				} else if (newState == MeasuringState.INITIALIZING){
					
					if ((((ExecutedEntity) this.getEntity()).getCurrentState(measuredEntityId) != MeasuringState.INITIALIZING) || 
							(((ExecutedEntity) this.getEntity()).startNewInterval(measuredEntityId))) {
						
						changeState(measuredEntityId, MeasuringState.INITIALIZING, false);
						
					}
					
				} else {
					logger.error("The new state is being set to undefined, which is incorrect");
				}	
			}
		}
		logger.debug("setCurrentState End " + String.valueOf(measuredEntityId));
	}
	
	public synchronized void setCurrentState(MeasuringState newState, Integer measuredEntityId) {
		logger.debug("setCurrentState Start " + String.valueOf(measuredEntityId));
		if (newState == MeasuringState.OPERATING) {
			if ( (((ExecutedEntity) this.getEntity()).getCurrentState(measuredEntityId) != MeasuringState.OPERATING) || 
					(((ExecutedEntity) this.getEntity()).startNewInterval(measuredEntityId)) ) {

				changeState(measuredEntityId, MeasuringState.OPERATING, false);

			}

		} else if (newState == MeasuringState.SCHEDULEDOWN){

			if ((((ExecutedEntity) this.getEntity()).getCurrentState(measuredEntityId) != MeasuringState.SCHEDULEDOWN) || 
					(((ExecutedEntity) this.getEntity()).startNewInterval(measuredEntityId))) {

				changeState(measuredEntityId, MeasuringState.SCHEDULEDOWN, false);

			}

		} else if (newState == MeasuringState.UNSCHEDULEDOWN){

			if ((((ExecutedEntity) this.getEntity()).getCurrentState(measuredEntityId) != MeasuringState.UNSCHEDULEDOWN) || 
					(((ExecutedEntity) this.getEntity()).startNewInterval(measuredEntityId))) {

				changeState(measuredEntityId, MeasuringState.UNSCHEDULEDOWN, false);

			}

		} else if (newState == MeasuringState.SYSTEMDOWN) {

			if ((((ExecutedEntity) this.getEntity()).getCurrentState(measuredEntityId) != MeasuringState.SYSTEMDOWN) || 
					(((ExecutedEntity) this.getEntity()).startNewInterval(measuredEntityId))) {

				changeState(measuredEntityId, MeasuringState.SYSTEMDOWN, false);

			}

		} else if (newState == MeasuringState.INITIALIZING) {

			if ((((ExecutedEntity) this.getEntity()).getCurrentState(measuredEntityId) != MeasuringState.INITIALIZING) || 
					(((ExecutedEntity) this.getEntity()).startNewInterval(measuredEntityId))) {

				changeState(measuredEntityId, MeasuringState.INITIALIZING, false);

			}

		} 
		
		else {
			logger.error("The new state is being set to undefined, which is incorrect");
		}	
		logger.debug("setCurrentState End " + String.valueOf(measuredEntityId));
	}
	/**
	 * Update a previously defined stat, assigning its reason code. 
	 *  
	 * @param startDttmStr	start datetime when the interval to update started - this value works as a key for the interval states. 
	 * @param reasonCode	Reason code to assign.
	 * 	
	 * @return	true if the intervals was found and updated, false otherwise.
	 */
	public synchronized boolean updateStateInterval(Integer measuredEntityId, MeasuredEntityType measuredEntityType,  String startDttmStr, ReasonCode reasonCode) {
		logger.debug("updateStateInterval Start " + String.valueOf(measuredEntityId));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		LocalDateTime startDttm = LocalDateTime.parse(startDttmStr, formatter);
		
		logger.debug("reasoncode id:" + reasonCode.getId() + " descr:" + reasonCode.getDescription() + " startDttm:" + startDttm );
		
		boolean ret = false;
		
		if ( ((ExecutedEntity) this.getEntity()).getCurrentStatDateTime(measuredEntityId).withNano(0).equals(startDttm)){
			
			logger.debug("Updating the current state interval");
			
			((ExecutedEntity) this.getEntity()).setCurrentReasonCode(measuredEntityId, reasonCode);
			ret = true;
		
		} else {

			logger.debug("Updating the past state interval");
			
			LocalDateTime oldest = stateCache.getOldestTime();
			
			logger.debug("oldest" + oldest.format(formatter));
			

			LocalDateTime enddttm; 
			boolean updateEntityReason;
			if(oldest.isAfter(startDttm) )
			{
				logger.debug("the datetime given is after");
				// some values are in the database and maybe we have to continue updating the intervals. 
				enddttm = stateCache.updateExecutedEntityStateInterval(this.entity.getId(), this.entity.getType(), 
													measuredEntityId, measuredEntityType, startDttm, reasonCode);
				
				// We have to continue updating the intervals in the cache.
				if (enddttm == null) {
					updateEntityReason = false;
					SortedMap<LocalDateTime, String> tail = this.statesMap.tailMap(enddttm);
					for (Map.Entry<LocalDateTime, String> entry : tail.entrySet()) {
						StateInterval tmp = stateCache.getFromCache(entry.getValue());
						if ( tmp.getState() == MeasuringState.OPERATING) {
							updateEntityReason = true;
							break;
						}
						
						else {
							if ((tmp.getRelatedObject() == measuredEntityId) && (tmp.getRelatedObjectType() == measuredEntityType.getValue())){   
								stateCache.updateCacheStateInterval(entry.getValue(), reasonCode);
							}
						}
					}
					if (((ExecutedEntity) this.getEntity()).getCurrentState(measuredEntityId) == MeasuringState.UNSCHEDULEDOWN && !(updateEntityReason)) {
						((ExecutedEntity) this.getEntity()).setCurrentReasonCode(measuredEntityId, reasonCode);
					}
				}
					 				
			} else if(oldest.isBefore(startDttm)){
				// all values are in the state cache
				logger.debug("the datetime given is before");
				updateEntityReason = false;
				SortedMap<LocalDateTime, String> tail = this.statesMap.tailMap(startDttm);
				for (Map.Entry<LocalDateTime, String> entry : tail.entrySet()) {
					StateInterval tmp = stateCache.getFromCache(entry.getValue());
					if ( tmp.getState() == MeasuringState.OPERATING) {
						updateEntityReason = true;
						break;
					}
					
					else {
						if ((tmp.getRelatedObject() == measuredEntityId) && (tmp.getRelatedObjectType() == measuredEntityType.getValue())){   
							stateCache.updateCacheStateInterval(entry.getValue(), reasonCode);
						}
					}
				}
				if (((ExecutedEntity) this.getEntity()).getCurrentState(measuredEntityId) == MeasuringState.UNSCHEDULEDOWN && !(updateEntityReason)) {
					((ExecutedEntity) this.getEntity()).setCurrentReasonCode(measuredEntityId, reasonCode);
				}
			}
		
		}
		logger.debug("updateStateInterval End " + String.valueOf(measuredEntityId));
		return ret;
	}

	/**
	 * This method converts the current state in the executed entity in a StateInterval and returns as list.
	 * 
	 * @return current State Interval in list.
	 */
	public synchronized List<StateInterval> getCurrentStateInterval(Integer measuredEntityId) {
		logger.debug("getCurrentStateInterval Start " + String.valueOf(measuredEntityId));
		LocalDateTime localDateTime = LocalDateTime.now();

		// Creates the time interval, from the last status change to now.
		TimeInterval interval = new TimeInterval(((MeasuredEntity)this.getEntity()).getCurrentStatDateTime(), localDateTime);

		StateInterval current = BuildStateInterval(((MeasuredEntity)this.getEntity()).getCurrentState(), 
													((MeasuredEntity)this.getEntity()).getCurrentReason(), interval, measuredEntityId);
		
		List<StateInterval> list = new ArrayList<StateInterval>();
		
		list.add(current);
		logger.debug("getCurrentStateInterval End " + String.valueOf(measuredEntityId));
		return list;
	}

	/**
	 * Returns the json array with the format:<br>
	 * [ ... {"machine":machine,"status":sts,"startDttm":start,"endDttm":end,"reason":reason}...]
	 * 
	 * @return Json Array of current states. Actually is only one entry.
	 * @throws PropertyVetoException 
	 */
	public synchronized JSONArray getJsonCurrentState(Integer measuredEntityId) {
		logger.debug("getJsonCurrentState Start " + String.valueOf(measuredEntityId));
		logger.debug("getJsonCurrentState");
		JSONArray array = null;
		String cannonicalMachine = "";
			
		cannonicalMachine = entity.getCanonicalKey();

		List<StateInterval> intervals = getCurrentStateInterval(measuredEntityId);
		array = new JSONArray();
		for (StateInterval interval : intervals) {
			if (interval != null)
			{
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
				array.put(jsob);
			}
		}
		logger.debug("getJsonCurrentState End " + String.valueOf(measuredEntityId));
		return array;
	}
	
}

