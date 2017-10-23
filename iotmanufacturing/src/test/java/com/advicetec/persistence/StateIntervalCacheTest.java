package com.advicetec.persistence;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

import com.advicetec.configuration.ReasonCode;
import com.advicetec.measuredentitity.MeasuredEntityType;

public class StateIntervalCacheTest {
	
	@Test
	public void updateStateInterval() {
		
		StateIntervalCache cache = StateIntervalCache.getInstance();
		Integer entity = new Integer(86);

		String startDttmStr = "2017-08-12 18:50:09.292";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		LocalDateTime startDttm = LocalDateTime.parse(startDttmStr, formatter);
		
		ReasonCode reasonCode = new ReasonCode(1, "energy interruption");
		reasonCode.setClassification("Electricity");
		reasonCode.setCreateDate(LocalDateTime.now());
		reasonCode.setIdleDown(true);
		reasonCode.setGroup("NotInOurHands");


		
		cache.updateMeasuredEntityStateInterval(entity, MeasuredEntityType.MACHINE, startDttm,reasonCode );
		
	}
	

}
