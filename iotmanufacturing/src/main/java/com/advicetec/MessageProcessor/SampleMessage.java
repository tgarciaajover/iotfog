package com.advicetec.MessageProcessor;

import com.advicetec.monitorAdapter.SampleType;

public class SampleMessage extends MeasuringMessage {

	SampleType type;
	byte[] value;
	public SampleMessage(SampleType type, byte[] value) {
		super();
		this.type = type;
		this.value = value;
	}
	public SampleType getType() {
		return type;
	}
	public void setType(SampleType type) {
		this.type = type;
	}
	public byte[] getValue() {
		return value;
	}
	public void setValue(byte[] value) {
		this.value = value;
	}
	
	public String toString(){
		return "Sample Message: Type = "+type+" value = "+ value.toString();
	}
}
