package com.advicetec.eventprocessor;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
	
	static Logger logger = LogManager.getLogger(AggregationEvent.class.getName());
	
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
		
		logger.info("In getSecondsToNextExecution");
		
		long seconds = 0;
				
		try {
			
			TimeZone tz = TimeZone.getDefault();
			
			Date start =  new Date();
			
			for (Date nextRecurrenceDate : DateIteratorFactory.createDateIterable(recurrence, start, tz, true)) {
			  
			  Instant instant = nextRecurrenceDate.toInstant();
			  ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
			  LocalDate localNextRecurrDate = zdt.toLocalDate();
			  
			  LocalDateTime localDateTimeNextRecurrDate = localNextRecurrDate.plusDays(1).atStartOfDay();
			  LocalDateTime today = LocalDateTime.now();
			  
			  seconds = today.until( localDateTimeNextRecurrDate, ChronoUnit.SECONDS);

			  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			  String formattedFrom = localDateTimeNextRecurrDate.format(formatter);			  
			  logger.info("Next Recurrence Date:" + formattedFrom + " getSecondsToNextExecution seconds:" + seconds);

			  break;
			  
			}
			
		} catch (ParseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		logger.info("getSecondsToNextExecution seconds:" + seconds);
		
		return seconds;
	}
}
