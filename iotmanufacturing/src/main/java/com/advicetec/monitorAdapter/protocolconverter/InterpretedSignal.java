package com.advicetec.monitorAdapter.protocolconverter;

import com.advicetec.core.AttributeType;

/**
 * This class represents the signal once converted to the atomic types managed
 *  by the language.
 *  <p>
 * The atomic types: Integer, Boolean, Double, String, Date, Time, and Datetime
 *  are the same as those in the attribute values.  
 */
public class InterpretedSignal 
{
	/**
	 * type of attribute
	 */
	AttributeType type;
	/**
	 * Value
	 */
	Object value;
	
	/**
	 * Constructs this object with the type and object given. 
	 * @param type Attribute type
	 * @param value Obejct
	 * @see AttributeType
	 */
	public InterpretedSignal(AttributeType type, Object value) {
		super();
		this.type = type;
		this.value = value;
	}

	/**
	 * Returns the type of this interpreted signal.
	 * @return the type of this interpreted signal.
	 */
	public AttributeType getType() {
		return type;
	}

	/**
	 * Sets the type of attribute to this interpreted signal.
	 * @param type of attribute
	 */
	public void setType(AttributeType type) {
		this.type = type;
	}

	/**
	 * Returns the content object.
	 * @return the content object.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets an object to the value of this interpreted signal.
	 * @param value content object.
	 */
	public void setValue(Object value) {
		this.value = value;
	}	
}
