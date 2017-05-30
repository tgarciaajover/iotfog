package com.advicetec.monitorAdapter;

import java.util.List;

import com.advicetec.MessageProcessor.UnifiedMessage;

/**
 * This interface groups 
 * @author user
 *
 */
public interface ProtocolConverter {

	public List<UnifiedMessage> getUnifiedMessage() throws Exception;
}
