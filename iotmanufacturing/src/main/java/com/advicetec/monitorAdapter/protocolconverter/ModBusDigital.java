package com.advicetec.monitorAdapter.protocolconverter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.AttributeType;

public class ModBusDigital implements Translator {
	
	static Logger logger = LogManager.getLogger(ModBusDigital.class.getName());
	
	public ModBusDigital() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<InterpretedSignal> translate(byte[] payload) {
		
		List<InterpretedSignal> listReturn = new ArrayList<InterpretedSignal>();
		Double value = Double.parseDouble(new String(payload, StandardCharsets.UTF_8));
		InterpretedSignal valueSignal;
		
		// TODO: replace constant values by constant defined in a property file.
		if ((value.floatValue() >= 0.0) && (value.floatValue() <= 0.5)){
			logger.debug("arrived value is interpreted as FALSE");
			boolean finalValue = false;
			valueSignal = new InterpretedSignal(AttributeType.BOOLEAN, new Boolean(finalValue));
		} else {
			logger.debug("arrived value is interpreted as TRUE");
			boolean finalValue = true;
			valueSignal = new InterpretedSignal(AttributeType.BOOLEAN, new Boolean(finalValue));
		}
		
		listReturn.add(valueSignal);
		return null;
	}

}
