package com.advicetec.iot.rest;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.data.Status;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityContainer;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

public class MeasuredEntityResource extends ServerResource  
{

	static Logger logger = LogManager.getLogger(MeasuredEntityResource.class.getName());

	/**
	 * Returns the measured entity instance requested by the URL. 
	 * 
	 * @return The JSON representation of the measured entity, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	 * unique ID is not registered.
	 * 
	 * @throws Exception If problems occur making the representation. Shouldn't occur in 
	 * practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getMeasuredEntity() throws Exception {

		// Create an empty JSON representation.
		Representation result;

		// Get the measured entity unique ID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Looks for identifier in the measured entity container.
		MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

		if (measuredEntityFacade == null) {
			// The requested measured entity was not found, so set the status to indicate this condition.
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			result = new JsonRepresentation("");
		} 
		else {
			// The requested measured entity was found, so we add the measured entity to the JSON response representation.
			result = new JsonRepresentation(((MeasuredEntity) measuredEntityFacade.getEntity()).toJson());
			// Status code defaults to 200 if we don't set it.
		}
		// Return the representation.  The Status code tells the client if the representation is valid.
		return result;
	}

	/**
	 * Adds the given measured entity to the measured entity container.
	 * @param representation The JSON representation of the new measured entity to add.
	 * 
	 * If it already exists on the system, then we update the definition.
	 * 
	 * @return null.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Put("json")
	public Representation putMeasuredEntity(Representation representation) throws Exception {

		// Get the JSON representation of the measured entity.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Convert the Json representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();

		// Look for it in the Signal Unit database.
		MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityContainer container = measuredEntityManager.getMeasuredEntityContainer();

		MeasuredEntity measuredEntity = container.fromJSON(jsonText);

		// Creates the facade for the measuring entity.
		if (measuredEntityManager.getFacadeOfEntityById(measuredEntity.getId()) == null){
			measuredEntityManager.addNewEntity(measuredEntity);
		}

		// Sets the status code as success.
		getResponse().setStatus(Status.SUCCESS_OK);	    
		Representation result = new JsonRepresentation("");
		return result;

	}

	/**
	 * Deletes the measured entity identified by uniqueID from the container.
	 * 
	 * This function assumes that all related objects associated 
	 * 	with the measured entity were previously deleted 
	 *  
	 * @return null.
	 * 
	 * @throws SQLException 
	 */
	@Delete("json")
	public Representation deleteMeasuredEntity() throws SQLException {

		// Creates an empty JSON representation.
		Representation result;

		// Gets the requested measured entity ID from the URL.
		String uniqueIdStr = (String) this.getRequestAttributes().get("uniqueID");

		if (uniqueIdStr == null){
			String error = "The request should include a uniqueId";
			logger.error(error);
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
			result = new JsonRepresentation("");
			return result;
		} 

		try 
		{	
			Integer uniqueID = Integer.valueOf(uniqueIdStr);

			// Looks for the measured entity identifier in the Measured Entity container.
			MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
			
			MeasuredEntityFacade measuredEntityFacade =  measuredEntityManager.getFacadeOfEntityById(uniqueID);
			
			if (measuredEntityFacade != null) {
			
				measuredEntityManager.removeMeasuredEntity(measuredEntityFacade.getEntity().getId(), measuredEntityFacade.getEntity().getType());

				getResponse().setStatus(Status.SUCCESS_OK);	    
				
			} else {
				String error = "The measured entity :" + uniqueID.toString() + " was not found";
				logger.error(error);
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);

			}
			
			result = new JsonRepresentation("");
			return result;

		} catch (NumberFormatException e) {
			String error = "The value given in uniqueId is not a valid measured entity";
			logger.error(error);
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
			result = new JsonRepresentation("");
			return result;

		}
	}	  
}
