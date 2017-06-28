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

public class UdpUtils {
	
	public static byte[] CheckSum(final byte[] bytes){
		Checksum checksum = new CRC32();
		checksum.update(bytes, 0, bytes.length);
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(0, checksum.getValue());
		return buffer.array();
	}

	
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
	
	
	public static String merge(final String...arrays){
		String prev = "";
		for(String array : arrays){
			prev.concat(array);
		}		
		return prev;
	}
	
	
	public static byte[] toBytes(final char[] chars){
		ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(CharBuffer.wrap(chars));
		return byteBuffer.array();
	}

	
	public static void printOutput(final byte[] message) {
		System.out.println(Charset.forName("UTF-8").decode(ByteBuffer.wrap(message)));
		for(byte b : message){		
			System.out.printf("<0x%02x> = %s \n", b,String.valueOf(b));
		}
	}
	
	/**
	 * Publishes a udp packet and returns the response
	 * @param bytes The data to publish.
	 * @return A string with the response.
	 * @throws IOException 
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
	
	public static String checksum(final byte[] bytes){
		int checksum = 0;
		
		for(byte b: bytes){
			checksum += 0xff & b;
		}
		String s = String.format("%04X", checksum);
		return swap(s);
	}
	
	
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
	
	
	public static String getBytes(String bytes,int from, int to) throws IndexOutOfBoundsException{
		if(from*2 > bytes.length() || to*2 > bytes.length()){
			throw new IndexOutOfBoundsException("Cannot get the bytes from:"+from+" to:"+to);
		}
		
		return bytes.substring(from*2, to*2);
	}
	
	/**
	 * Returns a substring from a hex string representation.
	 * @param hexStr
	 * @param offset
	 * @param many
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	public static String getFields(String hexStr,int offset, int many) throws IndexOutOfBoundsException{		
		return getBytes(hexStr, offset, (offset+many));
	}
	
	
	/**
	 * Returns a hex string, in litle endiand format, of lenght given by paramenter.
	 * @param x The integer
	 * @param bytes String lenght.
	 * @return
	 */
	public static String int2HexString(int x, int bytes){
		StringBuilder res = new StringBuilder(Integer.toHexString(x));
		while(res.length()<bytes*2)
			res.insert(0, "0");
		return swap(res.toString());
	}
	
	public static int hexString2Int(String hexStr){
		return Integer.parseInt(UdpUtils.swap(hexStr),16);
	}
	
	/**
	 * 
	 * @param hex
	 * @return
	 */
	public static String hexString2Ascii(String hex){
		byte[] bytes = DatatypeConverter.parseHexBinary(hex);
		String s = null;
		try {
			s =new String(bytes,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public static String ascii2hexString(String s){
		byte[] bytes = s.getBytes();
		return DatatypeConverter.printHexBinary(bytes);
	}


	/**
	 * Creates a hex string from the parameter, with the number of bytes 
	 * specified by paramenter.
	 * 
	 * @param string
	 * @param i
	 * @return
	 */
	public static String ascii2hexString(String s, int bytes) {
		char[] array = s.toCharArray();
		StringBuilder res = new StringBuilder();
		
		for (int i = 0; i < array.length; i++) {
			String by =Integer.toHexString((int)array[i]);
			if(by.length() > 2)
				by = "0"+by;
			res.append(by);
		}
		
		if(res.length() >= bytes*2)
			return res.substring(0, bytes*2);
		
		while(res.length() < bytes*2)
			res.append("0");
		
		return res.toString();
	}
}
