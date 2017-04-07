package com.advicetec.MessageProcessor;

import java.util.List;

import com.advicetec.core.Processor;
import com.advicetec.language.transformation.InterpreterSw;
import com.advicetec.language.transformation.SyntaxChecking;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;


public class SampleProcessor implements Processor 
{

	SampleMessage sample;
	
	
	public SampleProcessor(SampleMessage sample) {
		super();
		this.sample = sample;
	}


	@Override
	public void process() 
	{
		
		// Finds the measuring Entity involved. The string value is always not null
		String measuringEntity = sample.getmEntity();
		String program = sample.getMeaningTransformation();
		
		MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade entityFacade = entityManager.getFacadeOfEntityById(measuringEntity);
		
		
		SyntaxChecking sintaxChecking = new SyntaxChecking();
		try 
		{
			// First, we verify the transformation.
			sintaxChecking.process(program);
			
			// Then, we read parameters from message and pass them to the interpreter as global variables.
			List<InterpretedSignal> list = sample.getValues();
			InterpreterSw interpreter = new InterpreterSw();
			interpreter.process(program,list);
		
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		

	}

}
