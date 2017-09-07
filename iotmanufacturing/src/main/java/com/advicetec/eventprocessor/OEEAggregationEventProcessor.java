package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.aggregation.oee.OEEAggregationCalculator;
import com.advicetec.aggregation.oee.OEEAggregationContainer;
import com.advicetec.aggregation.oee.OEEAggregationManager;
import com.advicetec.aggregation.oee.OverallEquipmentEffectiveness;
import com.advicetec.core.Processor;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.utils.PeriodUtils;

/**
 * This class process a OEE aggregation event, it takes as a parameter the event to execute. 
 * To process an event of thsi type the system performs:
 * 		1. calculates the current date
 * 		2. calculates the current date OEE aggregation
 * 		3. calculates the current moth OEE aggreagtion
 * 		4. calculates the current year OEE aggregation
 *
 * If an aggregation was already calculated for any of these periods, then the system replace it.
 * 
 * @author Andres Marentes
 *
 */
public class OEEAggregationEventProcessor implements Processor
{

	static Logger logger = LogManager.getLogger(OEEAggregationEventProcessor.class.getName());
	
	/**
	 * Event  Aggregation to execute
	 */
	AggregationEvent event;

	/**
	 * Constructor for the class. It receives as parameter the OEE aggregation event.
	 * @param event OEE aggregation event
	 */
	public OEEAggregationEventProcessor(AggregationEvent event) {
		super();
		this.event = event;
	}

	/**
	 * process the event.
	 * 
	 * In this case the list of events returned is empty. 
	 */
	public List<DelayEvent> process() throws SQLException 
	{

		Integer measuringEntity = this.event.getEntity();
		MeasuredEntityType measuredEntityType = ((AggregationEvent) this.event).getOwnerType(); 

		MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade entityFacade = entityManager.getFacadeOfEntityById(measuringEntity);

		if (entityFacade != null){

			OEEAggregationCalculator oeeAggregationCalculator = new OEEAggregationCalculator(); 
			OEEAggregationManager manager = OEEAggregationManager.getInstance();
			OEEAggregationContainer OEEContainer = manager.getOeeAggregationContainer();
			List<OverallEquipmentEffectiveness> ret;
			
			logger.debug("before starting to execute OEE Aggregation Event Processor");
			
			// Calculates the current day
			LocalDateTime current = LocalDateTime.now();
			LocalDateTime formerDay = PeriodUtils.getStartOfDay(current);
			ret = oeeAggregationCalculator.calculateDay(measuringEntity, measuredEntityType,formerDay, true, true);
			
			for (OverallEquipmentEffectiveness oee : ret){
				logger.debug(oee.toString());
			}
			
			logger.debug("After executing for the day OEE Aggregation Event Processor");
			
			// Obtains the current month
			LocalDateTime month = LocalDateTime.of(current.getYear(), current.getMonthValue(), 1, 0, 0, 0 );
			ret = oeeAggregationCalculator.calculateMonth(measuringEntity, measuredEntityType,month, true, true);

			for (OverallEquipmentEffectiveness oee : ret){
				logger.debug(oee.toString());
			}
			
			logger.debug("After executing for the month OEE Aggregation Event Processor");
			
			// Obtains the current year
			LocalDateTime year = LocalDateTime.of(current.getYear(), 1, 1, 0, 0, 0 );
			ret = oeeAggregationCalculator.calculateYear(measuringEntity, measuredEntityType,year, true, true);
			
			for (OverallEquipmentEffectiveness oee : ret){
				logger.debug(oee.toString());
			}

			logger.debug("After executing for the year OEE Aggregation Event Processor");
			
		} else {
			logger.error("Facade not found" + measuringEntity);
		}

		return new ArrayList<DelayEvent>();

	}

}
