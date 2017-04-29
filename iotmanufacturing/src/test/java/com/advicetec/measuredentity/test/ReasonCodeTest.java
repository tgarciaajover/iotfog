package com.advicetec.measuredentity.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.configuration.ReasonCodeContainer;
import com.advicetec.configuration.Signal;
import com.advicetec.configuration.SignalContainer;
import com.advicetec.configuration.SignalType;
import com.advicetec.configuration.SignalUnit;
import com.advicetec.configuration.SignalUnitContainer;

public class ReasonCodeTest 
{
	@Test
	public void Test_Reason_Code() 
	{
		ReasonCode reasonCode = new ReasonCode(1, "energy interruption");
		reasonCode.setClassification("Electricity");
		reasonCode.setCreateDate(LocalDateTime.now());
		reasonCode.setIdleDown(true);
		reasonCode.setGroup("NotInOurHands");
						
		
		String jsonString = reasonCode.toJson();
		System.out.println("reasonCode" + jsonString);

		
		ConfigurationManager instance = ConfigurationManager.getInstance();
		
		ReasonCodeContainer container = instance.getReasonCodeContainer(); 
		
		container.fromJSON(jsonString);
		ReasonCode reasonCode2 = (ReasonCode) container.getObject(1);
		
		System.out.println(reasonCode2.toJson());
		assertEquals("Import from Json does not work,",reasonCode.toJson(), reasonCode2.toJson() );

	}
}
