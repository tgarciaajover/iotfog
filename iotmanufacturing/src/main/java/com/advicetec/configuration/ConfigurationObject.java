package com.advicetec.configuration;

/**
 * Abstract class parent for all configurable objects 
 * 
 * @author Andres Marentes
 *
 */
public abstract class ConfigurationObject 
{
	
	/**
	 * Unique identification of the configured object.
	 */
	Integer id;

	/**
	 * Constructor for the class, it receives the identification of the configured object.
	 * 
	 * @param id  Identifier for the configured object.
	 */
	public ConfigurationObject(Integer id) {
		super();
		this.id = id;
	}

	/**
	 * Gets the configuration object identifier.
	 * @return  configuration object identifier
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * Sets the configuration object identifier.
	 * @param id configuration object identifier
	 */
	public void setId(Integer id){
		this.id= id;
	}
	
}
