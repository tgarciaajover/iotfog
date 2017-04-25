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
	
	public void setId(Integer id){
		this.id= id;
	}
	
}
