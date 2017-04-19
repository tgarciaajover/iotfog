package com.advicetec.language.transformation.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.advicetec.core.AttributeType;
import com.advicetec.language.ast.RecognitionExceptionUtil;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.TransformationSymbol;
import com.advicetec.language.transformation.InterpreterSw;
import com.advicetec.language.transformation.SyntaxChecking;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

public class LanguageTransformationTest 
{
	private static final String EXTENSION = "properties";
	
	@Test
	public void Test_Semantic_Language()
	{
		
		String program = "test/transformtest_withouterrors." + EXTENSION;

		System.out.println("Interpreting file:" + program);
		
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
			Map<String, Symbol> globalScope = interpreter.getGlobalScope().getSymbolMap();
			
			TransformationSymbol transform = (TransformationSymbol) globalScope.get("transform");
				
			Map<String, Symbol> transforScope = transform.getMembers();
			
			for (String name : transforScope.keySet()){
				System.out.println(name);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void Test_Syntax_Language()
	{
		
		String program = "test/transformtest_errors." + EXTENSION;

		System.out.println("Syntax Check:" + program);
		
		SyntaxChecking interpreter = new SyntaxChecking();
		try 
		{
			List<SyntaxError> errorList = interpreter.process(program);
			
			assertEquals("the number of errors is not the expected value: 2",(int) 2, (int) errorList.size() );

	        for (SyntaxError e : errorList) {
	            // RecognitionExceptionUtil is my custom class discussed next.
	            System.out.println(RecognitionExceptionUtil.formatVerbose(e));
	        }
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
