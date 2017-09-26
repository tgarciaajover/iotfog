package com.advicetec.measuredentitity;

import java.io.IOException;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * This enumeration denotes possible states of a Measured Entity.
 * Basically, the measured entity may be in one of the following states:
 * {Operating, ScheduledDown, UnscheduleDown, Undefined}.
 * Each state is also represented in this enumeration with an id.
 * 
 * @author advicetec
 *
 */
public enum MeasuringState 
{
	// possible states.
	OPERATING(0,"Operating"),
	SCHEDULEDOWN(1,"ScheduleDown"),
	UNSCHEDULEDOWN(2,"UnScheduleDown"),
	UNDEFINED(3, "Undefined");

	@JsonProperty 
	private int value;
	@JsonProperty 
	private String name;

	private MeasuringState(int value, String name) {
		this.value = value;
		this.name = name;
	}

	/**
	 * Returns the value (id) of this state.
	 * @return value (id) of this state.
	 */
	public int getValue() {
		return this.value;
	}

	/**
	 * Returns the name/description of this measuring state.
	 * @return the name/description of this measuring state.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the Measuring State reference for the given value.
	 * @param value (id) of this state
	 * @return the Measuring State reference for the given value.
	 */
	static public MeasuringState getByValue(int value){
		return values()[value];
	}

	/**
	 * Returns the Measuring State reference for the given name/description.
	 * @param name the state's name.
	 * @return the Measuring State reference for the given name.
	 */
	static public MeasuringState getByName(String name){
		name = name.trim();

		if (name.equalsIgnoreCase("Operating")){
			return OPERATING;
		} else if ( name.equalsIgnoreCase("ScheduleDown") ) {
			return SCHEDULEDOWN;
		} else if(name.equalsIgnoreCase("UnScheduleDown")){
			return UNSCHEDULEDOWN;
		} else {
			return UNDEFINED;
		}
	}

	/**
	 * Returns the JSON representation of this measuring state.
	 * @return the JSON representation of this measuring state.
	 */
	public String toJson(){
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			System.err.println("Cannot serialize the MeasuringState object.");
			e.printStackTrace();
		}
		return json;
	}
}
