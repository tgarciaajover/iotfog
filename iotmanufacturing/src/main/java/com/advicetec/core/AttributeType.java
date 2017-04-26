package com.advicetec.core;

import java.io.IOException;

import javax.xml.bind.annotation.XmlAttribute;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

public enum AttributeType 
{
	
	   DOUBLE(0,"Double"),
	   INT(1,"Integer"),
	   BOOLEAN(2,"Boolean"),
	   STRING(3,"String"),		  
	   DATETIME(4,"Datetime"),
	   VOID(5,"Void"),
	   DATE(6,"Date"),
	   TIME(7,"Time");
	   
	   @JsonProperty 
	   private int value;
	   @JsonProperty
	   private String name;
	   
	   private AttributeType(int value, String name) {
	      this.value = value;
	      this.name = name;
	   }
	   
	   @XmlAttribute
	   public int getValue() {
	      return this.value;
	   }
	   @XmlAttribute
	   public String getName() {
		  return this.name;
	   }
	   
	   public boolean equals(AttributeType o){
		   return this.name.equals(o.getName());
	   }

	   public static AttributeType getByValue(int val){
		   return AttributeType.values()[val];
	   }
	   
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
