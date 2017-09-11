package com.advicetec.monitorAdapter.protocolconverter;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.AttributeType;

/**
 * Main functionality is to translate a register value in Modbus protocol to a
 * collection of interpreted signals.
 * This class implement the <code>Translator</code> interface.
 * @author advicetec
 * @see Translator
 */
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
			logger.debug("Translated value: " + String.valueOf(val));
			InterpretedSignal valueSignal = new InterpretedSignal(AttributeType.INT, val);
			listReturn.add(valueSignal);
			i = i + 2;
		}
		return listReturn;
	}
	
}
