package com.advicetec.displayadapter;

import java.net.InetAddress;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.utils.UdpUtils;

public class JetFile2Protocol {

	static Logger logger = LogManager.getLogger(JetFile2Protocol.class.getName());
	
	public static final String ZERO = "00";
	public static final String ZEROS = "0000";
	
	public static final String ONE = "01";
	
	
	
	public static final String ADDRESS = "02";

	public static final String DATA_PREFIX_OUT = "55" + "a7";
	public static final String DATA_PREFIX_IN = "55" + "a8";
	public static final String DST_ADDR = "01" + "01";
	
	public static final String SQ = "5351";

	public static final String TEMP_FILE = "temp.Nmg";

	public enum StatusCode{
		SUCESS("0090","Sucess"),
		OK("4f4b","Sucess"),
		OPEN_FILE_FAIL("1090","Failure open file"),
		WRITE_FILE_FAIL("1290","Failure writing the file");
		
		private static final Map<String, StatusCode> _map = new HashMap<String, StatusCode>();
		private String code;
		private String meaning;
		
		static {
			for(StatusCode code: StatusCode.values()){
				_map.put(code.code, code);
			}
		}
		
		private  StatusCode(String cod, String mean) {
			code = cod;
			meaning = mean;
		}
		
		public static StatusCode getStatus(String code){
			return _map.get(code); 
		}
		
		public static String getMeaning(String code){
			return _map.get(code).meaning;
		}
		
	}
	
	/**
	 * 01 READING DATA
	 * @author user
	 *
	 */
	public static class ReadingData{
		private final static String com = "01";
		public final static String ABS_ADD = com + "01";
		public final static String SYS_FILES = com + "02";
		public final static String FONT_LIB = com + "03";
		
		public final static String DEFAULT_DISP_STY = com + "0c";
		
		/**
		 * Read system Files
		 * @return
		 */
		public static JetFile2Packet command0102(int filesize, int packetserial){
			JetFile2Packet packet = new JetFile2Packet();
			packet.setCommand(SYS_FILES);
			StringBuilder argHex = new StringBuilder( UdpUtils.ascii2hexString("CONFIG.SYS",12) );
			argHex.append(UdpUtils.int2HexString(filesize, 2)); // packet size
			argHex.append(UdpUtils.int2HexString(packetserial, 2)); // packet serial
			
			packet.setArgs(argHex.toString());
			packet.setChecksum();
			return packet;
		}
	}

	/**
	 * 02 INFORMATION WRITE
	 * @author user
	 *
	 */
	public static class InfoWrite{
		private final static String com = "02";
		
		public final static String WRITE_SYS_FILE = com + "02";
		public final static String WRITE_FONT = com + "03";
		public final static String WRITE_TXT = com + "04";
		public final static String WRITE_STR = com + "05";
		
		public final static String WRITE_CRC = com + "0e";
		
		/**
		 * System file write-in
		 * @return
		 */
		public static JetFile2Packet command0202(String check, String filesize){
			JetFile2Packet packet = new JetFile2Packet();
			packet.setCommand(WRITE_SYS_FILE);
			
			StringBuilder args = new StringBuilder(); // 6 args
			args.append( UdpUtils.ascii2hexString("SEQUENT.SYS",12) )
			.append(UdpUtils.int2HexString(44, 4)) // file size 0x2c
			.append("0003") // size of packet
			.append(UdpUtils.int2HexString(1, 2)) // quantity 
			.append(UdpUtils.int2HexString(1, 2)) // current packet
			.append(UdpUtils.int2HexString(0, 2)) // Note
			;
			// set args
			packet.setArgs(args.toString());
			
			StringBuilder data = new StringBuilder(SQ); // header
			data.append(UdpUtils.int2HexString(4, 1)); // file type
			data.append(UdpUtils.int2HexString(0, 1)); // valid marking
			data.append(UdpUtils.int2HexString(1, 2)); // scheduled files
			data.append(UdpUtils.int2HexString(0, 2)); // rsv
			
			data.append(UdpUtils.ascii2hexString("D")); // drive
			data.append(UdpUtils.ascii2hexString("T")); // T for text
			data.append("0f"); // file label
			data.append("7f"); // week repetition
			LocalDate today = LocalDate.now();
			data.append(new DateTimeStruct(today.getYear(),today.getMonthValue(),today.getDayOfMonth()).toHexString());
			data.append(new DateTimeStruct(today.getYear(),today.getMonthValue(),today.getDayOfMonth()).toHexString());
			data.append(check);	// checksum 2 "0c16"; verifica el contenido del archivo
			data.append(filesize); // filesize 2 ba00 del siguiente archivo
			data.append(UdpUtils.ascii2hexString(TEMP_FILE, 12));// filename 12
			
			// set data
			packet.setData(data.toString());
			// set checksum
			packet.setChecksum();
			
			return packet;
		}
		
		public static JetFile2Packet command0204(String hexData ){
			JetFile2Packet packet = new JetFile2Packet();
			packet.setCommand(InfoWrite.WRITE_TXT);
			packet.setData(hexData);
			
			String codeDiskPartition = UdpUtils.ascii2hexString("D",1);
			String ringingTimes = "00";
			String textfileLabel = UdpUtils.ascii2hexString(TEMP_FILE,12);
			String filesize = UdpUtils.int2HexString(packet.getDatalen(), 4);
			String pcktsize = UdpUtils.int2HexString(768, 2);
			String quantity = UdpUtils.int2HexString(1, 2);
			String currentPacket = UdpUtils.int2HexString(1, 2);
			
			StringBuilder args = new StringBuilder(codeDiskPartition);
			args.append(ringingTimes)
			.append(textfileLabel)
			.append(filesize)
			.append(pcktsize)
			.append(quantity)
			.append(currentPacket);
			
			logger.debug("args:"+args.toString());
			packet.setArgs(args.toString());
			logger.debug(packet.toHexString());
			packet.setChecksum();
			
			return packet;
		}
		
	}
	/**
	 * 03 TEST COMMAND
	 * @author user
	 *
	 */
	public static class TestCommand{
		private final static String com = "03";
		
		public final static String CONX_TEST = com + "01";
		public final static String AUTO_TEST = com + "02";
		public final static String ALL_BRIGTH_TEST = com + "03";
		public final static String ALL_BLUE_TEST = com + "06";
		
		public final static String END_TEST = com + "09";
		
		
		/**
		 * Auto test
		 * @return
		 */
		public static JetFile2Packet command0302(){
			JetFile2Packet packet = new JetFile2Packet();
			packet.setCommand(AUTO_TEST);
			packet.setChecksum();
			return packet;
		}
		
		/**
		 * Implements the command CONNECTION TEST 0301. <br>
		 * @return
		 */
		public static JetFile2Packet command0301(){
			JetFile2Packet packet = new JetFile2Packet();
			packet.setCommand(CONX_TEST);
			packet.setChecksum();
			return packet;
		}
	}
	
	/**
	 * 04 BLACK SCREEN COMMAND
	 * @author user
	 *
	 */
	public static class BlackScreenCommand{
		private final static String com = "04";
		
		public final static String RESET = com + "00";
		public final static String START = com + "01";
		public final static String END = com + "02";
		public final static String SWITCH_OFF = com + "03";
		public final static String SWITCH_ON= com + "04";
		
		
		/**
		 * Reset command 0400
		 * @return
		 */
		public static JetFile2Packet reset(){
			JetFile2Packet packet = new JetFile2Packet();
			packet.setCommand(BlackScreenCommand.RESET);
			packet.setArgs("");
			return packet;
		}
		
		/**
		 * Reset command 0401
		 * @return
		 */
		public static JetFile2Packet start(){
			JetFile2Packet packet = new JetFile2Packet();
			packet.setCommand(BlackScreenCommand.START);
			return packet;
		}
		
		/**
		 * Reset command 0402
		 * @return
		 */
		public static JetFile2Packet endBlackScreen(){
			JetFile2Packet packet = new JetFile2Packet();
			packet.setCommand(BlackScreenCommand.END);
			return packet;
		}
	}
	
	/**
	 * 07 FILE CONTROL COMMAND
	 * @author user
	 *
	 */
	public static class FileControlCommand{
		private final static String com = "07";
		
		public final static String DISK_INFO = com + "0D";
		
		/**
		 * disk information command 070d
		 * @return
		 */
		public static JetFile2Packet diskInformation(String disk){
			JetFile2Packet packet = new JetFile2Packet();
			packet.setCommand(DISK_INFO);
			packet.setArgs(UdpUtils.ascii2hexString(disk, 4));
			packet.setChecksum();
			return packet;
		}
		
		/**
		 * Reads information of the designated disk, it includes the type of disk
		 * total size, and free size. <br> 
		 * Command: 070d 
		 * @return
		 */
		public static String obtainDiskInformationCommand() {
			
			return null;
		}
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
			logger.debug(getCheckNumber(strHex));
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
	
	public static String getGroup(String message){
		return getDestinAddress(message).substring(0, 2);
	}
	
	public static String getUnit(String message){
		return getDestinAddress(message).substring(2, 4);
	}

	public static String getPacketSerial(String message)
	{
		return message.substring(START_PCKSSER*2, (END_PCKSSER+1)*2);
	}

	public static String getCommands(String message)
	{
		return message.substring(START_CMD*2, (END_CMD + 1)*2);
	}

	public static String getFlag(String message) {
		
		return message.substring(START_FLAG*2, (END_FLAG + 1)*2);
	}
	
	public static String getArgs(int arglen, String message) {
		if(arglen == 0)
			return "";
		return message.substring(ARG*2, ARG*2 + 2*4*arglen);
	}
	
	public static String getData(int arglen, int datalen, String message){
		int dataStart = ARG*2 + 2*4*arglen;
		if(datalen == 0)
			return "";
		return message.substring(dataStart, dataStart + datalen*2);
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
			logger.debug("Prog Version:"+UdpUtils.getBytes(args, 0, 2));
			logger.debug("FPGA Version:"+UdpUtils.getBytes(args, 2, 4));
			byte[] ip = DatatypeConverter.parseHexBinary(UdpUtils.swap(UdpUtils.getBytes(args, 4, 8)));
			logger.debug("Ip Address:"+InetAddress.getByAddress(ip).toString());
			logger.debug("Sign address:"+UdpUtils.getBytes(args, 8, 10));
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
			
			String filesize = UdpUtils.swap(UdpUtils.getBytes(args, 0, 2));
			logger.debug("File size:"+filesize+" - "+Integer.parseInt(filesize, 16));
			String serial = UdpUtils.swap(UdpUtils.getBytes(args, 2, 4));
			logger.debug("Packet serial:"+serial+" - "+Integer.parseInt(serial, 16));
			String bigfilesize = UdpUtils.swap(UdpUtils.getBytes(args, 4, 8));
			logger.debug("BigFile size:"+bigfilesize+" - "+Integer.parseInt(bigfilesize, 16));
			logger.debug("DATA:"+data);

		} catch (Exception e) {
			logger.error("Header test was wrong!");
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static String readFontLibraryCommand(int serial,String group, String unit) {

		return null;
	}

	

	public static String getStatus(String message) {
		if(getFlag(message).equals(ONE))
			return UdpUtils.swap(message.substring(ARG*2, (ARG+2)*2));
		return "";
	}

	/////////////////////////////////////////////////////////////////
	//                        PACKET PROCESSING
	////////////////////////////////////////////////////////////////
	
	/**
	 * This method processes the response from the display.
	 * @param packet
	 * @return
	 */
	public static String processPacket(JetFile2Packet packet){
		if(!checkStructure(packet)){
			logger.warn("Something is wrong with the packet content!");
			return null;
		}
		String command = packet.getCommand();
		switch(command){
		case InfoWrite.WRITE_SYS_FILE:
			return command0202(packet);
			
		case InfoWrite.WRITE_TXT:
			return command0204(packet);
			
		case TestCommand.CONX_TEST:
			return command0301(packet);
			
		case ReadingData.SYS_FILES:
			return null;
			
		default:
			logger.error("Command is not supported!");
		}
		
		return null;
	}
	
	/**
	 * System file read after 0102 command.<br>
	 * Interpret the response to 0102 packet.
	 * 
	 * @param packet
	 * @return
	 */
	public static ConfigHead command0102(JetFile2Packet packet){
		String args = packet.getArgs();
		if(packet.getArglen() *2 == args.length()){
			StringBuilder sb = new StringBuilder();
			sb.append("Arglen:").append(packet.getArglen()) // 2
			.append(",file size:").append(UdpUtils.getBytes(args, 0, 2))
			.append(",packet serial numb:").append(UdpUtils.getBytes(args, 2, 4))
			.append(",file size:").append(UdpUtils.getBytes(args, 4, 8));

		}
		return new ConfigHead(packet.getData(),packet.getDatalen());
	}
	
	/** 
	 * System write file
	 * @param packet
	 * @return
	 */
	public static String command0202(JetFile2Packet packet){
		
		if(packet.getFlag() == 1) // in-echo
			return packet.getData();
					
		if(packet.getArglen() != 6)
			logger.warn("Command 0x0202 needs 6 arguments");
		
		String args = packet.getArgs();
		if(args.length() != 24*2)
			logger.warn("Command 0x0202 needs 6 arguments");

		String systemFilename = args.substring(0, 24);
		String totalSize = args.substring(24, 32);
		String packetSize = args.substring(32, 36);
		String quantity = args.substring(36, 40);
		String currentPacket = args.substring(40, 44);
		String note = args.substring(44);
		String data = packet.getData();
			
		StringBuilder sb = new StringBuilder();
		sb.append("System filename:").append(systemFilename)
		.append(",total size:").append(totalSize)
		.append(",packet size:").append(packetSize)
		.append(",quantity:").append(quantity)
		.append(",curren packet:").append(currentPacket)
		.append(",note").append(note)
		.append(",data:").append(data);
		
		return sb.toString();
	}

	/**
	 * Writes text file
	 * @param packet
	 * @return
	 */
	public static String command0204(JetFile2Packet packet){
		
		if(packet.getFlag() == 1) // in-echo
			return packet.getData();
		
		String args = packet.getArgs();
		if(packet.getArglen() != 6 || args.length() != 24*2)
			logger.warn("Command 0x0204 needs 6 arguments");
		
		String codeDiskPartition = args.substring(0, 2);
		String ringingTimes = args.substring(2, 4);
		String textfileLabel = args.substring(4, 28);
		String filesize = args.substring(28, 36);
		String pcktsize = args.substring(36, 40);
		String quantity = args.substring(40, 44);
		String currentPacket = args.substring(44, 48);
		String data = packet.getData();
		
		StringBuilder sb = new StringBuilder();
		sb.append("Disk partition:").append(codeDiskPartition)
		.append(",ringing:").append(ringingTimes)
		.append(",file label:").append(textfileLabel)
		.append(",file size:").append(filesize)
		.append(",packet size:").append(pcktsize)
		.append(",quantity:").append(quantity)
		.append(",current:").append(currentPacket)
		.append(",data:").append(data);
		return sb.toString();
	}

	

	
	/**
	 * Connection test
	 * @return
	 */
	public static String command0301(JetFile2Packet packet){
		String echo = packet.toHexString();
		StringBuilder res = new StringBuilder();
		try {
			packet.getArgs();
			String args = UdpUtils.getBytes(echo, ARG, ARG + 12);
			res.append("Prog Version:"+UdpUtils.getBytes(args, 0, 2));
			res.append(",FPGA Version:"+UdpUtils.getBytes(args, 2, 4));
			byte[] ip = DatatypeConverter.parseHexBinary(UdpUtils.swap(UdpUtils.getBytes(args, 4, 8)));
			res.append(",Ip Address:"+InetAddress.getByAddress(ip).toString());
			res.append(",Sign address:"+UdpUtils.getBytes(args, 8, 10));
		} catch (Exception e) {
			logger.error("Header test was wrong!");
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return res.toString();
	}

	
	/**
	 * 
	 * @param packet
	 * @return
	 */
	private static boolean checkStructure(JetFile2Packet packet) {
		String body = packet.bodyString();
		String chk = packet.getChecksum();
		if(!UdpUtils.checksum(DatatypeConverter.parseHexBinary(body)).equalsIgnoreCase(chk))
			return false;

		if(packet.getArgs().length() != 2*4*packet.getArglen())
			return false;
		
		if(packet.getData().length() != 2*packet.getDatalen())
			return false;
		
		return true;
	}
	
	
}
