package com.advicetec.mpmcqueue;

public class Producer implements Runnable {

	private PriorityQueue<Queueable> queue;
	private int itemsToProduce;
	
		
	public Producer(PriorityQueue<Queueable> queue, int items) {
		super();
		this.queue = queue;
		this.itemsToProduce = items;
	}


	public void run( ) {

		try {

			for (int i = 0; i < this.itemsToProduce; i++) {
		    	int priority = (int) Math.round((Math.random() * 6));
		    	
		    	if (priority == 0) priority = 1;
		    	
				Queueable obj = new Queueable(QueueType.GENERIC, new Integer(i));
					queue.enqueue(priority, obj);
			}	
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
