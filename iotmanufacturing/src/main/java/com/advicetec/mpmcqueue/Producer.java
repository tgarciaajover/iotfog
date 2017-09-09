package com.advicetec.mpmcqueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.MessageHandler;
import com.advicetec.eventprocessor.EventHandler;
import com.advicetec.monitorAdapter.AdapterHandler;

/**
 * This class models a producer object that inserts objects into the queue.
 * It implements the runnable interface to create multiple thread instances.
 * It is the model for other producer/consumer structure on this project.
 * 
 * @author advicetec
 * @see MessageHandler
 * @see AdapterHandler
 * @see EventHandler
 */
public class Producer implements Runnable {
	
	static Logger logger = LogManager.getLogger(Producer.class.getName());
	/**
	 * Queue instance to produce elements.
	 */
	private PriorityQueue<Queueable> queue;

	private int itemsToProduce;

	public Producer(PriorityQueue<Queueable> queue, int items) {
		super();
		this.queue = queue;
		this.itemsToProduce = items;
	}

	/**
	 * Thread run method. It creates a number of items and queued them.
	 * The element's type is <code>Queueable</code>. If there is an exception,
	 * this method catches it and reports the message.
	 * @see Queueable 
	 * 
	 */
	public void run( ) {

		try {
			for (int i = 0; i < this.itemsToProduce; i++) {
				int priority = (int) Math.round((Math.random() * 6));

				if (priority == 0) priority = 1;
				// create and queue the object wrapper.
				Queueable obj = new Queueable(QueueType.GENERIC, new Integer(i));
				queue.enqueue(priority, obj);
			}	
		} catch (InterruptedException e) {
			logger.debug("Interrpution:"+e.getMessage());
			e.printStackTrace();
		}
	}

}
