package com.advicetec.persistence;

public class DowntimeReason {
	
	private String reason;
	private Integer occurrences;
	private Double minDuration;
	private String machine;
	private String description;
	
	public DowntimeReason(String machine, String reason,String description,Integer occurrences,Double minDuration ) {
		this.reason = reason;
		this.occurrences = occurrences;
		this.minDuration = minDuration;
		this.machine = machine;
		this.description = description;
	}
	
	public String getReasonDescr(){
		return description;
	}
	
	public String getMachine(){
		return machine;
	}
	
	public String getReason(){
		return reason;
	}
	
	public Integer getOccurrences(){
		return occurrences;
	}
	
	public Double getDurationMinutos(){
		return minDuration;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setOccurrences(Integer occurrences) {
		this.occurrences = occurrences;
	}

	public void setMinDuration(Double minDuration) {
		this.minDuration = minDuration;
	}

	public void setMachine(String machine) {
		this.machine = machine;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
