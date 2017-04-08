package com.advicetec.core;

/**
 * This class describes the measure unit. e.g. Kilograms.
 * @author user
 *
 */
public class MeasuringUnit 
{
	String symbol;  // identifier.
	String description;    // description.
	
	public MeasuringUnit( String symbol, String description) {
		super();
		this.symbol = symbol;
		this.description = description;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getDescription() {
		return description;
	}
	
	public boolean equals (MeasuringUnit other){
		return ( other.getDescription().equalsIgnoreCase(this.getDescription()) && 
				other.getSymbol().equalsIgnoreCase(this.getSymbol()));
	}
	
}
