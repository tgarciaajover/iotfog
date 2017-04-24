package com.advicetec.core;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.hamcrest.core.IsInstanceOf;
import org.w3c.dom.Element;

/**
 * This class describes the measure unit. e.g. Kilograms.
 * @author user
 *
 */
public class MeasuringUnit 
{
	private String symbol;  // identifier.
	private String description;    // description.
	
	public MeasuringUnit( String symbol, String description) {
		super();
		this.symbol = symbol;
		this.description = description;
	}

	@XmlAttribute
	public String getSymbol() {
		return symbol;
	}

	@XmlAttribute
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
