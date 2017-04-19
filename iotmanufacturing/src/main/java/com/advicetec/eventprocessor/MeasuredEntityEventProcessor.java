package com.advicetec.eventprocessor;

import com.advicetec.core.Processor;
import com.advicetec.language.behavior.InterpreterSw;

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
			interpreter.process(this.event.getBehaviorTransformation());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
