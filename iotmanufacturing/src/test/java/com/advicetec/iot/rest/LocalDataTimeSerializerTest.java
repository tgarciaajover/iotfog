package com.advicetec.iot.rest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

public class LocalDataTimeSerializerTest {

	@Test
	public void LocalDateTimeSerializer(){
		String textData = "2017-07-05 21:25:55.7";
		DateTimeFormatter format = DateTimeFormatter.ofPattern("uuuu-MM-dd H:m:s.n");
        LocalDateTime tmp = LocalDateTime.parse(textData,format);
        System.out.println(tmp);
	}
}
