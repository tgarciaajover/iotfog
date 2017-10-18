package com.advicetec.core;

import java.io.IOException;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.ObjectMapper;

import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.advicetec.measuredentitity.MeasuredEntityType;


/**
 * This class models the attribute values that comes from the host system .
 * 
 * Subclasses of this class are: MeasuredAttributeValue
 * 
 * @author Andres Marentes
 *
 */
@JsonTypeInfo(
		use=JsonTypeInfo.Id.NAME,
		include=JsonTypeInfo.As.PROPERTY,
	property="sub")
@JsonSubTypes({
	@Type(value=MeasuredAttributeValue.class,name="MeasuredAttributeValue")
})
public class AttributeValue
{
	
	/**
	 * unique key for this attribute. It is used to reference it in the cache.
	 */
	protected String key;
	
	/**
	 * attribute (it can be understood as a type of data) for which this value was created.  
	 */
	protected Attribute attr;
	
	/**
	 * The Value. depending on the type of attribute value assumes different types. For example if 
	 * the attribute type is DOUBLE, then value should be a java Double instance.
	 */
	protected Object value;

	/**
	 * Maintains a references to the measured entity, i.e., job, machine, etc. that this attribute value belongs to.
	 */
	protected Integer generator;
	
	/**
	 * Type of measured entity that this attribute value belongs to.
	 */
	protected MeasuredEntityType generatorType;

	/**
	 * Constructor for the class, it populates required fields.  
	 * 
	 * @param key 		 : Identifier
	 * @param type 		 : Type
	 * @param value 	 : Value
	 * @param parent 	 : Describes the origin of the measuring entity. e.g. machine, jobid
	 * @param parentType : Type of parent
	 */
	@JsonCreator
	public AttributeValue(@JsonProperty("key")String key, 
			@JsonProperty("attr")Attribute attr, 
			@JsonProperty("value")Object value, 
			@JsonProperty("generator")Integer parent, 
			@JsonProperty("generatorType")MeasuredEntityType parentType) {
		super();
		this.key =  key;
		this.attr = attr;
		this.value = value;
		this.generator = parent;
		this.generatorType = parentType;
	}


	/**
	 * Gets the key for this attribute value in the cache.
	 * 
	 * @return attribute values's key
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Gets the attribute 
	 * 
	 * @return parent attribute
	 */
	public Attribute getAttr()
	{
		return attr;
	}

	/**
	 * Gets the measured entity that this attribute value belongs to  
	 * @return  measured entity identifier
	 */
	public Integer getGenerator() {
		return generator;
	}

	/**
	 * Gets the measured entity type that this attribute value belongs to
	 * 
	 * @return  measured entity type object
	 */
	public MeasuredEntityType getGeneratorType() {
		return generatorType;
	}

	/**
	 * Method that compares to attribute values. 
	 * 
	 * 	An attribute value is said to be equal to another attribute value if:
	 * 	
	 * 		- their keys are equals
	 * 		- have the same attribute parent 
	 * 		- have the same value
	 *      - have the same generator 
	 *      - have the same generator type.
	 * 
	 * @param other attribute value for comparison.
	 * 
	 * @return true if this attribute value is equal to the given by parameter.
	 */
	public boolean equals(Object o){
		
		if (o instanceof AttributeValue) {
			
			AttributeValue other = (AttributeValue) o;   
			if (other.key.compareTo(this.key) != 0){
				return false;
			}
			
			if (other.attr.equals(this.attr) != true)
				return false;
			
			if (other.value.equals(this.value) != true)
				return false;
			
			if (other.generator != this.generator)
				return false;
			
			if (other.generatorType != this.generatorType) 
				return false;
			
			return true;
		} else { 
			
			return false;
			
		}
	}

	/**
	 * Gets the value
	 * 
	 * @return value object
	 */
	public Object getValue() {
		return value;
	}

	/** 
	 * Get a string representation 
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("key: ").append(key).append(",");
		sb.append("attr: ").append(attr.toString()).append(",");
		sb.append("value: ").append(value.toString()).append(",");
		sb.append("generator: ").append(generator).append(",");
		sb.append("generator type: ").append(generatorType.toString());
		return sb.toString();
	}

	/**
	 * Gets a json representation of the class
	 * 
	 * @return Json object
	 */
	public String toJson() {
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			System.err.println("Cannot export as Attribute Value as the json object.");
			e.printStackTrace();
		}
		return json;
	}
	
	/**
	 * Compare method between two attribute values 
	 * 
	 * @param a  attribute value for comparison.
	 * 
	 * @return integer saying if both are equal, 0 value 
	 * 					less than, negative value
	 * 					greater than, positive value.
	 */
	public int compareTo(AttributeValue a)
	{
		return this.toString().compareTo(a.toString());
	}

}
