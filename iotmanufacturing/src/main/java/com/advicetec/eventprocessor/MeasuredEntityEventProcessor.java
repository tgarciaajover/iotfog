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

/**
 * 
 * 
 * @author andres
 *
 */
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
		
		Integer measuringEntity = this.event.getMeasuredEntity();
		String behaviorName = this.event.getBehaviorTransformation();
		
        logger.debug("process - behavior:" + behaviorName);
		
		MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade entityFacade = entityManager.getFacadeOfEntityById(measuringEntity);

		ArrayList<DelayEvent> ret = new ArrayList<DelayEvent>();  

		if (entityFacade != null){
		
			String program = entityFacade.getEntity().getBehaviorText(behaviorName);
			
			if (program == null || program.isEmpty()){
				logger.error("the program for behavior name:" + behaviorName + " does not exist!! for measure entity:" + measuringEntity);
				return ret;
			}
			
			logger.debug("it is going to execute behavior:" + behaviorName);
			
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
					if (interpreter.getGlobalScope() == null){
						logger.error("The behavior intepreter fails !!! for behavior:" + behaviorName);
						return ret;
					}
					
					logger.debug("Finish executing behavior" + behaviorName);
					
					entityFacade.importSymbols(interpreter.getGlobalScope().getSymbolMap(), AttributeOrigin.BEHAVIOR);
					
					// Import symbols' values and state
					entityFacade.importAttributeValues(interpreter.getGlobalAttributes());
					entityFacade.setCurrentState(interpreter.getState());

					Map<String, Symbol> symbols =  interpreter.getGlobalScope().getSymbolMap();

					for (String symbolId : symbols.keySet())
					{
						Symbol symbol = symbols.get(symbolId);

						if (symbol instanceof TimerSymbol)
						{
							long duetime = ((TimerSymbol) symbol).getMilliseconds();
							String behavior = getBehavior(((TimerSymbol) symbol).getCompleteName());

							// We don't send parameters to the event. 
							MeasuredEntityEvent event = new MeasuredEntityEvent(behavior, this.event.getDevice(), this.event.getPort(), measuringEntity, new ArrayList<InterpretedSignal>());
							DelayEvent dEvent = new DelayEvent(event,duetime);
							ret.add(dEvent);
						}
					}
										
					entityFacade.getStatus();
				}
				else {
					logger.error("behavior" + behaviorName+ " has errors #(errors):" + String.valueOf(errorList.size()));
				}
			} catch (Exception e) {
				logger.error("Error message:" + e.getMessage());
				e.printStackTrace();
			}
			

		} else {
			logger.error("Facade not found" + measuringEntity);
		}
		
		logger.debug("end process - behavior:" + behaviorName);

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
