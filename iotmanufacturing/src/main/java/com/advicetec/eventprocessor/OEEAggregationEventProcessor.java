package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
import com.advicetec.utils.PredefinedPeriod;

public class OEEAggregationEventProcessor implements Processor
{

	static Logger logger = LogManager.getLogger(OEEAggregationEventProcessor.class.getName());
	AggregationEvent event;

	public OEEAggregationEventProcessor(AggregationEvent event) {
		super();
		this.event = event;
	}

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
			
			logger.info("before starting to execute OEE Aggregation Event Processor");
			
			// Calculates the current day
			LocalDateTime current = LocalDateTime.now();
			LocalDateTime formerDay = PeriodUtils.getStartOfDay(current);
			ret = oeeAggregationCalculator.calculateDay(measuringEntity, measuredEntityType,formerDay, true, true);
			
			for (OverallEquipmentEffectiveness oee : ret){
				logger.info(oee.toString());
			}
			
			logger.info("After executing for the day OEE Aggregation Event Processor");
			
			/* Removes the information for the current day. 
			 * We are replacing previous calculations for the current date*/
			// OEEContainer.dbInsert(ret);

			// Obtains the current month
			LocalDateTime month = LocalDateTime.of(current.getYear(), current.getMonthValue(), 1, 0, 0, 0 );
			ret = oeeAggregationCalculator.calculateMonth(measuringEntity, measuredEntityType,month, true, true);

			for (OverallEquipmentEffectiveness oee : ret){
				logger.info(oee.toString());
			}
			
			logger.info("After executing for the month OEE Aggregation Event Processor");
			
			/* Removes the information for the current month. 
			 * We are replacing previous calculations for the current month*/
			// OEEContainer.dbDelete(ret);
			// OEEContainer.dbInsert(ret);


			// Obtains the current year
			LocalDateTime year = LocalDateTime.of(current.getYear(), 1, 1, 0, 0, 0 );
			ret = oeeAggregationCalculator.calculateYear(measuringEntity, measuredEntityType,year, true, true);
			
			for (OverallEquipmentEffectiveness oee : ret){
				logger.info(oee.toString());
			}

			logger.info("After executing for the year OEE Aggregation Event Processor");
			
			/* Removes the information for the current year. 
			 * We are replacing previous calculations for the current year*/
			// OEEContainer.dbDelete(ret);
			// OEEContainer.dbInsert(ret);

		} else {
			logger.error("Facade not found" + measuringEntity);
		}

		return new ArrayList<DelayEvent>();

	}

}
