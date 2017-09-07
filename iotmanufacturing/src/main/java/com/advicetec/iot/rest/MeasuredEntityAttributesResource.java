package com.advicetec.iot.rest;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

/**
 * This class exposes the attributes registered in any measured entity. By using this interface, users can review 
 * the attributes marked as trend and registered and their definition.
 * 
 *  In order to call the interface the user has to reference the measured entity by its canonical keys.  
 *    
 * @author Ferney Maldonado
 *
 */
public class MeasuredEntityAttributesResource extends ServerResource  
{
	
	static Logger logger = LogManager.getLogger(MeasuredEntityAttributesResource.class.getName());

	/**
	 * Canonical company code
	 */
	private String canCompany;
	
	/**
	 * Canonical location code
	 */
	private String canLocation;
	
	/**
	 * Canonical plant code
	 */
	private String canPlant;
	
	/**
	 * Canonical machine group code
	 */
	private String canMachineGroup;
	
	/**
	 * Canonical machine code
	 */
	private String canMachineId;
	
	
	/**
	 * Obtains and verifies the parameters from a JSON representation.
	 * 
	 * @param representation  JSON representation that maintains the parameters for the interface.
	 * 
	 * It does not have a return value, but it registers the parameters in the class's attributes.   
	 */
	private void getParamsFromJson(Representation representation) {
		
		try {
			
			// Gets the Json representation with all parameters.
			JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

			// Converts the Json representation to a Java representation.
			JSONObject jsonobject = jsonRepresentation.getJsonObject();
			
			// Gets the parameters required to process the interface.
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
	 * Gets the attributes marked as trend in the measured entity. The following is the list of expected parameters:
	 * 
	 * 	company
	 * 	location
	 * 	plant
	 *  machineGroup
	 *  machineId
	 * 
	 * This interface is case sensitive.
	 * 
	 * @param representation The Json representation of the parameters to reference a measured entity.
	 * 
	 * @return null.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	*/
	@Get("json")
	public Representation getMeasuredAttributes(Representation representation) throws Exception {
 
		Representation result = null;

		
		// get the query parameters given in the URL 
		this.canCompany = getQueryValue("company");
		this.canLocation = getQueryValue("location");
		this.canPlant = getQueryValue("plant");
		this.canMachineGroup = getQueryValue("machineGroup");
		this.canMachineId = getQueryValue("machineId");

		if (canMachineId == null) {
			// if parameters were not provided in the URL, then we search in the json representation.
			getParamsFromJson(representation);
		}

		try {
			Integer uniqueID = MeasuredEntityManager.getInstance()
					.getMeasuredEntityId(this.canCompany,this.canLocation,this.canPlant, 
													this.canMachineGroup,this.canMachineId);
			// Look for it in the database.
			
			if (uniqueID == null) {
				logger.error("Measured Entity for company:" + this.canCompany +
						 " location:" + this.canLocation + " Plant:" + this.canPlant + 
						 "Machine Group:"  + this.canMachineGroup + 
						 " machineId:" + this.canMachineId + " was not found");
				result = new JsonRepresentation("");
			}
			
			MeasuredEntityFacade facade = MeasuredEntityManager.getInstance()
					.getFacadeOfEntityById(uniqueID);

			if(facade == null){
				result = new JsonRepresentation("");
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
				logger.error("Facade does not found:"+uniqueID);
				result = new JsonRepresentation("");
			} else {
								
				// get the array of attributes from the facade.
				JSONArray jsonArray = facade.getJsonAttributeTrend();
				result = new JsonRepresentation(jsonArray);
			}
		} catch (SQLException e) {
			logger.error("SQL failure.");
			e.printStackTrace();
		}
		return result;	
	}	
}
