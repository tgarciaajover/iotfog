package com.advicetec.displayadapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.displayadapter.JetFile2Protocol;
import com.advicetec.displayadapter.JetFile2Protocol.TestCommand;
import com.advicetec.displayadapter.TextFormat;
import com.advicetec.displayadapter.TextFormat.DisposalMode;
import com.advicetec.utils.UdpUtils;


/**
 * 
 * @author user
 *
 */
public class LedSignDisplay implements Output {
	
	static Logger logger = LogManager.getLogger(LedSignDisplay.class.getName());
	
	/**
	 * Address
	 */
	private int group;
	private int unit;
	
	private int signalWidth;
	private int signalHeight;
	private int dstPort;
	private InetAddress netAddress;
	private String description;
	
	private int serial;
	
	private String inMode;
	private String outMode;
	private String speed;
	private String pause;
	private String lineSpacing;
	private String letterSize;
	private String flash;
	private String textColor;
	private String backColor;
	private String verticalAlign;
	private String horizontalAlign;

	private String message;
	
	/**
	 * Default constructor.
	 */
	public LedSignDisplay(){
		this.group = 1;
		this.unit = 1;
		
		this.serial = 1;
		
		this.signalWidth = 128;
		this.signalHeight = 32;
		this.dstPort = 3001;
		try {
			this.netAddress = (Inet4Address) Inet4Address.getByName("192.168.0.100");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.inMode = TextFormat.PatternControl.I_MOVE_LEFT;
		this.outMode = TextFormat.PatternControl.O_MOVE_LEFT;
		this.speed = TextFormat.Speed.MED_FAST;
		this.pause = TextFormat.Pause.SEC_0000;
		this.lineSpacing = TextFormat.LineSpacing.LS1;
		this.letterSize = TextFormat.FontSize.EN_7X6;
		this.flash = TextFormat.Flash.OFF;
		this.textColor = TextFormat.TextColor.RED;
		this.backColor = TextFormat.Background.BLACK;
		this.verticalAlign = TextFormat.VerticalAlign.BOTTOM;
		this.horizontalAlign = TextFormat.HorizontalAlign.RIGHT;
	}
	
	public LedSignDisplay(int signalWidth, int signalHeight,
			Inet4Address netAddress, String inMode, String outMode,
			String speed, String pause, String lineSpacing, String letterSize,
			String flash, String textColor, String backColor,
			String verticalAlign, String horizontalAlign) {
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
		
		return UdpUtils.merge(
				TextFormat.HEAD,
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
				message,
				TextFormat.EOT
				).getBytes();
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
		StringBuilder sb = new StringBuilder( TextFormat.HEAD );
		sb.append(DisposalMode.DEFAULT);
		sb.append(lineSpacing);
		sb.append( pause + UdpUtils.ascii2hexString("0000") );
		sb.append(verticalAlign);
		sb.append(inMode);
		sb.append(outMode);
		sb.append(speed);
		sb.append(textColor);
		sb.append(backColor);
		sb.append(letterSize);
		sb.append(flash);
		sb.append(UdpUtils.ascii2hexString(message) );
		sb.append(TextFormat.LINE_FEED);
		sb.append(TextFormat.EOF);
		
		return publishBytes(encodeMessage(message));
	}

	/**
	 * Publishes a udp packet and returns the response
	 * @param bytes The data to publish.
	 * @return A string with the response.
	 */
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

	public String getInMode() {
		return inMode;
	}

	public void setInMode(String inMode) {
		this.inMode = inMode;
	}

	public String getOutMode() {
		return outMode;
	}

	public void setOutMode(String outMode) {
		this.outMode = outMode;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getPause() {
		return pause;
	}

	public void setPause(String pause) {
		this.pause = pause;
	}

	public String getLineSpacing() {
		return lineSpacing;
	}

	public void setLineSpacing(String lineSpacing) {
		this.lineSpacing = lineSpacing;
	}

	public String getLetterSize() {
		return letterSize;
	}

	public void setLetterSize(String letterSize) {
		this.letterSize = letterSize;
	}

	public String getFlash() {
		return flash;
	}

	public void setFlash(String flash) {
		this.flash = flash;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	public String getBackColor() {
		return backColor;
	}

	public void setBackColor(String backColor) {
		this.backColor = backColor;
	}
	
	public void setLanguageBackColor(String backColor) {
		switch(backColor){
		case "K":
			this.backColor = TextFormat.Background.BLACK;
			break;
		case "R":
			this.backColor = TextFormat.Background.RED;
			break;
		case "G":
			this.backColor = TextFormat.Background.GREEN;
			break;
		case "Y":
			this.backColor = TextFormat.Background.AMBER;
			break;
		default:
			logger.error("Invalid color "+ backColor + " for backgroud.");	
			break;
		}
		
	}

	public String getVerticalAlign() {
		return verticalAlign;
	}

	public void setVerticalAlign(String verticalAlign) {
		this.verticalAlign = verticalAlign;
	}

	public String getHorizontalAlign() {
		return horizontalAlign;
	}

	public void setHorizontalAlign(String horizontalAlign) {
		this.horizontalAlign = horizontalAlign;
	}

	public String getDescription() {
		return description;
	}

	public void setLanguageInMode(String inMode) {
		switch (inMode) {
		case "JO":
			this.inMode = TextFormat.PatternControl.I_JUMP_OUT;
			break;
		case "ML":
			this.inMode = TextFormat.PatternControl.I_MOVE_LEFT;
			break;
		case "MR":
			this.inMode = TextFormat.PatternControl.I_MOVE_RIGHT;
			break;
		case "SL":
			this.inMode = TextFormat.PatternControl.I_SCROLL_LEFT;
			break;
		case "SR":
			this.inMode = TextFormat.PatternControl.I_SCROLL_RIGHT;
			break;
		default:
			logger.error("Invalid In mode "+ inMode + ".");
			break;
		}
	}

	public void setLanguageOutMode(String outMode2) {
		switch (outMode2) {
		case "JO":
			this.outMode = TextFormat.PatternControl.O_JUMP_OUT;
			break;
		case "ML":
			this.outMode = TextFormat.PatternControl.O_MOVE_LEFT;
			break;
		case "MR":
			this.outMode = TextFormat.PatternControl.O_MOVE_RIGHT;
			break;
		case "SL":
			this.outMode = TextFormat.PatternControl.O_SCROLL_LEFT;
			break;
		case "SR":
			this.outMode = TextFormat.PatternControl.O_SCROLL_RIGHT;
			break;
		default:
			logger.error("Invalid OUT mode "+ outMode2 + ".");
			break;
		}
		
	}

	public void setLanguageLetterSize(String letterSize) {
		switch (letterSize) {
		case "0":
			this.letterSize = TextFormat.FontSize.EN_5X5;
			break;
		case "1":
			this.letterSize = TextFormat.FontSize.EN_7X6;
			break;
		case "2":
			this.letterSize = TextFormat.FontSize.EN_14X8;
			break;
		case "3":
			this.letterSize = TextFormat.FontSize.EN_15X9;
			break;
		case "4":
			this.letterSize = TextFormat.FontSize.EN_16X9;
			break;
		default:
			logger.error("Invalid font size "+ letterSize + ".");
			break;
		}
	}

	public void setLanguageLineSpacing(Integer lineSpacing2) {
		switch (lineSpacing2) {
		case 0:
			lineSpacing = TextFormat.LineSpacing.LS0;
			break;
		case 1:
			lineSpacing = TextFormat.LineSpacing.LS1;
			break;
		case 2:
			lineSpacing = TextFormat.LineSpacing.LS2;
			break;
		case 3:
			lineSpacing = TextFormat.LineSpacing.LS3;
			break;
		case 4:
			lineSpacing = TextFormat.LineSpacing.LS4;
			break;
		case 5:
			lineSpacing = TextFormat.LineSpacing.LS5;
			break;
		case 6:
			lineSpacing = TextFormat.LineSpacing.LS6;
			break;
		case 7:
			lineSpacing = TextFormat.LineSpacing.LS7;
			break;
		case 8:
			lineSpacing = TextFormat.LineSpacing.LS8;
			break;
		case 9:
			lineSpacing = TextFormat.LineSpacing.LS9;
			break;
		default:
			logger.error("Invalid linespacing "+ lineSpacing2 + ".");
			break;
		}
		
	}

	public void setLanguageSpeed(String speed2) {
	
		switch (speed2) {
		case "0":
			speed = TextFormat.Speed.VERY_FAST;
			break;
		case "1":
			speed = TextFormat.Speed.FAST;
			break;
		case "2":
			speed = TextFormat.Speed.MED_FAST;
			break;
		case "3":
			speed = TextFormat.Speed.MEDIUM;
			break;
		case "4":
			speed = TextFormat.Speed.MED_SLOW;
			break;
		case "5":
			speed = TextFormat.Speed.SLOW;
			break;
		case "6":
			speed = TextFormat.Speed.VERY_SLOW;
			break;
		default:
			logger.error("Invalid speed "+ speed2 + ".");
			break;
		}
	}

	public void setLanguageTextColor(String color) {
		switch(color){
		case "K":
			this.textColor = TextFormat.TextColor.BLACK;
			break;
		case "R":
			this.textColor = TextFormat.TextColor.RED;
			break;
		case "G":
			this.textColor = TextFormat.TextColor.GREEN;
			break;
		case "Y":
			this.textColor = TextFormat.TextColor.AMBER;
			break;
		default:
			logger.error("Invalid text color "+ color + ".");	
			break;
		}
		
	}

	public void setLanguageVerticalAlign(String vertical) {
		
		switch (vertical) {
		case "0":
			verticalAlign = TextFormat.VerticalAlign.CENTER;
			break;
		case "1":
			verticalAlign = TextFormat.VerticalAlign.TOP;
			break;
		case "2":
			verticalAlign = TextFormat.VerticalAlign.BOTTOM;
			break;
		default:
			logger.error("Invalid vertical align "+ vertical + ".");
			break;
		}
	}

	public void setLanguageHorizontalAlign(String horizontal) {
		switch (horizontal) {
		case "0":
			horizontalAlign = TextFormat.HorizontalAlign.CENTER;
			break;
		case "1":
			horizontalAlign = TextFormat.HorizontalAlign.LEFT;
			break;
		case "2":
			horizontalAlign = TextFormat.HorizontalAlign.RIGHT;
			break;
		default:
			logger.error("Invalid horizontal align "+ horizontal + ".");
			break;
		}
		
	}
	
	public void setGroup( int g){
		this.group = g;
	}
	
	public void setUnit(int u){
		this.unit = u;
	}
	////////////////////////////////////////////////////////////////
	
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
				
		String checkHex = UdpUtils.checksum(DatatypeConverter.parseHexBinary(array));
		
		System.out.println("check" + checkHex );
		
		String headSt = JetFile2Protocol.DATA_PREFIX_OUT;
		
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
	
	
	public void sentToDisplay(JetFile2Packet packet){
		publishBytes(packet.toBytes());
	}
	
	
	public boolean startDisplay(){
		// create a packet start
		JetFile2Packet out = new JetFile2Packet();
		out.setCommand(JetFile2Protocol.BlackScreenCommand.START);
		out.setGroup(group);
		out.setUnit(unit);
		out.setSerial(serial);
		
		JetFile2Packet in = new JetFile2Packet( publishBytes(out.toBytes()) );
		return in.getStatus().equals(JetFile2Protocol.SUCESS);
	}
	
	
	public boolean writeSysFile(){
		JetFile2Packet out = new JetFile2Packet();
		out.setCommand(JetFile2Protocol.InfoWrite.WRITE_SYS_FILE);
		out.setGroup(group);
		out.setUnit(unit);
		out.setSerial(serial);
		
		return false;
	}
	
	public boolean endDisplay(){
		// create a packet start
		JetFile2Packet out = new JetFile2Packet();
		out.setCommand(JetFile2Protocol.BlackScreenCommand.END);
		out.setGroup(group);
		out.setUnit(unit);
		out.setSerial(serial);
		
		JetFile2Packet in = new JetFile2Packet( publishBytes(out.toBytes()) );
		return in.getStatus().equals(JetFile2Protocol.SUCESS);
	}

	
}
