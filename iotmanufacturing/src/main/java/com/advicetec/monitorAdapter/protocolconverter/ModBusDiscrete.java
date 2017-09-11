package com.advicetec.monitorAdapter.protocolconverter;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.AttributeType;

/**
 * Main functionality is to translate a discrete signal in Modbus protocol to a
 * collection of interpreted signals.
 * This class implement the <code>Translator</code> interface.
 * @author advicetec
 * @see Translator
 */
public class ModBusDiscrete implements Translator {
	
	static Logger logger = LogManager.getLogger(ModBusDiscrete.class.getName());
	/**
	 * Empty constructor.
	 */
	public ModBusDiscrete() {}

	@Override
	public List<InterpretedSignal> translate(byte[] payload) {
		
		List<InterpretedSignal> listReturn = new ArrayList<InterpretedSignal>();
		
		for (int i = 0; i < payload.length; i++){
			InterpretedSignal valueSignal = 
					new InterpretedSignal(AttributeType.BOOLEAN, payload[i]==1? true : false);
			listReturn.add(valueSignal);
		}
		return listReturn;
	}

}
