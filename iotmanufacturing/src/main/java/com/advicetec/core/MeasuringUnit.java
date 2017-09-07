package com.advicetec.core;

import java.io.IOException;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * This class describes the measure unit to be used by attributes; e.g, Kilograms.
 * 
 * @author Andres Marentes
 *
 */
public class MeasuringUnit 
{
	
	static Logger logger = LogManager.getLogger(MeasuringUnit.class.getName());
	
	/**
	 *  Symbol used for the unit of measure
	 */
	private String symbol;  	// identifier.
	
	/**
	 * unit measure's description
	 */
	private String description; // description.
	
	/**
	 * Constructor for the class
	 * @param symbol  		unit measure's symbol
	 * @param description	unit measure's description
	 */
	@JsonCreator
	public MeasuringUnit(@JsonProperty("symbol") String symbol,
			@JsonProperty("description") String description) {
		this.symbol = symbol;
		this.description = description;
	}

	/**
	 * Gets the unit of measure symbol 
	 * @return unit of measure symbol
	 */
	@XmlAttribute
	public String getSymbol() {
		return symbol;
	}

	/**
	 * Gets the unit of measure description
	 * @return unit of measure description
	 */
	@XmlAttribute
	public String getDescription() {
		return description;
	}
	
	/**
	 * Get a hash code from symbol and description
	 */
	public int hashCode(){
		return Objects.hash(symbol,description);
	}
	
	/** 
	 * Determines if this unit of measure is equal to the object given as parameter
	 * 
	 * A unit of measure is equals if has the same description and symbol.
	 * 
	 * @return true if equals, false otherwise  
	 */
	public boolean equals (Object o){
		if (!(o instanceof MeasuringUnit)) {
			return false;
		} else {
			MeasuringUnit other = (MeasuringUnit)o;
			return ( other.getDescription().equalsIgnoreCase(this.getDescription()) && 
				other.getSymbol().equalsIgnoreCase(this.getSymbol()));
		}
	}

	/**
	 * Serialize the unit of measure into JSON.
	 * 
	 * @return Json object that represents the unit of measure.
	 */
	public String toJson() {
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * Serialize the unit of measure into String
	 * 
	 * @return String that represents the unit of measure.
	 */
	public String toString(){
		StringBuilder build = new StringBuilder();
		build.append("symbol: ").append(symbol).append(", ");
		build.append("description: ").append(description);
		return build.toString();
	}
}
