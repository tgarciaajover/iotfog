package com.advicetec.eventprocessor;

import java.util.List;

import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

public class DisplayEvent extends Event 
{

	// name of the display
	private String display_name;
	
	//  Text to show
	private String display_text;
	
	// boolean 
	
	public DisplayEvent(String name, String text) 
	{
		super(EventType.DISPLAY_EVENT);
		this.display_name = name;
		this.display_text = text;
	}

	public String getDisplayName() {
		return display_name;
	}

	public String getDisplayText() {
		return display_text;
	}
	
	@Override
	public String toString() {
		return "{" +
	                " display_name" + display_name +
	                ", display_text=" + display_text +
	                '}';
	}
}
