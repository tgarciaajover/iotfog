package com.advicetec.MessageProcessor;

import com.advicetec.core.Processor;
import com.advicetec.language.transformation.InterpreterSw;


public class SampleProcessor implements Processor 
{

	SampleMessage sample;
	
	
	public SampleProcessor(SampleMessage sample) {
		super();
		this.sample = sample;
	}


	@Override
	public void process() {

		InterpreterSw interpreter = new InterpreterSw();
		try 
		{
			interpreter.process(this.sample.getMeaningTransformation());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
