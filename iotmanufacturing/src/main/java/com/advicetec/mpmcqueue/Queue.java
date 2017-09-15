package com.advicetec.mpmcqueue;

import java.lang.reflect.Array;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Template class that implements a circular queue.
 * This structure uses tail and head pointers represented as index numbers.
 * The length of the circular queue is fixed by the constant MAX_QUEUE_LEN
 * @author advicetec
 *
 * @param <T> the type of elements to queue.
 */
public class Queue<T> 
{

	// queue definition.
	private T vector[];

	// head of the queue
	private int head;

	// last element of the queue
	private int tail;

	// num elements in the queue.
	private int count;

	// locks
	final Lock lock = new ReentrantLock();
	final Condition notFull = lock.newCondition();
	final Condition notEmpty = lock.newCondition();

	// it should be a power of two. In this case: 2^13.
	private static final int MAX_QUEUE_LEN = 8192;

	// the mask to be used to calculate the queue size.
	private static final int Q_MASK = MAX_QUEUE_LEN - 1;

	/**
	 * queues the specified object.
	 * @param type of the object to insert.
	 */
	@SuppressWarnings("unchecked")
	public Queue(Class<T> type){
		vector = (T[]) Array.newInstance(type,MAX_QUEUE_LEN);
		count = 0;
		tail =0;
		head = 0;
	}

	/**
	 * Pushes the specified object into the queue
	 * @param obj element to be pushed into the queue.
	 * @throws InterruptedException If the process is interrupted.
	 */
	public void push(T obj) throws InterruptedException{
		// locks and thread safe
		lock.lock();

		try{
			// awaits if the queue reaches the maximum occupation
			while (count == MAX_QUEUE_LEN){
				notFull.await();
			}
			vector[head] = obj;
			if (++head == MAX_QUEUE_LEN) head = 0;
			++count;
			notEmpty.signal();

		} finally {
			// release the lock
			lock.unlock();
		}
	}

	/**
	 * Pops elements from the queue
	 * @return The element in the end of the queue.
	 * @throws InterruptedException if the process is interrupted.
	 */
	public T pop() throws InterruptedException {
		// locks the queue
		lock.lock();
		try{
			// waits for more elements in the queue
			while (count == 0) 
				notEmpty.await();
			T obj = vector[tail];
			if (++tail == MAX_QUEUE_LEN) tail=0;
			// decrease the monitor of elements in the queue.
			count--;
			notFull.signal();
			return obj;
		} finally {
			// releases the lock
			lock.unlock();
		}
	}

	/**
	 * It can be used when the caller does not wait for a new entry in the 
	 * queue. In particular, it is used in the priority queue.
	 * @return the object at tail.
	 * @throws InterruptedException
	 */
	public T nonwait_pop() throws InterruptedException {
		lock.lock();
		try{	  
			if (count > 0) { 
				T obj = vector[tail];
				if (++tail == MAX_QUEUE_LEN) tail=0;
				count--;
				notFull.signal();
				return obj;
			}
			else {
				return null;
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns the element in the head position.
	 * @return the element from the head position.
	 */
	public T getHead(){
		return vector[head++ & Q_MASK];
	}

	/**
	 * Return the element at the tail position.
	 * @return the element from the tail position.
	 */
	public T getTail(){
		return vector[tail++ & Q_MASK];
	}

	/**
	 * Returns the amount of elements into the queue.
	 * @return the amount of elements into the queue.
	 */
	public int size(){
		return count;
	}
}
