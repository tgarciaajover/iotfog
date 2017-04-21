package com.advicetec.rest;

import org.restlet.resource.Get;

//@Path("/rest")
public class RESTservice {

	@Get
	//@Produces(MediaType.TEXT_PLAIN)
	public String sayPlainTextHello(){
		return "HOLA";
	}
	
	@Get
	//@Produces(MediaType.TEXT_XML)
	public String sayXMLHello(){
		return "<?xml version\"1.0\"?>" + "<hello>HOLA</hello>";
	}
	
	@Get
	//@Produces(MediaType.TEXT_HTML)
	public String sayHtmlHello(){
		return "<html> " + "<title>" + "Hello Jersey" + "</title>"
		        + "<body><h1>" + "Hello Jersey" + "</body></h1>" + "</html> ";
	}
	
}
