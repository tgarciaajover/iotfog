package com.advicetec.applicationAdapter;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;

import com.advicetec.measuredentitity.ExecutedEntity;
import com.advicetec.measuredentitity.MeasuredEntityType;

/**
 * This class is used to represent the production order. 
 * 
 * @author Andres Marentes
 *
 */


public class ProductionOrder extends ExecutedEntity
{

    /**
     * The constructor of the production order. The type of measuing entity is JOB. 
     * 
     * @param id : The id assigned to the register representing the production order in the FOG database. 
     */
    public ProductionOrder(@JsonProperty("id") Integer id) 
    {
    	super(id, MeasuredEntityType.JOB);
	}

}
