package com.advicetec.measuredentity.test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.json.JSONArray;
import org.junit.Test;

import com.advicetec.applicationAdapter.ProductionOrder;
import com.advicetec.applicationAdapter.ProductionOrderManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeType;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.TimeInterval;
import com.advicetec.measuredentitity.ExecutedEntity;
import com.advicetec.measuredentitity.ExecutedEntityFacade;
import com.advicetec.measuredentitity.Machine;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.MeasuringState;
import com.advicetec.measuredentitity.StateInterval;

public class MeasuredEntityFacadeTest 
{

	@Test
	public void Test_State_Cache_Update() 
	{

		try
		{
			String productionRateId = "tasa_vel_esperada";
			String unit1PerCycles = "factor_conversion_kg_ciclo";
			String unit2PerCycles = "factor_conversion_mil_ciclo";
			String actualProductionCountId = "ProductionPulseTotal";

			String company = "compania0";
			String location = "sede0";
			String plant = "planta0";
			String reasonStr = "reason_0"; 
			String group = "grupo0";
			String machine = "maquina0";

			Integer entityId = new Integer(0);

			MeasuredEntityManager manager = MeasuredEntityManager.getInstance();
			ConfigurationManager confManager = ConfigurationManager.getInstance(); 



			Integer reasonId = new Integer(0);
			ReasonCode reason = new ReasonCode(reasonId, "test_reason");
			reason.setIdleDown(true);
			reason.setRootCause("Electricity");
			reason.setCannonicalCompany(company);
			reason.setCannonicalLocation(location);
			reason.setCannonicalPlant(plant);
			reason.setCannonicalReasonId(reasonStr);

			confManager.getReasonCodeContainer().insertReason(reason, company, location, plant, reasonStr);

			LocalDateTime start = LocalDateTime.now();
			Thread.sleep(5);
			LocalDateTime end = LocalDateTime.now();
			
			TimeInterval interval = new TimeInterval(start, end);

			Machine entity = new Machine(entityId);
			entity.setCannonicalCompany(company);
			entity.setCannonicalLocation(location);
			entity.setCannonicalPlant(plant);
			entity.setCannonicalGroup(group);
			entity.setCannonicalMachineId(machine);


			Attribute tasaVel = new Attribute("tasa_vel_esperada", AttributeType.DOUBLE);
			Attribute kgCycle = new Attribute("factor_conversion_kg_ciclo", AttributeType.DOUBLE);
			Attribute milCycle = new Attribute("factor_conversion_mil_ciclo", AttributeType.DOUBLE);
			Attribute cycles = new Attribute("ProductionPulseTotal", AttributeType.DOUBLE);

			entity.registerAttribute(tasaVel);
			entity.registerAttribute(kgCycle);
			entity.registerAttribute(milCycle);
			entity.registerAttribute(cycles);

			AttributeValue tasaVelVal = new AttributeValue("tasa_vel_esperada", tasaVel, new Double(80.5), entity.getId(), MeasuredEntityType.MACHINE );
			AttributeValue kgCycleVal = new AttributeValue("factor_conversion_kg_ciclo", kgCycle, new Double(0.5), entity.getId(), MeasuredEntityType.MACHINE );
			AttributeValue milCycleVal = new AttributeValue("factor_conversion_mil_ciclo", milCycle, new Double(0.3), entity.getId(), MeasuredEntityType.MACHINE );
			AttributeValue cyclesVal = new AttributeValue("ProductionPulseTotal", cycles, new Double(20.0), entity.getId(), MeasuredEntityType.MACHINE );

			entity.registerAttributeValue(tasaVelVal);
			entity.registerAttributeValue(kgCycleVal);
			entity.registerAttributeValue(milCycleVal);
			entity.registerAttributeValue(cyclesVal);

			manager.addNewEntity(entity);
			MeasuredEntityFacade facade = manager.getFacadeOfEntityById(entity.getId());

			System.out.println("before registering interval");
			
			facade.registerInterval(MeasuringState.UNSCHEDULEDOWN, null, interval);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

			String startDttmStr = start.format(formatter);
			
			boolean ret = facade.updateStateInterval(startDttmStr, reason);
			
			JSONArray states = facade.getJsonStates(LocalDateTime.now().minusDays(1),  LocalDateTime.now());

			System.out.println("Interval" + states.toString());
			
			Thread.sleep(2000);
			
			System.out.println("getting up after slepping" );
			
			Integer idProduction = ProductionOrderManager.getInstance().getProductionOrderId(company,location,
					plant,group,machine, 2017, 5, "prod01" );
			
			System.out.println("ProductionId:" + idProduction);
			
			ProductionOrder pOrder = (ProductionOrder) ProductionOrderManager.getInstance().getProductionOrderContainer().getObject(idProduction);
			
			if (pOrder == null){
				System.out.println("something went wrong with the production order");
			} else {
			
				ProductionOrderManager.getInstance().addProductionOrder(pOrder);
				
				ExecutedEntityFacade productionOrderFacade = ProductionOrderManager.getInstance().getFacadeOfPOrderById(idProduction);
				
				productionOrderFacade.start(entity.getId());
				
				facade.addExecutedObject((ExecutedEntity) productionOrderFacade.getEntity());
				
				states = facade.getJsonStates(LocalDateTime.now().minusDays(1),  LocalDateTime.now());
				
				System.out.println("Intervals :" + states.toString());
				
				Thread.sleep(2000);
				
				productionOrderFacade.stop(entity.getId());

				// Remove the production order from the measured entity.
		    	facade.removeExecutedObject(idProduction);
				
				ProductionOrderManager.getInstance().removeFacade(idProduction);
		    	
				System.out.println("in 1");
				ProductionOrderManager.getInstance().getProductionOrderContainer().removeObject(idProduction);
		    	
				System.out.println("in 2");
				
		    	states = facade.getJsonStates(LocalDateTime.now().minusDays(1),  LocalDateTime.now());
		    	
		    	System.out.println("in 3");
		    	System.out.println("Intervals :" + states.toString());
			
			}

		} catch(Exception e){
			System.out.println(e.getMessage());
		}

	}
	
	@Test
	public void Test_State_Cache_References_Delete() {
		
	}
	
}