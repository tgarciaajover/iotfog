package com.advicetec.MessageProcessor;


public class SampleMessage extends MeasuringMessage {

	byte[] value;
	
	public SampleMessage(byte[] value) {
		super(UnifiedMessageType.SAMPLE);
		this.value = value;
	}
	public byte[] getValue() {
		return value;
	}
	public void setValue(byte[] value) {
		this.value = value;
	}
	
	public String toString(){
		return "Sample Message: Type = "+getType().getName()+" value = "+ value.toString();
	}
}
