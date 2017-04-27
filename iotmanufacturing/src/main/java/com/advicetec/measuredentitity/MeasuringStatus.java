package com.advicetec.measuredentitity;

import org.codehaus.jackson.annotate.JsonProperty;

public enum MeasuringStatus 
{

   OPERATING(0,"Operating"),
   SCHEDULEDOWN(1,"ScheduleDown"),
   UNSCHEDULEDOWN(2,"UnScheduleDown");
   
   @JsonProperty 
   private int code;
   @JsonProperty 
   private String name;
   
   private MeasuringStatus(int value, String name) {
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
