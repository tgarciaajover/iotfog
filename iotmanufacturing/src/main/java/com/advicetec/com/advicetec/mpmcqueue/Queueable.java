package com.advicetec.mpmcqueue;

public class Queueable 
{
	
	private QueueType type;
	private Object content;
	
	public QueueType getType() {
		return type;
	}

	/*
	 * 
	 */
	public void setType(QueueType type) {
		this.type = type;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public Queueable(QueueType type, Object content) {
		super();
		this.type = type;
		this.content = content;
	}

	public String toString(){
		return "type: " + type + " content: " + content;
	}
	
}
