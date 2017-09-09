package com.advicetec.mpmcqueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class models a queue producer.
 * t implements the Runnable interface.
 * 
 * @author advicetec
 *
 */
public class ProducerQueue implements Runnable {

	static Logger logger = LogManager.getLogger(ProducerQueue.class.getName());

	/**
	 * The queue.
	 */
	private Queue<Queueable> queue;
	/**
	 * Items to be automatically produced and queued.
	 */
	private int itemsToProduce;

	/**
	 * Constructor of producers with the number of elemets to produce as task.
	 * @param queue reference to operate Queable objects.
	 * @param items The number of items to create.
	 * @see Queueable
	 */
	public ProducerQueue(Queue<Queueable> queue, int items) {
		super();
		this.queue = queue;
		this.itemsToProduce = items;
	}

	/** 
	 * Runs method that insert elements to the queue for processing.
	 */
	public void run( ) {
		for (int i = 0; i < this.itemsToProduce; i++) {
			Queueable obj = new Queueable(QueueType.GENERIC, new Integer(i));
			try {
				queue.push(obj);
			} catch (InterruptedException e) {
				logger.debug("Interruption:"+e.getMessage());
				e.printStackTrace();
			}
		}	
	}	
}
