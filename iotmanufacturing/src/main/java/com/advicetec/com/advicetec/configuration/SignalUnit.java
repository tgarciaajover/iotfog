package com.advicetec.configuration;

import java.time.LocalDateTime;

public class SignalUnit extends ConfigurationObject
{
	private String descr;
	private LocalDateTime create_date;
	
	public SignalUnit(Integer id) {
		super(id);
	}

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
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
