package com.advicetec.language.ast;

/**
 * This class defines a symbol to represent a display where the user by using the 
 * lenguage should send a message. 
 * 
 * In this case the name corresponds to the display device where we should show the text. 
 * There is only a symbol by each device. 
 * 
 * If the transformation or behavior calls multiple times the display function for 
 * the same device, then only one symbol will be created.

 * @author Andres Marentes
 *
 */
public class DisplaySymbol extends Symbol 
{	
	String diplayText;
	
	/**
	 * Constructor for the class. The name of the referenced display 
	 * 
	 * @param name  name of the display 
	 */
	public DisplaySymbol(String name) 
	{ 
		super(name,Type.tVOID); 
	}

	/**
	 * Sets the text to display in the device
	 * 
	 * @param displayText  text to show.
	 */
	public void setDisplayText(String displayText)
	{
		this.diplayText = displayText;
	}
	
	/**
	 * Gets the text to display  
	 * 
	 * @return text to display
	 */
	public String getDisplayText(){
		return this.diplayText;
	}
}
