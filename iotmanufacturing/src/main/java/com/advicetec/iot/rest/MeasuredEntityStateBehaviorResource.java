package com.advicetec.iot.rest;

import java.io.IOException;

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
	 * Returns the MeasuredEntity instance requested by the URL. 
	 * 
	 * @return The JSON representation of the Measured Entity, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	 * unique ID is not present.
	 * 
	 * @throws Exception If problems occur making the representation. Shouldn't occur in 
	 * practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getMeasuredEntityStateBehavior() throws Exception {

		// Create an empty JSon representation.
		Representation result;

		// Get the contact's uniqueID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Look for it in the Measured Entity database.
		MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

		if (measuredEntityFacade == null) {
			// The requested contact was not found, so set the Status to indicate this.
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			result = new JsonRepresentation("");
		} 
		else {
			Integer behaviorID = Integer.valueOf((String)this.getRequestAttributes().get("BehaviorID"));

			// The requested contact was found, so add the Contact's XML representation to the response.
			if (measuredEntityFacade.getEntity() != null){
				if (measuredEntityFacade.getEntity().getStateBehavior(behaviorID) != null){
					// Status code defaults to 200 if we don't set it.
					result = new JsonRepresentation(measuredEntityFacade.getEntity().getStateBehavior(behaviorID).toJson());
				} else {
					getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
					result = new JsonRepresentation("");
				}
			} else {
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
				result = new JsonRepresentation("");
			}
		}

		// Return the representation.  The Status code tells the client if the representation is valid.
		return result;
	}
	  
	/**
	 * Adds the passed MeasuredEntity to our internal database of Measured Entities.
	 * @param representation The Json representation of the new Contact to add.
	 * 
	 * @return null.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Put("json")
	public Representation putMeasuredEntityStateBehavior(Representation representation) throws Exception {

		Representation result = null;

		// Get the Json representation of the SignalUnit.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Get the contact's uniqueID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Convert the Json representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();

		logger.debug("jsonText received:" + jsonText);
		
		// Look for it in the Signal Unit database.
		MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();

		if (measuredEntityManager.getFacadeOfEntityById(uniqueID) != null){

			  MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

			  // The requested contact was found, so add the Contact's XML representation to the response.
			  if (measuredEntityFacade.getEntity() != null){
				  
				  logger.debug("MeasuredEntityFacade found");
				  
				  try{
					  
					  ObjectMapper mapper = new ObjectMapper();
					  MeasuredEntityStateBehavior behavior = mapper.readValue(jsonText, MeasuredEntityStateBehavior.class);
					  
					  logger.debug("object reveived:" + behavior.getId() + behavior.getDescr() );
					  
					  measuredEntityFacade.getEntity().putStateBehavior(behavior.getId(), behavior.getStateBehaviorType(), 
							  behavior.getDescr(), behavior.getBehavior_text());
					  
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
	  
}
