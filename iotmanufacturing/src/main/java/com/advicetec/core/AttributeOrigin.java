package com.advicetec.core;

import java.io.IOException;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * Enumeration class used to describe the possible origins for attributes.
 * 
 * @author Andres Marentes
 *
 */
public enum AttributeOrigin {
	
	/**
	 * It was defined as part of a transformation
	 */
	TRANSFORMATION(0,"Transformation"), 
	
	/**
	 * It was defined in an interface from the host system
	 */
	ERP(1,"ERP"),
	
	/**
	 * It was defined as part of a behavior
	 */
	BEHAVIOR(2,"Behavior");

	/**
	 * Value specifying the origin.
	 */
	private int value;
	
	/**
	 * Description of the origin
	 */
	private String name;

	/**
	 * Constructor for the enumeration class
	 * @param value value
	 * @param name description
	 */
	private AttributeOrigin(int value, String name) {
		this.value = value;
		this.name = name;
	}
	
	/**
	 * Gets the value that defines the origin of the attribute
	 * 
	 * @return attribute origin
	 */
	@XmlAttribute
	public int getValue() {
		return this.value;
	}
	
	/**
	 * Gets origin's name of the attribute
	 * 
	 * @return origin's name of the attribute
	 */
	@XmlAttribute
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the attribute origin from its identifier
	 * 
	 * @param val  attribute origin identifier
	 * 
	 * @return  attribute origin object
	 */
	public static AttributeOrigin getByValue(int val){
		return AttributeOrigin.values()[val];
	}
	
	/**
	 * Serialize the attribute origin to Json.
	 * 
	 * @return json representation.
	 */
	public String toJson(){
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			System.err.println("Cannot serialize the AttributeOrigin object.");
			e.printStackTrace();
		}
		return json;
	}
}
