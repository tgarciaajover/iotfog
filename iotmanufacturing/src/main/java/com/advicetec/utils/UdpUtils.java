package com.advicetec.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.xml.bind.DatatypeConverter;

/**
 * Utility class for UDP connections and related protocols.
 * @author advicetec
 *
 */
public abstract class UdpUtils {
	
	/**
	 * Calculates the CRC code for the given bytes.
	 * @param bytes Payload bytes.
	 * @return The CRC code for the given bytes.
	 */
	public static byte[] CheckSum(final byte[] bytes){
		Checksum checksum = new CRC32();
		checksum.update(bytes, 0, bytes.length);
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(0, checksum.getValue());
		return buffer.array();
	}

	/**
	 * Merges multiples char arrays onto one.
	 * @param arrays char arrays to merge.
	 * @return A consolidate char array from the input arrays.
	 */
	public static char[] merge(final char[]...arrays){
		char[] prev = {};
		for(char[] array : arrays){
			char[] res = new char[prev.length + array.length];
			System.arraycopy(prev, 0, res, 0, prev.length);
			System.arraycopy(array, 0, res, prev.length, array.length);
			prev = res;
		}
		
		return prev;
	}
	
	/**
	 * Merges multiples String arrays onto one.
	 * @param arrays char arrays to merge.
	 * @return A consolidate String array from the input arrays.
	 */
	public static String merge(final String...arrays){
		String prev = "";
		for(String array : arrays){
			prev.concat(array);
		}		
		return prev;
	}
	
	/**
	 * Translates the given char array to byte array.
	 * @param chars Array to be translate.
	 * @return A byte array of input array.
	 */
	public static byte[] toBytes(final char[] chars){
		ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(CharBuffer.wrap(chars));
		return byteBuffer.array();
	}

	/**
	 * Prints the given input decoded by UTF-8 and byte representation.
	 * @param message
	 */
	public static void printOutput(final byte[] message) {
		System.out.println(Charset.forName("UTF-8").decode(ByteBuffer.wrap(message)));
		for(byte b : message){		
			System.out.printf("<0x%02x> = %s \n", b,String.valueOf(b));
		}
	}
	
	/**
	 * Publishes an UDP packet and returns its response.
	 * @param bytes The data to publish.
	 * @param netAddress Network address of the destination.
	 * @param dstPort Number of the destination port
	 * @return A string with the response.
	 * @throws IOException If a communication exception occurs.
	 */
	public static String publishBytes(byte[] bytes, InetAddress netAddress, int dstPort) throws IOException{
		String s = null;

		byte[] toReceive = new byte[1024];
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length,netAddress, dstPort);
		DatagramSocket socket = new DatagramSocket();
		socket.send(packet);

		// receives the response
		socket.setSoTimeout(10000);
		DatagramPacket received = new DatagramPacket(toReceive,toReceive.length);
		socket.receive(received);
		byte[] bytes2 = received.getData();
		//System.out.println("length:"+received.getLength());
		s = new String(DatatypeConverter.printHexBinary(bytes2));
		s=(String)s.subSequence(0, received.getLength()*2);

		socket.close();
		return s;
	}
	
	/**
	 * Calculates the checksum code for the given bytes.
	 * @param bytes Payload bytes.
	 * @return The checksum code for the given bytes.
	 */
	public static String checksum(final byte[] bytes){
		int checksum = 0;
		
		for(byte b: bytes){
			checksum += 0xff & b;
		}
		String s = String.format("%04X", checksum);
		return swap(s);
	}
	
	/**
	 * Reverse the given bytes.
	 * @param hexString Payload String.
	 * @return Reverse string of given bytes.
	 */
	public static String swap(String hexString){
		StringBuilder sb = new StringBuilder(hexString);
		if(hexString.length() % 2 != 0){
			sb = new StringBuilder("0".concat(hexString));
		}
		for(int i =0; i<=(sb.length()/4); i=i+2){
			int pos = i*2;
			String subA = sb.substring(pos, pos+2);
			String subB= sb.substring(sb.length()-pos-2, sb.length()-pos);
			sb.replace(pos, pos+2, subB);
			sb.replace(sb.length()-pos-2, sb.length()-pos, subA);
		}
		return sb.toString();
	}
	
	/**
	 * Returns the substring of bytes for the given positions.
	 * @param bytes payload as String.
	 * @param from start position of substring.
	 * @param to final position of substring.
	 * @return Substring of byte for the given positions.
	 * @throws IndexOutOfBoundsException if the given positions are out of the 
	 * payload's bound.
	 */
	public static String getBytes(String bytes,int from, int to) throws IndexOutOfBoundsException{
		if(from*2 > bytes.length() || to*2 > bytes.length()){
			throw new IndexOutOfBoundsException("Cannot get the bytes from:"+from+" to:"+to);
		}
		
		return bytes.substring(from*2, to*2);
	}
	
	/**
	 * Returns a substring from a hex string representation.
	 * @param hexStr data.
	 * @param offset start position of substring.
	 * @param many number of bytes from the offset position.
	 * @return a substring from the given data.
	 * @throws IndexOutOfBoundsException if the given positions are out of the 
	 * payload's bound.
	 */
	public static String getFields(String hexStr,int offset, int many) throws IndexOutOfBoundsException{		
		return getBytes(hexStr, offset, (offset+many));
	}
	
	
	/**
	 * Returns a hex string, in litle endiand format, of length given by paramenter.
	 * @param value The integer to be parsed to hex.
	 * @param len String length.
	 * @return A String of hex representation of given value. String length is
	 * given by parameter.
	 */
	public static String int2HexString(int value, int len){
		StringBuilder res = new StringBuilder(Integer.toHexString(value));
		while(res.length()<len*2)
			res.insert(0, "0");
		return swap(res.toString());
	}
	
	/**
	 * Returns the integer value of a hex string given by parameter.
	 * @param hexStr Hex String.
	 * @return the integer value of a hex string given by parameter.
	 */
	public static int hexString2Int(String hexStr){
		return Integer.parseInt(UdpUtils.swap(hexStr),16);
	}
	
	/**
	 * Returns the ASCII representation of a hex string in UTF-8 format.
	 * @param hex Hex String.
	 * @return the ASCII representation of a hex string given by parameter.
	 */
	public static String hexString2Ascii(String hex){
		byte[] bytes = DatatypeConverter.parseHexBinary(hex);
		String s = null;
		try {
			s =new String(bytes,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}

	/**
	 * Returns the ASCII representation in UTF-8 format of a byte array.
	 * @param bytes data in byte array
	 * @return the ASCII representation in UTF-8 format of a byte array.
	 */
	public static String byteArray2Ascii(byte[] bytes){
		String s = null;
		try {
			s =new String(bytes,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	
	/**
	 * Returns the hex representation of Text string. 
	 * @param text to be parsed to hex.
	 * @return a hex string that represents the text given.
	 */
	public static String ascii2hexString(String text){
		byte[] bytes = text.getBytes();
		return DatatypeConverter.printHexBinary(bytes);
	}


	/**
	 * Creates a hex string from the parameter, with the number of bytes 
	 * specified by paramenter.
	 * 
	 * @param text to be parsed to hex.
	 * @param len resulting String length.
	 * @return a hex string that represents the given text with the given length.
	 */
	public static String ascii2hexString(String text, int len) {
		char[] array = text.toCharArray();
		StringBuilder res = new StringBuilder();
		
		for (int i = 0; i < array.length; i++) {
			String by =Integer.toHexString((int)array[i]);
			if(by.length() > 2)
				by = "0"+by;
			res.append(by);
		}
		
		if(res.length() >= len*2)
			return res.substring(0, len*2);
		
		while(res.length() < len*2)
			res.append("0");
		
		return res.toString();
	}
}
