package com.advicetec.measuredentitity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.applicationAdapter.ProductionOrder;
import com.advicetec.configuration.ConfigurationObject;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.Entity;
import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;

/**
 * This class represents any object being executed (produced) in a measured entity. 
 * 
 * Examples of classes inheriting from this class are:
 * 		ProductionOrder.
 * 
 * @author Advicetec
 * @see P
 */
@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type")
	@JsonSubTypes({
	    @Type(value = ProductionOrder.class, name = "J") })
public abstract class ExecutedEntity extends Entity 
{

	static final Logger logger = LogManager.getLogger(ExecutedEntity.class.getName()); 

	
	/**
	 * Date and time when the instance was created.  
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime createDate;

	/**
	 * Last date and time when the instance was updated  
	 */
	@JsonProperty("last_updttm") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime lastDateTime;
		

	/**
	 * Constructor for the class 
	 * @param id	executed entity identification
	 * @param type	type of executed entity
	 */
	public ExecutedEntity(@JsonProperty("id") Integer id, MeasuredEntityType type) {
		super(id, type);
	}

           
    /**
     * Indicates if this entity is equals to some another executed entity. 
     * @param other	The other executed entity to compare against  
     * @return	TRUE if both, the given and this entities are equal, FALSE otherwise. 
     */
    @JsonIgnore
    public boolean equals(Object o){
    	
    	if (o instanceof ExecutedEntity) { 
	    	ExecutedEntity other = (ExecutedEntity) o;
	    	
	    	// we said that a production order is equal to another if both have the same 
	    	// attributes and their attribute values are also equal. 
	    	if (getId() != other.getId())
	    		return false;
	    	
	    	// Check if both orders have the same attributes.  
			for (int i = 0; i < this.attributes.size(); i++){
				Attribute attr = other.getAttribute(this.attributes.get(i).getName());
				if ((attr == null) || (attr.equals(this.attributes.get(i)) == false)){
					return false;
				}
			}
	
	    	// Check if both orders have the same attributes values.  
			for (int i = 0; i < this.attributeValues.size(); i++){
				AttributeValue attr = other.getAttributeValue(this.attributes.get(i).getName());
				if ((attr == null) || (attr.equals(this.attributeValues.get(i)) == false)){
					return false;
				}
			}
	    	return true;
    	} else {
    		return false;
    	}
    }

                       
}
