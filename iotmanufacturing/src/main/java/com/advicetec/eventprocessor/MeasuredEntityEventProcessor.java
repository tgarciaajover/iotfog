package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.Processor;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.behavior.BehaviorInterpreterSw;
import com.advicetec.language.behavior.BehaviorSyntaxChecking;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

public class MeasuredEntityEventProcessor implements Processor
{

	static Logger logger = LogManager.getLogger(MeasuredEntityEventProcessor.class.getName());
	MeasuredEntityEvent event;
	
	public MeasuredEntityEventProcessor(MeasuredEntityEvent event) {
		super();
		this.event = event;
	}

	public List<DelayEvent> process() throws SQLException 
	{
		
		Integer measuringEntity = this.event.getEntity();
		String behaviorName = this.event.getBehaviorTransformation();
		long milliseconds = this.event.getMilliseconds();
		boolean repeat = this.event.isRepeated();
		
        logger.debug("process - behavior:" + behaviorName);
		
		MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade entityFacade = entityManager.getFacadeOfEntityById(measuringEntity);

		ArrayList<DelayEvent> ret = new ArrayList<DelayEvent>();  

		if (entityFacade != null){
		
			String program = entityFacade.getEntity().getBehaviorText(behaviorName);

			logger.debug("program:" + program);
			
			BehaviorSyntaxChecking sintaxChecking = new BehaviorSyntaxChecking();
			try 
			{
				// First, we verify the behavior.
				List<SyntaxError> errorList = sintaxChecking.process(program);			
				// If no errors, then process.
				if (errorList.size() == 0){ 
					List<InterpretedSignal> listParams = this.event.getParameters();
					BehaviorInterpreterSw interpreter = new BehaviorInterpreterSw();

					interpreter.process(program, measuringEntity, listParams);

					// Store the new attributes and their values.  
					entityFacade.importSymbols(interpreter.getGlobalScope().getSymbolMap(), AttributeOrigin.BEHAVIOR);
					entityFacade.importAttributeValues(interpreter.getGlobalSpace().getSymbolMap());
					entityFacade.setCurrentState(interpreter.getGlobalSpace().getSymbolMap());

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
					
					if (repeat){
						DelayEvent dEvent = new DelayEvent(this.event,milliseconds);
						ret.add(dEvent);
					}
					
					entityFacade.getStatus();
				}
				else {
					logger.debug("Number of Errors :" + String.valueOf(errorList.size()));
				}
			} catch (Exception e) {
				logger.debug("Error message:" + e.getMessage());
				e.printStackTrace();
			}

		} else {
			logger.error("Facade not found" + measuringEntity);
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
