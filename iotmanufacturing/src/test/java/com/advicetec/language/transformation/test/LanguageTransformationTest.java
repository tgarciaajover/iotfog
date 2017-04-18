package com.advicetec.language.transformation.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.advicetec.core.AttributeType;
import com.advicetec.language.transformation.InterpreterSw;
import com.advicetec.language.transformation.SyntaxChecking;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

public class LanguageTransformationTest 
{
	private static final String EXTENSION = "properties";
	
	@Test
	public void Test_Semantic_Language()
	{
		
		String program = "test/transformtest." + EXTENSION;

		System.out.println("Interpreting file " + program);
		
		List<InterpretedSignal> list = new ArrayList<InterpretedSignal>();
		LocalDateTime current = LocalDateTime.of(2017, 3, 9, 19, 46, 45);
		InterpretedSignal timeSignal = new InterpretedSignal(AttributeType.DATETIME, current );
		list.add(timeSignal);
		
		Integer value = 1;
		InterpretedSignal valueSignal = new InterpretedSignal(AttributeType.INT, value );
		list.add(valueSignal);
		
		InterpreterSw interpreter = new InterpreterSw();
		try 
		{
			interpreter.process(program,list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void Test_Sintax_Language()
	{
		
		String program = "test/transformtest." + EXTENSION;

		System.out.println("Interpreting Sintax File " + program);
		
		SyntaxChecking interpreter = new SyntaxChecking();
		try 
		{
			interpreter.process(program);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
