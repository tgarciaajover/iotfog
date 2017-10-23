package com.advicetec.measuredentitity;

import java.time.LocalDateTime;

import com.advicetec.configuration.ReasonCode;

public class EntityState {

	/**
	 * state of the current interval.
	 */
	protected MeasuringState state;

	/**
	 * the reason code for the current state.
	 */
	protected ReasonCode reason;

	/**
	 * start date-time of the current interval.
	 */
	protected LocalDateTime startDateTimeStatus;

	EntityState(MeasuringState currentState, ReasonCode reason, LocalDateTime datetime ){
		
		this.state = currentState;
		this.reason = reason;
		this.startDateTimeStatus = datetime;
		
	}

	public void update(MeasuringState currentState, ReasonCode reason, LocalDateTime dateTime) {
		this.state = currentState;
		this.reason = reason;
		this.startDateTimeStatus = dateTime;		
	}
	
	public MeasuringState getState() {
		return state;
	}

	public void setState(MeasuringState state) {
		this.state = state;
	}

	public ReasonCode getReason() {
		return reason;
	}

	public void setReason(ReasonCode reason) {
		this.reason = reason;
	}

	public LocalDateTime getStartDateTimeStatus() {
		return startDateTimeStatus;
	}

	public void setStartDateTimeStatus(LocalDateTime startDateTimeStatus) {
		this.startDateTimeStatus = startDateTimeStatus;
	}
	
}
