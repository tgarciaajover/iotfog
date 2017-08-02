package com.advicetec.eventprocessor;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.measuredentitity.MeasuredEntityContainer;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.google.ical.compat.javautil.DateIteratorFactory;


public class AggregationEvent extends Event
{

	// Type of Aggregation Event type
	private AggregationEventType type;

	// Measuring entity for the aggregation.
	private int measuredEntity;
	
	// Establishes the time of measured entity.
	MeasuredEntityType ownerType;
	
	// Recurrence Rule
	private String recurrence; 

	public AggregationEvent(int measuredEntity, MeasuredEntityType ownerType,
			AggregationEventType type, String recurrence) {
		super(EventType.AGGREGATION_EVENT);
		this.measuredEntity = measuredEntity;
		this.type = type;
		this.ownerType = ownerType;
		this.recurrence = recurrence;
	}
	
	public AggregationEventType getType() {
		return type;
	}

	public int getEntity() {
		return measuredEntity;
	}

	public MeasuredEntityType getOwnerType() {
		return ownerType;
	}

	public String getKey(){
		return getEvntType().getName() + "-" + getEntity() + "-" + getType();
	}
	
	public String getRecurrence() {
		return recurrence;
	}

	public long getSecondsToNextExecution() {
		
		long seconds = 0;
				
		try {
			
			TimeZone tz = TimeZone.getDefault();
			
			Date start =  new Date();
			
			for (Date date : DateIteratorFactory.createDateIterable(recurrence, start, tz, true)) {
			  
			  Instant instant = date.toInstant();
			  ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
			  LocalDate localDate = zdt.toLocalDate();
			  LocalDate today = LocalDate.now();
			  
			  seconds = ChronoUnit.SECONDS.between(today, localDate);
			  
			}
			
		} catch (ParseException e) {
			Logger logger = LogManager.getLogger(AggregationEvent.class.getName());
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return seconds;
	}
}
