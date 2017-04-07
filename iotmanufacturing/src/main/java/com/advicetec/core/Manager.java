package com.advicetec.core;

import com.advicetec.mpmcqueue.PriorityQueue;
import com.advicetec.mpmcqueue.Queueable;

/**
 * This class implements a priority queue and is also
 * configurable for processing properties files.
 * 
 * @author user
 *
 */
public abstract class Manager extends Configurable
{
	protected PriorityQueue queue;
	
	
	public Manager(String filename) {
		super(filename);
		this.queue = new PriorityQueue(Queueable.class);
	}
	
	
	public PriorityQueue getQueue()
	{
		return this.queue;
	}
}
