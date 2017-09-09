package com.advicetec.mpmcqueue;

/**
 * This class models a thread consumer from a queue. 
 * It implements the Runnable interface.
 * 
 * @author advicetec
 * @see Runnable
 */
public class ConsumerQueue implements Runnable {

	/**
	 * The queue.
	 */
	private Queue queue;

	/** 
	 * Constructor that initializes the queue
	 * @param queue
	 */
	public ConsumerQueue(Queue queue) {
		super();
		this.queue = queue;
	}

	/** 
	 * Runs method that pops elements from the queue for processing.
	 */
	public void run() {
		while (true){
			try {
				queue.pop();
				// System.out.println("Consuming" + queue.pop() + "  Thread: " + Thread.currentThread().getName() + "queue size: " + queue.size() );
			} catch (InterruptedException e) {
				System.err.println("Error poping elements from the queue:"
						+ e.getMessage());
				e.printStackTrace();
			}
			
		}
		
	}

}
