package com.advicetec.MessageProcessor;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class UnifiedMessage 
{
	/**
	 * Stores the timestamp when the sample is created
	 */
	private LocalDateTime timestamp;
	private UUID uuid;
	private UnifiedMessageType type;
	
	public UnifiedMessage(UnifiedMessageType type) {
		super();
		this.type = type;
	}

	protected	LocalDateTime getTimestamp() {
		return timestamp;
	}
	
	protected void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	
	protected UUID getUuid() {
		return uuid;
	}
	
	protected void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	
	protected UnifiedMessageType getType() {
		return type;
	}

}
