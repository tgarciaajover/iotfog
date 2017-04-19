package com.advicetec.MessageProcessor;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class DelayQueueProducer implements Runnable {
	
    // Creates an instance of blocking queue using the DelayQueue.
    private BlockingQueue queue;

	private final Random random = new Random();
	
	public DelayQueueProducer(BlockingQueue queue) 
	{
		super();
		this.queue = queue;
	}
	
	public void run() {
		while (true) {
			try {

				// Put some Delayed object into the DelayQueue.
				int delay = random.nextInt(10000);
				DelayEvent object = new DelayEvent(
						UUID.randomUUID().toString(), delay);

				System.out.printf("Put object = %s%n", object);
				queue.put(object);
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}