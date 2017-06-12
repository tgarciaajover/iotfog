package com.advicetec.monitorAdapter.protocolconverter;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.AttributeType;

public class ModBusRegister implements Translator {

	static Logger logger = LogManager.getLogger(ModBusDiscrete.class.getName());
	
	public ModBusRegister() {
	}

	@Override
	public List<InterpretedSignal> translate(byte[] payload) {
		
		List<InterpretedSignal> listReturn = new ArrayList<InterpretedSignal>();
		int i = 1; // The first byte corresponds to array len.
		while  (i < payload.length) {
			Integer val = ((payload[i] & 0xff) << 8) | (payload[i+1] & 0xff);
			InterpretedSignal valueSignal = new InterpretedSignal(AttributeType.INT, val);
			listReturn.add(valueSignal);
			i = i + 2;
		}
		
		return listReturn;
	}
	
}