package com.advicetec.measuredentity.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.junit.Test;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.configuration.ReasonCodeContainer;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.OEEAggregationManager;
import com.advicetec.measuredentitity.OverallEquipmentEffectiveness;
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
		
		int day1 = 10; 
		int day2 = 11;
		int day3 = 12;
		int day4 = 13;
		int day5 = 14;
		int day6 = 15;
		
		int owner = 10;
		
		
		PredefinedPeriod predefinedPeriodYear = new PredefinedPeriod(year1);

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
		
		OverallEquipmentEffectiveness oeeyear = new OverallEquipmentEffectiveness(predefinedPeriodYear, owner, MeasuredEntityType.MACHINE);
		oeeyear.setAvailableTime(10000);
		oeeyear.setProductiveTime(9997);
		oeeyear.setQtySchedToProduce(3000);
		oeeyear.setQtyProduced(2000);
		oeeyear.setQtyDefective(1000);

		OverallEquipmentEffectiveness oee = new OverallEquipmentEffectiveness(predefinedPeriodYear, owner, MeasuredEntityType.MACHINE);
		oee.setAvailableTime(10000);
		oee.setProductiveTime(9997);
		oee.setQtySchedToProduce(3000);
		oee.setQtyProduced(2000);
		oee.setQtyDefective(1000);

		List<OverallEquipmentEffectiveness> 
		OEEAggregationManager OEEmanager = OEEAggregationManager.getInstance();
		
		OEEmanager.getOeeAggregationContainer().dbInsert(List<OverallEquipmentEffectiveness> list)
		
		
	}

}
