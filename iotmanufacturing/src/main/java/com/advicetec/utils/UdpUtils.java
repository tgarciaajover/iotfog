package com.advicetec.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

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
}
