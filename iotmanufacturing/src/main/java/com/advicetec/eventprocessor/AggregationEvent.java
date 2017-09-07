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

import com.advicetec.measuredentitity.MeasuredEntityType;
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
	private int measuredEntity;
	
	/**
	 * Establishes the time of measured entity.
	 */
	MeasuredEntityType ownerType;
	
	/**
	 * Recurrence String Rule, it defines how often the event is run. By the time, we only support recurrences occurring at the day level.
	 */
	private String recurrence;
	
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
			AggregationEventType type, String recurrence) {
		
		super(EventType.AGGREGATION_EVENT, 
					EventType.AGGREGATION_EVENT.getName() + "-" + 
						Integer.toString(measuredEntity) + "-" + type.getName());
		
		this.measuredEntity = measuredEntity;
		this.type = type;
		this.ownerType = ownerType;
		this.recurrence = recurrence;
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
	public int getEntity() {
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
		
		logger.debug("getSecondsToNextExecution seconds:" + seconds);
		
		return seconds;
	}
	
	/**
	 * Gets the measured entity for which this aggregation event was created.
	 * 
	 *  @return Measured entity identifier.
	 */
	@Override
	public Integer getMeasuredEntity()
	{
		return new Integer(this.measuredEntity);
	}

}
