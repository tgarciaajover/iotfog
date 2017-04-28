package com.advicetec.core;

import java.io.IOException;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * This class describes the measure unit. e.g. Kilograms.
 * @author user
 *
 */
public class MeasuringUnit 
{
	private String symbol;  	// identifier.
	private String description; // description.
	
	@JsonCreator
	public MeasuringUnit(@JsonProperty("symbol") String symbol,
			@JsonProperty("description") String description) {
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
		if (!(o instanceof MeasuringUnit)) {
			return false;
		} else {
			MeasuringUnit other = (MeasuringUnit)o;
			return ( other.getDescription().equalsIgnoreCase(this.getDescription()) && 
				other.getSymbol().equalsIgnoreCase(this.getSymbol()));
		}
	}

	public String toJson() {
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	public String toString(){
		StringBuilder build = new StringBuilder();
		build.append("symbol: ").append(symbol).append(", ");
		build.append("description: ").append(description);
		return build.toString();
	}
}
