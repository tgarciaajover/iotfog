package com.advicetec.displayadapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JetFile2Protocol {

	static Logger logger = LogManager.getLogger(JetFile2Protocol.class.getName());

	public String ConectionTest(){
		return null;
	}

	
	public final int START_SYNC = 0;
	public final int END_SYNC = 1;
	public final int START_CHECKSUM = 2;
	public final int END_CHECKSUM = 3;
	public final int START_DATALEN = 4;
	public final int END_DATALEN = 5;
	public final int START_SRCADDR = 6;
	public final int END_SRCADDR =7;
	public final int START_DSTADDR =8;
	public final int END_DSTADDR =9;
	public final int START_PCKSSER =10;
	public final int END_PCKSSER =11;
	public final int START_CMD =12;
	public final int END_CMD =13;
	public final int START_ARGLEN =14;
	public final int END_ARGLEN =14;
	public final int START_FLAG =15;
	public final int END_FLAG =15;
	
	public void ConnectionTest(String originalPacket, String strHex) throws Exception{

		// Verifies the header 
		HeaderTest(originalPacket, strHex);
		
		// Verifies the number of parameters 
		
	}
	
	private void HeaderTest(String originalPacket, String strHex) throws Exception
	{
						
		if (strHex.length() >= END_FLAG){
			// Verifies Sync Code.
			String syncCode = getSyncCode(strHex); 
			if (syncCode.compareTo("5508") != 0){
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
			String checkSumStr = strHex.substring(START_DATALEN, strHex.length());
			String checkHex = Display.checksum(DatatypeConverter.parseHexBinary(checkSumStr));
			if (checkHex.compareTo(getCheckNumber(strHex)) != 0){
				throw new Exception("Invalid Check Number Response");
			}
				
		} else {
			throw new Exception("Invalid Response");
		}
	}
	
	public String getSyncCode(String message)
	{
		return message.substring(START_SYNC, END_SYNC +1);
	}
	
	public String getCheckNumber(String message)
	{
		return message.substring(START_CHECKSUM, END_CHECKSUM +1);
	}
	
	public String getSourceAddress(String message)
	{
		return message.substring(START_SRCADDR, END_SRCADDR+1);
	}
	
	public String getDestinAddress(String message)
	{
		return message.substring(START_DSTADDR, END_DSTADDR+1);
	}

	public String getPacketSerial(String message)
	{
		return message.substring(START_PCKSSER, END_PCKSSER+1);
	}

	public String getCommands(String message)
	{
		return message.substring(START_CMD, END_CMD + 1);
	}

	/**
	 * Implements the command READ SYSTEM FILES 0102. <br>
	 * This command is used to read CONFIG.SYS
	 * @return
	 */
	public String ReadSystemFiles(){
		String ret = "";
		
		ret.concat(arg0)

		return ret;
	}

	

}
