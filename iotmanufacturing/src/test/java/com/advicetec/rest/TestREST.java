package com.advicetec.rest;

import static org.junit.Assert.assertEquals;

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
		AttributeValue val= new AttributeValue("123", att, 10.0, "P00", MeasuredEntityType.MACHINE);
		
		JSONObject jsonObject = new JSONObject(val);
		System.out.println(jsonObject.toString());
		String result = "{\"parent\":\"P00\",\"attribute\":"
				+ "{\"trend\":false,\"origin\":"
				+ "{\"name\":\"Behavior\",\"value\":2},"
				+ "\"name\":\"AttrName\",\"type\":"
				+ "{\"name\":\"Double\",\"value\":0}},\"value\":10,\"parentType\":"
				+ "{\"name\":\"Machine\",\"value\":0},\"key\":\"123\"}";
		assertEquals(jsonObject.toString(),result);
	}
}
