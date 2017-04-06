package com.advicetec.configuration;

public abstract class ConfigurationObject 
{
	Integer id;

	public ConfigurationObject(Integer id) {
		super();
		this.id = id;
	}

	public Integer getId() {
		return id;
	}
	
	
}
