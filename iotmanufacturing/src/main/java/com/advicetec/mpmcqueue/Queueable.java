package com.advicetec.mpmcqueue;

import java.time.LocalDateTime;

/**
 * This is a wrapper class that creates an homogeneous object to queue and pop.
 * The wrapper defines a kind of object with a <code>QueueType</code>
 * @author advicetec
 *
 * @see QueueType
 */
public class Queueable 
{
	/**
	 * Type of object into this wrap.
	 */
	private QueueType type;
	private Object content;
	private LocalDateTime createDate;
	/**
	 * Returns the type of object under this wrap.
	 * @return the type of object under this wrap.
	 */

	public QueueType getType() {
		return type;
	}

	/**
	 * Sets a type from <code>QueueType</code> that describe the content.
	 * @see QueueType
	 */
	public void setType(QueueType type) {
		this.type = type;
	}

	/**
	 * Returns the object included on this wrap.
	 * @return the object included on this wrap.
	 */
	public Object getContent() {
		return content;
	}

	/**
	 * Sets a content to this wrap.
	 * @param content The object to be homogenized.
	 */
	public void setContent(Object content) {
		this.content = content;
	}

	/**
	 * Constructor of a wrapper for the specified content. The type is used to 
	 * recover the original content.
	 * @param type Type of queued object into this wrap.
	 * @param content original object before is wrapped.
	 * @see QueueType
	 */
	public Queueable(QueueType type, Object content) {
		super();
		this.type = type;
		this.content = content;
		this.createDate = LocalDateTime.now();
	}

	public LocalDateTime getDateTime(){
		return this.createDate;
	}
	
	public String toString(){
		return "type: " + type + " content: " + content;
	}
}