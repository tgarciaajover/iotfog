package com.advicetec.eventprocessor;


// A compatibility layer for joda-time
import com.google.ical.compat.jodatime.LocalDateIteratorFactory;
// A Joda time class that represents a day regardless of timezone
import org.joda.time.LocalDate;
import org.junit.Test;


public class Rfc2445JodaTimeTest {

	

	  /** print the first 13 Friday the 13ths in the 3rd millenium AD. */
		@Test
	  public void JodaTest() throws java.text.ParseException {
	    LocalDate start = new LocalDate(2001, 4, 13);

	    // Every friday the thirteenth.
	    String ical = "RRULE:FREQ=MONTHLY"
	                  + ";BYDAY=FR"  // every Friday
	                  + ";BYMONTHDAY=13"  // that occurs on the 13th of the month
	                  + ";COUNT=13";  // stop after 13 occurences

	    // Print out each date in the series.
	    for (LocalDate date :
	         LocalDateIteratorFactory.createLocalDateIterable(ical, start, true)) {
	      System.out.println(date);
	    }
	  }
	
}
