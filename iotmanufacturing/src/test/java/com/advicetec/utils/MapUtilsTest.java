package com.advicetec.utils;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class MapUtilsTest {
	
	@Test
	public void Splitter()
	{
		
		
		int batchRows = 400;
		Map<String, Integer> entries = new HashMap<String, Integer>(); 
		
		for (int i= 0; i < 40000; i++) {
			String key = "key" + Integer.toString(i);
			entries.put(key, new Integer(i));
		}
 		
		// Splits the entries in batches of batchRows  
		List<Map<String, Integer>> listofMaps =
						entries.entrySet().stream().collect(MapUtils.mapSize(batchRows));
	
		assertTrue(listofMaps.size() == 100);
	}

}
