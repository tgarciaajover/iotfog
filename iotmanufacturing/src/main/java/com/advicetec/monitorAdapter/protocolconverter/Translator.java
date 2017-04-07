package com.advicetec.monitorAdapter.protocolconverter;

import java.util.List;

public interface Translator 
{
	
	public List<InterpretedSignal> translate(byte[] payload);
	
}
