package com.advicetec.displayadapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;

import com.advicetec.utils.UdpUtils;

public class DisplayAdapterTest {

	@Test
	public void constantsTest(){
		String val = TextFormat.Flash.OFF;
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
		display.publishMessage("Esto es una prueba!");
	}
	
	@Test
	public void checkSumTest(){
		String chk = UdpUtils.checksum(
				DatatypeConverter.parseHexBinary("000000000101010003010000"));
		
		System.out.println("Check"+chk);
	}
	
	@Test
	public void publishMessageTest(){
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
		
		//byte[] bytes2 = display.generatePacketPayLoad(group, subGroup, seq, TestCommand.AUTO_TEST,"");
		//System.out.println("test"+DatatypeConverter.printHexBinary(bytes2));
		
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
		JetFile2Protocol.readSystemFilesEcho(snt,rcv);

		snt = "55a7f302000000000101030001020400434f4e4649472e535953000000030100"; 
		rcv = "55a81c2cd80000000101030001020200d8000100d8000000aa558000+"
				+ "100000000400040030750000000000400800040a0700220000000041010100006400a8c0001d6f01da6f0900898e2509000000000000000014000000000000000000000000000000fefffeff00000000000b3230313630363231303837003894dbe3cec5e4400a54657863656c6c656e74000010003500a400d000e000e800f000ff0000c800a8c000ffffff0080200100000000002f0b0000000000000f000000020000000000000000000000000000000000000000000000000000ffff990001000100626f6c6431312e666e74000053000000";
	}
	
	@Test
	public void readFontLibraryCommandTest(){
		String snt = "55a7c704000000000101040001030400466f6e744c6973742e6c737404000100";
		String cal = JetFile2Protocol.readFontLibraryCommand(4,"01","01");
	}
	
	
	@Test
	public void obtainDiskInformationCommandTest(){
		String snt = "55a7a0000000000001010900070d0100463a0000";
		JetFile2Packet packet = new JetFile2Packet(snt);
		String cal = JetFile2Protocol.FileControlCommand.obtainDiskInformationCommand();
	}
	
	@Test 
	public void JetFile2Test(){
		String snt = "55a7c704000000000101040001030400466f6e744c6973742e6c737404000100";
		JetFile2Packet p1 = new JetFile2Packet(snt);
		System.out.println(snt);
		System.out.println(p1.toString());
		assertTrue(snt.equalsIgnoreCase(p1.toHexString()));
		
		snt = "55a81c2cd80000000101030001020200d8000100d8000000aa558000"
				+ "100000000400040030750000000000400800040a0700220000000041010100006400a8c0001d6f01da6f0900898e2509000000000000000014000000000000000000000000000000fefffeff00000000000b3230313630363231303837003894dbe3cec5e4400a54657863656c6c656e74000010003500a400d000e000e800f000ff0000c800a8c000ffffff0080200100000000002f0b0000000000000f000000020000000000000000000000000000000000000000000000000000ffff990001000100626f6c6431312e666e74000053000000";
		JetFile2Packet p2 = new JetFile2Packet(snt);
		System.out.println(snt);
		System.out.println(p2.toString());
		assertTrue(snt.equalsIgnoreCase(p2.toHexString()));
	}
	
	@Test
	public void writeSysFileTest(){
		String snt = "55a7210a2c00000001010b000202060053455155454e542e535953002c0000000003010001000000535104000100000044540f7f172005150101010117200515010101010c16ba0074656d702e4e6d6700000000";
		JetFile2Packet p1 = new JetFile2Packet(snt);
		
		String rcv = "55a8a4000200000001010a00040100010090";
		JetFile2Packet in = new JetFile2Packet( rcv );
		//assertTrue(in.getData().equalsIgnoreCase(JetFile2Protocol.StatusCode.SUCESS));
		
		snt = "55a7210a2c00000001010b000202060053455155454e542e535953002c0000000003010001000000535104000100000044540f7f172005150101010117200515010101010c16ba0074656d702e4e6d6700000000";
		JetFile2Packet p2 = new JetFile2Packet(snt);
		rcv = "55a8a4000200000001010b00020200010090";
		JetFile2Packet p3 = new JetFile2Packet(rcv);
		System.out.println(JetFile2Protocol.processPacket(p2));
		System.out.println(JetFile2Protocol.processPacket(p3));
	}
	
	@Test
	public void writeText(){
		LedSignDisplay display = new LedSignDisplay();
		String snt = "55a74e26ba00000001010c0002040600440074656d702e4e6d6700000000ba000000000301000100015a303002410f1b306208310e32303030301f321e310a49310a4f2f0f321c311d301a31073057454c434f4d45210d73646b6a666c6b660d044e6f74654e6d672066696c652076657273696f6e3a76332e373120202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202004";
		JetFile2Packet p1 = new JetFile2Packet(snt);
		
		System.out.println(display.publishPacket(p1) );
		
		System.out.println(JetFile2Protocol.processPacket(p1));
		
		String rcv = "55a8a7000200000001010c00020400010090";
		JetFile2Packet p2 = new JetFile2Packet(rcv);
		System.out.println(JetFile2Protocol.processPacket(p2));
	}
	
	
	@Test
	public void connectionTest(){
		LedSignDisplay display = new LedSignDisplay();
		System.out.println("start display:" + display.startBlackScreen() );
		System.out.println("connection test: " + display.connectionTest() );
	//	System.out.println("auto test:"+display.autoTest());
		
		System.out.println("message test:"+display.publishMessage("Mensa"));
		System.out.println("end display: " + display.endBlackScreen() );
	}
	
	
	@Test
	public void readSysFile(){
		LedSignDisplay display = new LedSignDisplay();
		//String str = "55a82c2cd80000000101130001020200d8000100d8000000aa558000100000000400040030750000000000400800040a0700220000000041010100006400a8c0001d6f01da6f0900898e2509000000000000000014000000000000000000000000000000fefffeff00000000000b3230313630363231303837003894dbe3cec5e4400a54657863656c6c656e74000010003500a400d000e000e800f000ff0000c800a8c000ffffff0080200100000000002f0b0000000000000f000000020000000000000000000000000000000000000000000000000000ffff990001000100626f6c6431312e666e74000053000000";
		
		display.readSystemFiles();
		
		JetFile2Packet out = JetFile2Protocol.ReadingData.command0102(4,1);
		out.setChecksum();
		System.out.println("packet out:\n" + out.toHexString());
		JetFile2Packet in = display.publishPacket(out);
		System.out.println("packet in :\n" + in.getData());
		ConfigHead resp = JetFile2Protocol.command0102(in);
		System.out.println(resp);
	}
	
	
	@Test
	public void sysFileWriteTest()
	{
		LedSignDisplay display = new LedSignDisplay();
		display.setOutMode(TextFormat.PatternControl.O_RANDOM);
		display.setInMode(TextFormat.PatternControl.I_RANDOM);
		String message = "jajajja ya Funciona!";

		display.out(message);
	}
	
	@Test
	public void resetTest(){
		LedSignDisplay display = new LedSignDisplay();
		display.out("Before reset!");
		try {
			display.reset();
			Thread.sleep(10000);
			display.out("After reset!");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}

