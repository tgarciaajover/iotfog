package com.advicetec.iot.rest;

import javax.xml.ws.Endpoint;

public class RestEndpoint {

	public static void main(String[] args) {
		String address = "http://127.0.0.1:8023/WebServiceDemo";
		//Endpoint.publish(address, new RESTservice());
		System.out.println("Listening: "+ address);
	}

}
