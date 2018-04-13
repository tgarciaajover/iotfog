package com.advicetec.aggregation.sched;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.aggregation.oee.OverallEquipmentEffectiveness;
import com.advicetec.configuration.AggregateMethod;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.utils.PredefinedPeriod;

/**
 * This class implements the algorithm to calculate OEE aggregations. There are four kinds of predefined aggregation periods: year, month, day and hour.
 * @author andres Marentes
 *
 */
public class SchedAggregationCalculator {

	static Logger logger = LogManager.getLogger(SchedAggregationCalculator.class.getName());

	/**
	 * 
	 */
	public SchedAggregationCalculator(){
		super();
	}
	
	
	
	
	/**
	 * Calculate the aggregation method.
	 * @param measuringEntity: measuring entity for which we are going to calculate the aggregation.
	 * @param measuredEntityType: type of measuring entity
	 * @param formerYear: Aggregation calculation year 
	 * @param insert   True, it inserts in the database sub-periods calculations. False otherwise.
	 * @param replace: if already exist in the DB, then it recalculates and replaces the OEE aggregation. 
	 * @return The new OEE aggregation calculated. It is returned in a list to make it easier for caller's procedures.
	 */
	public boolean calculateAggregateMethod(Integer measuringEntity, AggregateMethod aggregateMethod) {

		// if the former year is already store, then does nothing, else calculate

		SchedAggregationManager manager = SchedAggregationManager.getInstance();
		SchedAggregationContainer scheedContainer = manager.getSchedAggregationContainer();
		scheedContainer.dbCall_USP(aggregateMethod,measuringEntity);
		
		return true;
	}
}
