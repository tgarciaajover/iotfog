package com.advicetec.mpmcqueue;

public class ProducerQueue implements Runnable {

	private Queue<Queueable> queue;
	private int itemsToProduce;
	
		
	public ProducerQueue(Queue<Queueable> queue, int items) {
		super();
		this.queue = queue;
		this.itemsToProduce = items;
	}



	public void run( ) {
	    for (int i = 0; i < this.itemsToProduce; i++) {
			Queueable obj = new Queueable(QueueType.GENERIC, new Integer(i));
			try {
				queue.push(obj);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}

	
	
}
