package com.advicetec.configuration;

public class SignalType extends ConfigurationObject
{

	private String name;
	private String className;
	
	public SignalType(Integer id) {
		super(id);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String class_name) {
		this.className = class_name;
	}
	
}
