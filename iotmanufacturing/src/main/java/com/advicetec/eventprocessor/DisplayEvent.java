package com.advicetec.eventprocessor;

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
		super(EventType.DISPLAY_EVENT);
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
	
	/**
	 * Gets a unique key to reference the event in queues. 
	 * 
	 * In this case the key is the type of event, the name of the display and the text to be shown.
	 */
	@Override
	public String getKey(){
		return super.getEvntType().getName() + "-" +  getDisplayName() + "-" +  getDisplayText();
	}
}
