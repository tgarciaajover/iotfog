package com.advicetec.measuredentitity;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.applicationAdapter.ProductionOrderManager;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.EntityFacade;
import com.advicetec.core.TimeInterval;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.eventprocessor.PurgeFacadeCacheMapsEvent;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.persistence.StateIntervalCache;


/**
 * This class is a MeasuredEntity facade.
 * 
 * It allows the language processor to access the measured entity functionality
 * without exposing all its methods.
 *   
 * @author maldofer
 *
 */
public final class MeasuredEntityFacade extends EntityFacade {


	static Logger logger = LogManager.getLogger(MeasuredEntityFacade.class.getName());

		
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
	 *  This field establishes the temporality for calculate indicators. 
	 */
	private String ratesTemporality;
	
	/**
	 *  This field establishes the name of variable from estimated the actual rate. 
	 */
	private String actualRateVariable;
	
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
								 String unit1PerCycles, String unit2PerCycles,  
								   String actualProductionCountId, Integer purgeFacadeCacheMapEntries)
	{
		
		super(entity,purgeFacadeCacheMapEntries);
		logger.debug("MeasuredEntityFacade Start " + String.valueOf(entity.getId()));
		logger.debug("Creating Measured entity facade");
		
		this.productionRateId = productionRateId;
		this.unit1PerCycles = unit1PerCycles;
		this.unit2PerCycles = unit2PerCycles;
		this.actualProductionCountId = actualProductionCountId;
		this.purgeFacadeCacheMapEntries = purgeFacadeCacheMapEntries;
		this.ratesTemporality = getTemporalityRate();
		this.actualRateVariable = getActualRateVariableName();
		logger.info("Entity: " + entity.getId().toString() + " Temporality: " + this.ratesTemporality + " Variable Name: " + this.actualRateVariable);
		
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
		logger.debug("MeasuredEntityFacade End " + String.valueOf(entity.getId()));
		logger.debug("Finish Creating Measured entity facade");
	}
	

	/**
	 * Creates a new interval from the measuring state, reason code and data in the measured entity
	 * 
	 * @param status		new status for the measured entity 
	 * @param reasonCode	reason code of the new status
	 * @param interval		Time interval in which the measure entity remains in the status defined.
	 * 
	 * @return new state interval
	 */
	private StateInterval BuildStateInterval(MeasuringState status, ReasonCode reasonCode, TimeInterval interval)
	{
		logger.debug("BuildStateInterval Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		//logger.debug("In BuildStateInterval - status: " + status.getName().toString() + " interval: " + interval.getStart().toString());
		// Obtains the current executed entity being processed.
		ExecutedEntity executedEntity = ((MeasuredEntity)this.getEntity()).getCurrentExecutedEntity();
				
		// search the production rate in the actual production job, if not defined then search on the measured entity. 
		Double rate = ((MeasuredEntity)this.getEntity()).getProductionRate(this.productionRateId);
		Double conversion1 = ((MeasuredEntity)this.getEntity()).getConversion1(this.unit1PerCycles);
		Double conversion2 = ((MeasuredEntity)this.getEntity()).getConversion2(this.unit2PerCycles);
		
		if (rate == null)
			rate = new Double(0.0); // No rate defined. 
		
		if (conversion1 == null) {
			conversion1 = new Double(0.0); // No conversion defined
		}
		
		if (conversion2 == null) {
			conversion2 = new Double(0.0); // No conversion defined
		}
		
		Double actualRate = null; 
		
		if(this.actualRateVariable != null && !this.actualRateVariable.isEmpty()) {
			// Verifies that the actual production count id field is an attribute in the measuring entity
						if (!isAttribute(this.actualRateVariable)) {
							logger.error("The given attribute: " + this.actualRateVariable + " does not exists as attribute in the measuring entity");
							actualRate = new Double(0.0);
						} else {
							List<AttributeValue> list = getByIntervalByAttributeName(this.actualRateVariable, interval.getStart(), interval.getEnd());
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
									logger.error("The production count attribute: " + this.actualRateVariable + " parametrized is not of type Double or Integer");
									break;
								}
							}
							
							LocalDateTime tempDateTime = LocalDateTime.from( interval.getStart() );
							long seconds = tempDateTime.until( interval.getEnd(), ChronoUnit.SECONDS);
							double tmp = 0;
							if (seconds > 0) {
								if (this.ratesTemporality.equals("H")) {
									tmp = (sum * 3600) / seconds;
								} else {
									tmp = (sum * 60) / seconds;
								}
							}
								
							actualRate = new Double(tmp);    // The actual rate is in cycle over minutes.
						}
		} else {
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
				double tmp = 0;
				if (seconds > 0) {
					if (this.ratesTemporality.equals("H")) {
						tmp = (sum * 3600) / seconds;
					} else {
						tmp = (sum * 60) / seconds;
					}
				}
					
				actualRate = new Double(tmp);    // The actual rate is in cycle over minutes.
			}
		}
			
		StateInterval stateInterval = null;
		if (executedEntity != null){
			stateInterval = new StateInterval(status, reasonCode, interval, getEntity().getId(), getEntity().getType(), 
												executedEntity.getId(), executedEntity.getType().getValue(),executedEntity.getCanonicalKey(), 
													rate, conversion1, conversion2, actualRate, new Double(0));
		}
		else{
			stateInterval = new StateInterval(status, reasonCode, interval, getEntity().getId(), getEntity().getType(), 
												0, 0, "", rate, conversion1, conversion2, actualRate, new Double(0));
		}
		stateInterval.setKey(getEntity().getId()+ ":" + stateInterval.getKey());
		logger.debug("BuildStateInterval End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		return stateInterval;
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
		logger.debug("registerInterval Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		//logger.debug("In registerInterval - status: " + status.getName().toString() + " interval: " + interval.getStart().toString());
		StateInterval stateInterval = BuildStateInterval(status, reasonCode, interval);
		// key in the map and the cache must be consistent
		super.statesMap.put(interval.getStart(),stateInterval.getKey());
		StateIntervalCache.getInstance().storeToCache(stateInterval);
		logger.debug("registerInterval End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
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
		logger.debug("getJsonDowntimeReasons Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
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
		logger.debug("getJsonDowntimeReasons End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		return array;
	}
	
	public synchronized double getDBActualProductionRate() {
		Double productionRate = 0.0;
		if (this.getCurrentState().getName().equals("UnScheduleDown")) {
			productionRate = 0.0;
		} else {
			if (this.getCurrentState().getName().equals("ScheduleDown")) {
				List<StateInterval> intervals = this.getCurrentStateInterval();
				for (StateInterval interval : intervals){
					productionRate = interval.getActualProductionRate();
				}
			} else {
				productionRate = stateCache.getDBActualProductionRate(((MeasuredEntity)this.getEntity()).getId(), ((MeasuredEntity)this.getEntity()).getType().getValue());
			}
		}
		return productionRate;
	}

	
		/**
	 * 
	 * @param list
	 * @return
	 */
	protected Map<Integer,DowntimeReason> sumarizeDowntimeReason(List<StateInterval> list) {
		logger.debug("sumarizeDowntimeReason Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
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
		logger.debug("sumarizeDowntimeReason End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
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
		logger.debug("addExecutedObject Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		((MeasuredEntity) this.getEntity()).addExecutedEntity(executedEntity);
		logger.debug("addExecutedObject End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
	}
	
	/**
	 * Stops all executed object that were previously executed in this measured entity. 
	 */
	public synchronized void stopExecutedObjects()
	{
		logger.debug("stopExecutedObjects Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		((MeasuredEntity) this.getEntity()).stopExecuteEntities();
		logger.debug("stopExecutedObjects End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
	}
	
	/**
	 * Stops an executed object that was previously executed in this measured entity.
	 * 
	 * @param id identifier of the executed object to remove.
	 */
	public synchronized void removeExecutedObject(Integer id)
	{
		logger.debug("removeExecutedObject Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		((MeasuredEntity) this.getEntity()).removeExecutedEntity(id);
		
		ExecutedEntityChange();
		logger.debug("removeExecutedObject End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
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
		logger.debug("getExecutedObjectAttribute Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		AttributeValue attributeValue = ((MeasuredEntity) this.getEntity()).getAttributeFromExecutedObject(attributeId);
		logger.debug("getExecutedObjectAttribute End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		return attributeValue;
	}
	
	private void changeState(MeasuringState newState, boolean persistReason)  {
		logger.debug("changeState Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		
		logger.debug("Change State - new State:" + newState.getName());
		
		LocalDateTime localDateTime = LocalDateTime.now();

		// Creates the time interval, from the last status change to now.
		TimeInterval interval = new TimeInterval(((MeasuredEntity)this.getEntity()).getCurrentStatDateTime(), localDateTime);
		
		// Registers the interval in the Measured Entity
		this.registerInterval(((MeasuredEntity)this.getEntity()).getCurrentState(), 
							  ((MeasuredEntity)this.getEntity()).getCurrentReason(), 
							  interval);
		
		// Starts a new Interval depending of new state and origin.
		if (persistReason) {
			((MeasuredEntity)this.getEntity()).startInterval(localDateTime, newState, ((MeasuredEntity)this.getEntity()).getCurrentReason());
		} else {
			((MeasuredEntity)this.getEntity()).startInterval(localDateTime, newState, null);
		}
		
		
		// Verify whether or not there is an executed entity being processed.
		// In case of being processed, then updates the executed entity state.
		
		try {
			
			ExecutedEntity executedEntity = getCurrentExecutedEntity();
			
			if (executedEntity != null) {				
				
				ProductionOrderManager orderManager;
					orderManager = ProductionOrderManager.getInstance();
				ExecutedEntityFacade executedEntityFacade = orderManager.getFacadeOfPOrderById(executedEntity.getId()); 
				
				if (executedEntityFacade != null) {
					
					//logger.debug("Change state - it is going to change executed object state - new state:" + newState.getName());
					//TG 20180901 18:50 TRATANDO DE SOLUCIONAR LOS BLOQUEOS;
					//executedEntityFacade.setCurrentState(newState, getEntity().getId());
				}
			}
		
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		logger.debug("changeState End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		
	}
	
	/**
	 * Change the executing entity being processed in the measure entity. 
	 * 
	 * Registers an interval representing the change. 
	 */
	public synchronized void ExecutedEntityChange(){
		logger.debug("ExecutedEntityChange Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		LocalDateTime localDateTime = LocalDateTime.now();

		// Creates the time interval, from the last status change to now.
		TimeInterval interval = new TimeInterval(((MeasuredEntity)this.getEntity()).getCurrentStatDateTime(), localDateTime);
		
		// Registers the interval in the Measured Entity
		this.registerInterval(((MeasuredEntity)this.getEntity()).getCurrentState(), 
							  ((MeasuredEntity)this.getEntity()).getCurrentReason(), 
							  interval);
		
		// Starts a new Interval.
		((MeasuredEntity)this.getEntity()).startInterval(localDateTime, this.getCurrentState(), this.getCurrentReason());
		logger.debug("ExecutedEntityChange End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
	}
	
	public synchronized ExecutedEntity getCurrentExecutedEntity() {
		logger.debug("getCurrentExecutedEntity Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		ExecutedEntity executedEntity = ((MeasuredEntity) this.getEntity()).getCurrentExecutedEntity();
		logger.debug("getCurrentExecutedEntity End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		return executedEntity;
	}
	
	/**
	 * Returns the current state of the measured entity.
	 *  
	 * @return  If there is not entity assigned return undefined.
	 */    
	public synchronized MeasuringState getCurrentState(){
		 logger.debug("getCurrentState Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
    	 if (this.getEntity() == null){
    		 logger.debug("getCurrentState End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
    		 return MeasuringState.UNDEFINED;
    	 } else {
    		 MeasuringState measuringState = ((MeasuredEntity) this.getEntity()).getCurrentState();
    		 logger.debug("getCurrentState End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
    		 return measuringState;
    	 }
     }
	
	
	/**
	 * Returns the current reason of the measured entity.
	 *  
	 * @return  If there is not entity assigned return null.
	 */    
	public synchronized ReasonCode getCurrentReason(){
		logger.debug("getCurrentReason Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
    	 if (this.getEntity() == null){
    		 logger.debug("getCurrentReason End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
    		 return null;
    	 } else {
    		 ReasonCode reasonCode = ((MeasuredEntity)this.getEntity()).getCurrentReason();
    		 logger.debug("getCurrentReason End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
    		 return reasonCode;
    	 }
     }


	/**
	 * Updates the state of the entity taking as parameter 
	 * the measured attributes resulting from a transformation or behavior execution 
	 * 
	 * @param symbolMap				symbols generated by the transformation or behavior execution.
	 */
	public synchronized void setCurrentState(Map<String, ASTNode> symbolMap) {
		logger.debug("setCurrentState Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		logger.debug ("Measured entity in setCurrentState" );
		
		for (Map.Entry<String, ASTNode> entry : symbolMap.entrySet()) 
		{
			if ( entry.getKey().compareTo("state") == 0 )
			{
				
				ASTNode node = entry.getValue();
				MeasuringState newState = node.asMeasuringState();
				
				logger.debug ("Measured entity setCurrentState new state:" +  newState.getName());
				
				MeasuringState currentState = ((MeasuredEntity)this.getEntity()).getCurrentState();
				
				if ( newState == MeasuringState.OPERATING ){
					if (( currentState != MeasuringState.OPERATING) || 
							(((MeasuredEntity)this.getEntity()).startNewInterval())) {
						
						changeState(MeasuringState.OPERATING, false);
												
					}
				} else if ( newState == MeasuringState.SCHEDULEDOWN ){
					
					if (( currentState != MeasuringState.SCHEDULEDOWN) || 
							(((MeasuredEntity)this.getEntity()).startNewInterval())) {
						
						changeState(MeasuringState.SCHEDULEDOWN, false);						
					}
					
				} else if ( newState == MeasuringState.UNSCHEDULEDOWN ){
					
					if (( currentState != MeasuringState.UNSCHEDULEDOWN) || 
							(((MeasuredEntity)this.getEntity()).startNewInterval())) {
						
						changeState(MeasuringState.UNSCHEDULEDOWN, true);						
					}

				}  else if ( newState == MeasuringState.INITIALIZING ){

					if (( currentState != MeasuringState.INITIALIZING) || 
							(((MeasuredEntity)this.getEntity()).startNewInterval())) {
						
						changeState(MeasuringState.INITIALIZING, false);						
					}

				} else {
					
					logger.error("The new state is being set to undefined, which is incorrect");
					
				}	
			}
		}
		logger.debug("setCurrentState End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
	}

	public synchronized void setCurrentState(MeasuringState newState) {
		logger.debug("setCurrentState Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		
		logger.debug("Change Current State - new State:" + newState.getName() );
		
		MeasuringState currentState = ((MeasuredEntity)this.getEntity()).getCurrentState();
		
		if (newState == MeasuringState.OPERATING ){
			if (( currentState != MeasuringState.OPERATING) || 
					(((MeasuredEntity)this.getEntity()).startNewInterval())) {
				
				changeState(MeasuringState.OPERATING, false);
										
			}
		} else if (newState == MeasuringState.SCHEDULEDOWN){
			
			if (( currentState != MeasuringState.SCHEDULEDOWN) || 
					(((MeasuredEntity)this.getEntity()).startNewInterval())) {
				
				changeState(MeasuringState.SCHEDULEDOWN, false);						
			}
			
		} else if (newState == MeasuringState.UNSCHEDULEDOWN){
			
			if (( currentState != MeasuringState.UNSCHEDULEDOWN) || 
					(((MeasuredEntity)this.getEntity()).startNewInterval())) {
				
				changeState(MeasuringState.UNSCHEDULEDOWN, false);						
			}
			
		} else if (newState == MeasuringState.SYSTEMDOWN) {

			if (( currentState != MeasuringState.SYSTEMDOWN) || 
					(((MeasuredEntity)this.getEntity()).startNewInterval())) {
				
				changeState(MeasuringState.SYSTEMDOWN, false);						
			}
			
		} else if (newState == MeasuringState.INITIALIZING){
			
			if (( currentState != MeasuringState.INITIALIZING) || 
					(((MeasuredEntity)this.getEntity()).startNewInterval())) {
				
				changeState(MeasuringState.INITIALIZING, false);						
			}
			
		} 
		
		else {
			logger.error("The new state is being set to undefined, which is incorrect");
		}	
		logger.debug("setCurrentState End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
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
		logger.debug("updateStateInterval Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		LocalDateTime startDttm = LocalDateTime.parse(startDttmStr, formatter);
		
		logger.debug("reasoncode id:" + reasonCode.getId() + " descr:" + reasonCode.getDescription() + " startDttm:" + startDttm );
		
		boolean ret = false;
		
		if ( ((MeasuredEntity) this.getEntity()).getCurrentStatDateTime().withNano(0).equals(startDttm)){
			
			logger.debug("Updating the current state interval");
			
			((MeasuredEntity) this.getEntity()).setCurrentReasonCode(reasonCode);
			ret = true;
			
		} else {

			logger.debug("Updating the past state interval");
			
			LocalDateTime oldest = stateCache.getOldestTime();
			
			logger.debug("oldest" + oldest.format(formatter));
			
			
			LocalDateTime enddttm; 
			boolean updateEntityReason;
			if(oldest.isAfter(startDttm) )
			{
				// some values are in the database and maybe we have to continue updating the intervals. 
				enddttm = stateCache.updateMeasuredEntityStateInterval(this.entity.getId(), this.entity.getType(), startDttm, reasonCode);
				
				// We have to continue updating the intervals in the cache.
				if (enddttm == null) {
					updateEntityReason = false;
					SortedMap<LocalDateTime, String> tail = this.statesMap.tailMap(startDttm);
					for (Map.Entry<LocalDateTime, String> entry : tail.entrySet()) {
						if (stateCache.getFromCache(entry.getValue()).getState() == MeasuringState.OPERATING) {
							updateEntityReason = true;
							break;
						}
						else
							stateCache.updateCacheStateInterval(entry.getValue(), reasonCode);
						
					}
					if (((MeasuredEntity) this.getEntity()).getCurrentState() == MeasuringState.UNSCHEDULEDOWN && !(updateEntityReason)) {
						((MeasuredEntity) this.getEntity()).setCurrentReasonCode(reasonCode);
					}
				}
					 				
			} else if(oldest.isBefore(startDttm)){
				// all values are in the state cache
				logger.debug("the datetime given is before");
				updateEntityReason = false;
				SortedMap<LocalDateTime, String> tail = this.statesMap.tailMap(startDttm);
				for (Map.Entry<LocalDateTime, String> entry : tail.entrySet()) {
					if (stateCache.getFromCache(entry.getValue()).getState() == MeasuringState.OPERATING) {
						updateEntityReason = true;
						break;
					}
					else
						stateCache.updateCacheStateInterval(entry.getValue(), reasonCode);
				}			
				if (((MeasuredEntity) this.getEntity()).getCurrentState() == MeasuringState.UNSCHEDULEDOWN && !(updateEntityReason)) {
					((MeasuredEntity) this.getEntity()).setCurrentReasonCode(reasonCode);
				}
			}
		}
		logger.debug("updateStateInterval End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		return ret;
	}
	
	/**
	 * This method converts the current state in the measured entity in a StateInterval and returns as list.
	 * 
	 * @return current State Interval in list.
	 */
	public synchronized List<StateInterval> getCurrentStateInterval() {
		logger.debug("getCurrentStateInterval Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		LocalDateTime localDateTime = LocalDateTime.now();

		// Creates the time interval, from the last status change to now.
		TimeInterval interval = new TimeInterval(((MeasuredEntity)this.getEntity()).getCurrentStatDateTime(), localDateTime);

		StateInterval current = BuildStateInterval(((MeasuredEntity)this.getEntity()).getCurrentState(), 
													((MeasuredEntity)this.getEntity()).getCurrentReason(), interval);
		
		List<StateInterval> list = new ArrayList<StateInterval>();
		
		list.add(current);
		logger.debug("getCurrentStateInterval End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		return list;
	}

	
	/**
	 * Returns the json array with the format:<br>
	 * [ ... {"machine":machine,"status":sts,"startDttm":start,"endDttm":end,"reason":reason}...]
	 * 
	 * @return Json Array of current states. Actually is only one entry.
	 * @throws PropertyVetoException 
	 */
	public synchronized JSONArray getJsonCurrentState() {
		logger.debug("getJsonCurrentState Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		logger.debug("getJsonCurrentState");
		JSONArray array = null;
		String cannonicalMachine = "";
			
		cannonicalMachine = entity.getCanonicalKey();

		List<StateInterval> intervals = getCurrentStateInterval();
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
				jsob.append("productionRate", interval.getProductionRate());
				jsob.append("actualProductionRate", interval.getActualProductionRate());
				array.put(jsob);
			}
		}
		logger.debug("getJsonCurrentState End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		return array;
	}

	
	public synchronized double getCurrentStateDuration(){
		logger.debug("getCurrentStateDuration Start " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		LocalDateTime to = LocalDateTime.now(); 
		LocalDateTime from = LocalDateTime.now().minusDays(2);
		List<StateInterval> intervals = getStatesByInterval(from, to);
		StateInterval intervalActual = getCurrentStateInterval().get(0);
		ListIterator li = intervals.listIterator(intervals.size());
		String currenstate="", laststate="";
		Double totDuration = 0.0;
		
		currenstate = intervalActual.getState().getName();
		laststate = currenstate;
		totDuration = intervalActual.getDurationMin();
		
		while(li.hasPrevious()) {
			StateInterval interval = (StateInterval) li.previous();
			currenstate = interval.getState().getName();			
			if(laststate == currenstate){
				totDuration = totDuration + interval.getDurationMin();
			}else{
				return totDuration;
			}
			laststate = currenstate;
		}	
		logger.debug("getCurrentStateDuration End " + String.valueOf(((MeasuredEntity)this.getEntity()).getId()));
		return totDuration;
	}

}

