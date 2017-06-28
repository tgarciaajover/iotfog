package com.advicetec.mpmcqueue;

public class Consumer implements Runnable {

	private PriorityQueue queue;

	public Consumer(PriorityQueue queue) {
		super();
		this.queue = queue;
	}

	public void run() {
		int count = 0;
		try {

			while (true){
					queue.pop();
					count++;
					if (count % 200 == 0) 
						// System.out.println("Consumed:" + count + "  Thread: " + Thread.currentThread().getName() );
						
				    //System.out.println("Consuming" + queue.pop() + "  Thread: " + Thread.currentThread().getName() );
				
			}

		} catch (InterruptedException e) {
			//System.out.println("parando" + count);
			e.printStackTrace();
		}
			
	}

}
