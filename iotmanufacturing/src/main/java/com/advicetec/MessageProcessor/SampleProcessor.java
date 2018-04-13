package com.advicetec.MessageProcessor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.Processor;
import com.advicetec.eventprocessor.DisplayEvent;
import com.advicetec.eventprocessor.MeasuredEntityEvent;
import com.advicetec.eventprocessor.SchedAggregateEntityEvent;
import com.advicetec.language.ast.ArraySymbol;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.BehaviorSymbol;
import com.advicetec.language.ast.DisplaySymbol;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.ImportSymbol;
import com.advicetec.language.ast.ScopedSymbol;
import com.advicetec.language.ast.StateSymbol;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.ast.AggregateSymbol;
import com.advicetec.language.ast.TransformationSymbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.language.transformation.InterpreterSw;
import com.advicetec.language.transformation.SyntaxChecking;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

/**
 * This class implements the <code>Processor</code> interface, and its method 
 * <code>process</code> that takes care of the sample which comes from a measured
 * entity.
 * 
 * @author advicetec
 * @see Processor
 * @see {@link #process()}
 */
public class SampleProcessor implements Processor 
{
	static final Logger logger = LogManager.getLogger(SampleProcessor.class.getName());
	/**
	 * Sample Message to be processed.
	 */
	private SampleMessage sample;
	/**
	 * Interpreter to 
	 */
	private InterpreterSw interpreter;

	/**
	 * Constructor.
	 * @param sample Message of a sample to be processed.
	 */
	public SampleProcessor(SampleMessage sample) {
		super();
		this.sample = sample;
	}


	/**
	 * Finds the Measured Entity and Program Transformation related to this sample.
	 * Then, the transformation syntax is checked, if there are not errors, 
	 * sample values are interpreted and processed.
	 * Finally, symbols, attributes, and attribute values update the 
	 * <code>STATUS</code>.
	 * If the Event is repetitive, it should be re scheduled at some point in
	 * the future, a new DelayedEvent is created and added to the returning list.
	 * 
	 * Returns a list of delayed events related to this <code>SampleMessage</code>
	 * 
	 * @return list of delayed events related to this object.
	 * @see SyntaxChecking
	 */
	public List<DelayEvent> process() throws SQLException 
	{
		// Finds the measuring Entity involved. The string value is always not null
		Integer measuringEntity = sample.getmEntity();
		Integer mearuringDevice = sample.getmDevice().getId();
		Integer ioPort = sample.getPort().getId();
		// Finds the program Transformation related to the sample.
		String program = sample.getMeaningTransformation();

		MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade entityFacade = entityManager.getFacadeOfEntityById(measuringEntity);

		ArrayList<DelayEvent> ret = new ArrayList<DelayEvent>();

		if (entityFacade == null){
			logger.error("Measured Entity not found - id:"+ measuringEntity );
		} else {

			SyntaxChecking sintaxChecking = new SyntaxChecking();
			try 
			{
				// First, we verify the transformation.
				List<SyntaxError> errorList = sintaxChecking.process(program, measuringEntity);
				
				// If no errors, then process.
				if (errorList.size() == 0){ 
					// Then, we read parameters from message and pass them to 
					// the interpreter as global variables.
					List<InterpretedSignal> list = sample.getValues();
					InterpreterSw interpreter = new InterpreterSw();
					interpreter.process(program,measuringEntity,list);
					// stores the status of attributes
					entityFacade.importSymbols(interpreter.getGlobalScope().getSymbolMap(), AttributeOrigin.TRANSFORMATION);
					entityFacade.importAttributeValues(interpreter.getGlobalAttributes());
					((MeasuredEntityFacade) entityFacade).setCurrentState(interpreter.getState());

					Map<String, Symbol> symbols =  interpreter.getGlobalScope().getSymbolMap();

					logger.debug("Number of Symbols returned:" + String.valueOf(symbols.size()));

					for (String symbolId : symbols.keySet())
					{

						Symbol symbol = symbols.get(symbolId);

						if (symbol instanceof  ArraySymbol){
							logger.debug("Symbol:" + symbolId + " ArraySymbol");
						} else if (symbol instanceof  AttributeSymbol){
							logger.debug("Symbol:" + symbolId + " AttributeSymbol");
						} else if (symbol instanceof  BehaviorSymbol){
							logger.debug("Symbol:" + symbolId + " BehaviorSymbol");
						} else if (symbol instanceof  FunctionSymbol){
							logger.debug("Symbol:" + symbolId + " FunctionSymbol");
						} else if (symbol instanceof  ImportSymbol){
							logger.debug("Symbol:" + symbolId + " ImportSymbol");
						} else if (symbol instanceof  ScopedSymbol){
							logger.debug("Symbol:" + symbolId + " ScopedSymbol");
						} else if (symbol instanceof  TimerSymbol){
							logger.debug("Symbol:" + symbolId + " TimerSymbol");
						} else if (symbol instanceof  UnitMeasureSymbol){
							logger.debug("Symbol:" + symbolId + " UnitMeasureSymbol");
						} else if (symbol instanceof  TransformationSymbol){
							logger.debug("Symbol:" + symbolId + " TransformationSymbol");
						} else if (symbol instanceof  AggregateSymbol){
							logger.debug("Symbol:" + symbolId + "AggregateSymbol");
						} else if (symbol instanceof StateSymbol) {
							logger.debug("Symbol:" + symbolId + " StateSymbol");
						} else if (symbol instanceof DisplaySymbol) {
							logger.debug("Symbol:" + symbolId + " DisplaySymbol");
						} else {
							logger.debug("Symbol:" + symbolId + " Invalid symbol");
						}

						if (symbol instanceof TimerSymbol)
						{

							long duetime = ((TimerSymbol) symbol).getMilliseconds();
							boolean repeated = ((TimerSymbol) symbol).getRepeated();
							
							String behavior = getBehavior(((TimerSymbol) symbol).getCompleteName());
							
							
							logger.info("Symbol:" + symbolId + "behavior:" + behavior);
							// We don't send parameters to the event. 
							MeasuredEntityEvent event = new MeasuredEntityEvent(behavior, measuringEntity, entityFacade.getType(), mearuringDevice, ioPort, new ArrayList<InterpretedSignal>());
							event.setRepeated(repeated);
							event.setMilliseconds(duetime);

							DelayEvent dEvent = new DelayEvent(event,duetime);
							ret.add(dEvent);
						}
						
						if (symbol instanceof AggregateSymbol)
						{
							long duetime = ((AggregateSymbol) symbol).getMilliseconds();
							boolean repeated = ((AggregateSymbol) symbol).getRepeated();
							
							String aggregateMethod = getBehavior(((AggregateSymbol) symbol).getCompleteName());
							
							
							logger.debug("Symbol:" + symbolId + " aggregateMethod:" + aggregateMethod+" : measuringEntity "+measuringEntity);
							// We don't send parameters to the event. 
							SchedAggregateEntityEvent event = new SchedAggregateEntityEvent(aggregateMethod, measuringEntity,mearuringDevice, ioPort, new ArrayList<InterpretedSignal>());
							event.setRepeated(repeated);
							event.setMilliseconds(duetime);

							DelayEvent dEvent = new DelayEvent(event,duetime);
							ret.add(dEvent);
						}
						
						if (symbol instanceof DisplaySymbol)
						{
							
							String displayName = ((DisplaySymbol) symbol).getName();
							String displayText = ((DisplaySymbol) symbol).getDisplayText();
							
							DisplayEvent event = new DisplayEvent(displayName, displayText);
							event.setMilliseconds(0);
							DelayEvent dEvent = new DelayEvent(event,0);
							ret.add(dEvent);
							
						}
					}

				}
				else {
					logger.error("The interpreter found " + errorList.size() +  " errors.");
				}

			} catch (Exception e1) {
				logger.error("Error:" + e1.getMessage());
				e1.printStackTrace();
			}		
		}
		return ret;
	}

	/**
	 * Return the behavior from the given list of names given as parameter. 
	 * 
	 * @param names machinegroup.machine.behaviorid
	 * @return the behavior name from the given list of names given as parameter.
	 */
	public String getBehavior(List<String> names)
	{
		StringBuilder behaviorName = new StringBuilder();
		for (String name : names){
			behaviorName.append(name); 
		}

		return behaviorName.toString();
	}
}
