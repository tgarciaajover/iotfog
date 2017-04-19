package com.advicetec.MessageProcessor;

import java.util.concurrent.BlockingQueue;

public class DelayQueueConsumer implements Runnable 
{
	
	private String name;
	
    private BlockingQueue queue;

	public DelayQueueConsumer(String name, BlockingQueue queue) 
	{
		super();
		this.name = name;
		this.queue = queue;
	}
	
	public void run() {
		while (true) {
			try {
				// Take elements out from the DelayQueue object.
				DelayEvent object = (DelayEvent) queue.take();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}