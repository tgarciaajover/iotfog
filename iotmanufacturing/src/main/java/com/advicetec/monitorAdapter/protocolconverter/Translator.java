package com.advicetec.monitorAdapter.protocolconverter;

import java.util.List;

/**
 * General interface for signal translation.
 * This interface implements the tranlate signature that receives the payload
 * in bytes and builds a collection of Interpreted signals. 
 * @author advicetec
 *
 */
public interface Translator 
{
	/**
	 * Translation method. It translates the payload in a specific protocol and
	 * translates it to unified Interpreted signals. It returns a list because
	 * a single read (payload) could be interpreted as a set of signals.
	 * 
	 * @param payload The bytes to be translate.
	 * @return a collection of unified interpreted signals.
	 */
	public List<InterpretedSignal> translate(byte[] payload);
}
