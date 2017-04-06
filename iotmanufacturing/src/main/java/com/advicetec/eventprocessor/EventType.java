package com.advicetec.eventprocessor;

public enum EventType 
{

	   TIMER_EVENT(0,"Timer Event"),
	   MEASURING_ENTITY_EVENT(1,"Measuring Entity Event"),
	   META_MODEL_EVENT(2,"Meta Model Event"),
	   PRODUCTION_ORDER(3,"Production Order"),
	   WEB_APPLICATION_QUERY_EVENT(4,"Web Application Query Event"),
	   INVALID(5,"Invalid Event");
	  
	   private int code;
	   private String name;
	   
	   private EventType(int value, String name) {
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
