package com.advicetec.iot.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.LocalDateTime;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import com.advicetec.core.TimeInterval;

public class IntervalTest {

	@Test
	public void jsonTimeInterval(){
		LocalDateTime s = LocalDateTime.of(2016, 12, 24, 23, 59);
		LocalDateTime e = LocalDateTime.now();
		TimeInterval int1 = new TimeInterval(s, e);
		String json = int1.toJson();
		System.out.println(json);
		System.out.println(int1);
		TimeInterval int2 = null;

		try {
			int2 = new ObjectMapper().readValue(json, TimeInterval.class);
			System.out.println(int2);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		assertEquals("they don't match.",int1.toString(),int2.toString());
	}

}
