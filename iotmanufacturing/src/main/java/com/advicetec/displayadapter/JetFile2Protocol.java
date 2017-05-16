package com.advicetec.displayadapter;

import java.net.InetAddress;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.utils.UdpUtils;

public class JetFile2Protocol {

	static Logger logger = LogManager.getLogger(JetFile2Protocol.class.getName());
	
	public static final String ZERO = "00";
	public static final String ZEROS = "0000";
	
	public static final String ONE = "01";
	
	public static final String START  = "01";
	public static final String ADDRESS = "02";

	public static final String DATA_PREFIX_OUT = "55" + "a7";
	public static final String DATA_PREFIX_IN = "55" + "a8";
	public static final String DST_ADDR = "01" + "01";
	
	public static final String HEAD = "51" + "5A" + "30" + "30" + "53" + "41" + "58";
	public static final String EOF = "04";
	public static final String NEW_FRAME = "0c";
	public static final String LINE_FEED = "0d";
	public static final String Halfspace = "82";

	public static class ReadingData{
		private final static String com = "01";
		public final static String ABS_ADD = com + "01";
		public final static String SYS_FILES = com + "02";
	}

	public static class Flash{
		private final static String com = "07";
		public final static String ON = com + "1";
		public final static String OFF = com + "0";
	}

	public static class LineSpacing{
		private final static String com = "08";

		public final static String LS0 = com + "30";
		public final static String LS1 = com + "31";
		public final static String LS2 = com + "32";
		public final static String LS3 = com + "33";
		public final static String LS4 = com + "34";
		public final static String LS5 = com + "35";
		public final static String LS6 = com + "36";
		public final static String LS7 = com + "37";
		public final static String LS8 = com + "38";
		public final static String LS9 = com + "39";
	}

	public static class PatternControl {
		private final static String com = "0A";
		private final static String I = "49";
		private final static String O = "4F";

		public final static String I_RANDOM = com + I + "2F";
		public final static String O_RANDOM = com + O + "2F";
		public final static String I_JUMP_OUT = com + I + "30";
		public final static String O_JUMP_OUT = com + O + "30";
		public final static String I_MOVE_LEFT = com + I + "31";
		public final static String O_MOVE_LEFT = com + O + "31";
		public final static String I_MOVE_RIGHT = com + I + "32";
		public final static String O_MOVE_RIGHT =  com + O + "32";
		public final static String I_SCROLL_LEFT = com + I + "33";
		public final static String O_SCROLL_LEFT = com + O + "33";
		public final static String I_SCROLL_RIGHT = com + I + "34";
		public final static String O_SCROLL_RIGHT = com + O + "34";
		public final static String I_MOVE_UP = com + I + "35";
		public final static String O_MOVE_UP = com + O + "35";
		public final static String I_MOVE_DOWN = com + I + "36";
		public final static String O_MOVE_DOWM = com + O + "36";
		public final static String I_SCROLL_LR = com + I + "37";
		public final static String O_SCROLL_LR = com + O + "37";
		public final static String I_SCROLL_UP = com + I + "38";
		public final static String O_SCROLL_UP = com + O + "38";
		public final static String I_SCROLL_DOWN = com + I + "39";
		public final static String O_SCROLL_DOWN = com + O + "39";
		public final static String I_FOLD_LR = com + I + "3A";
		public final static String O_FOLD_LR = com + O + "3A";
		public final static String I_FOLD_UD = com + I + "3B";
		public final static String O_FOLD_UD = com + O + "3B";
		public final static String I_SCROLL_UD = com + I + "3C";
		public final static String O_SCROLL_UD = com + O + "3C";
		public final static String I_SUTTLE_LR = com + I + "3D";
		public final static String O_SUTTLE_LR = com + O + "3D";
		public final static String I_SUTTLE_UD = com + I + "3E";
		public final static String O_SUTTLE_UD = com + O + "3E";
		public final static String I_PEEL_OFF_L = com + I + "3F";
		public final static String O_PEEL_OFF_L = com + O + "3F";
		public final static String I_PEEL_OFF_R = com + I + "40";
		public final static String O_PEEL_OFF_R = com + O + "40";

		public final static String I_RAINDROPS = com + I + "43";
		public final static String O_RAINDROPS = com + O + "43";
		public final static String I_RANDOM_MOSAIC = com + I + "44";
		public final static String O_RANDOM_MOSAIC = com + O + "44";
		public final static String I_TWINKLE_STARS = com + I + "45";
		public final static String O_TWINKLE_STARS = com + O + "45";
		public final static String I_HIP_HOP = com + I + "46";
		public final static String O_HIP_HOP = com + O + "46";
		public final static String I_RADAR = com + I + "47";
		public final static String O_RADAR = com + O + "47";
	}

	public static class Pause{
		private final static String com = "0E";

		public final static String SEC_00 = com + "30";
		public final static String MIL_SEC_00 = com + "31";
		public final static String SEC_0000 = com + "32";
		public final static String MILSEC_0000 = com + "33";
	}

	public static class Speed{
		private final static String com = "0F";

		public final static String VERY_FAST = com + "30";
		public final static String FAST = com + "31";
		public final static String MED_FAST = com + "32";
		public final static String MEDIUM = com + "33";
		public final static String MED_SLOW = com + "34";
		public final static String SLOW = com + "35";
		public final static String VERY_SLOW = com + "36";
	}

	public static class FontSize{
		private final static String com = "1A";

		public final static String EN_5X5 = com + "30";
		public final static String EN_7X6 = com + "31";
		public final static String EN_14X8 = com + "32";
		public final static String EN_15X9 = com + "33";
		public final static String EN_16X9 = com + "34";
		public final static String EN_24X16 = com + "36";
		public final static String EN_32X18 = com + "38";
	}

	public static class DisposalMode{
		static final String CONSTRAINT = "1b" + "30" + "61";
		static final String DEFAULT = "1b" + "30" + "62";
	}

	public static class TextColor{
		private final static String com = "1C";

		public final static String BLACK = com + "30";
		public final static String RED = com + "31";
		public final static String GREEN = com + "32";
		public final static String AMBER = com + "33";
		public final static String MIX_PAL1 = com + "34";
		public final static String MIX_PAL2 = com + "35";
		public final static String MIX_PAL3 = com + "36";
		public final static String MIX_PAL4 = com + "37";
	}

	public static class Background{
		private final static String com = "1D";

		public final static String BLACK = com + "30";
		public final static String RED = com + "31";
		public final static String GREEN = com + "32";
		public final static String AMBER = com + "33";
	}

	public static class HorizontalAlign{
		private final static String com = "1E";

		public final static String CENTER = com + "30";
		public final static String LEFT = com + "31";
		public final static String RIGHT = com + "32";
	}

	public static class VerticalAlign{
		private final static String com = "1F";

		public final static String CENTER = com + "30";
		public final static String TOP = com + "1";
		public final static String BOTTOM = com + "2";
	}

	public static class TestCommand{
		private final static String com = "03";
		
		public final static String CONX_TEST = com + "01";
		public final static String AUTO_TEST = com + "02";
		public final static String ALL_BRIGTH_TEST = com + "03";
		
		public final static String END_TEST = com + "09";
	}
	
	
	/*
	 * Constants for byte offset for the packet.
	 */
	public final static int START_SYNC = 0;
	public final static int END_SYNC = 1;
	public final static int START_CHECKSUM = 2;
	public final static int END_CHECKSUM = 3;
	public final static int START_DATALEN = 4;
	public final static int END_DATALEN = 5;
	public final static int START_SRCADDR = 6;
	public final static int END_SRCADDR =7;
	public final static int START_DSTADDR =8;
	public final static int END_DSTADDR =9;
	public final static int START_PCKSSER =10;
	public final static int END_PCKSSER =11;
	public final static int START_CMD =12;
	public final static int END_CMD =13;
	public final static int START_ARGLEN =14;
	public final static int END_ARGLEN =14;
	public final static int START_FLAG =15;
	public final static int END_FLAG =15;
	public final static int ARG = 16;
	
	
	
	
	public void ConnectionTest(String originalPacket, String strHex) throws Exception{

		// Verifies the header 
		headerTest(originalPacket, strHex);
		
		// Verifies the number of parameters 
		
	}
	
	/**
	 * Verifies the header has arrived ok.
	 * @param originalPacket
	 * @param strHex
	 * @throws Exception
	 */
	private static void headerTest(String originalPacket, String strHex) throws Exception
	{
						
		if (strHex.length() >= END_FLAG){
			// Verifies Sync Code.
			String syncCode = getSyncCode(strHex); 
			if (syncCode.compareTo(DATA_PREFIX_IN) != 0){
				throw new Exception("Invalid Sync Response");
			}
			
			// Verifies Source Address
			String sourceAddress = getSourceAddress(strHex); 
			if (sourceAddress.compareTo(getSourceAddress(originalPacket)) != 0 ){
				throw new Exception("Invalid Source Address Response");
			}

			// Verifies Destination Address
			String destinAddress = getDestinAddress(strHex);
			if (destinAddress.compareTo(getDestinAddress(originalPacket)) != 0){
				throw new Exception("Invalid Destin Address Response");
			}
			
			// Verifies Packet Serial
			String packetSerial = getPacketSerial(strHex);
			if (packetSerial.compareTo(getPacketSerial(originalPacket)) != 0 ){
				throw new Exception("Invalid Packet Serial Response");
			}
			
			// Verifies Main and Sub CMD
			String command = getCommands(strHex);
			if (command.compareTo(getCommands(originalPacket)) != 0){
				throw new Exception("Invalid Commands Response");
			}

			// Verifies Check Sum.
			String checkSumStr = strHex.substring(START_DATALEN*2, strHex.length());
			String checkHex = UdpUtils.checksum(DatatypeConverter.parseHexBinary(checkSumStr));
			System.out.println(getCheckNumber(strHex));
			if (checkHex.compareToIgnoreCase(getCheckNumber(strHex)) != 0){
				throw new Exception("Invalid Check Number Response");
			}
				
		} else {
			throw new Exception("Invalid Response");
		}
	}
	
	public static String getArglen(String message){
		return message.substring(START_ARGLEN*2, (END_ARGLEN + 1)*2);
	}
	
	public static String getDataLength(String message){
		return message.substring(START_DATALEN*2, (END_DATALEN + 1)*2);
	}
	
	public static String getSyncCode(String message)
	{
		return message.substring(START_SYNC*2, (END_SYNC +1)*2);
	}
	
	public static String getCheckNumber(String message)
	{
		return message.substring(START_CHECKSUM*2, (END_CHECKSUM +1)*2);
	}
	
	public static String getSourceAddress(String message)
	{
		return message.substring(START_SRCADDR*2, (END_SRCADDR+1)*2);
	}
	
	public static String getDestinAddress(String message)
	{
		return message.substring(START_DSTADDR*2, (END_DSTADDR+1)*2);
	}

	public static String getPacketSerial(String message)
	{
		return message.substring(START_PCKSSER*2, (END_PCKSSER+1)*2);
	}

	public static String getCommands(String message)
	{
		return message.substring(START_CMD*2, (END_CMD + 1)*2);
	}

	
	///////////////////////////////////////////////////////
	///////////////////////////////////////////////////////
	
	/**
	 * Implements the command READ SYSTEM FILES 0102. <br>
	 * This command is used to read CONFIG.SYS
	 * @return
	 */
	public static String readSystemFilesCommand(int serial, String group, String unit){
		StringBuilder ret = new StringBuilder();
		// datalength
		ret.append(ZEROS);
		// src
		ret.append(UdpUtils.int2HexString(0, 2));
		// group
		ret.append(group);
		// unit
		ret.append(unit);
		// serial
		ret.append(UdpUtils.int2HexString(serial, 2));
		// command
		ret.append(ReadingData.SYS_FILES);
		// arg len
		ret.append(UdpUtils.int2HexString(4, 1));
		// flag
		ret.append(ZERO);
		// adding 
		String filename = "CONFIG.SYS";
		ret.append(DatatypeConverter.printHexBinary(filename.getBytes()));
		int len = filename.length();
		while(len<12){
			ret.append(ZERO);
			len++;
		}
		// packet size
		ret.append(UdpUtils.int2HexString(4, 2));
		// serial number
		ret.append(UdpUtils.int2HexString(1, 2));
		
		String chk = UdpUtils.checksum(DatatypeConverter.parseHexBinary(ret.toString()));
		ret.insert(0, chk);
		ret.insert(0, DATA_PREFIX_OUT);
		
		return ret.toString();
	}
	
	/**
	 * Implements the command CONNECTION TEST 0301. <br>
	 * @return
	 */
	public static String connectionTestCommand(int serial,String group, String unit){
		StringBuilder ret = new StringBuilder();
		
		// DATA LENGTH
		ret.append(ZEROS);
		// SRC
		ret.append(ZEROS);
		// group
		ret.append(group);
		// unit
		ret.append(unit);
		// serial
		ret.append(UdpUtils.swap(ZERO.concat(ONE)));
		// command
		ret.append(TestCommand.CONX_TEST);
		// fill
		ret.append(ZEROS);
		
		String chk = UdpUtils.checksum(DatatypeConverter.parseHexBinary(ret.toString()) );
		ret.insert(0, chk);
		ret.insert(0, DATA_PREFIX_OUT);
		
		return ret.toString();
	}
	

	public static String connectionTestEcho(String original,String echo){
		try {
			headerTest(original,echo);
			String args = UdpUtils.getBytes(echo, ARG, ARG + 12);
			System.out.println("Prog Version:"+UdpUtils.getBytes(args, 0, 2));
			System.out.println("FPGA Version:"+UdpUtils.getBytes(args, 2, 4));
			byte[] ip = DatatypeConverter.parseHexBinary(UdpUtils.swap(UdpUtils.getBytes(args, 4, 8)));
			System.out.println("Ip Address:"+InetAddress.getByAddress(ip).toString());
			System.out.println("Sign address:"+UdpUtils.getBytes(args, 8, 10));
		} catch (Exception e) {
			logger.error("Header test was wrong!");
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	
	public static String readSystemFilesEcho(String sent, String echo) {
		try {
			headerTest(sent,echo);
			
			int datalen = Integer.parseInt(UdpUtils.swap(getDataLength(echo)),16);
			int arglen = Integer.parseInt(getArglen(echo), 16);
			int dataOff = ARG + 4*arglen;
			String args = UdpUtils.getBytes(echo, ARG, dataOff );
			String data = UdpUtils.getBytes(echo,dataOff,dataOff + datalen);
			
			System.out.println("File size:"+UdpUtils.getBytes(args, 0, 2));
			System.out.println("Packet serial:"+UdpUtils.getBytes(args, 2, 4));
			System.out.println("BigFile size:"+UdpUtils.getBytes(args, 4, 8));
			System.out.println("DATA:"+data);

		} catch (Exception e) {
			logger.error("Header test was wrong!");
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	////////////////////
	////////////////////
	
}
