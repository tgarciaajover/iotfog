package com.advicetec.measuredentity.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.advicetec.aggregation.oee.OEEAggregationManager;
import com.advicetec.aggregation.oee.OverallEquipmentEffectiveness;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.utils.PredefinedPeriod;

public class OverallEquipmentEffectivenessTest {

	@Test
	public void Test_OEE() 
	{

		int year1 = 2016;
		int year2 = 2017;

		int month1 = 10;
		int month2 = 11;
		int month3 = 12;
		int month4 = 1;

		int day1 = 9; 
		int day2 = 8;
		int day3 = 12;
		int day4 = 13;
		int day5 = 14;
		int day6 = 15;

		int owner = 10;

		PredefinedPeriod predefinedPeriodYear1 = new PredefinedPeriod(year1);
		PredefinedPeriod predefinedPeriodYear2 = new PredefinedPeriod(year2);

		PredefinedPeriod predefinedPeriodMonth1 = new PredefinedPeriod(year1, month1);
		PredefinedPeriod predefinedPeriodMonth2 = new PredefinedPeriod(year1, month2);
		PredefinedPeriod predefinedPeriodMonth3 = new PredefinedPeriod(year1, month3);

		PredefinedPeriod predefinedPeriodDay1 = new PredefinedPeriod(year2, month4, day1);
		PredefinedPeriod predefinedPeriodDay2 = new PredefinedPeriod(year2, month4, day2);
		PredefinedPeriod predefinedPeriodDay3 = new PredefinedPeriod(year2, month4, day3);
		PredefinedPeriod predefinedPeriodDay4 = new PredefinedPeriod(year2, month4, day4);
		PredefinedPeriod predefinedPeriodDay5 = new PredefinedPeriod(year2, month4, day5);
		PredefinedPeriod predefinedPeriodDay6 = new PredefinedPeriod(year2, month4, day6);

		Integer parent; 
		MeasuredEntityType parentType;

		OverallEquipmentEffectiveness oeeyear = new OverallEquipmentEffectiveness(predefinedPeriodYear1, owner, MeasuredEntityType.MACHINE);
		oeeyear.setProductiveTime(9997);
		oeeyear.setQtySchedToProduce(3000);
		oeeyear.setQtyProduced(2000);
		oeeyear.setQtyDefective(1000);

		OverallEquipmentEffectiveness oee = new OverallEquipmentEffectiveness(predefinedPeriodYear2, owner, MeasuredEntityType.MACHINE);
		oee.setProductiveTime(30000);
		oee.setQtySchedToProduce(36000);
		oee.setQtyProduced(30000);
		oee.setQtyDefective(6000);

		List<OverallEquipmentEffectiveness> yearList = new ArrayList<OverallEquipmentEffectiveness>();
		yearList.add(oeeyear);
		yearList.add(oee);

		OEEAggregationManager OEEmanager = 	OEEAggregationManager.getInstance();
		//OEEmanager.getOeeAggregationContainer().dbInsert(yearList);
		OEEmanager.getOeeAggregationContainer().dbDelete(yearList);


		// month
		OverallEquipmentEffectiveness oeemonth1 = new OverallEquipmentEffectiveness(predefinedPeriodMonth1, owner, MeasuredEntityType.MACHINE);
		oeemonth1.setProductiveTime(9997);
		oeemonth1.setQtySchedToProduce(3000);
		oeemonth1.setQtyProduced(2000);
		oeemonth1.setQtyDefective(1000);


		OverallEquipmentEffectiveness oeemonth2 = new OverallEquipmentEffectiveness(predefinedPeriodMonth2, owner, MeasuredEntityType.MACHINE);
		oeemonth2.setProductiveTime(9997);
		oeemonth2.setQtySchedToProduce(3000);
		oeemonth2.setQtyProduced(2000);
		oeemonth2.setQtyDefective(1000);

		List<OverallEquipmentEffectiveness> monthList = new ArrayList<OverallEquipmentEffectiveness>();
		monthList.add(oeemonth1);
		monthList.add(oeemonth2);

		OEEmanager.getOeeAggregationContainer().dbInsert(monthList);

		// day
		OverallEquipmentEffectiveness oeeDay1 = new OverallEquipmentEffectiveness(predefinedPeriodDay1, owner, MeasuredEntityType.MACHINE);
		oeeDay1.setProductiveTime(9997);
		oeeDay1.setQtySchedToProduce(3000);
		oeeDay1.setQtyProduced(2000);
		oeeDay1.setQtyDefective(1000);


		OverallEquipmentEffectiveness oeeDay2 = new OverallEquipmentEffectiveness(predefinedPeriodDay2, owner, MeasuredEntityType.MACHINE);
		oeeDay2.setProductiveTime(9997);
		oeeDay2.setQtySchedToProduce(3000);
		oeeDay2.setQtyProduced(2000);
		oeeDay2.setQtyDefective(1000);

		List<OverallEquipmentEffectiveness> dayList = new ArrayList<OverallEquipmentEffectiveness>();
		dayList.add(oeeDay1);
		dayList.add(oeeDay2);

		OEEmanager.getOeeAggregationContainer().dbInsert(dayList);
	}


	@Test
	public void Test_OEE_hour() 
	{

		int year1 = 2016;
		int year2 = 2017;

		int month1 = 2;
		int month2 = 11;
		int month3 = 3;
		int month4 = 1;

		int day1 = 6; 
		int day2 = 3;
		int day3 = 12;
		
		int hour1 = 8;
		int hour2 = 16;
		
		int owner = 10;
		
		PredefinedPeriod predefinedPeriodHour1 = new PredefinedPeriod(year1, month2, day1, hour1);
		PredefinedPeriod predefinedPeriodHour2 = new PredefinedPeriod(year2, month3, day2, hour2);
		
		OverallEquipmentEffectiveness oeeHour1 = new OverallEquipmentEffectiveness(predefinedPeriodHour1, owner, MeasuredEntityType.MACHINE);
		oeeHour1.setProductiveTime(9997);
		oeeHour1.setQtySchedToProduce(3000);
		oeeHour1.setQtyProduced(2000);
		oeeHour1.setQtyDefective(1000);

		OverallEquipmentEffectiveness oeehour2 = new OverallEquipmentEffectiveness(predefinedPeriodHour2, owner, MeasuredEntityType.MACHINE);
		oeehour2.setProductiveTime(30000);
		oeehour2.setQtySchedToProduce(36000);
		oeehour2.setQtyProduced(30000);
		oeehour2.setQtyDefective(6000);

		List<OverallEquipmentEffectiveness> hourList = new ArrayList<OverallEquipmentEffectiveness>();
		hourList.add(oeeHour1);
		hourList.add(oeehour2);

		OEEAggregationManager OEEmanager = 	OEEAggregationManager.getInstance();
		OEEmanager.getOeeAggregationContainer().dbInsert(hourList);
		//OEEmanager.getOeeAggregationContainer().dbDelete(hourList);
	}
}
