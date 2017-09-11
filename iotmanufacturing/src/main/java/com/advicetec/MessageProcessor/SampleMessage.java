package com.advicetec.MessageProcessor;

import java.util.List;

import com.advicetec.configuration.InputOutputPort;
import com.advicetec.configuration.MonitoringDevice;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

/**
 * This class models a Message that as result of Measuring a Sample.
 * It comprises a list of values, represented by Interpreted Signals, and a 
 * Transformation that gives a meaning to this <i>sample</i>.
 * 
 * @author advicetec
 * @see InterpretedSignal
 * @see 
 */
public class SampleMessage extends MeasuringMessage 
{
	/**
	 * List of values that come from signals.
	 */
	List<InterpretedSignal> values;
	/**
	 * Transformation that gives meaning to those values.
	 */
	private String meaningTransformation;
	
	/**
	 * Regular constructor.
	 * @param device to be monitored.
	 * @param port number that sends singnals.
	 * @param entityId identifier of the measured entity
	 * @param values List of values that compose this sample.
	 * @param meaningTransformation name of the transformation.
	 */
	public SampleMessage(MonitoringDevice device, InputOutputPort port, 
			Integer entityId, List<InterpretedSignal> values, 
			String meaningTransformation) 
	{
		super(UnifiedMessageType.SAMPLE, device, port, entityId);
		this.values = values;
		this.meaningTransformation = meaningTransformation;
	}
	
	public String toString(){
		return "Sample Message: Type = "+getType().getName()+" value = "+ values.toString();
	}
	
	/**
	 * Returns the name or title of the transformation that gives sense to this
	 * sample.
	 * @return transformation that gives meaning to the sample.
	 */
	public String getMeaningTransformation() {
		return meaningTransformation;
	}

	/**
	 * Returns the list of values associated to this sample.
	 * @return the list of values associated to this sample.
	 */
	public List<InterpretedSignal> getValues() {
		return values;
	}
}
