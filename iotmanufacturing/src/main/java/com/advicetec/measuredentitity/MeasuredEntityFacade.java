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
import com.advicetec.core.EntityFacade;
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

		logger.debug("Creating Measured entity facade");
		
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
		
		logger.info("Finish Creating Measured entity facade");
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
			stateInterval = new StateInterval(status, reasonCode, interval, getEntity().getId(), getEntity().getType(), 
												executedEntity.getId(), executedEntity.getType().getValue(),executedEntity.getCanonicalKey(), 
													rate, conversion1, conversion2, actualRate, new Double(0));
		}
		else{
			stateInterval = new StateInterval(status, reasonCode, interval, getEntity().getId(), getEntity().getType(), 
												0, 0, "", rate, conversion1, conversion2, actualRate, new Double(0));
		}
		stateInterval.setKey(getEntity().getId()+stateInterval.getKey());
		// key in the map and the cache must be consistent
		super.statesMap.put(interval.getStart(),stateInterval.getKey());
		StateIntervalCache.getInstance().storeToCache(stateInterval);
		
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
	 * 
	 * @param list
	 * @return
	 */
	protected Map<Integer,DowntimeReason> sumarizeDowntimeReason(List<StateInterval> list) {
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
		((MeasuredEntity) this.getEntity()).addExecutedEntity(executedEntity);
		
		ExecutedEntityChange();
	}
	
	/**
	 * Stops all executed object that were previously executed in this measured entity. 
	 */
	public synchronized void stopExecutedObjects()
	{
		((MeasuredEntity) this.getEntity()).stopExecuteEntities();
	}
	
	/**
	 * Stops an executed object that was previously executed in this measured entity.
	 * 
	 * @param id identifier of the executed object to remove.
	 */
	public synchronized void removeExecutedObject(Integer id)
	{
		((MeasuredEntity) this.getEntity()).removeExecutedEntity(id);
		
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
		return ((MeasuredEntity) this.getEntity()).getAttributeFromExecutedObject(attributeId);
	}
	
	/**
	 * Change the executing entity being processed in the measure entity. Registers the corresponding interval representing the change. 
	 */
	public synchronized void ExecutedEntityChange(){
		
		LocalDateTime localDateTime = LocalDateTime.now();
		TimeInterval interval = new TimeInterval(this.entity.getCurrentStatDateTime(), localDateTime);
		this.registerInterval(this.entity.getCurrentState(), this.entity.getCurrentReason(), interval);
		this.getEntity().startInterval(localDateTime, this.entity.getCurrentState(), null);						

	}
	
	public synchronized ExecutedEntity getCurrentExecutedEntity() {
		return ((MeasuredEntity) this.getEntity()).getCurrentExecutedEntity();
	}

}

