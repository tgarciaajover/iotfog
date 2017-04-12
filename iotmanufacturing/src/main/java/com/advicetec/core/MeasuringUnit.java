package com.advicetec.core;

import java.util.Objects;

import org.hamcrest.core.IsInstanceOf;

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
	
	
	public int hashCode(){
		return Objects.hash(symbol,description);
	}
	
	public boolean equals (Object o){
		if(o instanceof MeasuringUnit){
			return false;
		}else{
			MeasuringUnit other = (MeasuringUnit)o;
		return ( other.getDescription().equalsIgnoreCase(this.getDescription()) && 
				other.getSymbol().equalsIgnoreCase(this.getSymbol()));
		}
	}
	
}
