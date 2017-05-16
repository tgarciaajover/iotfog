package com.advicetec.utils;

import java.io.IOException;
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
		System.out.println("length:"+received.getLength());
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
		
		// put it on little endian.
		s = s.substring(2,4) + s.substring(0,2);
		
		return s;
	}
	
	
	public static String swap(String hexString){
		StringBuilder sb = new StringBuilder(hexString);
		if(hexString.length() % 2 != 0){
			sb = new StringBuilder("0".concat(hexString));
		}
		for(int i =0; i<(sb.length()/4); i=i+2){
			int pos = i*2;
			String subA = sb.substring(pos, pos+2);
			String subB= sb.substring(sb.length()-pos-2, sb.length()-pos);
			sb.replace(pos, pos+2, subB);
			sb.replace(sb.length()-pos-2, sb.length()-pos, subA);
		}
		return sb.toString();
	}
}
