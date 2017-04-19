package com.advicetec.MessageProcessor;

import java.util.List;

import com.advicetec.core.Processor;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.transformation.InterpreterSw;
import com.advicetec.language.transformation.SyntaxChecking;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.persistence.StatusStore;


public class SampleProcessor implements Processor 
{

	private SampleMessage sample;
	private InterpreterSw interpreter;
	
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
			List<SyntaxError> errorList = sintaxChecking.process(program);
			
			// If no errors, then process.
			if (errorList.size() == 0){ 
				// Then, we read parameters from message and pass them to the interpreter as global variables.
				List<InterpretedSignal> list = sample.getValues();
				interpreter = new InterpreterSw();
				interpreter.process(program,list);
				// stores the status of attributes
				entityFacade.importSymbols(interpreter.getGlobalScope().getSymbolMap());
				entityFacade.importAttributeValues(interpreter.getGlobalSpace().getSymbolMap());
			}
		
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		

	}
	
	
}
