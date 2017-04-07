package com.advicetec.MessageProcessor;

import java.util.List;

import com.advicetec.configuration.InputOutputPort;
import com.advicetec.configuration.MonitoringDevice;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

public class SampleMessage extends MeasuringMessage 
{

	List<InterpretedSignal> values;
	private String meaningTransformation;
	
	public SampleMessage(MonitoringDevice device, InputOutputPort port, String entityId, List<InterpretedSignal> values, String meaningTransformation) 
	{
		super(UnifiedMessageType.SAMPLE, device, port, entityId);
		this.values = values;
		this.meaningTransformation = meaningTransformation;
	}
	
	public List<InterpretedSignal> getValue() {
		return values;
	}
	
	public String toString(){
		return "Sample Message: Type = "+getType().getName()+" value = "+ values.toString();
	}
	
	public String getMeaningTransformation() {
		return meaningTransformation;
	}

	public List<InterpretedSignal> getValues() {
		return values;
	}
	
	
	
}
