package com.advicetec.MessageProcessor;

import com.advicetec.configuration.InputOutputPort;
import com.advicetec.configuration.MonitoringDevice;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

/**
 * This class models an Error Message that is a result of Measuring a Sample.
 * 
 * @author advicetec
 * @see InterpretedSignal
 * @see 
 */
public class MeasuringErrorMessage extends MeasuringMessage 
{

	public MeasuringErrorMessage(MonitoringDevice device, InputOutputPort port,
			Integer entityId) {
		super(UnifiedMessageType.ERROR_SAMPLE, device, port, entityId);
		
	}

	public String toString(){
		return "Error Sample Message: Type = " + getType().getName();
	}

}
