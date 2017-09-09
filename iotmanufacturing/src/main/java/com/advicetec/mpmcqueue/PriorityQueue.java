package com.advicetec.mpmcqueue;

import java.lang.reflect.Array;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.advicetec.eventprocessor.Event;
import com.advicetec.eventprocessor.EventHandler;

/**
 * Models a queue with priorities. It implements a template to queue any type 
 * of object.
 * This queue is used by the handlers that process Event, Message and adapters.
 * This queue allows blocking to execute operation in thread safe mode.
 * It uses a set of queues, represented by the constant MAX_PRIORITIES. 
 * Each queue operates objects depending on the priority of the queued object.
 * 
 * @author advicetec
 *
 * @param <T> generic object to queue.
 * @see EventHandler
 */
public class PriorityQueue<T> 
{
	/**
	 * The priority is implemented with an array of queues.
	 * Each priority has a unique queue to deal with objects of that
	 * priority.
	 */
	private Queue<T>[] queuePriorities;
	/**
	 * Queue length monitor counter.
	 */
	//TODO this should be private?
	 int count;
	/**
	 * Locks to permit atomic push and pop.
	 */
	final Lock lock = new ReentrantLock();
	final Condition notFull = lock.newCondition();
	final Condition notEmpty = lock.newCondition();
	
	
	/**
	 * Defines the priority. The maximum priority is 0.
	 *  
	 */
	private static final int MAX_PRIORITIES = 7;
	
	/**
	 * Priority Queue constructor. Initializes the ammount of elements in 0. 
	 * @param type Type of object to be queued.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public PriorityQueue(Class<T> type) 
	{
		queuePriorities = (Queue<T>[]) Array.newInstance(Queue.class,MAX_PRIORITIES);
		
		for (int i = 0; i < MAX_PRIORITIES; i++) {
			this.queuePriorities[i] =  new Queue(type);
		}
		
		count = 0;
	}

	/**
	 * This method adds a element to the queue. The priority and the object are
	 * given by parameter and queue the respective object into the queue based
	 * on the priority.
	 * @param priority number that defines the priority queue to be inserted in.
	 * This value varies from 0 to MAX_PRIORITIES constant.
	 * @param obj The object to be queued with the respective priority. 
	 * @throws InterruptedException If the priority value is not in the range 
	 * from 0 to MAX_PRIORITIES constant.
	 */
	public void enqueue(int priority, T obj ) throws InterruptedException {
		// locks the queue
		lock.lock();
		try {
			// checks correct value for priority and throws an exception
			if ((priority >= MAX_PRIORITIES) || (priority < 0)) {
		        throw new ArrayIndexOutOfBoundsException("Prioities should be between 0 and " + MAX_PRIORITIES);
		    }
			queuePriorities[priority].push(obj);
			// increments the number of elements in the queue.
			// TODO a counter by queue.
			// there is not a counter by queue, this is a general counter.
			count++;
			notEmpty.signal();
		} finally {
			lock.unlock();
		}
	
	}

	/**
	 * Pops an element from the queue.
	 * @return The element in the output end.
	 * @throws InterruptedException if the thread is interrupted.
	 */
	public T pop() throws InterruptedException{
		// locks the queue
		lock.lock();
		try{
			T obj = null;
			// if there are not elements in the queue awaits.
			// once the number of elements
			while (count == 0)
				notEmpty.await();
			// visit each queue for elements.
			for (int i = 0; i < queuePriorities.length; i++) {
				obj = queuePriorities[i].nonwait_pop();
				if ( obj != null ){
					break;
				}
			}
			// after pops an element decreases the general counter of elements.
			count--;
			notFull.signal();
			return obj;

		} finally {
			// release the lock
			lock.unlock();
		}
	}

	/**
	 * Returns an object from the queue with the specified priority.
	 * @param priority value and id of queue.
	 * @return the object from the queue with the specified priority.
	 * @throws InterruptedException if the thread is interrupted.
	 */
	public T pop(int priority) throws InterruptedException{

		if ((priority >= MAX_PRIORITIES) || (priority < 0)) {
	        throw new ArrayIndexOutOfBoundsException("Priority must be between 0 and " + MAX_PRIORITIES);
	    }
		// returns the object from the specified queue.
		return queuePriorities[priority].pop();
	}
	
	/**
	 * Returns an array of integers with the number of elements of each queue.
	 * @return an array of integers with the number of elements of each queue
	 */
	public int[] size(){
		int arrayReturn[] = new int[MAX_PRIORITIES];
		
		for (int i = 0; i < queuePriorities.length; i++) {
			arrayReturn[i] = queuePriorities[i].size();
		}
		
		return arrayReturn;
	}

}
