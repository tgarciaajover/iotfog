package com.advicetec.eventprocessor;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.aggregation.oee.OverallEquipmentEffectiveness;
import com.advicetec.aggregation.sched.SchedAggregationCalculator;
import com.advicetec.configuration.AggregateMethod;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.DisplayDevice;
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
 * This class process Measure Entity events, it takes as parameter the measure entity event to be executed. 
 * then:
 * 
 * 	It instantiates the behavior parser and the behavior interpreter. 
 *  It then executes the behavior contained in the event and gets the global attributes resulting from that execution  
 *  With the global attributes, it pushes their values as new measured attribute values in the measured entity facade and updates
 *  the state of the measured attribute whenever it was changed.
 *  
 * @author Jose Pulgarin
 *
 */
public class SchedAggregateEntityEventProcessor implements Processor
{

	static Logger logger = LogManager.getLogger(SchedAggregateEntityEventProcessor.class.getName());
	
	/**
	 * Measured entity event 
	 */
	SchedAggregateEntityEvent event;
	
	/**
	 * Constructor for the class. 
	 * 
	 * It creates a measured entity processor that executes the measured entity event given as parameter
	 * 
	 * @param event SchedAggregate entity event to execute.
	 */
	public SchedAggregateEntityEventProcessor(SchedAggregateEntityEvent event) {
		super();
		this.event = event;
	}

	/**
	 * This method takes the event parameter and process the behavior.
	 */
	public List<DelayEvent> process() throws SQLException 
	{
		
		Integer measuringEntity = this.event.getMeasuredEntity();
		String aggregateMethod = this.event.getAggregateMethod();
		
        logger.debug("process - aggregateMethod:" + aggregateMethod);
		
		MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade entityFacade = entityManager.getFacadeOfEntityById(measuringEntity);

		ArrayList<DelayEvent> ret = new ArrayList<DelayEvent>();  

		if (entityFacade != null){
			
	        ConfigurationManager confManager = ConfigurationManager.getInstance();
	        AggregateMethod aggregateMethodIns = confManager.getAggregateMethods().getAggregateMethod(aggregateMethod);
			
	        if(aggregateMethodIns == null){
		        logger.debug("Aggregate Method ["+aggregateMethod+"] don't exists.");
	        }else{
		        logger.debug("SchedAggregate :" + aggregateMethod+" : ["+aggregateMethodIns.getMethod()+"]["+aggregateMethodIns.getAttribute()+"]["+aggregateMethodIns.getType()+"]");
		        
				SchedAggregationCalculator schedAggrCalc = new SchedAggregationCalculator();
				schedAggrCalc.calculateAggregateMethod(measuringEntity,aggregateMethodIns);
	        }
			
		    /*
			String program = entityFacade.getEntity().getBehaviorText(behaviorName);
			
			if (program == null || program.isEmpty()){
				logger.error("the program for behavior name:" + behaviorName + " does not exist!! for measure entity:" + measuringEntity);
				return ret;
			}
			
			logger.debug("it is going to execute behavior:" + behaviorName);
			*/
			/*
			BehaviorSyntaxChecking sintaxChecking = new BehaviorSyntaxChecking();
			try 
			{
				// First, we verify the behavior.
				List<SyntaxError> errorList = sintaxChecking.process(program, measuringEntity);			
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
			*/

		} else {
			logger.error("Facade not found" + measuringEntity);
		}
		
		logger.debug("end process - aggregateMethod:" + aggregateMethod);

		return new ArrayList<DelayEvent>();

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
