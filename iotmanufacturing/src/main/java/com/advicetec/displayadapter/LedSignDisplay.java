package com.advicetec.displayadapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.displayadapter.Display;
import com.advicetec.displayadapter.Display.Background;
import com.advicetec.displayadapter.Display.TestCommand;
import com.advicetec.displayadapter.Display.TextColor;
import com.advicetec.displayadapter.Display.Flash;
import com.advicetec.displayadapter.Display.FontSize;
import com.advicetec.displayadapter.Display.HorizontalAlign;
import com.advicetec.displayadapter.Display.LineSpacing;
import com.advicetec.displayadapter.Display.PatternControl;
import com.advicetec.displayadapter.Display.Pause;
import com.advicetec.displayadapter.Display.Speed;
import com.advicetec.displayadapter.Display.VerticalAlign;
import com.advicetec.utils.UdpUtils;

public class LedSignDisplay implements Output {
	
	static Logger logger = LogManager.getLogger(LedSignDisplay.class.getName());
	
	private int signalWidth;
	private int signalHeight;
	private int dstPort;
	private InetAddress netAddress;
	private String description;
	
	private char[] inMode;
	private char[] outMode;
	private char[] speed;
	private char[] pause;
	private char[] lineSpacing;
	private char[] letterSize;
	private char[] flash;
	private char[] textColor;
	private char[] backColor;
	private char[] verticalAlign;
	private char[] horizontalAlign;

	private String message;
	
	/**
	 * Default constructor.
	 */
	public LedSignDisplay(){
		this.signalWidth = 128;
		this.signalHeight = 32;
		this.dstPort = 3001;
		try {
			this.netAddress = (Inet4Address) Inet4Address.getByName("192.168.0.100");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.inMode = PatternControl.I_MOVE_LEFT;
		this.outMode = PatternControl.O_MOVE_LEFT;
		this.speed = Speed.MED_FAST;
		this.pause = Pause.SEC_0000;
		this.lineSpacing = LineSpacing.LS1;
		this.letterSize = FontSize.EN_7X6;
		this.flash = Flash.OFF;
		this.textColor = TextColor.RED;
		this.backColor = Background.BLACK;
		this.verticalAlign = VerticalAlign.CENTER;
		this.horizontalAlign = HorizontalAlign.CENTER;
	}
	
	public LedSignDisplay(int signalWidth, int signalHeight,
			Inet4Address netAddress, char[] inMode, char[] outMode,
			char[] speed, char[] pause, char[] lineSpacing, char[] letterSize,
			char[] flash, char[] textColor, char[] backColor,
			char[] verticalAlign, char[] horizontalAlign) {
		super();
		this.signalWidth = signalWidth;
		this.signalHeight = signalHeight;
		this.netAddress = netAddress;
		this.inMode = inMode;
		this.outMode = outMode;
		this.speed = speed;
		this.pause = pause;
		this.lineSpacing = lineSpacing;
		this.letterSize = letterSize;
		this.flash = flash;
		this.textColor = textColor;
		this.backColor = backColor;
		this.verticalAlign = verticalAlign;
		this.horizontalAlign = horizontalAlign;
	}

	
	public void setDescription(String description){
		this.description = description;
	}
	
	@Override
	public void out(String message) {
		// create the UDP packet
		
	}
	
	/**
	 * This method builds the message in raw format to be sent to the display.
	 * 
	 * @param message
	 * @return
	 */
	public byte[] encodeMessage(String message){
		return UdpUtils.toBytes(UdpUtils.merge(
				Display.HEAD,
				inMode,
				outMode,
				speed,
				pause,
				lineSpacing,
				letterSize,
				flash,
				textColor,
				backColor,
				verticalAlign,
				horizontalAlign,
				message.toCharArray(),
				Display.EOF
				));
	}

	@Override
	public String getName() {
		return null;
	}

	/**
	 * This method publishes an UDP packet and waits for the response.
	 * 
	 * @param message String with the message.
	 * @return The response.
	 */
	public String publishMessage(String message){
		return publishBytes(encodeMessage(message));
	}

	
	public String publishBytes(byte[] bytes){
		String s = null;
		try {
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
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Error sending the message:"
					+ DatatypeConverter.printBase64Binary(bytes));
			e.printStackTrace();
		}
		return s;
	}

	public int getSignalWidth() {
		return signalWidth;
	}

	public void setSignalWidth(int signalWidth) {
		this.signalWidth = signalWidth;
	}

	public int getSignalHeight() {
		return signalHeight;
	}

	public void setSignalHeight(int signalHeight) {
		this.signalHeight = signalHeight;
	}

	public int getDstPort() {
		return dstPort;
	}

	public void setDstPort(int dstPort) {
		this.dstPort = dstPort;
	}

	public InetAddress getNetAddress() {
		return netAddress;
	}

	public void setNetAddress(InetAddress netAddress) {
		this.netAddress = netAddress;
	}

	public char[] getInMode() {
		return inMode;
	}

	public void setInMode(char[] inMode) {
		this.inMode = inMode;
	}

	public char[] getOutMode() {
		return outMode;
	}

	public void setOutMode(char[] outMode) {
		this.outMode = outMode;
	}

	public char[] getSpeed() {
		return speed;
	}

	public void setSpeed(char[] speed) {
		this.speed = speed;
	}

	public char[] getPause() {
		return pause;
	}

	public void setPause(char[] pause) {
		this.pause = pause;
	}

	public char[] getLineSpacing() {
		return lineSpacing;
	}

	public void setLineSpacing(char[] lineSpacing) {
		this.lineSpacing = lineSpacing;
	}

	public char[] getLetterSize() {
		return letterSize;
	}

	public void setLetterSize(char[] letterSize) {
		this.letterSize = letterSize;
	}

	public char[] getFlash() {
		return flash;
	}

	public void setFlash(char[] flash) {
		this.flash = flash;
	}

	public char[] getTextColor() {
		return textColor;
	}

	public void setTextColor(char[] textColor) {
		this.textColor = textColor;
	}

	public char[] getBackColor() {
		return backColor;
	}

	public void setBackColor(char[] backColor) {
		this.backColor = backColor;
	}
	
	public void setBackColor(String backColor) {
		switch(backColor){
		case "K":
			this.backColor = Background.BLACK;
			break;
		case "R":
			this.backColor = Background.RED;
			break;
		case "G":
			this.backColor = Background.GREEN;
			break;
		case "Y":
			this.backColor = Background.AMBER;
			break;
		default:
			logger.error("Invalid color "+ backColor + " for backgroud.");	
			break;
		}
		
	}

	public char[] getVerticalAlign() {
		return verticalAlign;
	}

	public void setVerticalAlign(char[] verticalAlign) {
		this.verticalAlign = verticalAlign;
	}

	public char[] getHorizontalAlign() {
		return horizontalAlign;
	}

	public void setHorizontalAlign(char[] horizontalAlign) {
		this.horizontalAlign = horizontalAlign;
	}

	public String getDescription() {
		return description;
	}

	public void setInMode(String inMode) {
		switch (inMode) {
		case "JO":
			this.inMode = Display.PatternControl.I_JUMP_OUT;
			break;
		case "ML":
			this.inMode = Display.PatternControl.I_MOVE_LEFT;
			break;
		case "MR":
			this.inMode = Display.PatternControl.I_MOVE_RIGHT;
			break;
		case "SL":
			this.inMode = Display.PatternControl.I_SCROLL_LEFT;
			break;
		case "SR":
			this.inMode = Display.PatternControl.I_SCROLL_RIGHT;
			break;
		default:
			logger.error("Invalid In mode "+ inMode + ".");
			break;
		}
	}

	public void setOutMode(String outMode2) {
		switch (outMode2) {
		case "JO":
			this.outMode = Display.PatternControl.O_JUMP_OUT;
			break;
		case "ML":
			this.outMode = Display.PatternControl.O_MOVE_LEFT;
			break;
		case "MR":
			this.outMode = Display.PatternControl.O_MOVE_RIGHT;
			break;
		case "SL":
			this.outMode = Display.PatternControl.O_SCROLL_LEFT;
			break;
		case "SR":
			this.outMode = Display.PatternControl.O_SCROLL_RIGHT;
			break;
		default:
			logger.error("Invalid OUT mode "+ outMode2 + ".");
			break;
		}
		
	}

	public void setLetterSize(String letterSize) {
		switch (letterSize) {
		case "0":
			this.letterSize = FontSize.EN_5X5;
			break;
		case "1":
			this.letterSize = FontSize.EN_7X6;
			break;
		case "2":
			this.letterSize = FontSize.EN_14X8;
			break;
		case "3":
			this.letterSize = FontSize.EN_15X9;
			break;
		case "4":
			this.letterSize = FontSize.EN_16X9;
			break;
		default:
			logger.error("Invalid font size "+ letterSize + ".");
			break;
		}
	}

	public void setLineSpacing(Integer lineSpacing2) {
		switch (lineSpacing2) {
		case 0:
			lineSpacing = LineSpacing.LS0;
			break;
		case 1:
			lineSpacing = LineSpacing.LS1;
			break;
		case 2:
			lineSpacing = LineSpacing.LS2;
			break;
		case 3:
			lineSpacing = LineSpacing.LS3;
			break;
		case 4:
			lineSpacing = LineSpacing.LS4;
			break;
		case 5:
			lineSpacing = LineSpacing.LS5;
			break;
		case 6:
			lineSpacing = LineSpacing.LS6;
			break;
		case 7:
			lineSpacing = LineSpacing.LS7;
			break;
		case 8:
			lineSpacing = LineSpacing.LS8;
			break;
		case 9:
			lineSpacing = LineSpacing.LS9;
			break;
		default:
			logger.error("Invalid linespacing "+ lineSpacing2 + ".");
			break;
		}
		
	}

	public void setSpeed(String speed2) {
	
		switch (speed2) {
		case "0":
			speed = Speed.VERY_FAST;
			break;
		case "1":
			speed = Speed.FAST;
			break;
		case "2":
			speed = Speed.MED_FAST;
			break;
		case "3":
			speed = Speed.MEDIUM;
			break;
		case "4":
			speed = Speed.MED_SLOW;
			break;
		case "5":
			speed = Speed.SLOW;
			break;
		case "6":
			speed = Speed.VERY_SLOW;
			break;
		default:
			logger.error("Invalid speed "+ speed2 + ".");
			break;
		}
	}

	public void setTextColor(String color) {
		switch(color){
		case "K":
			this.textColor = TextColor.BLACK;
			break;
		case "R":
			this.textColor = TextColor.RED;
			break;
		case "G":
			this.textColor = TextColor.GREEN;
			break;
		case "Y":
			this.textColor = TextColor.AMBER;
			break;
		default:
			logger.error("Invalid text color "+ color + ".");	
			break;
		}
		
	}

	public void setVerticalAlign(String vertical) {
		
		switch (vertical) {
		case "0":
			verticalAlign = VerticalAlign.CENTER;
			break;
		case "1":
			verticalAlign = VerticalAlign.TOP;
			break;
		case "2":
			verticalAlign = VerticalAlign.BOTTOM;
			break;
		default:
			logger.error("Invalid vertical align "+ vertical + ".");
			break;
		}
	}

	public void setHorizontalAlign(String horizontal) {
		switch (horizontal) {
		case "0":
			horizontalAlign = HorizontalAlign.CENTER;
			break;
		case "1":
			horizontalAlign = HorizontalAlign.LEFT;
			break;
		case "2":
			horizontalAlign = HorizontalAlign.RIGHT;
			break;
		default:
			logger.error("Invalid horizontal align "+ horizontal + ".");
			break;
		}
		
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public byte[] getDataLen(){
		int len = message.length();
		String s = Integer.toHexString(len);
		while(s.length() < 4)
			s="0".concat(s);
		return DatatypeConverter.parseHexBinary(s);
	}
	
	public byte[] generatePacketPayLoad(String group, String unit, String seq, String comand, String payHex){
		
		String flag = "00" + "00";
		String array = TestCommand.AUTO_TEST + flag;
		
		array = seq + array;
		array = unit + array;
		array = group + array;
		
		String src_address = "00" + "00";
		array = src_address + array;
		
		System.out.println("array until now :" + array );
		String len = getBytesDataLen(payHex);
		
		System.out.println("payload len:" + len);
		array = len + array;
		
		System.out.println("array until now 2:" + array );
		
		// put data payload
		array = array + payHex;
		
		System.out.println("array until now 3:" + array );
				
		String checkHex = Display.checksum(DatatypeConverter.parseHexBinary(array));
		
		System.out.println("check" + checkHex );
		
		String headSt = Display.DATA_PREFIX_OUT;
		
		String head = headSt + checkHex;
		
		head = head + array;
		
		System.out.println("head:" + head );
		
		return  DatatypeConverter.parseHexBinary(head);
	}
	
	
	private String getBytesDataLen(String payHex){ 
		String len = Integer.toHexString(payHex.length());
		
		while(len.length()<4){
			len = "0".concat(len);
		}
				
		String a = len.substring(2,4) + len.substring(0,2);
		
		return a;
	}
	

}
