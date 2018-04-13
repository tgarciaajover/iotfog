package com.advicetec.language.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Symbol to represent a aggregate scheduled by a user. The AggregateSched construct lets 
 * the user to schedule in the language a recurrence for an aggregate function execution.
 * 
 * @author Jose Pulgarin
 */
public class AggregateSymbol extends Symbol 
{

	/**
	 * Unit of time for the timer  
	 */
	TimeUnit timeUnit;
	
	/**
	 * Number of time units expressed in time units.
	 */
	int tunits;
	
	/**
	 * Repeated.
	 */
	boolean repeated;
	
	/**
	 * It includes all names that are required to refer to the behavior progra,=m.
	 */
	List<String> longName; 
	
	/**
	 * Constructor for the class. It creates the aggregate symbol from the name 
	 * of the aggregation method to execute, and its schedule definition.
	 * 
	 * @param name       Aggregation method name
	 * @param timeUnit	 Unit for time (seconds, minutes, hours)
	 * @param tunits	 Number of unit expressed in units of time.
	 * @param repeated	 It is repeated or it is executed once. 
	 */
	public AggregateSymbol(String name, TimeUnit timeUnit, int tunits, boolean repeated) 
	{ 
		super(name,Type.tVOID); 
		this.timeUnit = timeUnit;
		this.tunits = tunits;
		this.repeated = repeated;
		longName = new ArrayList<String>();
	}

	/**
	 * Gets the unit of time
	 * 
	 * @return time unit
	 */
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	/**
	 * Gets the number of unit of time where the timer should be triggered
	 * 
	 * @return time to trigger the timer.
	 */
	public int getTunits() {
		return tunits;
	}
	
	/**
	 * Adds a part for the name of the behavior program. TThe behavior program name
	 * is composed of many parts divided by the dot symbol "."
	 * 
	 * @param id A part of the behavior name.
	 */
	public void addId(String id) {
		this.longName.add(id);
	}
	
	/**
	 * Returns all parts in the program behavior name.
	 * 
	 * @return a list of name components.
	 */
	public List<String> getCompleteName(){
		return longName;
	}
	
	/**
	 * Gets the delay time in milliseconds
	 * 
	 * @return Milliseconds required to trigger the timer.
	 */
	public long getMilliseconds(){
		long valReturn = 0; 
		if (timeUnit == TimeUnit.SECONDS){
			valReturn = TimeUnit.SECONDS.toMillis(tunits);
		} else if (timeUnit == TimeUnit.MINUTES){
			valReturn = TimeUnit.MINUTES.toMillis(tunits);
		} else if (timeUnit == TimeUnit.HOURS){
			valReturn = TimeUnit.HOURS.toMillis(tunits);
		}
		
		return valReturn;
	}
	
	/**
	 * is repeated or or?
	 * 
	 * @return repeated.
	 */
	public boolean getRepeated(){
		return this.repeated;
	}
}
