package com.advicetec.mpmcqueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class models a consumer that pops elements from the queue.
 * It implements the runnable interface to create multiple thread instances. 
 * @author advicetec
 *
 */
public class Consumer implements Runnable {
	
	static Logger logger = LogManager.getLogger(Consumer.class.getName());

	/**
	 * Queue instance to consume elements.
	 */
	@SuppressWarnings("rawtypes")
	private PriorityQueue queue;

	/**
	 * Constructs the consumer and 
	 * @param queue
	 */
	@SuppressWarnings("rawtypes")
	public Consumer(PriorityQueue queue) {
		super();
		this.queue = queue;
	}

	/**
	 * Thread run method.
	 */
	public void run() {
		@SuppressWarnings("unused")
		int count = 0;
		try {
			while (true){
				queue.pop();
				count++;				
			}
		} catch (InterruptedException e) {
			logger.debug("Interruption:"+e.getMessage());
			e.printStackTrace();
		}	
	}
}
