package com.advicetec.core;

/**
 * This class describes the measure unit. e.g. Kilograms.
 * @author user
 *
 */
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
