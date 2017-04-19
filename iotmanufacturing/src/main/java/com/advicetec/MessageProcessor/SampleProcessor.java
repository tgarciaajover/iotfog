package com.advicetec.MessageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.advicetec.core.Processor;
import com.advicetec.eventprocessor.MeasuredEntityEvent;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.transformation.InterpreterSw;
import com.advicetec.language.transformation.SyntaxChecking;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.persistence.StatusStore;
import com.advicetec.language.ast.Symbol;


public class SampleProcessor implements Processor 
{

	private SampleMessage sample;
	private InterpreterSw interpreter;
	
	public SampleProcessor(SampleMessage sample) {
		super();
		this.sample = sample;
	}


	@Override
	public List<DelayEvent> process() 
	{
		
		// Finds the measuring Entity involved. The string value is always not null
		String measuringEntity = sample.getmEntity();
		String program = sample.getMeaningTransformation();
		
		MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade entityFacade = entityManager.getFacadeOfEntityById(measuringEntity);
		
		ArrayList<DelayEvent> ret = new ArrayList<DelayEvent>();  
		SyntaxChecking sintaxChecking = new SyntaxChecking();
		try 
		{
			// First, we verify the transformation.
			List<SyntaxError> errorList = sintaxChecking.process(program);
			
			// If no errors, then process.
			if (errorList.size() == 0){ 
				// Then, we read parameters from message and pass them to the interpreter as global variables.
				List<InterpretedSignal> list = sample.getValues();
				InterpreterSw interpreter = new InterpreterSw();
				interpreter.process(program,measuringEntity,list);
				// stores the status of attributes
				entityFacade.importSymbols(interpreter.getGlobalScope().getSymbolMap());
				entityFacade.importAttributeValues(interpreter.getGlobalSpace().getSymbolMap());
				
				Map<String, Symbol> symbols =  interpreter.getGlobalScope().getSymbolMap();
				
				for (String symbolId : symbols.keySet())
				{
					Symbol symbol = symbols.get(symbolId);
					
					if (symbol instanceof TimerSymbol)
					{
						long duetime = ((TimerSymbol) symbol).getMilliseconds();
						String behavior = getBehavior(((TimerSymbol) symbol).getCompleteName());
						
						// We don't send parameters to the event. 
						MeasuredEntityEvent event = new MeasuredEntityEvent(behavior, measuringEntity, new ArrayList<InterpretedSignal>());
						DelayEvent dEvent = new DelayEvent(event,duetime);
						ret.add(dEvent);
					}
				}
				
			}
			else {
				// TODO: put in the log all the traced errors.
			}
						
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		

		return ret;
	}
	
	/**
	 * This function get the behavior from list of names given as parameter. 
	 * We expect to have machinegroup.machine.behaviorid as the name 
	 * @param names
	 * @return
	 */
	
	public String getBehavior(List<String> names)
	{
		// TODO: create the method.
		return null;
	}
	
	
}
