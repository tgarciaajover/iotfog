package com.advicetec.MessageProcessor;

import java.util.ArrayList;
import java.util.List;

import com.advicetec.core.Processor;

/**
 * This class models the processor of messages from the barcode device.
 * It implements the <code>Processor</code> interface.
 * 
 * @author advicetec
 * @see Processor
 */
public class BarcodeProcessor implements Processor
{
	@Override
	public List<DelayEvent> process() {
		// TODO method thar process the message.
		return new ArrayList<DelayEvent>();
	}	
}
