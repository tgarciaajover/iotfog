package com.advicetec.applicationAdapter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.configuration.ConfigurationObject;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;
import com.advicetec.measuredentitity.AttributeMeasuredEntity;
import com.advicetec.measuredentitity.ExecutedEntity;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityBehavior;
import com.advicetec.measuredentitity.MeasuredEntityStateBehavior;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.MeasuringState;

public class ProductionOrder extends ExecutedEntity
{

    public ProductionOrder(@JsonProperty("id") Integer id) 
    {
    	super(id, MeasuredEntityType.JOB);
	}

}
