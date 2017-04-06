package com.advicetec.monitorAdapter.protocolconverter;

import com.advicetec.FogClasses.AttributeType;

/*
 * This class represents the signal once converted to the atomic types managed by the language.
 * The atomic types: Integer,  are the same as those in the attribute values.  
 */
public class InterpretedSignal 
{
	AttributeType type;
	Object value;
	
	public InterpretedSignal(AttributeType type, Object value) {
		super();
		this.type = type;
		this.value = value;
	}

	public AttributeType getType() {
		return type;
	}

	public void setType(AttributeType type) {
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
}
