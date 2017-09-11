package com.advicetec.core;

import com.advicetec.mpmcqueue.PriorityQueue;
import com.advicetec.mpmcqueue.Queueable;

/**
 * This class implements a priority queue and it is configurable for processing properties files.
 * 
 * @author Andres Marentes
 *
 */
public abstract class Manager extends Configurable
{
	/**
	 * priority queue to maintain objects to be processed. 
	 * A priority queue has seven different queues where priority zero is the greatest priority and six is the last one. 
	 */
	protected PriorityQueue<Queueable> queue;
	
	/**
	 * Constructor for the class, it requires the file name of the properties file to read. 
	 * @param filename  file name of the properties file to read.
	 */
	public Manager(String filename) {
		super(filename);
		this.queue = new PriorityQueue<Queueable>(Queueable.class);
	}
	
	/**
	 * Gets a reference to the priority queue.
	 *   
	 * @return priority queue reference 
	 */
	public PriorityQueue<Queueable> getQueue()
	{
		return this.queue;
	}
}
