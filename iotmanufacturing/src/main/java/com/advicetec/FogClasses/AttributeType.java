package com.advicetec.FogClasses;

public enum AttributeType 
{
	
	   DOUBLE(0,"Double"),
	   DATETIME(1,"Datetime"),
	   STRING(2,"String"),
	   INT(3,"Integer");
	  
	   private int code;
	   private String name;
	   
	   private AttributeType(int value, String name) {
	      this.code = value;
	      this.name = name;
	   }
	   
	   public int getValue() {
	      return this.code;
	   }
	   
	   public String getName() {
		  return this.name;
	   }

}
