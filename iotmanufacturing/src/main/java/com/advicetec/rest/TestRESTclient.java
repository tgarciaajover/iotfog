package com.advicetec.rest;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

public class TestRESTclient {

	public static void main(String[] args) {
		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);
		WebTarget target = client.target(getBaseURI());

		String response = target.path("rest").path("machine").request()
				.accept(MediaType.TEXT_PLAIN).get(Response.class).toString();

		String plain = target.path("rest").path("machine").request()
				.accept(MediaType.TEXT_PLAIN).get(String.class).toString();
		String xml = target.path("rest").path("machine").request()
				.accept(MediaType.TEXT_XML).get(String.class).toString();
		String html = target.path("rest").path("machine").request()
				.accept(MediaType.TEXT_HTML).get(String.class).toString();

		System.out.println(response+"\n"+ plain+"\n"+xml+"\n"+html);

	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8080/com.advicetec.rest").build();
	}
}
