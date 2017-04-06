package com.advicetec.mpmcqueue;

public class ConsumerQueue implements Runnable {

	private Queue queue;

	public ConsumerQueue(Queue queue) {
		super();
		this.queue = queue;
	}

	public void run() {
		while (true){
			try {
				queue.pop();
				// System.out.println("Consuming" + queue.pop() + "  Thread: " + Thread.currentThread().getName() + "queue size: " + queue.size() );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

}
