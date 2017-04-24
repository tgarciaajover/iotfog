package com.advicetec.iot.rest;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/saludo")
@WebService
public class RESTservice {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String sayPlainTextHola(){
		return "HOLA";
	}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	public String sayXMLHola(){
		return "<?xml version\"1.0\"?>" + "<hello>Hola hola</hello>";
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sayHtmlHola(){
		return "<html> " + "<title>" + "Hello Jersey" + "</title>"
		        + "<body><h1>" + "Hello Jersey" + "</body></h1>" + "</html> ";
		
	}
	
	@WebMethod
	public String getName(){
		return "my name";
	}
}
