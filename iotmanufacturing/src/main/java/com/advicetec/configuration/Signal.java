package com.advicetec.configuration;

import java.time.LocalDateTime;

public class Signal extends ConfigurationObject
{

	private SignalUnit unit;
	private SignalType type;
	private String descr;
	private LocalDateTime create_date;
	
	public Signal(Integer id) {
		super(id);
	}
	
	public SignalUnit getUnit() {
		return unit;
	}
	public void setUnit(SignalUnit unit) {
		this.unit = unit;
	}
	public SignalType getType() {
		return type;
	}
	public void setType(SignalType type) {
		this.type = type;
	}
	public String getDescr() {
		return descr;
	}
	public void setDescr(String descr) {
		this.descr = descr;
	}
	public LocalDateTime getCreate_date() {
		return create_date;
	}
	public void setCreate_date(LocalDateTime create_date) {
		this.create_date = create_date;
	}
	
}
