package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.applicationAdapter.ProductionOrderContainer;
import com.advicetec.applicationAdapter.ProductionOrderManager;
import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.EntityFacade;
import com.advicetec.core.Processor;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.behavior.BehaviorInterpreterSw;
import com.advicetec.language.behavior.BehaviorSyntaxChecking;
import com.advicetec.measuredentitity.ExecutedEntity;
import com.advicetec.measuredentitity.ExecutedEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

/**
 * This class process Measure Entity events, it takes as parameter the measure entity event to be executed. 
 * then:
 * 
 * 	It instantiates the behavior parser and the behavior interpreter. 
 *  It then executes the behavior contained in the event and gets the global attributes resulting from that execution  
 *  With the global attributes, it pushes their values as new measured attribute values in the measured entity facade and updates
 *  the state of the measured attribute whenever it was changed.
 *  
 * @author Andres Marentes
 *
 */
public class MeasuredEntityEventProcessor implements Processor
{

	static Logger logger = LogManager.getLogger(MeasuredEntityEventProcessor.class.getName());
	
	/**
	 * Measured entity event 
	 */
	MeasuredEntityEvent event;
	
	/**
	 * Constructor for the class. 
	 * 
	 * It creates a measured entity processor that executes the measured entity event given as parameter
	 * 
	 * @param event meaured entity event to execute.
	 */
	public MeasuredEntityEventProcessor(MeasuredEntityEvent event) {
		super();
		this.event = event;
	}

	private List<DelayEvent> execute_behavior(String behaviorName, String program, 
												EntityFacade entityFacade, Integer measuringEntityId, 
													List<InterpretedSignal> listParams) throws Exception
	{
		
		logger.info("processing - behavior:" + behaviorName);
		
		ArrayList<DelayEvent> ret = new ArrayList<DelayEvent>();  
		
		BehaviorInterpreterSw interpreter = new BehaviorInterpreterSw();

		interpreter.process(program, entityFacade, measuringEntityId, listParams);

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
				
				if (entityFacade instanceof MeasuredEntityFacade){ 
					// We don't send parameters to the event. 
					MeasuredEntityEvent event = new MeasuredEntityEvent(behavior, this.event.getDevice(), 
															this.event.getPort(), ((MeasuredEntityFacade) entityFacade).getEntity().getId(), 
															new ArrayList<InterpretedSignal>());
					
					
					DelayEvent dEvent = new DelayEvent(event,duetime);
					ret.add(dEvent);
				}
			}
		}
		
		logger.info("ending - behavior:" + behaviorName);
		
		return ret;
		
	}
	
	/**
	 * This method takes the event parameter and process the behavior.
	 */
	public List<DelayEvent> process() throws SQLException 
	{
		
		Integer measuringEntity = this.event.getMeasuredEntity();
		String behaviorName = this.event.getBehaviorName();
		
        logger.debug("process - behavior:" + behaviorName);
		
		MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade entityFacade = entityManager.getFacadeOfEntityById(measuringEntity);

		ArrayList<DelayEvent> ret = new ArrayList<DelayEvent>();  

		if (entityFacade != null){
		
			String program = ((MeasuredEntity) entityFacade.getEntity()).getBehaviorText(behaviorName);
			
			if (program == null || program.isEmpty()){
				logger.error("the program for behavior name:" + behaviorName + " does not exist!! for measure entity:" + measuringEntity);
				return ret;
			}
			
			logger.debug("it is going to execute behavior:" + behaviorName);
			
			BehaviorSyntaxChecking sintaxChecking = new BehaviorSyntaxChecking();
			try 
			{
				// First, we verify the behavior.
				List<SyntaxError> errorList = sintaxChecking.process(program, measuringEntity);			
				// If no errors, then process.
				if (errorList.size() == 0)
				{ 
					
					List<InterpretedSignal> listParams = this.event.getParameters();
					execute_behavior(behaviorName, program, entityFacade, measuringEntity, listParams);
										
					ExecutedEntity executedEntity = entityFacade.getCurrentExecutedEntity();
					
					// If it is an executed entity being processed, then we execute the behavior for it
					if (executedEntity != null) {
						
						logger.info("it is going to execute the behavior for the executed entity");
						
						ProductionOrderManager prodOrderManager = ProductionOrderManager.getInstance();
						ExecutedEntityFacade pOrderFacade = prodOrderManager.getFacadeOfPOrderById(executedEntity.getId());
						execute_behavior(behaviorName, program, pOrderFacade, measuringEntity, listParams);
						
					} else {
						logger.info("No executed entity is being run");
					}
					
				} else {
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
	 * This function gets the behavior from a list of names given as parameter. 
	 * 
	 * We expect to have machinegroup.machine.behaviorid as the name 
	 * 
	 * @param names import like string containing a reference to a behavior
	 * 
	 * @return  the behavior which name as the list the names given by parameter.
	 */
	public String getBehavior(List<String> names)
	{
		// TODO: create the method.
		return null;
	}
	
}
