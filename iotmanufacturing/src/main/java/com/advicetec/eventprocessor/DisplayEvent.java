package com.advicetec.eventprocessor;

import com.advicetec.measuredentitity.MeasuredEntityType;

public class DisplayEvent extends Event 
{

	/**
	 * Name of the display
	 */
	private String display_name;
	
	/**
	 * Text to show
	 */
	private String display_text;
		
	/**
	 * Constructor for the class, it creates a new event in order to show a message.
	 * @param name  name of the display where the message is going to be shown (the name of the display is an alternative identifier for the display ) 
	 * @param text  message text to be shown.
	 */
	public DisplayEvent(String name, String text) 
	{
		super(EventType.DISPLAY_EVENT, 
					EventType.DISPLAY_EVENT.getName() + "-" + name + "-" +  text);
		this.display_name = name;
		this.display_text = text;
	}

	/**
	 * Gets the name of the display where the message is going to be shown.
	 * @return  display name.
	 */
	public String getDisplayName() {
		return display_name;
	}

	/**
	 * Gets the message text to be shown.
	 * 
	 * @return message text
	 */
	public String getDisplayText() {
		return display_text;
	}
	
	/** 
	 * Serialize the object into string
	 */
	@Override
	public String toString() {
		return "{" +
	                " display_name" + display_name +
	                ", display_text=" + display_text +
	                '}';
	}

	@Override
	public Integer getEntity() {
		return -1;
	}

	@Override
	public MeasuredEntityType getOwnerType() {
		return MeasuredEntityType.UNDEFINED;
	}
	
	
}
