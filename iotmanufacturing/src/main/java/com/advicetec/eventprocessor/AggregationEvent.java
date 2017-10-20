package com.advicetec.eventprocessor;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.utils.PeriodUtils;
import com.google.ical.compat.javautil.DateIteratorFactory;


/**
 * Class to represent the super class of an aggregation event. 
 * 
 * Aggregation events are events that when processed, they consolidate information in a measured entity, the consolidated information is then associated to the measured entity. 
 * 
 * @author Andres Marentes
 *
 */
public class AggregationEvent extends Event
{

	/**
	 * Type of Aggregation Event type
	 */
	private AggregationEventType type;

	/**
	 * Measuring entity for the aggregation.
	 */
	private Integer measuredEntity;
	
	/**
	 * Establishes the time of measured entity.
	 */
	MeasuredEntityType ownerType;
	
	/**
	 * Recurrence String Rule, it defines how often the event is run. By the time, we only support recurrences occurring at the day level.
	 */
	private String recurrence;
	
	/**
	 * Time when the scheduled event should run.  
	 */
	private LocalTime dayTime;
	
	static Logger logger = LogManager.getLogger(AggregationEvent.class.getName());
	
	/**
	 * Constructor for the class.  
	 * 
	 * @param measuredEntity	measured entity identifier
	 * @param ownerType			measured entity type
	 * @param type				type of aggregation event 
	 * @param recurrence		string defining the recurrence. it follows the RFC 2445.
	 */
	public AggregationEvent(int measuredEntity, MeasuredEntityType ownerType,
			AggregationEventType type, String recurrence, LocalTime dayTime) {
		
		super(EventType.AGGREGATION_EVENT, 
					EventType.AGGREGATION_EVENT.getName() + "-" + 
						Integer.toString(measuredEntity) + "-" + type.getName());
		
		this.measuredEntity = measuredEntity;
		this.type = type;
		this.ownerType = ownerType;
		this.recurrence = recurrence;
		this.dayTime = dayTime;
	}
	
	/**
	 * Gets the type of aggregation event
	 * @return  aggregation event type
	 */
	public AggregationEventType getType() {
		return type;
	}

	/**
	 * Gets the measured entity identifier  
	 * @return measured entity identifier
	 */
	public Integer getEntity() {
		return measuredEntity;
	}

	/**
	 * Gets the measured entity type 
	 * @return measured entity type
	 */
	public MeasuredEntityType getOwnerType() {
		return ownerType;
	}
	
	/**
	 * Gets the string defining the recurrence 
	 * @return recurrence string
	 */
	public String getRecurrence() {
		return recurrence;
	}

	/**
	 * Gets the time when the scheduled event should be run
	 * 
	 * @return time for running.
	 */
	public LocalTime getTime() {
		return dayTime;
	}
	
	/**
	 * Method to calculate the number of seconds from now until the next recurrence should be fired
	 *  
	 * @return Seconds to the next execution.
	 */
	public long getSecondsToNextExecution() {
		
		logger.debug("In getSecondsToNextExecution");
		
		long seconds = 0;
				
		try {
			
			TimeZone tz = TimeZone.getDefault();
			
			Date start =  new Date();
			
			for (Date nextRecurrenceDate : DateIteratorFactory.createDateIterable(recurrence, start, tz, true)) {
			  
			  // gets the date for the next recurrence
			  Instant instant = nextRecurrenceDate.toInstant();
			  ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
			  LocalDate localNextRecurrDate = zdt.toLocalDate();
			  
			  // Combine the date with given time
			  LocalDateTime localDateTimeNextRecurrDate = localNextRecurrDate.atTime(getTime());
			  LocalDateTime today = LocalDateTime.now();
			  
			  // Calculates the seconds to the next recurrence date and time.
			  seconds = today.until( localDateTimeNextRecurrDate, ChronoUnit.SECONDS);
			  
			  // Add another day because the involved date-time has already occurred
			  if (seconds < 0) {
				  seconds = seconds + PeriodUtils.HOURSPERDAY * PeriodUtils.SECONDSPERHOUR;
			  }

			  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			  String formattedFrom = localDateTimeNextRecurrDate.format(formatter);			  
			  logger.debug("Next Recurrence Date:" + formattedFrom + " getSecondsToNextExecution seconds:" + seconds);

			  break;
			  
			}
			
		} catch (ParseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		logger.debug("getSecondsToNextExecution seconds:" + seconds);
		
		return seconds;
	}

}
