package com.advicetec.core;

import java.io.IOException;

import javax.xml.bind.annotation.XmlAttribute;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Enumeration specifying the attribute types, these types correspond to primitive types from java. 
 * 
 * @author Andres Marentes
 *
 */
public enum AttributeType 
{
	
	/**
	 * Double Type 
	 */
	DOUBLE(0,"Double"),

	/**
	 * Integer Type
	 */
	INT(1,"Integer"),
	
	/**
	 *  Boolean Type 
	 */
	BOOLEAN(2,"Boolean"),
	
	/**
	 * String type 
	 */
	STRING(3,"String"),
	
	/**
	 *  Datetime type 
	 */
	DATETIME(4,"Datetime"),
	
	/**
	 * Void 
	 */
	VOID(5,"Void"),
	
	/**
	 * Date Type  
	 */
	DATE(6,"Date"),
	
	/**
	 * Time Type.
	 */
	TIME(7,"Time");

	/**
	 * value uniquely identifying the attribute type.
	 */
	@JsonProperty 
	private int value;
	
	
	/**
	 * Description of the attribute type.
	 */
	@JsonProperty
	private String name;

	/**
	 * Constructor for the enumeration
	 * 
	 * @param value  identifier of the attribute type
	 * @param name   name of the attribute type.
	 */
	private AttributeType(int value, String name) {
		this.value = value;
		this.name = name;
	}

	/**
	 * Gets the attribute type identifier
	 * @return attribute type identifier
	 */
	@XmlAttribute
	public int getValue() {
		return this.value;
	}
	
	/**
	 * Gets the attribute type name
	 * 
	 * @return name
	 */
	@XmlAttribute
	public String getName() {
		return this.name;
	}

	/**
	 * Equals with other attribute type. 
	 * 
	 * @param o  attribute type.  
	 * 
	 * @return   true if equals, false otherwise.
	 */
	public boolean equals(AttributeType o){
		return this.name.equals(o.getName());
	}

	/**
	 * Gets the attribute type from its identifier
	 * 
	 * @param val  attribute type identifier
	 * 
	 * @return  attribute type object
	 */
	public static AttributeType getByValue(int val){
		return AttributeType.values()[val];
	}

	/**
	 * Serialize the attribute type to Json.
	 * 
	 * @return json representation.
	 */
	public String toJson() {
		String json= null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			System.err.println("Cannot map AttributeType object to Json.");
			e.printStackTrace();
		}
		return json;
	}
}
