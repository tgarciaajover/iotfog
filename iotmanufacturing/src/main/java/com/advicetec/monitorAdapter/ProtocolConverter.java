package com.advicetec.monitorAdapter;

import java.util.List;

import com.advicetec.MessageProcessor.UnifiedMessage;

/**
 * This interface standardizes the method <code>getUnifiedMessage()</code>
 * 
 * @author advicetec
 *
 */
public interface ProtocolConverter {
	public List<UnifiedMessage> getUnifiedMessage() throws Exception;
}
