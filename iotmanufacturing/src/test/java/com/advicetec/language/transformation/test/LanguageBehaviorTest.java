package com.advicetec.language.transformation.test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.AttributeType;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.MeasuringUnit;
import com.advicetec.language.behavior.InterpreterSw;
import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.advicetec.measuredentitity.MeasuredEntityType;

public class LanguageBehaviorTest 
{
	private static final String EXTENSION = "properties";
	
	@Test
	public void Test_Semantic_Language()
	{
		
		String program = "test/behaviortest." + EXTENSION;

		System.out.println("Interpreting file " + program);
		
		List<AttributeValue> list = new ArrayList<AttributeValue>();
		
		MeasuringUnit cyc = new MeasuringUnit("CYC", "CYCLE"); 
		Attribute attr = new Attribute("Cycles", AttributeType.INT, cyc,false,AttributeOrigin.TRANSFORMATION);
		
		Integer intValue = 1;
		String measuredEntity = "1010";
		LocalDateTime current = LocalDateTime.of(2017, 3, 9, 19, 46, 45);
		MeasuredAttributeValue value = new MeasuredAttributeValue(attr,intValue, measuredEntity, MeasuredEntityType.MACHINE, current); 
		
		list.add(value);
		
		InterpreterSw interpreter = new InterpreterSw();
		try 
		{
			interpreter.process(program, measuredEntity, list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
