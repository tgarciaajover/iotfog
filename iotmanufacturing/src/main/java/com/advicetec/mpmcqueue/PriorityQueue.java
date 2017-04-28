package com.advicetec.mpmcqueue;

import java.lang.reflect.Array;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueue<T> 
{
	
	private Queue<T>[] queuePriorities;
	
	int count;
	
	final Lock lock = new ReentrantLock();
	final Condition notFull = lock.newCondition();
	final Condition notEmpty = lock.newCondition();
	
	
	/**
	 * The maximum priority is 0. 
	 */
	private static final int MAX_PRIORITIES = 7;
	
	/**
	 * 
	 * @param c
	 */
	public PriorityQueue(Class<T> c) 
	{
		queuePriorities = (Queue<T>[]) Array.newInstance(Queue.class,MAX_PRIORITIES);
		
		for (int i = 0; i < MAX_PRIORITIES; i++) {
			this.queuePriorities[i] =  new Queue(c);
		}
		
		count = 0;
	}

	/**
	 * 
	 * @param priority
	 * @param obj
	 * @throws InterruptedException
	 */
	public void enqueue(int priority, T obj ) throws InterruptedException {
		lock.lock();
		try {
			if ((priority >= MAX_PRIORITIES) || (priority < 0)) {
		        throw new ArrayIndexOutOfBoundsException("Prioities should be between 0 and " + MAX_PRIORITIES);
		    }
			System.out.println("aqui vamos" + priority);
			queuePriorities[priority].push(obj);
			System.out.println("aqui vamos 1");
			count++;
			notEmpty.signal();
			System.out.println("aqui vamos 2");
		} finally {
			lock.unlock();
		}
	
	}

	public T pop() throws InterruptedException{
		lock.lock();
		try{
			T obj = null;
			while (count == 0)
				notEmpty.await();
			
			for (int i = 0; i < queuePriorities.length; i++) {
				obj = queuePriorities[i].nonwait_pop();
				if ( obj != null ){
					break;
				}
			}
			
			count--;
			notFull.signal();
			return obj;

		} finally {
			lock.unlock();
		}

	}

	public T pop(int priority) throws InterruptedException{

		if ((priority >= MAX_PRIORITIES) || (priority < 0)) {
	        throw new ArrayIndexOutOfBoundsException("Prioities should be between 0 and " + MAX_PRIORITIES);
	    }

		return queuePriorities[priority].pop();
		
	}
	
	public int[] size(){
		
		int arrayReturn[] = new int[MAX_PRIORITIES];
		
		for (int i = 0; i < queuePriorities.length; i++) {
			arrayReturn[i] = queuePriorities[i].size();
			 
		}
		
		return arrayReturn;
		
	}

}
