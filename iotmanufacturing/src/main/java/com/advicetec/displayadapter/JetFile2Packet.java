package com.advicetec.displayadapter;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.utils.UdpUtils;

public class JetFile2Packet{
	
	static Logger logger = LogManager.getLogger(JetFile2Packet.class.getName());
 
	private String syn;
	private String checksum;
	private int datalen;
	private String src;
	private int group;
	private int unit;
	private int packetSerial;
	private String command;
	private int arglen;
	private int flag;
	private String args;
	private String data;
	
	public JetFile2Packet() {
		syn = JetFile2Protocol.DATA_PREFIX_OUT;
		checksum = "00";
		datalen = 0;
		src = "0000";
		group = 0;
		unit = 0;
		packetSerial = 0;
		command = "";
		arglen = 0;
		flag = 0;
		args = "";
		data = "";
		
	}
	
	/**
	 * Creates an instance of JetFile packet from a hex string.
	 * @param hexString
	 */
	public JetFile2Packet(String hexString){
		syn = JetFile2Protocol.getSyncCode(hexString);
		checksum = JetFile2Protocol.getCheckNumber(hexString);
		datalen = Integer.parseInt(UdpUtils.swap(JetFile2Protocol.getDataLength(hexString)),16);
		src = JetFile2Protocol.getSourceAddress(hexString);
		group = Integer.parseInt(JetFile2Protocol.getGroup(hexString),16);
		unit = Integer.parseInt(JetFile2Protocol.getUnit(hexString),16);
		packetSerial = Integer.parseInt(UdpUtils.swap(JetFile2Protocol.getPacketSerial(hexString)),16);
		command = JetFile2Protocol.getCommands(hexString);
		arglen = Integer.parseInt(JetFile2Protocol.getArglen(hexString),16);
		flag = Integer.parseInt(JetFile2Protocol.getFlag(hexString),16);
		data = JetFile2Protocol.getData(arglen, datalen, hexString);
		args = JetFile2Protocol.getArgs(arglen,hexString);
		
	}

	public void setSyn(String syn) {
		this.syn = syn;
	}

	public void setDatalen() {
		this.datalen = data.length() / 2;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public void setUnit(int unit) {
		this.unit = unit;
	}

	public void setSerial(int serial) {
		this.packetSerial = serial;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public void setArgs(String hexStr) {
		this.args = hexStr;
		arglen = hexStr.length() / 8;
	}

	public void setData(String hexdata) {
		this.data = hexdata;
		datalen = hexdata.length() / 2;
	}

	public String getSyn() {
		return syn;
	}

	public int getDatalen() {
		return datalen;
	}

	public int getGroup() {
		return group;
	}

	public int getUnit() {
		return unit;
	}

	public int getSerial() {
		return packetSerial;
	}

	public String getCommand() {
		return command;
	}

	public int getFlag() {
		return flag;
	}

	public String getStatus(){
		if(getFlag() == 1)
			return data;
		return "";
	}
	public String getArgs() {
		return args;
	}

	public String getData() {
		return data;
	}
	
	public String getChecksum(){
		return checksum;
	}
	
	public void setChecksum(){
		checksum = UdpUtils.checksum(DatatypeConverter.parseHexBinary(bodyString()));
	}
	
	public String bodyString(){
		StringBuilder sb = new StringBuilder();
		sb.append(UdpUtils.int2HexString(datalen, 2));
		sb.append(src);
		sb.append(UdpUtils.int2HexString(group, 1));
		sb.append(UdpUtils.int2HexString(unit, 1));
		sb.append(UdpUtils.int2HexString(packetSerial, 2));
		sb.append(command);
		sb.append(UdpUtils.int2HexString(arglen, 1));
		sb.append(UdpUtils.int2HexString(flag, 1));
		sb.append(args);
		sb.append(data);
		return sb.toString();
	}
	
	/**
	 * 
	 * @return
	 */
	public String toHexString(){
		
		String body = bodyString();
		byte[] bytes = DatatypeConverter.parseHexBinary(body);
		String chk = UdpUtils.checksum(bytes);
		
		StringBuilder sb = new StringBuilder();
		sb.append(syn);
		sb.append(chk);
		sb.append(body);
		return sb.toString();
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		String body = bodyString();
		byte[] bytes = DatatypeConverter.parseHexBinary(body);
		String chk = UdpUtils.checksum(bytes);
		sb.append(syn);
		sb.append(chk);
		sb.append(body);
		return sb.toString();
	}

	/**
	 * Returns the packet as a byte array.
	 * @return
	 */
	public byte[] toBytes() {
		return DatatypeConverter.parseHexBinary(toHexString());
	}

	public int getArglen() {
		return arglen;
	}

	public String getDatalenHex() {
		return UdpUtils.int2HexString(datalen, 2);
	}
	
	
}