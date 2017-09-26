package com.advicetec.measuredentitity;

/**
 * This class models a Downtime Reason for the production plan. 
 * @author advicetec
 */
public class DowntimeReason {
	/**
	 * Canonical reason
	 */
	private String reason;
	/** 
	 * occurrence counter
	 */
	private Integer occurrences;
	/**
	 * Duration in minutes of this downtime.
	 */
	private Double minDuration;
	/**
	 * Canonical machine id that experiments the downtime.
	 */
	private String machine;
	/**
	 * Canonical downtime description
	 */
	private String description;
	
	/**
	 * Creates a DowntimeReason from with canonical information.
	 * @param machine Canonical machine id.
	 * @param reason Canonical reason code.
	 * @param description Canonical description.
	 * @param occurrences Number of occurrences of this downtime.
	 * @param minDuration Duration in minutes of this downtime
	 */
	public DowntimeReason(String machine, String reason,String description,
			Integer occurrences,Double minDuration ) {
		
		this.reason = reason;
		this.occurrences = occurrences;
		this.minDuration = minDuration;
		this.machine = machine;
		this.description = description;
	}
	
	/**
	 * Returns canonical reason description.
	 * @return reason description.
	 */
	public String getReasonDescr(){
		return description;
	}
	
	/**
	 * Returns canonical machine id.
	 * @return machine id.
	 */
	public String getMachine(){
		return machine;
	}
	
	/**
	 * Returns the canonical reason id.
	 * @return
	 */
	public String getReason(){
		return reason;
	}
	
	/**
	 * Returns the number of occurrences of this downtime.
	 * @return the number of occurrences of this downtime.
	 */
	public Integer getOccurrences(){
		return occurrences;
	}
	
	/**
	 * Returns the duration in minutes of the downtime.
	 * @return the duration in minutes of the downtime.
	 */
	public Double getDurationMinutos(){
		return minDuration;
	}

	/**
	 * Sets a new canonical reason id.
	 * @param reason canonical reason.
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * Sets the number of occurrences of this downtime reason.
	 * @param occurrences number of occurrences.
	 */
	public void setOccurrences(Integer occurrences) {
		this.occurrences = occurrences;
	}

	/**
	 * Sets downtime duration in minutes.
	 * @param minDuration downtime duration in minutes.
	 */
	public void setMinDuration(Double minDuration) {
		this.minDuration = minDuration;
	}

	/**
	 * Sets canonical machine id.
	 * @param machine canonical machine id.
	 */
	public void setMachine(String machine) {
		this.machine = machine;
	}

	/**
	 * Sets canonical downtime description.
	 * @param description canonical downtime description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
