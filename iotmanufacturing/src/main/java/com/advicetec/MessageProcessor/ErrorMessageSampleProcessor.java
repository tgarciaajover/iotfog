package com.advicetec.MessageProcessor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.Processor;
import com.advicetec.eventprocessor.MeasuredEntityEvent;
import com.advicetec.language.ast.ArraySymbol;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.BehaviorSymbol;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.ImportSymbol;
import com.advicetec.language.ast.ScopedSymbol;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.ast.TransformationSymbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.language.transformation.InterpreterSw;
import com.advicetec.language.transformation.SyntaxChecking;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.MeasuringState;
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
public class ErrorMessageSampleProcessor implements Processor 
{
	static final Logger logger = LogManager.getLogger(ErrorMessageSampleProcessor.class.getName());
	/**
	 * Sample Message to be processed.
	 */
	private MeasuringErrorMessage message;

	/**
	 * Constructor.
	 * @param sample Message of a sample to be processed.
	 */
	public ErrorMessageSampleProcessor(MeasuringErrorMessage message) {
		super();
		this.message = message;
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
		logger.info("In Error Message Sample Processor");
		
		// Finds the measuring Entity involved. The string value is always not null
		Integer measuringEntity = message.getmEntity();

		MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade entityFacade = entityManager.getFacadeOfEntityById(measuringEntity);

		ArrayList<DelayEvent> ret = new ArrayList<DelayEvent>();

		if (entityFacade == null){
			logger.error("Measured Entity not found - id:"+ measuringEntity );
		} else {

			((MeasuredEntityFacade) entityFacade).setCurrentState(MeasuringState.SYSTEMDOWN);

		}
		
		return ret;
	}

}
