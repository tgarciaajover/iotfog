package com.advicetec.displayadapter;

import org.junit.Test;

import com.advicetec.utils.UdpUtils;

public class DisplayAdapterTest {

	@Test
	public void constantsTest(){
		char[] val = Display.Flash.OFF;
		System.out.println(String.valueOf(val));
	}
	

	@Test
	public void messageTest(){
		LedSignDisplay display = new LedSignDisplay();
		UdpUtils.printOutput( display.encodeMessage("Welcome!"));
	}
	
	@Test
	public void publishMessageTest(){
		LedSignDisplay display = new LedSignDisplay();
		System.out.println(display.publishMessage("Welcome!"));
	}
}

