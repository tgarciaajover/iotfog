package com.advicetec.aggregation.oee;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.utils.PredefinedPeriod;

/**
 * This class implements the algorithm to calculate OEE aggregations. There are four kinds of predefined aggregation periods: year, month, day and hour.
 * @author andres Marentes
 *
 */
public class OEEAggregationCalculator {

	static Logger logger = LogManager.getLogger(OEEAggregationCalculator.class.getName());

	/**
	 * 
	 */
	public OEEAggregationCalculator(){
		super();
	}
	
	/**
	 * Calculate the OEE aggregation for the year. The idea is to verify whether aggregations for the months are previously calculated. If they exist then it
	 * aggregates the values and calculates the year aggregation. Otherwise it calls the month aggregation.
	 * @param measuringEntity: measuring entity for which we are going to calculate the aggregation.
	 * @param measuredEntityType: type of measuring entity
	 * @param formerYear: Aggregation calculation year 
	 * @param insert   True, it inserts in the database sub-periods calculations. False otherwise.
	 * @param replace: if already exist in the DB, then it recalculates and replaces the OEE aggregation. 
	 * @return The new OEE aggregation calculated. It is returned in a list to make it easier for caller's procedures.
	 */
	public List<OverallEquipmentEffectiveness> calculateYear(Integer measuringEntity, 
						MeasuredEntityType measuredEntityType, LocalDateTime formerYear, 
						boolean insert, boolean replace) {

		// if the former year is already store, then does nothing, else calculate

		OEEAggregationManager manager = OEEAggregationManager.getInstance();
		OEEAggregationContainer OEEContainer = manager.getOeeAggregationContainer();
		List<OverallEquipmentEffectiveness> ret = new ArrayList<OverallEquipmentEffectiveness>();

		PredefinedPeriod yearPeriod = new PredefinedPeriod(formerYear.getYear());
		if (OEEContainer != null){
			if (!OEEContainer.existPeriodOEE(measuringEntity, measuredEntityType, yearPeriod) 
					|| replace ){
				List<OverallEquipmentEffectiveness> list = 
						OEEContainer.getOEEList(measuringEntity, measuredEntityType, yearPeriod);
				
				LocalDateTime now = LocalDateTime.now();
				
				if(list.size() != 12){

					for (int i = 1; i <= 12; i++) {
						
						LocalDateTime month = LocalDateTime.of(formerYear.getYear(), i, 1, 1, 0);
						
						if (month.isBefore(now)) {
						
							PredefinedPeriod per = new PredefinedPeriod(formerYear.getYear(), i);
							if (!existsOEEinList( list, per)){
								LocalDateTime localdatetime = LocalDateTime.of(formerYear.getYear(), i, 1, 0, 0);
								List<OverallEquipmentEffectiveness> oeeMonth = 
										calculateMonth(measuringEntity, measuredEntityType, localdatetime, insert, false);
								list.addAll(oeeMonth);
							}
						} else {
							// This corresponds to a month in the future
							PredefinedPeriod predefined = new PredefinedPeriod(formerYear.getYear(), i);
							OverallEquipmentEffectiveness oee = new OverallEquipmentEffectiveness(predefined, measuringEntity,measuredEntityType);
							list.add(oee);
						}
					}	
				}

				ret = aggregateList(measuringEntity, measuredEntityType, yearPeriod,  list);
				if (insert){
					
					logger.debug("Replacing the OEE for year:" + yearPeriod.getKey());
					
					OEEContainer.dbDelete(ret);
					OEEContainer.dbInsert(ret);
				}
			}  else {
				logger.debug("The aggregation is already calculed to predefined period:" + yearPeriod.getKey() 
						+ " Measured Entity:" + measuringEntity + "Entity Type:" + measuredEntityType.getName() );
			}

		} else {

			logger.error("The OEE Aggretation Container was not created");
		}
		return ret;
	}

	/**
	 * Verify if a particular OEE aggregation is in a list of OEE aggregations.
	 * 
	 * @param list: a list of OEE aggregations where we find a specific aggregation with the predefined period.  
	 * @param predefinedPeriod: period to find.
	 * @return True if the predefined period exists, false otherwise.
	 */
	private boolean existsOEEinList(List<OverallEquipmentEffectiveness> list, PredefinedPeriod predefinedPeriod){
		for(OverallEquipmentEffectiveness month : list){
			if(month.getPredefinedPeriod().equals(predefinedPeriod)){
				return true;
			}
		}
		return false;
	}


	/**
	 * Calculate the OEE aggregation for a month. The idea is to verify whether aggregations for the days are previously calculated. If they exist, then it
	 * aggregates the values and calculates the month aggregation. Otherwise it calls the day aggregation.
	 * @param measuringEntity: measuring entity for which we are going to calculate the aggregation.
	 * @param measuredEntityType: type of measuring entity.
	 * @param formerMonth: Aggregation calculation month. 
	 * @param insert: True, it inserts in the database sub-periods calculations. False otherwise.
	 * @param replace: if already exist in the DB, then it recalculates and replaces the OEE aggregation. 
	 * @return The new OEE month aggregation calculated. It is returned in a list to make it easier for caller's procedures.
	 */
	public List<OverallEquipmentEffectiveness> calculateMonth(
			Integer measuringEntity, MeasuredEntityType measuredEntityType, 
			LocalDateTime formerMonth, boolean insert, boolean replace) {

		OEEAggregationManager manager = OEEAggregationManager.getInstance();
		OEEAggregationContainer OEEContainer = manager.getOeeAggregationContainer();
		List<OverallEquipmentEffectiveness> ret = new ArrayList<OverallEquipmentEffectiveness>();

		PredefinedPeriod monthPeriod = new PredefinedPeriod(formerMonth.getYear(), formerMonth.getMonthValue());
		if (OEEContainer != null){
			if (!OEEContainer.existPeriodOEE(measuringEntity, measuredEntityType, monthPeriod) || replace){
				
				List<OverallEquipmentEffectiveness> list = 
						OEEContainer.getOEEList(measuringEntity, measuredEntityType, monthPeriod);

				YearMonth yearMonthObject = YearMonth.of(formerMonth.getYear(), formerMonth.getMonthValue());
				int daysInMonth = yearMonthObject.lengthOfMonth();
				
				LocalDateTime now = LocalDateTime.now();
				
				if(list.size() != daysInMonth){
					
					for (int i = 1; i <= daysInMonth; i++) {
						
						LocalDateTime day = LocalDateTime.of(formerMonth.getYear(), formerMonth.getMonthValue(), i, 1, 0);
						
						if (day.isBefore(now)) {
						
							PredefinedPeriod per = new PredefinedPeriod(formerMonth.getYear(), formerMonth.getMonthValue(), i);
							if (!existsOEEinList( list, per)){
								LocalDateTime localdatetime = LocalDateTime.of(formerMonth.getYear(), formerMonth.getMonthValue(), i, 0, 0);
								List<OverallEquipmentEffectiveness> oeeDay = calculateDay(measuringEntity, measuredEntityType, localdatetime, insert, false);
								list.addAll(oeeDay);
							}
						} else {
							// This corresponds to a day in the future
							PredefinedPeriod predefined = new PredefinedPeriod(formerMonth.getYear(), 
									formerMonth.getMonthValue(), i);
							OverallEquipmentEffectiveness oee = new OverallEquipmentEffectiveness(predefined, measuringEntity,measuredEntityType);
							list.add(oee);
						}
					}
					
				}

				ret = aggregateList(measuringEntity, measuredEntityType, monthPeriod,  list);
				if (insert){
					
					logger.debug("Replacing the OEE for month:" + monthPeriod.getKey());
					
					OEEContainer.dbDelete(ret);
					OEEContainer.dbInsert(ret);
				}

			} else {
				logger.debug("The aggregation is already calculed to predefined period:" + monthPeriod.getKey() 
								+ " Measured Entity:" + measuringEntity + "Entity Type:" + measuredEntityType.getName() );
			}

		} else {

				logger.error("The OEE Aggretation Container was not created");
		}

		return ret;
	}

	/**
	 * Calculate the OEE aggregation for a day. The idea is to verify whether aggregations for the hours have been previously calculated. If they exist, then it
	 * aggregates the values and calculates the day aggregation. Otherwise it calls the hour aggregation.
	 * @param measuringEntity: measuring entity for which we are going to calculate the aggregation.
	 * @param measuredEntityType: type of measuring entity.
	 * @param formerDay: Aggregation calculation day. 
	 * @param insert: True, it inserts in the database sub-periods calculations. False otherwise.
	 * @param replace: if already exist in the DB, then it recalculates and replaces the OEE aggregation. 
	 * @return The new OEE day aggregation calculated. It is returned in a list to make it easier for caller's procedures.
	 */
	public List<OverallEquipmentEffectiveness> calculateDay(Integer measuringEntity, 
			MeasuredEntityType measuredEntityType, 
			LocalDateTime formerDay, 
			boolean insert, boolean replace) {

		OEEAggregationManager manager = OEEAggregationManager.getInstance();
		OEEAggregationContainer OEEContainer = manager.getOeeAggregationContainer();
		PredefinedPeriod period = new PredefinedPeriod(formerDay.getYear(), 
				formerDay.getMonthValue(), formerDay.getDayOfMonth() );
		
		List<OverallEquipmentEffectiveness> ret = new ArrayList<OverallEquipmentEffectiveness>();

		if (OEEContainer != null){
			
			if (!OEEContainer.existPeriodOEE(measuringEntity, measuredEntityType,period) || replace ){
				List<OverallEquipmentEffectiveness> list = OEEContainer.getOEEList(
						measuringEntity, measuredEntityType, period);
				
				logger.debug("Nbr registers found:" + list.size());

				LocalDateTime now = LocalDateTime.now();

				if (list.size() != 24){

					for (int i = 0; i < 24; i++) {

						LocalDateTime hour = LocalDateTime.of(formerDay.getYear(), formerDay.getMonthValue(), formerDay.getDayOfMonth(), i, 0);

						if (hour.isBefore(now)){

							PredefinedPeriod per = new PredefinedPeriod(formerDay.getYear(), 
																		 formerDay.getMonthValue(), formerDay.getDayOfMonth(), i);

							if (!existsOEEinList( list, per)){
								List<OverallEquipmentEffectiveness> oeeHour = calculateHour(measuringEntity, measuredEntityType, hour, insert, false);
								list.addAll(oeeHour);
							}
						} else {
							// This corresponds to a value in the future
							PredefinedPeriod predefined = new PredefinedPeriod(formerDay.getYear(), 
									formerDay.getMonthValue(), formerDay.getDayOfMonth(), i);
							OverallEquipmentEffectiveness oee = new OverallEquipmentEffectiveness(predefined, measuringEntity,measuredEntityType);
							list.add(oee);
						}	
					}	
				}
				ret = aggregateList(measuringEntity, measuredEntityType, period,  list);
				if (insert){
					
					logger.debug("Number of registers:" + ret.size() + " Replacing the OEE for day:" + period.getKey());
					
					OEEContainer.dbDelete(ret);
					OEEContainer.dbInsert(ret);
				}
				
			} else {
				logger.debug("The aggregation is already calculed to predefined period:" + period.getKey() 
								+ " Measured Entity:" + measuringEntity + "Entity Type:" + measuredEntityType.getName() );
			}
			
		} else {

			logger.error("The OEE Aggretation Container was not created");
		}
		
		logger.debug("Finishing calculate by day "+ formerDay);
		
		return ret;
	}


	/**
	 * Calculate the OEE aggregation for an hour. The idea is to verify whether aggregations for five minute intervals within the hour have been previously calculated. 
	 * If they exist, then it aggregates the values and calculates the hour aggregation. Otherwise it calculates subperiods aggregation.
	 * @param measuringEntity: measuring entity for which we are going to calculate the aggregation.
	 * @param measuredEntityType: type of measuring entity.
	 * @param formerHour: Aggregation calculation hour. 
	 * @param insert: True, it inserts in the database sub-periods calculations. False otherwise.
	 * @param replace: if already exist in the DB, then it recalculates and replaces the OEE aggregation. 
	 * @return The new OEE day aggregation calculated. It is returned in a list to make it easier for caller's procedures.
	 */
	public List<OverallEquipmentEffectiveness> calculateHour(Integer measuringEntity, 
							  MeasuredEntityType measuredEntityType, 
							  LocalDateTime formerHour,
							  boolean insert, boolean replace) {

		logger.debug("In calculateHour params measuredEntity:" + measuringEntity + "entityType:" + measuredEntityType.getName() + 
					" hour: " + formerHour.toString());
		
		OEEAggregationManager manager = OEEAggregationManager.getInstance();
		OEEAggregationContainer OEEContainer = manager.getOeeAggregationContainer();
		PredefinedPeriod period = new PredefinedPeriod(formerHour.getYear(), formerHour.getMonthValue(), 
				formerHour.getDayOfMonth(), formerHour.getHour());

		List<OverallEquipmentEffectiveness> ret = new ArrayList<OverallEquipmentEffectiveness>();
		
		if (OEEContainer != null){
			if (!OEEContainer.existPeriodOEE(measuringEntity, measuredEntityType, period) || replace){

				String parQueryFrom =  period.getKey()+":00:00.001";
				String parQueryTo =  period.getKey()+":59:59.999";
				List<OverallEquipmentEffectiveness> list = OEEContainer.intervalsByHour(
						measuringEntity,measuredEntityType, period.getKey(), parQueryFrom,parQueryTo);
				logger.debug("number of oees calculated for the hour:" + list.size());
				ret = aggregateList(measuringEntity, measuredEntityType, period,  list);
				
				if (insert){
					
					logger.debug("Replacing the OEE for hour:" + period.getKey());
					
					OEEContainer.dbDelete(ret);
					OEEContainer.dbInsert(ret);
				}
			} else {
				logger.debug("The aggregation is already calculed to predefined period:" + period.getKey() 
						+ " Measured Entity:" + measuringEntity + "Entity Type:" + measuredEntityType.getName() );
			}

		} else {

			logger.error("The OEE Aggretation Container was not created");
		}
		
		return ret;
	}

	/**
	 * Performs the aggregation process from a list of OEE aggregations.
	 * @param measuringEntity: : measuring entity for which we are going to calculate the aggregation.
	 * @param measuredEntityType: type of measuring entity.
	 * @param period: Predefined period of the aggregation that is going to calculate.
	 * @param list: list of OEE aggregations forming the basis for the calculation process.
	 * @returnL an OEE aggregation represented the aggregated OEE.
	 */
	public List<OverallEquipmentEffectiveness> aggregateList(Integer measuringEntity,
			MeasuredEntityType measuredEntityType, PredefinedPeriod period,
			List<OverallEquipmentEffectiveness> list) {		

		List<OverallEquipmentEffectiveness> ret = new ArrayList<OverallEquipmentEffectiveness>();

		double productiveTime = 0;
		double qtySchedToProduce = 0;
		double qtyProduced = 0;
		double qtyDefective = 0;

		for (OverallEquipmentEffectiveness element : list) {
			productiveTime += element.getProductiveTime();
			qtyDefective += element.getQtyDefective();
			qtyProduced += element.getQtyProduced();
			qtySchedToProduce += element.getQtySchedToProduce();
		}

		OverallEquipmentEffectiveness oee=new OverallEquipmentEffectiveness(period,measuringEntity,measuredEntityType);
		oee.setProductiveTime(productiveTime);
		oee.setQtyDefective(qtyDefective);
		oee.setQtyProduced(qtyProduced);
		oee.setQtySchedToProduce(qtySchedToProduce);

		ret.add(oee);
		return ret;
	}

}
