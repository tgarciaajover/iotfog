package com.advicetec.eventprocessor;

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
		
		

	}

	
}
