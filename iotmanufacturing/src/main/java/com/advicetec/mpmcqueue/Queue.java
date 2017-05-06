package com.advicetec.mpmcqueue;

import java.lang.reflect.Array;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

	final Lock lock = new ReentrantLock();
	final Condition notFull = lock.newCondition();
	final Condition notEmpty = lock.newCondition();

	// it must be a power of two. In this case: 2^18.
	private static final int MAX_QUEUE_LEN = 262144;

	// the mask to be used to calculate the queue size.
	private static final int Q_MASK = MAX_QUEUE_LEN - 1;

	public Queue(Class<T> c){
		vector = (T[]) Array.newInstance(c,MAX_QUEUE_LEN);
		count = 0;
		tail =0;
		head = 0;
	}

	public void push(T obj) throws InterruptedException{
		lock.lock();

		try{
			while (count == MAX_QUEUE_LEN){
				notFull.await();
			}
			vector[head] = obj;
			if (++head == MAX_QUEUE_LEN) head = 0;
			++count;
			notEmpty.signal();

		} finally {
			lock.unlock();
		}
	}

	public T pop() throws InterruptedException {
		lock.lock();
		try{

			while (count == 0) 
				notEmpty.await();
			T obj = vector[tail];
			if (++tail == MAX_QUEUE_LEN) tail=0;
			count--;
			notFull.signal();
			return obj;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * It can be used when the caller does not wait for a new entry in the queue. In particular, it is used in the priority queue.
	 * @return
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


	public T getHead(){
		return vector[head++ & Q_MASK];
	}

	public T getTail(){
		return vector[tail++ & Q_MASK];
	}

	public int size(){
		return count;
	}
}
