package com.advicetec.iot.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.Attribute;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

/**
 * A server resource that will handle requests regarding a specific Contact.
 * Supported operations: GET, PUT, and DELETE.
 * Supported representations: XML.
 */
public class StatusResource extends ServerResource {
	
	static final Logger logger = LogManager.getLogger(StateResource.class.getName());
	
	private String canMachineId;
	private String canCompany;
	private String canLocation;
	private String canPlant;
	private String canMachineGroup;

	private void getParamsFromJson(Representation representation) {
		
		try {
			// Get the information from the request json object
			
			// Get the Json representation of the ReasonCode.
			JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

			// Convert the Json representation to the Java representation.
			JSONObject jsonobject = jsonRepresentation.getJsonObject();
			String jsonText = jsonobject.toString();
			
			this.canCompany = jsonobject.getString("company");
			this.canLocation = jsonobject.getString("location");
			this.canPlant = jsonobject.getString("plant");
			this.canMachineGroup = jsonobject.getString("machineGroup");
			this.canMachineId = jsonobject.getString("machineId");

		} catch (JSONException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Returns the Status instance requested by the URL. 
	 * @return The XML representation of the status, or CLIENT_ERROR_NOT_ACCEPTABLE if the unique ID is not present.
	 * 
	 * @throws Exception If problems occur making the representation.
	 * Shouldn't occur in practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getEntityStatus(Representation rep) throws Exception {
		
		// Create an empty representation.
		Representation result=null;
		
		// Get the contact's uniqueID from the URL.
		this.canCompany = getQueryValue("company");
		this.canLocation = getQueryValue("location");
		this.canPlant = getQueryValue("plant");
		this.canMachineGroup = getQueryValue("machineGroup");
		this.canMachineId = getQueryValue("machineId");
		
		if (canMachineId == null) {
			getParamsFromJson(rep);
		}

		try {

			//Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));
			Integer uniqueID = MeasuredEntityManager.getInstance()
					.getMeasuredEntityId(this.canCompany,this.canLocation,this.canPlant,this.canMachineGroup, this.canMachineId);
			// Look for it in the Entity Manager database.

			if ( uniqueID == null) {
				logger.error("Measured Entity for company:" + this.canCompany +
						" location:" + this.canLocation + " Plant:" + this.canPlant +
						"machineGroup" + this.canMachineGroup + " machineId:" + 
						this.canMachineId + " was not found");
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
				result = new JsonRepresentation("");
			} else {

				MeasuredEntityFacade facade = MeasuredEntityManager.getInstance().getFacadeOfEntityById(uniqueID);

				JSONArray jsonArray = facade.getStatusJSON();

				if (jsonArray.length() == 0) {
					// The requested contact was not found, so set the Status to indicate this.
					getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
					result = new JsonRepresentation("");
				} 
				else {
					// The requested contact was found, so add the Contact's XML representation to the response.
					result = new JsonRepresentation(jsonArray);
					// Status code defaults to 200 if we don't set it.
				}
				// Return the representation.  The Status code tells the client if the representation is valid.
			}
		} catch (SQLException e) {
			logger.error("SQL failure.");
			e.printStackTrace();
		}
		
		return result;
	}

}