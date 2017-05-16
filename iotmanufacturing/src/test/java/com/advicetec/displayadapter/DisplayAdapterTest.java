package com.advicetec.displayadapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;

import com.advicetec.displayadapter.JetFile2Protocol.TestCommand;
import com.advicetec.utils.UdpUtils;

public class DisplayAdapterTest {

	@Test
	public void constantsTest(){
		String val = JetFile2Protocol.Flash.OFF;
		System.out.println(String.valueOf(val));
	}
	
	@Test
	public void swapTest(){
		String code = "0102";
		System.out.println("Original"+code + "swap"+UdpUtils.swap(code));
		code = "010203";
		System.out.println("Original"+code + "swap"+UdpUtils.swap(code));
		code = "10203";
		System.out.println("Original"+code + "swap"+UdpUtils.swap(code));
		code = "01020304";
		System.out.println("Original"+code + "swap"+UdpUtils.swap(code));
	}

	@Test
	public void messageTest(){
		LedSignDisplay display = new LedSignDisplay();
		UdpUtils.printOutput( display.encodeMessage("Esto es una prueba!"));
	}
	
	@Test
	public void checkSumTest(){
		String chk = UdpUtils.checksum(
				DatatypeConverter.parseHexBinary("000000000101010003010000"));
		
		System.out.println("Check"+chk);
	}
	
	@Test
	public void publishMessageTest(){
		LedSignDisplay display = new LedSignDisplay();
		String chk = UdpUtils.checksum(
				DatatypeConverter.parseHexBinary("000000000101010003010000"));
		
		System.out.println("Check"+chk);
		// "55a7f302000000000101020001020400434f4e4649472e535953000004000100"
		// 
		//"55A8ED04000000000101010003010300898EFFFF6400A8C001010000"
		
		// "55a5db0014003031303101000000030100000000"
		
//		String mes = "000000000101020001020400434f4e4649472e535953000004000100";
		//String mes = "000000000101010003010300898EFFFF6400A8C001010000";
		//String mes = "a7377fa302040644303700000000020101015a30300241305468697320697320612073616d706c650d04";
		
	
		/*
		String mes = "14003031303101000000030100000000";
		
		chk = Display.checksum(
				DatatypeConverter.parseHexBinary(mes));
		System.out.println("checksum"+DatatypeConverter.printHexBinary(chk));
		
		byte[] bytes1 = DatatypeConverter.parseHexBinary("55a70700000000000101010003010000");
		System.out.println("response"+display.publishBytes(bytes1));
		*/
		String group = "01";
		String subGroup = "01";
		String seq = "01" + "00";
		
		byte[] bytes2 = display.generatePacketPayLoad(group, subGroup, seq, TestCommand.AUTO_TEST,"");
		System.out.println("test"+DatatypeConverter.printHexBinary(bytes2));
		
		//System.out.println("response"+display.publishBytes(bytes2));
	}
	
	@Test
	public void connectionTestCommandTest(){
		String snt = "55a70700000000000101010003010000";
		assertEquals(snt,JetFile2Protocol.connectionTestCommand(1,"01", "01"));
		
		String rcv = "55a8ed04000000000101010003010300898effff6400a8c001010000";
		JetFile2Protocol.connectionTestEcho(snt, rcv);
	}
	
	@Test
	public void readSystemFilesCommandTest(){
		String snt = "55a7f302000000000101020001020400434f4e4649472e535953000004000100";
		String cal = JetFile2Protocol.readSystemFilesCommand(2,"01","01");
		assertTrue(snt.equalsIgnoreCase(cal));
		
		String rcv = "55a83d03040000000101020001020200d8000100d8000000aa558000";
		System.out.println(snt);
		cal = JetFile2Protocol.readSystemFilesEcho(snt,rcv);
		System.out.println(cal);
	}
}

