package com.advicetec.measuredentitity;

import java.io.IOException;
import java.time.LocalDateTime;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.configuration.ConfigurationObject;
import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;
import com.advicetec.core.serialization.MeasuringStateSerializer;
import com.advicetec.core.serialization.MeasuringStateDeserializer;


public class MeasuredEntityStateTransition extends ConfigurationObject 
{

	@JsonProperty("state_from")
	@JsonSerialize(using = MeasuringStateSerializer.class)
	@JsonDeserialize(using = MeasuringStateDeserializer.class)	
	private MeasuringState stateFrom;
	
	@JsonProperty("reason_code")
	private int reasonCode;

	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
	private LocalDateTime createDate;

	
	@JsonProperty("behavior")
	private int behavior;  
	
	public MeasuredEntityStateTransition(@JsonProperty("id") Integer id) {
		super(id);
	}

	public MeasuringState getStateFrom() {
		return stateFrom;
	}

	public void setStateFrom(MeasuringState stateFrom) {
		this.stateFrom = stateFrom;
	}

	public int getResonCode() {
		return reasonCode;
	}

	public void setResonCode(int resonCode) {
		this.reasonCode = resonCode;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	public int getBehavior() {
		return behavior;
	}

	public void setBehavior(int behavior) {
		this.behavior = behavior;
	}

	public String toJson()
	{
		ObjectMapper mapper = new ObjectMapper();
			
		String jsonInString=null;
		try {
			
			jsonInString = mapper.writeValueAsString(this);
			
			
		} catch (JsonGenerationException e) {
			// TODO: log the error
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return jsonInString;
	}
}
