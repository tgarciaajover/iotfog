package com.advicetec.core;


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
	   
	   public int getValue() {
	      return this.code;
	   }
	   
	   public String getName() {
		  return this.name;
	   }
	   
	   public boolean equals(AttributeType o){
		   return this.name.equals(o.getName());
	   }
}
