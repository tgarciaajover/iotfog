package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.core.Processor;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.OEEAggregationContainer;
import com.advicetec.measuredentitity.OEEAggregationManager;
import com.advicetec.measuredentitity.OverallEquipmentEffectiveness;
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

		ArrayList<DelayEvent> ret = new ArrayList<DelayEvent>();  

		if (entityFacade != null){

			// Obtains the previous hour
			LocalDateTime current = LocalDateTime.now();
			LocalDateTime formerHour = PeriodUtils.getPreviousFinalHour(current);
			calculateHour(measuringEntity, measuredEntityType, formerHour);


			// Obtains the previous day
			LocalDateTime formerDay = PeriodUtils.getPreviousFinalHour( current);
			calculateDay(measuringEntity, measuredEntityType,formerDay);

			// Obtains the previous month
			LocalDateTime formerMonth = PeriodUtils.getPreviousFinalMonth(current);
			calculateMonth(measuringEntity, measuredEntityType,formerMonth);

			// Obtains the previous year
			LocalDateTime formerYear = PeriodUtils.getPreviousFinalYear(current);
			calculateYear(measuringEntity, measuredEntityType,formerYear);


		} else {
			logger.error("Facade not found" + measuringEntity);
		}

		return ret;

	}

	private List<OverallEquipmentEffectiveness> calculateYear(Integer measuringEntity, MeasuredEntityType measuredEntityType, LocalDateTime formerYear) {

		// if the former year is already store, then does nothing, else calculate

		OEEAggregationManager manager = OEEAggregationManager.getInstance();
		OEEAggregationContainer OEEContainer = manager.getOeeAggregationContainer();
		List<OverallEquipmentEffectiveness> ret = new ArrayList<OverallEquipmentEffectiveness>();

		PredefinedPeriod yearPeriod = new PredefinedPeriod(formerYear.getYear());
		if (OEEContainer != null){
			if (!OEEContainer.existPeriodOEE(measuringEntity, measuredEntityType, yearPeriod)){
				List<OverallEquipmentEffectiveness> list = 
						OEEContainer.getOEEList(measuringEntity, measuredEntityType, yearPeriod);
				if(list.size() != 12){

					for (int i = 1; i <= 12; i++) {
						PredefinedPeriod per = new PredefinedPeriod(formerYear.getYear(), i);
						if (!existsOEEinList( list, per)){
							LocalDateTime localdatetime = LocalDateTime.of(formerYear.getYear(), i, 1, 0, 0);
							List<OverallEquipmentEffectiveness> oeeMonth = 
									calculateMonth(measuringEntity, measuredEntityType, localdatetime);
							list.addAll(oeeMonth);
						}
					}	
				}

				ret = aggregateList(measuringEntity, measuredEntityType, yearPeriod,  list);
				OEEContainer.dbInsert(ret);
			}
		} else {

			logger.error("The OEE Aggretation Container was not created");
		}
		return ret;
	}

	private boolean existsOEEinList(List<OverallEquipmentEffectiveness> list, PredefinedPeriod predefinedPeriod){
		for(OverallEquipmentEffectiveness month : list){
			if(month.getPredefinedPeriod().equals(predefinedPeriod)){
				return true;
			}
		}
		return false;
	}


	private List<OverallEquipmentEffectiveness> calculateMonth(
			Integer measuringEntity, MeasuredEntityType measuredEntityType, 
			LocalDateTime formerMonth) {

		OEEAggregationManager manager = OEEAggregationManager.getInstance();
		OEEAggregationContainer OEEContainer = manager.getOeeAggregationContainer();
		List<OverallEquipmentEffectiveness> ret = new ArrayList<OverallEquipmentEffectiveness>();

		PredefinedPeriod monthPeriod = new PredefinedPeriod(formerMonth.getYear(), formerMonth.getMonthValue());
		if (OEEContainer != null){
			if (!OEEContainer.existPeriodOEE(measuringEntity, measuredEntityType, monthPeriod)){
				List<OverallEquipmentEffectiveness> list = 
						OEEContainer.getOEEList(measuringEntity, measuredEntityType, monthPeriod);

				YearMonth yearMonthObject = YearMonth.of(formerMonth.getYear(), formerMonth.getMonthValue());
				int daysInMonth = yearMonthObject.lengthOfMonth();
				if(list.size() != daysInMonth){
					for (int i = 1; i <= daysInMonth; i++) {
						PredefinedPeriod per = new PredefinedPeriod(formerMonth.getYear(), formerMonth.getMonthValue(), i);
						if (!existsOEEinList( list, per)){
							LocalDateTime localdatetime = LocalDateTime.of(formerMonth.getYear(), formerMonth.getMonthValue(), i, 0, 0);
							List<OverallEquipmentEffectiveness> oeeDay = calculateDay(measuringEntity, measuredEntityType, localdatetime);
							list.addAll(oeeDay);
						}
					}
					
				}

				ret = aggregateList(measuringEntity, measuredEntityType, monthPeriod,  list);
				OEEContainer.dbInsert(ret);

			} else {

				logger.error("The OEE Aggretation Container was not created");
			}

		}
		return ret;
	}

	private List<OverallEquipmentEffectiveness> calculateDay(Integer measuringEntity, 
			MeasuredEntityType measuredEntityType, LocalDateTime formerDay) {

		OEEAggregationManager manager = OEEAggregationManager.getInstance();
		OEEAggregationContainer OEEContainer = manager.getOeeAggregationContainer();
		PredefinedPeriod period = new PredefinedPeriod(formerDay.getYear(), 
				formerDay.getMonthValue(), formerDay.getDayOfMonth() );
		List<OverallEquipmentEffectiveness> ret = new ArrayList<OverallEquipmentEffectiveness>();

		if (OEEContainer != null){
			if (!OEEContainer.existPeriodOEE(measuringEntity, measuredEntityType,period)){
				List<OverallEquipmentEffectiveness> list = OEEContainer.getOEEList(
						measuringEntity, measuredEntityType, period);

				if(list.size() != 24){
					for (int i = 0; i < 24; i++) {
						PredefinedPeriod per = new PredefinedPeriod(formerDay.getYear(), 
								formerDay.getMonthValue(), formerDay.getDayOfMonth(), i);

						if (!existsOEEinList( list, per)){
							String parQueryFrom =  per.getKey()+":00:00.001";
							String parQueryTo =  per.getKey()+":59:59.999";
							List<OverallEquipmentEffectiveness> hours = OEEContainer.intervalsByHour(
									measuringEntity,measuredEntityType, per.getKey(), parQueryFrom,parQueryTo);
							OEEContainer.dbInsert(hours);
							list.addAll(hours);
						}
					}	
				}
				ret = aggregateList(measuringEntity, measuredEntityType, period,  list);
				OEEContainer.dbInsert(ret);
			}
		} else {

			logger.error("The OEE Aggretation Container was not created");
		}
		return ret;
	}


	private void calculateHour(Integer measuringEntity, MeasuredEntityType measuredEntityType, LocalDateTime formerHour) {

		OEEAggregationManager manager = OEEAggregationManager.getInstance();
		OEEAggregationContainer OEEContainer = manager.getOeeAggregationContainer();
		PredefinedPeriod period = new PredefinedPeriod(formerHour.getYear(), formerHour.getMonthValue(), 
				formerHour.getDayOfMonth(), formerHour.getHour());
		
		if (OEEContainer != null){
			if (!OEEContainer.existPeriodOEE(measuringEntity, measuredEntityType, period)){

				String parQueryFrom =  period.getKey()+":00:00.001";
				String parQueryTo =  period.getKey()+":59:59.999";
				List<OverallEquipmentEffectiveness> list = OEEContainer.intervalsByHour(
						measuringEntity,measuredEntityType, period.getKey(), parQueryFrom,parQueryTo);
				OEEContainer.dbInsert(list);
			}
		} else {

			logger.error("The OEE Aggretation Container was not created");
		}
	}


	private List<OverallEquipmentEffectiveness> aggregateList(Integer measuringEntity,
			MeasuredEntityType measuredEntityType, PredefinedPeriod period,
			List<OverallEquipmentEffectiveness> list) {		

		List<OverallEquipmentEffectiveness> ret = new ArrayList<OverallEquipmentEffectiveness>();

		double productiveTime = 0;
		double qtySchedToProduce = 0;
		double qtyProduced = 0;
		double qtyDefective = 0;
		double availableTime = 0;

		for (OverallEquipmentEffectiveness element : list) {
			availableTime += element.getAvailableTime();
			productiveTime += element.getProductiveTime();
			qtyDefective += element.getQtyDefective();
			qtyProduced += element.getQtyProduced();
			qtySchedToProduce += element.getQtySchedToProduce();
		}

		OverallEquipmentEffectiveness oee=new OverallEquipmentEffectiveness(period,measuringEntity,measuredEntityType);
		oee.setAvailableTime(availableTime);
		oee.setProductiveTime(productiveTime);
		oee.setQtyDefective(qtyDefective);
		oee.setQtyProduced(qtyProduced);
		oee.setQtySchedToProduce(qtySchedToProduce);

		ret.add(oee);
		return ret;
	}
}
