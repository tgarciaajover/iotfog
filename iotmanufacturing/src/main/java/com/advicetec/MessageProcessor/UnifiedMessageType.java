package com.advicetec.MessageProcessor;

/**
 * Enumeration with the possible types form Unified Messages.
 * Unified Message has two types: Samples and Messages from Brokers.
 * 
 * @author advicetec
 */
public enum UnifiedMessageType 
{
	   SAMPLE(0,"Sample"),
	   BROKER_MESSAGE(1, "Broker Message"),
	   INVALID(2,"Invalid Event");
	  
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
