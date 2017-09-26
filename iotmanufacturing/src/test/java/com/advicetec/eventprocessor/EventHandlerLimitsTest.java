package com.advicetec.eventprocessor;

import static org.junit.Assert.assertEquals;

import java.util.stream.Collector;

import org.junit.Test;

import com.advicetec.mpmcqueue.PriorityQueue;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

public class EventHandlerLimitsTest 
{
	@Test
	public void UnlimitedHandlers() throws InterruptedException{
		
		EventManager eventManager = EventManager.getInstance();
		eventManager.run();
		
		PriorityQueue<Queueable> queue = eventManager.getQueue();
		
		int maxActive = 0;
		for (int i= 0; i < 100; i++) {
			Event event = new TestEvent(i);
			Queueable queuable = new Queueable(QueueType.EVENT, event);
			queue.enqueue(6, queuable);
			Thread.sleep(5);
			int currentactive = eventManager.getActiveProcessingHandlers(EventType.TEST);
			if (currentactive > maxActive) {
				maxActive = currentactive; 
			}
		}
		
		// It waits that the queue gets empty
		int size = 0;
		do {
			Thread.sleep(100);
			size = 0;
			int[] sizes = queue.size();
			for (int i=0; i < sizes.length; i++){
				size = size + sizes[i];  
			}
			
		} while ((size > 0) || (eventManager.getActiveProcessingHandlers(EventType.TEST) > 0));
			
		String numProcessHandlers = eventManager.getProperty("NumProcessHandlers");
		
		assertEquals(Integer.parseInt(numProcessHandlers), maxActive);
			
	}
	
	@Test
	public void CannotProcessHandlers() throws InterruptedException{
		EventManager eventManager = EventManager.getInstance();
		
		PriorityQueue<Queueable> queue = eventManager.getQueue();
		eventManager.setProcessingLimit(EventType.TEST, 0);
		eventManager.run();

		int maxActive = 0;
		for (int i= 0; i < 100; i++) {
			Event event = new TestEvent(i);
			Queueable queuable = new Queueable(QueueType.EVENT, event);
			queue.enqueue(6, queuable);
			Thread.sleep(5);
			int currentactive = eventManager.getActiveProcessingHandlers(EventType.TEST);
			if (currentactive > maxActive) {
				maxActive = currentactive; 
			}
		}
		
		assertEquals(0, maxActive);

	}

	@Test
	public void LimitedHandlers() throws InterruptedException{

		EventManager eventManager = EventManager.getInstance();
		eventManager.run();
		
		PriorityQueue<Queueable> queue = eventManager.getQueue();
		eventManager.setProcessingLimit(EventType.TEST, 6);

		int maxActive = 0;
		for (int i= 0; i < 100; i++) {
			Event event = new TestEvent(i);
			Queueable queuable = new Queueable(QueueType.EVENT, event);
			queue.enqueue(6, queuable);
			Thread.sleep(5);
			int currentactive = eventManager.getActiveProcessingHandlers(EventType.TEST);
			if (currentactive > maxActive) {
				maxActive = currentactive; 
			}
		}

		// It waits that the queue gets empty
		int size = 0;
		do {
			Thread.sleep(100);
			int[] sizes = queue.size();
			size = 0;
			for (int i=0; i < sizes.length; i++){
				size = size + sizes[i];  
			}
		} while ((size > 0) || (eventManager.getActiveProcessingHandlers(EventType.TEST) > 0));

		assertEquals(6, maxActive);

	}

}
