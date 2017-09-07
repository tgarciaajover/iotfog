package com.advicetec.iot.rest;

import java.io.IOException;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

/**
 * This class exposes the status of a measured entity to users. The user should specify the measured
 * entity by their canonical codes.
 * 
 * The user of this interface can retry the status for a measured entity.
 *   
 * @author Andres Marentes
 */
public class StatusResource extends ServerResource {
	
	static final Logger logger = LogManager.getLogger(StateResource.class.getName());
	
	/**
	 * Canonical machine identifier 
	 */
	private String canMachineId;
	
	/**
	 * Canonical company identifier
	 */
	private String canCompany;
	
	/**
	 * Canonical location identifier
	 */
	private String canLocation;
	
	/**
	 * Canonical plant identifier
	 */
	private String canPlant;
	
	/**
	 * Canonical machine group identifier
	 */
	private String canMachineGroup;

	/**
	 * Obtains and verifies the parameters from a JSON representation.
	 * 
	 * @param representation  JSON representation that maintains the parameters for the interface.
	 * 
	 * It does not have a return value, but it registers parameters in the class's attributes.   
	 */
	private void getParamsFromJson(Representation representation) {
		
		try {			
			// Gets the JSON representation of the ReasonCode.
			JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

			// Converts the JSON representation to the Java representation.
			JSONObject jsonobject = jsonRepresentation.getJsonObject();
			
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
	 *  
	 * @return The XML representation of the status, or CLIENT_ERROR_NOT_ACCEPTABLE if the unique ID is not present.
	 * 
	 * @throws Exception If problems occur making the representation.
	 * Shouldn't occur in practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getEntityStatus(Representation rep) throws Exception {
		
		// Creates an empty representation.
		Representation result=null;
		
		// Gets the canonical codes to obtain a unique measured entity ID from the URL.
		this.canCompany = getQueryValue("company");
		this.canLocation = getQueryValue("location");
		this.canPlant = getQueryValue("plant");
		this.canMachineGroup = getQueryValue("machineGroup");
		this.canMachineId = getQueryValue("machineId");
		
		if (canMachineId == null) {
			getParamsFromJson(rep);
		}

		try {

			Integer uniqueID = MeasuredEntityManager.getInstance()
					.getMeasuredEntityId(this.canCompany,this.canLocation,this.canPlant,this.canMachineGroup, this.canMachineId);
			
			// Looks for the measured entity in the entity manager.
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
					// The requested measured entity was not found, so we set the status to indicate this failing condition.
					getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
					result = new JsonRepresentation("");
				} 
				else {
					// The requested measured entity was found, so we add the status to the JSON response representation.
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