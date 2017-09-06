package com.advicetec.iot.rest;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.data.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.configuration.ReasonCodeContainer;

/**
 * This class exposes all reason code instances that are configured in the reason code container.
 * 
 * The user of this interface can retry the reason code definition, inserts a new reason code or deletes a registered one.
 *   
 * @author Andres Marentes
 *
 */
public class ReasonCodeResource extends ServerResource  
{
	
	static Logger logger = LogManager.getLogger(ReasonCodeResource.class.getName());
		
	/**
	 * Returns the Reason Code instance requested by the URL. 
	 * 
	 * @return The JSON representation of the Reason Code, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	 * unique ID is not present.
	 * 
	 * @throws Exception if problems occur making the representation. Shouldn't occur in 
	 * practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getReasonCode() throws Exception {

		// Creates an empty JSon representation.
		Representation result;

		// Gets the requested Contact ID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Looks for the reason code in the Reason Code container.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		ReasonCodeContainer reasonCodeCon = confManager.getReasonCodeContainer();

		ReasonCode reasonCode = (ReasonCode) reasonCodeCon.getObject(uniqueID);
		if (reasonCode == null) {
			// The requested Reason Code was not found, so we set the status to indicate this fail condition.
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			result = new JsonRepresentation("");
		} 
		else {
			// The requested reason code was found, so add the Reason Code's JSON representation to the response.
			result = new JsonRepresentation(reasonCode.toJson());
			// Status code defaults to 200 if we don't set it.
		}
		// Return the representation.  The Status code tells the client if the representation is valid.
		return result;
	}

	/**
	 * Adds the given Reason Code to the internal container of Reason Codes.
	 * 
	 * @param representation The Json representation of the new Reason Code to add.
	 * 
	 * @return null.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Put("json")
	@Post("json")
	public Representation putReasonCode(Representation representation) throws Exception {

		// Creates an empty JSon representation.
		Representation result;

		// Gets the Json representation of the ReasonCode.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Converts the Json representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();

		logger.info("jsonText:" + jsonText);

		// Look for it in the Reason Code database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		ReasonCodeContainer reasonCodeCon = confManager.getReasonCodeContainer();

		// This replace the reason code in the container.
		boolean res = reasonCodeCon.fromJSON(jsonText);

		if (res == true){
			getResponse().setStatus(Status.SUCCESS_OK);
		} else {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}

		result = new JsonRepresentation("");
		return result;
	}

	/**
	 * Deletes the reason code identified by uniqueID from the internal container. 
	 * @return null.
	 */
	@Delete("json")
	public Representation deleteReasonCode() {
		// Get the requested Contact ID from the URL.
		int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Make sure it is no longer present in the Signal Unit database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		ReasonCodeContainer reasonCodeCon = confManager.getReasonCodeContainer();

		reasonCodeCon.deleteReasonCode(uniqueID);
		return null;
	}	
}
