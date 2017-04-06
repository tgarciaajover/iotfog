package com.advicetec.configuration;

public class SignalType extends ConfigurationObject
{

	private String name;
	private String class_name;
	
	public SignalType(Integer id) {
		super(id);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getClass_name() {
		return class_name;
	}
	public void setClass_name(String class_name) {
		this.class_name = class_name;
	}
	
}
