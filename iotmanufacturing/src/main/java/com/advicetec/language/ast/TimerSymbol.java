package com.advicetec.language.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimerSymbol extends Symbol 
{

	TimeUnit timeUnit;  // 
	int tunits; 		// Number of units expressed in timeunits.  
	boolean repeated;   // Repeated.
	List<String> longName; // it includes all the names that are required to refer to the behavior.
	
	public TimerSymbol(String name, TimeUnit timeUnit, int tunits, boolean repeated) 
	{ 
		super(name,Type.tVOID); 
		this.timeUnit = timeUnit;
		this.tunits = tunits;
		this.repeated = repeated;
		longName = new ArrayList<String>();
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public int getTunits() {
		return tunits;
	}
	
	public void addId(String id) {
		this.longName.add(id);
	}
	
}
