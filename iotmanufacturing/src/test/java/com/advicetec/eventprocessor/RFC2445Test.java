package com.advicetec.eventprocessor;

// A compatibility layer for joda-time
import com.google.ical.compat.javautil.DateIteratorFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
// A Joda time class that represents a day regardless of timezone
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;


public class RFC2445Test {

	  
	  /** print the first 13 Friday the 13ths in the 3rd millenium AD. */
	  @Test
	  public void TestLocalDateTime() throws java.text.ParseException {
	    Date start =  new GregorianCalendar(2003, Calendar.APRIL, 13).getTime(); 
	    			    
	    // Every friday the thirteenth.
	    String ical = "RRULE:FREQ=MONTHLY"
	                  + ";BYDAY=FR"  // every Friday
	                  + ";BYMONTHDAY=13"  // that occurs on the 13th of the month
	                  + ";COUNT=13";  // stop after 13 occurences

	    TimeZone tz = TimeZone.getDefault();
	    
	    // Print out each date in the series.
	    for (Date date :
	    	DateIteratorFactory.createDateIterable(ical, start, tz, true)) {
	      System.out.println(date);
	      
	      Instant instant = date.toInstant();
	      ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
	      LocalDate localDate = zdt.toLocalDate();
	      
	      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");
	      String formattedString = localDate.format(formatter);
	      
	      System.out.println("LocalDate:" + formattedString);
	      
	    }
	  }	  
	
}
