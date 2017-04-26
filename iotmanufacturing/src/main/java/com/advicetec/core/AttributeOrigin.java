package com.advicetec.core;

import java.io.IOException;

import javax.xml.bind.annotation.XmlAttribute;

import org.codehaus.jackson.map.ObjectMapper;


public enum AttributeOrigin {

	TRANSFORMATION(0,"Transformation"), 
	ERP(1,"ERP"),
	BEHAVIOR(2,"Behavior");
	
	   private int code;
	   private String name;
	   
	   private AttributeOrigin(int value, String name) {
	      this.code = value;
	      this.name = name;
	   }
	   @XmlAttribute
	   public int getValue() {
	      return this.code;
	   }
	   @XmlAttribute
	   public String getName() {
		  return this.name;
	   }
	   
	   public boolean equals(AttributeType o){
		   return this.name.equals(o.getName());
	   }
	   
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