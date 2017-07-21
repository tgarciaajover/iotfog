package com.advicetec.measuredentitity;

import java.io.IOException;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

public enum MeasuringState 
{

   OPERATING(0,"Operating"),
   SCHEDULEDOWN(1,"ScheduleDown"),
   UNSCHEDULEDOWN(2,"UnScheduleDown"),
   UNDEFINED(3, "Undefined");
   
   @JsonProperty 
   private int value;
   @JsonProperty 
   private String name;
   
   private MeasuringState(int value, String name) {
      this.value = value;
      this.name = name;
   }
   
   public int getValue() {
      return this.value;
   }
   
   public String getName() {
	  return this.name;
   }
   
   static public MeasuringState getByValue(int value){
	   return values()[value];
   }
   
   static public MeasuringState getByName(String name){
	   name = name.trim();

	   if (name.equalsIgnoreCase("Operating")){
		   return OPERATING;
	   } else if ( name.equalsIgnoreCase("ScheduleDown") ) {
		   return SCHEDULEDOWN;
	   } else if(name.equalsIgnoreCase("UnScheduleDown")){
		   return UNSCHEDULEDOWN;
	   } else {
		   return UNDEFINED;
	   }
   }

   public String toJson(){
	   String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			System.err.println("Cannot serialize the MeasuringState object.");
			e.printStackTrace();
		}
		return json;
   }
}
