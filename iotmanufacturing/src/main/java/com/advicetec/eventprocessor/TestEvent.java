package com.advicetec.eventprocessor;

import com.advicetec.measuredentitity.MeasuredEntityType;

public class TestEvent extends Event 
{

	int testid;
	
	public TestEvent(int testId) {
		
		super(EventType.TEST, EventType.TEST.getName() + Integer.toString(testId));
		
		this.testid = testId;
	}
	
	public int getTestId() {
		return testid;
	}

	@Override
	public Integer getEntity() {
		return -1;
	}

	@Override
	public MeasuredEntityType getOwnerType() {
		return MeasuredEntityType.UNDEFINED;
	}
	
	
}
