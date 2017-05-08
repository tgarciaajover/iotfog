package com.advicetec.language.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DisplaySymbol extends Symbol 
{

    // In this case the name corresponds to the display device where we should show the text. 
	// There is only a symbol by each device. So if the transformation or behavior call 
    // multiple times the display function for the same device, then only one symbol will be created.
	
	String diplayText;
	
	public DisplaySymbol(String name) 
	{ 
		super(name,Type.tVOID); 
	}

	void setDisplayText(String displayText)
	{
		this.diplayText = displayText;
	}
	
	String getDisplayText(){
		return this.diplayText;
	}
	
}
