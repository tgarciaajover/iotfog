package com.advicetec.FogClasses;

public class MeasuringUnit 
{
	String symbol;  // identifier.
	String name;    // description.
	
	public MeasuringUnit( String symbol, String name) {
		super();
		this.symbol = symbol;
		this.name = name;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getName() {
		return name;
	}
	
}
