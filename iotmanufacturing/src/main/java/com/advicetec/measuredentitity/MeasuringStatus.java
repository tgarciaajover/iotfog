package com.advicetec.measuredentitity;

public enum MeasuringStatus 
{

   OPERATING(0,"Operating"),
   SCHEDULEDOWN(1,"ScheduleDown"),
   UNSCHEDULEDOWN(2,"UnScheduleDown");
   
   
   private int code;
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
