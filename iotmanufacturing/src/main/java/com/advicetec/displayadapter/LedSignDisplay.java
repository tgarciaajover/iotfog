package com.advicetec.displayadapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.displayadapter.Display;
import com.advicetec.displayadapter.Display.Background;
import com.advicetec.displayadapter.Display.Color;
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
	private Inet4Address netAddress;
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
		this.textColor = Color.RED;
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
		String s = null;
		try {
			byte[] toSend = encodeMessage(message);
			byte[] toReceive = new byte[1024];
			DatagramPacket packet = new DatagramPacket(toSend, toSend.length,netAddress, dstPort);
			DatagramSocket socket = new DatagramSocket();
			socket.send(packet);
			
			// receives the response
			socket.setSoTimeout(10000);
			DatagramPacket received = new DatagramPacket(toReceive,toReceive.length);
			socket.receive(received);
			s = new String(received.getData());
			socket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Error sending the message:"+message);
			e.printStackTrace();
		}
		
		return s;
	}
	
}
