package com.advicetec.eventprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.core.Processor;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.behavior.InterpreterSw;
import com.advicetec.language.behavior.SyntaxChecking;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

public class MeasuredEntityEventProcessor implements Processor
{

	MeasuredEntityEvent event;
	
	public MeasuredEntityEventProcessor(MeasuredEntityEvent event) {
		super();
		this.event = event;
	}

	public List<DelayEvent> process() 
	{
		
		String measuringEntity = this.event.getEntity();
		String behaviorName = this.event.getBehaviorTransformation();
		

		MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade entityFacade = entityManager.getFacadeOfEntityById(measuringEntity);

		ArrayList<DelayEvent> ret = new ArrayList<DelayEvent>();  

		if (entityFacade != null){
		
			String program = entityFacade.getEntity().getBehaviorText(behaviorName);

			SyntaxChecking sintaxChecking = new SyntaxChecking();
			try 
			{
				// First, we verify the behavior.
				List<SyntaxError> errorList = sintaxChecking.process(program);			
				// If no errors, then process.
				if (errorList.size() == 0){ 
					List<InterpretedSignal> listParams = this.event.getParameters();
					InterpreterSw interpreter = new InterpreterSw();

					interpreter.process(program, measuringEntity, listParams);

					// Store the new attributes and their values.  
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
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			// TODO: put the log error saying that there is not facade.
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
