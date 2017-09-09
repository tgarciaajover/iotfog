package com.advicetec.monitorAdapter.protocolconverter;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.AttributeType;

public class ModBusDiscrete implements Translator {
	
	static Logger logger = LogManager.getLogger(ModBusDiscrete.class.getName());
	
	public ModBusDiscrete() {
	}

	@Override
	public List<InterpretedSignal> translate(byte[] payload) {
		
		List<InterpretedSignal> listReturn = new ArrayList<InterpretedSignal>();
		
		for (int i = 0; i < payload.length; i++){
			InterpretedSignal valueSignal = new InterpretedSignal(AttributeType.BOOLEAN, payload[i]==1? true : false);
			listReturn.add(valueSignal);
		}
		
		return listReturn;
	}

}
