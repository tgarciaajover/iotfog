package com.advicetec.iot.rest;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.DeviceType;
import com.advicetec.configuration.DeviceTypeContainer;
import com.advicetec.configuration.IOSignalDeviceType;
import com.advicetec.configuration.Signal;
import com.advicetec.configuration.SignalType;
import com.advicetec.configuration.SignalUnit;
import com.advicetec.configuration.SignalUnitContainer;
import com.advicetec.measuredentitity.Machine;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuredEntityType;

public class MeasuredDeviceTest 
{
	@Test
	public void Test_Measured_Device() 
	{

		Machine machine1 = new Machine(1);
		
		machine1.setCreateDate(LocalDateTime.now());
		machine1.putBehavior(1, "ProductionCOT","asdjasl","akjsdalksdl");
		machine1.putBehavior(1, "ProductionCOT2","sadasdas","dfksdlfksj");
		machine1.setDescr("Termoformadora");
		
		String json = machine1.toJson();
		
		System.out.println("json:" + json);
		//assertEquals("Import from Json does not work,",deviceType.toJson(), deviceType2.toJson() );
		try{ 
				MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
				
				MeasuredEntity measuredEntity = measuredEntityManager.getMeasuredEntityContainer().fromJSON(json);
				
				MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(measuredEntity.getId());
				
				if (measuredEntityFacade == null){
					measuredEntityManager.addNewEntity(measuredEntity);
				}
				
				measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(measuredEntity.getId());
				
				assertEquals("Import from Json does not work,",((MeasuredEntity) measuredEntityFacade.getEntity()).toJson(), machine1.toJson() );
				
				String behaviorText = ((MeasuredEntity) measuredEntityFacade.getEntity()).getBehaviorText("ProductionCOT");
				
				assertEquals("behavior texts are not equal,",behaviorText,"akjsdalksdl" );
		} catch (Exception e)
		{
			System.err.println(e.getMessage());
		}
	}
}
