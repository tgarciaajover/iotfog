package com.advicetec.eventprocessor;

import com.advicetec.core.Processor;
import com.advicetec.language.behavior.InterpreterSw;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

public class MeasuredEntityEventProcessor implements Processor
{

	MeasuredEntityEvent event;
	
	
	public MeasuredEntityEventProcessor(MeasuredEntityEvent event) {
		super();
		this.event = event;
	}

	public void process() 
	{
		InterpreterSw interpreter = new InterpreterSw();
		try 
		{
			
			MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
			MeasuredEntityFacade entityFacade = entityManager.getFacadeOfEntityById(this.event.getEntity().getId());

			interpreter.process(this.event.getBehaviorTransformation(), this.event.getEntity().getId(), this.event.getParameters());

			// Store the new attributes and their values.  
			entityFacade.importSymbols(interpreter.getGlobalScope().getSymbolMap());
			entityFacade.importAttributeValues(interpreter.getGlobalSpace().getSymbolMap());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
