package com.advicetec.iot.rest;

import java.util.Collection;

import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.advicetec.core.Attribute;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

/**
 * A server resource that will handle requests regarding a specific Contact.
 * Supported operations: GET, PUT, and DELETE.
 * Supported representations: XML.
 */
public class StatusResource extends ServerResource {

	/**
	 * Returns the Status instance requested by the URL. 
	 * @return The XML representation of the status, or CLIENT_ERROR_NOT_ACCEPTABLE if the unique ID is not present.
	 * 
	 * @throws Exception If problems occur making the representation.
	 * Shouldn't occur in practice but if it does, Restlet will set the Status code. 
	 */
	@Get
	public Representation getEntityStatus() throws Exception {
		
		// Create an empty XML representation.
		DomRepresentation result = new DomRepresentation();
		// Get the contact's uniqueID from the URL.
		String uniqueID = (String)this.getRequestAttributes().get("uniqueID");
		// Look for it in the Entity Manager database.

		MeasuredEntityFacade facade = MeasuredEntityManager.getInstance().getFacadeOfEntityById(uniqueID); 
		Collection<Attribute> collection = facade.getStatus();

		if (collection.size() == 0) {
			// The requested contact was not found, so set the Status to indicate this.
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
		} 
		else {
			// The requested contact was found, so add the Contact's XML representation to the response.
			result.setDocument(facade.getXmlStatus());
			// Status code defaults to 200 if we don't set it.
		}
		// Return the representation.  The Status code tells the client if the representation is valid.
		return result;
	}

}