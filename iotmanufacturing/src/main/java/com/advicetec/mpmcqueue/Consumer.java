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
			}

		} catch (InterruptedException e) {
			//System.out.println("parando" + count);
			e.printStackTrace();
		}
			
	}

}
