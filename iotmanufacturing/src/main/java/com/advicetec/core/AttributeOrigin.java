package com.advicetec.core;

import javax.xml.bind.annotation.XmlAttribute;


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
}
