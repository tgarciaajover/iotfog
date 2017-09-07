package com.advicetec.iot.rest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

public class AttributeValueResource extends ServerResource
{

	/**
	 * Returns a set of measured attribute values. The system expects the following parameters:
	 * 
	 *  	- uniqueId : measure entity requested 
	 *  	- DttmFrom : start date-time from which the user requests the measured attribute values
	 *  	- DttmTo   : end date-time to which the user requests requests the measured attribute values
	 *  	- AttributeName	: name of the attribute requested
	 *  
	 * @return The JSON representation of the status, or CLIENT_ERROR_NOT_ACCEPTABLE if the unique ID is not present.
	 * 
	 * @throws Exception if problems occur making the representation.
	 * Shouldn't occur in practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getEntityAttribute(Representation representation) throws Exception {
		
		// Create an empty representation.
		Representation result;
		
		// Get the contact's uniqueID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));
		
		// Look for it in the database.
		MeasuredEntityFacade facade = MeasuredEntityManager.getInstance().getFacadeOfEntityById(uniqueID);
		if ( facade == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			result = new JsonRepresentation("");
		} else {
			
			JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);
			
			// Convert the Json representation to the Java representation.
			JSONObject jsonobject = jsonRepresentation.getJsonObject();
			String jsonText = jsonobject.toString();

			ObjectMapper mapper = new ObjectMapper(); 
			TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String,String>>() {};
			HashMap<String,String> o = mapper.readValue(jsonText, typeRef); 
			
			DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MMM-dd H:m:s.n");
			//SystemConstants.DATETIME_FORMAT;
			LocalDateTime dttmFrom = LocalDateTime.parse(o.get("DttmFrom"),format); 
			LocalDateTime dttmTo = LocalDateTime.parse(o.get("DttmTo"),format);		
			String jsonTextRet = facade.getByIntervalByAttributeNameJSON(o.get("AttributeName"), dttmFrom, dttmTo);
	
			if (jsonTextRet.length() == 0) {
				// The requested contact was not found, so set the Status to indicate this.
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
				result = new JsonRepresentation("");
			} 
			else {
				// The requested contact was found, so add the Contact's XML representation to the response.
				result = new JsonRepresentation(jsonTextRet);
				// Status code defaults to 200 if we don't set it.
			}
			// Return the representation.  The Status code tells the client if the representation is valid.
		}
		
		return result;
	}	
	
}
