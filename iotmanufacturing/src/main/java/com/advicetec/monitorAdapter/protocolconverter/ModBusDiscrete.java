package com.advicetec.monitorAdapter.protocolconverter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.AttributeType;

public class ModBusDiscrete implements Translator {
	
	static Logger logger = LogManager.getLogger(ModBusDiscrete.class.getName());
	
	public ModBusDiscrete() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<InterpretedSignal> translate(byte[] payload) {
		
		List<InterpretedSignal> listReturn = new ArrayList<InterpretedSignal>();
		Double value = Double.parseDouble(new String(payload, StandardCharsets.UTF_8));
		InterpretedSignal valueSignal = new InterpretedSignal(AttributeType.BOOLEAN, value);
		listReturn.add(valueSignal);
		
		return listReturn;
	}

}
