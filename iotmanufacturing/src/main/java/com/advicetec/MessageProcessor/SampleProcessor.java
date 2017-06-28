package com.advicetec.MessageProcessor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.Processor;
import com.advicetec.eventprocessor.MeasuredEntityEvent;
import com.advicetec.language.ast.ArraySymbol;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.BehaviorSymbol;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.ImportSymbol;
import com.advicetec.language.ast.ScopedSymbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.ast.TransformationSymbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.language.transformation.InterpreterSw;
import com.advicetec.language.transformation.SyntaxChecking;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.persistence.StatusStore;
import com.advicetec.language.ast.Symbol;


public class SampleProcessor implements Processor 
{

	static final Logger logger = LogManager.getLogger(SampleProcessor.class.getName()); 
	private SampleMessage sample;
	private InterpreterSw interpreter;
	
	
	public SampleProcessor(SampleMessage sample) {
		super();
		this.sample = sample;
	}


	@Override
	public List<DelayEvent> process() throws SQLException 
	{
		
		// Finds the measuring Entity involved. The string value is always not null
		Integer measuringEntity = sample.getmEntity();
		String program = sample.getMeaningTransformation();
		
		MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade entityFacade = entityManager.getFacadeOfEntityById(measuringEntity);
		
		ArrayList<DelayEvent> ret = new ArrayList<DelayEvent>();
		
		if (entityFacade == null){
			logger.error("the measured Entity was not found - id:"+ measuringEntity );
		} else {
		  
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
					entityFacade.importSymbols(interpreter.getGlobalScope().getSymbolMap(), AttributeOrigin.TRANSFORMATION);
					entityFacade.importAttributeValues(interpreter.getGlobalSpace().getSymbolMap());
					entityFacade.setCurrentState(interpreter.getGlobalSpace().getSymbolMap());
					
					Map<String, Symbol> symbols =  interpreter.getGlobalScope().getSymbolMap();
					
					logger.debug("Number of Symbols returned:" + String.valueOf(symbols.size()));
					
					for (String symbolId : symbols.keySet())
					{
						
						Symbol symbol = symbols.get(symbolId);
						
						if (symbol instanceof  ArraySymbol){
							logger.debug("Symbol:" + symbolId + "ArraySymbol");
						} else if (symbol instanceof  AttributeSymbol){
							logger.debug("Symbol:" + symbolId + "AttributeSymbol");
						} else if (symbol instanceof  BehaviorSymbol){
							logger.debug("Symbol:" + symbolId + "BehaviorSymbol");
						} else if (symbol instanceof  FunctionSymbol){
							logger.debug("Symbol:" + symbolId + "FunctionSymbol");
						} else if (symbol instanceof  ImportSymbol){
							logger.debug("Symbol:" + symbolId + "ImportSymbol");
						} else if (symbol instanceof  ScopedSymbol){
							logger.debug("Symbol:" + symbolId + "ScopedSymbol");
						} else if (symbol instanceof  TimerSymbol){
							logger.debug("Symbol:" + symbolId + "TimerSymbol");
						} else if (symbol instanceof  UnitMeasureSymbol){
							logger.debug("Symbol:" + symbolId + "UnitMeasureSymbol");
						} else if (symbol instanceof  TransformationSymbol){
							logger.debug("Symbol:" + symbolId + "TransformationSymbol");
						} else {
							logger.debug("Symbol:" + symbolId + "Invalid symbol");
						}
						
						if (symbol instanceof TimerSymbol)
						{
								
							long duetime = ((TimerSymbol) symbol).getMilliseconds();
							boolean repeated = ((TimerSymbol) symbol).getRepeated();
							
							String behavior = getBehavior(((TimerSymbol) symbol).getCompleteName());
							
							
							logger.debug("Symbol:" + symbolId + "behavior:" + behavior);
							// We don't send parameters to the event. 
							MeasuredEntityEvent event = new MeasuredEntityEvent(behavior, measuringEntity, new ArrayList<InterpretedSignal>());
							event.setRepeated(repeated);
							event.setMilliseconds(duetime);
							
							DelayEvent dEvent = new DelayEvent(event,duetime);
							ret.add(dEvent);
						}
					}
					
				}
				else {
					logger.error("the interpreter found errors numErrors:" + errorList.size());
				}
							
			} catch (Exception e1) {
				logger.error("Error:" + e1.getMessage());
				e1.printStackTrace();
			}		
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
		StringBuilder behaviorName = new StringBuilder();
		for (String name : names){
			behaviorName.append(name); 
		}
		
		return behaviorName.toString();
	}
	
	
}
