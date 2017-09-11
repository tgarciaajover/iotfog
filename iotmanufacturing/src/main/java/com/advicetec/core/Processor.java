package com.advicetec.core;

import java.sql.SQLException;
import java.util.List;

import com.advicetec.MessageProcessor.DelayEvent;

/**
 * Interface to specify that classes implementing this interface execute 
 * something and as the result of that process, it generates a list of
 * DaylEvents to schedule. 
 * 
 * @author Andres Marentes
 *
 */
public interface Processor 
{
	public List<DelayEvent> process() throws SQLException;
}
