package com.advicetec.language.transformation.test;

import org.junit.Test;

public class TestLanguagePrimitives {

	@Test
	public void And_Test(){
		
		Boolean a = null;
		Boolean b = null;
		Boolean c;
		Boolean d;
		Boolean e;
		
		c = a && b;
		
		b = new Boolean(true);
		d= a && b;
		
		e = b && a;
		
		
				
	}
}
