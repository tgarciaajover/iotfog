package com.advicetec.core;

public enum MeasuredEntityType 
{

	   MACHINE(0,"Machine"),
	   PLANT(1,"Plant"),
	   FACILITY(2,"Facility"),
	   COMPANY(3,"Company"),
	   JOB(4,"Job");
	  
	   private int code;
	   private String name;
	   
	   private MeasuredEntityType(int value, String name) {
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
