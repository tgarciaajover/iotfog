package com.advicetec.iot.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.junit.Test;

import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeType;
import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.MeasuredEntityType;


public class TestREST {

	@Test
	public void JsonAttributeValue(){
		Attribute att = new Attribute("AttrName", AttributeType.DOUBLE);
		AttributeValue val= new AttributeValue("123", att, 10.0, 1, MeasuredEntityType.MACHINE);
		
		JSONObject jsonObject = new JSONObject(val);
		System.out.println(jsonObject.toString());
		String result = "{\"generator\":\"P00\",\"attribute\":"
				+ "{\"trend\":false,\"origin\":"
				+ "{\"name\":\"Behavior\",\"value\":2},"
				+ "\"name\":\"AttrName\",\"type\":"
				+ "{\"name\":\"Double\",\"value\":0}},\"value\":10,\"parentType\":"
				+ "{\"name\":\"Machine\",\"value\":0},\"key\":\"123\"}";
		System.out.println(result);
		assertEquals(jsonObject.toString(),result);
	}
	
	@Test
	public void mapperTest(){
		Attribute att = new Attribute("AttrName", AttributeType.DOUBLE);
		AttributeValue val= new AttributeValue("123", att, 10.0, 1, MeasuredEntityType.MACHINE);
		
		ObjectMapper mapper = new ObjectMapper();
		String jStr = new JSONObject(val).toString();
		try {
			AttributeValue att2 = mapper.readValue(jStr,AttributeValue.class);
			System.out.println(att);
			System.out.println(att2);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
