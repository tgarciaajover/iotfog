package com.advicetec.iot.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;


import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.AttributeType;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.MeasuringUnit;
import com.advicetec.measuredentitity.MeasuredEntityType;

public class AttributeTest {
	
	@Test
	public void jsonAttributeTypeTest(){
		AttributeType type = AttributeType.DOUBLE;
		String json = type.toJson();
		System.out.println(json);
		AttributeType type2 = null;
		try {
			type2 = new ObjectMapper().readValue(json, AttributeType.class);
			System.out.println(type2);
			
		} catch (JsonParseException e) {e.printStackTrace();} 
		catch (JsonMappingException e) {e.printStackTrace();} 
		catch (IOException e) { e.printStackTrace();
		}
		
		assertEquals("they don't match.",type,type2);
	}


	@Test
	public void jsonAttributeUnitTest(){
		MeasuringUnit unit = new MeasuringUnit("s", "Seconds");
		String json = unit.toJson();
		//System.out.println(unit);
		//System.out.println(json);
		MeasuringUnit unit2 = null;
		try {
			unit2 = new ObjectMapper().readValue(json,MeasuringUnit.class);
			//System.out.println(unit2);
			
		} catch (JsonParseException e) {e.printStackTrace();} 
		catch (JsonMappingException e) {e.printStackTrace();} 
		catch (IOException e) { e.printStackTrace();
		}
		
		assertEquals("they don't match.",unit.toString(),unit2.toString());
	}
	
	@Test
	public void jsonAttributeOriginTest(){
		AttributeOrigin origin = AttributeOrigin.BEHAVIOR;
		
		String json = origin.toJson();
		System.out.println(origin);
		System.out.println(json);
		
		AttributeOrigin origin2 = null;
		try {
			origin2 = new ObjectMapper().readValue(json,AttributeOrigin.class);
			System.out.println(origin2);
			
		} catch (JsonParseException e) {e.printStackTrace();} 
		catch (JsonMappingException e) {e.printStackTrace();} 
		catch (IOException e) { e.printStackTrace();
		}
	}
	

	@Test
	public void jsonAttribute(){
		MeasuringUnit unit = new MeasuringUnit("s", "Seconds");
		Attribute att = new Attribute("AttrName", AttributeType.DOUBLE,unit,
				true,AttributeOrigin.BEHAVIOR);
		System.out.println(att.toString());
		String json = att.toJson();
		System.out.println(json);
		
		Attribute att2 = null;
		try {
			att2 = new ObjectMapper().readValue(json, Attribute.class);
			System.out.println(att2.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals("they don't match.",att.toString(),att2.toString());
	}
	
	@Test
	public void jsonMeasuredEntityType(){
		MeasuredEntityType type = MeasuredEntityType.MACHINE;
		
	}
	@Test
	public void jsonAttributeValue(){
		Attribute att = new Attribute("AttrName", AttributeType.DOUBLE);
		AttributeValue val= new AttributeValue("123", att, 10.0, "P00", MeasuredEntityType.MACHINE);
		System.out.println(val.toJson());
		AttributeValue val2 = null;
		try {
			val2 = new ObjectMapper().readValue(val.toJson(), AttributeValue.class);
			System.out.println(val2.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals("they don't match.",att.toString(),val2.toString());
	}
}
