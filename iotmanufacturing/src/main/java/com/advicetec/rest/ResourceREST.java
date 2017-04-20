package com.advicetec.rest;

import javax.ws.rs.core.MediaType;

import org.restlet.data.Metadata;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

public class ResourceREST extends ServerResource {

	@Get
	public Representation toXml(){
		return null;
	}
	
	@Post("json")
	public Representation accept(Representation entity){

		return null;
	}
}
