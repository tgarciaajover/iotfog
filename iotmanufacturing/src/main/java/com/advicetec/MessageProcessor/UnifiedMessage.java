package com.advicetec.MessageProcessor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This abstract class represents the generalization of a Unified Message.
 * In general it has a timestamp, a unique identifier, and a message type.
 * 
 * @author advicetec
 * @see UnifiedMessageType
 */
public abstract class UnifiedMessage 
{
	/**
	 * Stores the timestamp when the mesage, e.g. sample, is created.
	 */
	private LocalDateTime timestamp;
	/**
	 * Unique identifier of this message.
	 */
	private UUID uuid;
	/**
	 * Type of message.
	 */
	private UnifiedMessageType type;
	
	public UnifiedMessage(UnifiedMessageType type) {
		super();
		this.type = type;
	}
	/**
	 * Returns the time and date when the message was created.
	 * @return time and date when the message was created.
	 */
	protected LocalDateTime getTimestamp() {
		return timestamp;
	}
	/**
	 * Sets time and date of creation.
	 * @param timestamp when the message was created.
	 */
	protected void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	
	/**
	 * Returns the unique identifier of this message.
	 * @return unique identifier of this message.
	 */
	protected UUID getUuid() {
		return uuid;
	}
	/**
	 * Sets the unique message identifier
	 * @param uuid unique message identifier.
	 */
	protected void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * Returns this message type. 
	 */
	protected UnifiedMessageType getType() {
		return type;
	}

}
