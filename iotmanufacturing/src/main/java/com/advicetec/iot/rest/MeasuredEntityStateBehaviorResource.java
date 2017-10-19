package com.advicetec.iot.rest;

import java.io.IOException;
import java.sql.SQLException;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.SignalUnit;
import com.advicetec.configuration.SignalUnitContainer;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityContainer;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuredEntityStateBehavior;
import com.advicetec.measuredentitity.MeasuredEntityStateTransition;

public class MeasuredEntityStateBehaviorResource extends ServerResource  
{
      
	static Logger logger = LogManager.getLogger(MeasuredEntityStateBehaviorResource.class.getName());
	
	/**
	 * Returns the measured entity state behavior requested by the URL. 
	 * 
	 * @return The JSON representation of the measured entity state behavior, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	 * unique ID is not present.
	 * 
	 * @throws Exception If problems occur making the representation. Shouldn't occur in 
	 * practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getMeasuredEntityStateBehavior() throws Exception {

		// Creates an empty JSON representation.
		Representation result;

		// Gets the measured entity uniqueID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Looks for the measured entity in the Measured Entity manager.
		MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

		if (measuredEntityFacade == null) {
			// The requested measured entity was not found, so we set the status to indicate this failing condition.
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			result = new JsonRepresentation("");
		} 
		else {			
			if (measuredEntityFacade.getEntity() != null){
				// The requested measured entity was found.
				Integer behaviorID = Integer.valueOf((String)this.getRequestAttributes().get("BehaviorID"));
				if (((MeasuredEntity) measuredEntityFacade.getEntity()).getStateBehavior(behaviorID) != null){
					// We found the behavior state, so we can add it to the JSON response. Status code defaults to 200 if we don't set it.
					result = new JsonRepresentation(((MeasuredEntity) measuredEntityFacade.getEntity()).getStateBehavior(behaviorID).toJson());
				} else {
					String error = "The requested measured entity state behavior was not found";
					logger.warn(error);
					getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
					result = new JsonRepresentation("");
				}
			} else {
				String error = "Undefined measured entity";
				logger.warn(error);
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
				result = new JsonRepresentation("");
			}
		}

		// Returns the representation.  The Status code tells the client if the representation is valid.
		return result;
	}
	  
	/**
	 * Adds the given measured entity state behavior to measured entity container.
	 * 
	 * @param representation The JSON representation of the measured entity state behavior to add.
	 * 
	 * @return null.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Put("json")
	public Representation putMeasuredEntityStateBehavior(Representation representation) throws Exception {

		Representation result = null;

		// Gets the JSON representation of the request.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Gets the measured entity uniqueID from the request.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Converts the JSON representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();

		logger.debug("jsonText received:" + jsonText);
		
		// Looks for the measured entity in the measured entity manager.
		MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();

		if (measuredEntityManager.getFacadeOfEntityById(uniqueID) != null){

			  MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

			  // The requested measured entity was found
			  if (measuredEntityFacade.getEntity() != null){
				  
				  logger.debug("MeasuredEntityFacade found");
				  
				  try{
					  
					  ObjectMapper mapper = new ObjectMapper();
					  MeasuredEntityStateBehavior behavior = mapper.readValue(jsonText, MeasuredEntityStateBehavior.class);
					  
					  logger.debug("object reveived:" + behavior.getId() + behavior.getDescr() );
					  
					  ((MeasuredEntity) measuredEntityFacade.getEntity()).putStateBehavior(behavior);
					  
					  logger.debug("putMeasuredEntityStateBehavior OK");
					  
					  getResponse().setStatus(Status.SUCCESS_OK);
					  result = new JsonRepresentation("");
					  
				  } catch (IOException e){
					  logger.error(e.getMessage());
					  getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					  result = new JsonRepresentation("");
				  }

			  } else {
				  logger.error("MeasuredEntity not found");
				  getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
				  result = new JsonRepresentation("");
			  }


		} else {
			logger.error("MeasuredEntityFacade not found");
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			result = new JsonRepresentation("");	    	
		}

		getResponse().setStatus(Status.SUCCESS_OK);
		return result;
	}

	/**
	 * Deletes the given MeasuredEntity State Behavior in the measured entity container.
	 * 
	 * @param Json representation of the measured entity State Behavior to delete.
	 * 
	 * @return null.
	 * 
	 * @throws SQLException 
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Delete("json")
	public Representation deleteMeasuredEntityStateBehavior() throws SQLException {

		Representation result;

		// Get the Measured Entity's uniqueID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Get the Scheduled Event Id
		String behaviorIdStr = (String)this.getRequestAttributes().get("BehaviorID");
		if (behaviorIdStr != null){

			try{
				Integer behaviorId = Integer.valueOf(behaviorIdStr);

				// Look for it in the Measured Entity database.
				MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();

				// Get the measuring entity facade. 
				MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

				// Deletes the state behavior from the measured entity.
				((MeasuredEntity) measuredEntityFacade.getEntity()).removeStateBehavior(behaviorId);

				getResponse().setStatus(Status.SUCCESS_OK);

			} catch (NumberFormatException e) {
				String error = "The value given in State Behavior is not a valid number";
				logger.error(error);
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);

			}
		} else {
			String error = "behaviorId was not provided";
			logger.error(error);
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
		}

		result = new JsonRepresentation("");	
		return result;
	}
}
