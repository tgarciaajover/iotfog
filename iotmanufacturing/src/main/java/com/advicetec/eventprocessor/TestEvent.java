package com.advicetec.eventprocessor;

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
	
}
