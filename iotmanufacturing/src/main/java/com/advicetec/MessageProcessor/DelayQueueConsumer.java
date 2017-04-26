package com.advicetec.MessageProcessor;

import java.util.concurrent.BlockingQueue;

import com.advicetec.eventprocessor.EventManager;

public class DelayQueueConsumer implements Runnable 
{
	
	private String name;
    private BlockingQueue queue;
    private EventManager eventManager;

	public DelayQueueConsumer(String name, BlockingQueue queue) 
	{
		super();
		this.name = name;
		this.queue = queue;
		this.eventManager = EventManager.getInstance();
	}
	
	public void run() 
	{
		while (true) {
			try {
				// Take elements out from the DelayQueue object.
				DelayEvent object = (DelayEvent) queue.take();
				
				// TODO: to define the priority.
				this.eventManager.getQueue().enqueue(7, object.getEvent());
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}