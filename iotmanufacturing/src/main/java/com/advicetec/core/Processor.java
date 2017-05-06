package com.advicetec.core;

import java.sql.SQLException;
import java.util.List;

import com.advicetec.MessageProcessor.DelayEvent;

public interface Processor 
{
	public List<DelayEvent> process() throws SQLException;
}
