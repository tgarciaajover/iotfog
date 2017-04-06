package com.advicetec.MessageProcessor;

public enum UnifiedMessageType 
{


	   BARCODE(0,"Barcode"),
	   SAMPLE(1,"Sample"),
	   RFID(2,"Radio Frequence ID"),
	   BROKER_MESSAGE(3, "Broker Message"),
	   INVALID(4,"Invalid Event");
	  
	   private int code;
	   private String name;
	   
	   private UnifiedMessageType(int value, String name) {
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
