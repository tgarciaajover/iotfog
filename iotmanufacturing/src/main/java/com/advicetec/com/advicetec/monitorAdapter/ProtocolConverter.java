package com.advicetec.monitorAdapter;

import com.advicetec.MessageProcessor.UnifiedMessage;

/**
 * This interface groups 
 * @author user
 *
 */
public interface ProtocolConverter {

	public UnifiedMessage getUnifiedMessage() throws Exception;
}
