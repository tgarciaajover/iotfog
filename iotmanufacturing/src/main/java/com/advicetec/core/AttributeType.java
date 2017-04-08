package com.advicetec.core;

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
	   
	   public boolean equals(AttributeType o){
		   return this.name.equals(o.getName());
	   }

}
