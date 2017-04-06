package com.advicetec.eventprocessor;

import com.advicetec.mpmcqueue.PriorityQueue;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

public class EventHandler implements Runnable
{

	private PriorityQueue queue;
	
	public EventHandler(PriorityQueue queue) {
		super();
		this.queue = queue;
	}

	public void run() {

		try {

			while (true)
			{
				Queueable obj = (Queueable) queue.pop();
				Event evnt = (Event) obj.getContent();
				
				switch (evnt.getEvntType())
				{
				
				case MEASURING_ENTITY_EVENT:
				    MeasuredEntityEvent measuEntyEvt = (MeasuredEntityEvent) evnt;
				    MeasuredEntityEventProcessor processor = new MeasuredEntityEventProcessor(measuEntyEvt);
				    processor.process();
				    break;
				
				case META_MODEL_EVENT:
					// MeasuredEntityEvent measuEntyEvt = (MeasuredEntityEvent) evnt;
					break;
				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	
}
