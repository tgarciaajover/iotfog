package com.advicetec.language.transformation.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.advicetec.core.AttributeType;
import com.advicetec.language.behavior.BehaviorInterpreterSw;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

public class LanguageBehaviorTest 
{
	private static final String EXTENSION = "properties";
	
	@Test
	public void Test_Semantic_Language()
	{
		
		String program = "test/behaviortest." + EXTENSION;
		
		FileInputStream in;
		try {
			in = new FileInputStream(program);

			program = getFileContent( in );
			
			System.out.println("Interpreting file " + program);
			
			List<InterpretedSignal> list = new ArrayList<InterpretedSignal>();
			LocalDateTime current = LocalDateTime.of(2017, 3, 9, 19, 46, 45);
			InterpretedSignal timeSignal = new InterpretedSignal(AttributeType.DATETIME, current );
			list.add(timeSignal);
			
			Integer value = 1;
			InterpretedSignal valueSignal = new InterpretedSignal(AttributeType.INT, value );
			list.add(valueSignal);

			Integer entityId = 1;
			MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
			MeasuredEntityFacade entityFacade = entityManager.getFacadeOfEntityById(entityId);

			
			BehaviorInterpreterSw interpreter = new BehaviorInterpreterSw();
			interpreter.process(program, entityFacade, entityId, list);
		
		
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}
	
	public String getFileContent( FileInputStream fis ) {
	    StringBuilder sb = new StringBuilder();
	    Reader r;
		try {
			r = new InputStreamReader(fis, "UTF-8");

		    int ch;
				ch = r.read();
		    while(ch >= 0) {
		        sb.append(ch);
		        ch = r.read();
		    }

		    return sb.toString();
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;

	}
}
