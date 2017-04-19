package com.advicetec.messageprocessor.test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

import com.advicetec.MessageProcessor.DelayQueueConsumer;
import com.advicetec.MessageProcessor.DelayQueueProducer;

import org.junit.Test;

public class DelayQueueTest {

	@Test
	public void Test_Blocking_Queue() 
	{
		
		// Creates an instance of blocking queue using the DelayQueue.
		BlockingQueue queue = new DelayQueue();
		
		// Starting DelayQueue Producer to push some delayed objects to the queue 
		Thread t1 = new Thread(new DelayQueueProducer(queue));
		t1.start();
		System.out.println("producer created");
		
		// Starting DelayQueue Consumer to take the expired delayed objects from the queue
		Thread t2 = new Thread(new DelayQueueConsumer("Consumer Thread-1", queue));
		t2.start();
		System.out.println("consumer created");
		
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}