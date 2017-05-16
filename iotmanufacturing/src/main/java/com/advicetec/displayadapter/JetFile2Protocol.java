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
