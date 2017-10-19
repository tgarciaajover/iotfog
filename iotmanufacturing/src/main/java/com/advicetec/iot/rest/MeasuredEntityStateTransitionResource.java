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
import com.advicetec.configuration.MonitoringDeviceContainer;
import com.advicetec.configuration.SignalUnit;
import com.advicetec.configuration.SignalUnitContainer;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityContainer;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuredEntityStateTransition;

public class MeasuredEntityStateTransitionResource extends ServerResource  
{
	static Logger logger = LogManager.getLogger(MeasuredEntityStateTransitionResource.class.getName());


	/**
	 * Returns the MeasuredEntity State Transition instance requested by the URL. 
	 * 
	 * @return The JSON representation of the Measured Entity State Transition, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	 * unique ID is not present.
	 * 
	 * @throws Exception: if problems occur making the representation. Shouldn't occur in 
	 * 		 practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getMeasuredEntityStateTransition() throws Exception {

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
			Integer transitionID = Integer.valueOf((String)this.getRequestAttributes().get("TransitionID"));

			// The requested contact was found, so add the Contact's XML representation to the response.
			if (measuredEntityFacade.getEntity() != null){
				if (((MeasuredEntity) measuredEntityFacade.getEntity()).getStateTransition(transitionID) != null){
					// Status code defaults to 200 if we don't set it.
					result = new JsonRepresentation(((MeasuredEntity) measuredEntityFacade.getEntity()).getStateTransition(transitionID).toJson());
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
	 * Adds the passed MeasuredEntity State Transition to our internal database of Measured Entities.
	 * @param representation The Json representation of the new State Transition to add.
	 * 
	 * @return null.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Put("json")
	public Representation putMeasuredEntityStateTransition(Representation representation) throws Exception {

		logger.info("putMeasureEntityStateTransition");
		Representation result = null;

		// Get the Json representation of the SignalUnit.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Get the contact's uniqueID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Convert the Json representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();

		// Look for it in the Signal Unit database.
		MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityContainer container = measuredEntityManager.getMeasuredEntityContainer();

		logger.debug("jsonTxt:" + jsonText);

		if (measuredEntityManager.getFacadeOfEntityById(uniqueID) != null){

			MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

			// The requested contact was found, so add the Contact's XML representation to the response.
			if (measuredEntityFacade.getEntity() != null){

				logger.debug("MeasuredEntityFacade found");

				try{
					ObjectMapper mapper = new ObjectMapper();
					MeasuredEntityStateTransition transition = mapper.readValue(jsonText, MeasuredEntityStateTransition.class);
					((MeasuredEntity) measuredEntityFacade.getEntity()).putStateTransition(transition.getId(), transition.getStateFrom(), 
							transition.getResonCode(), transition.getBehavior(), transition.getCreateDate());

					logger.debug("putMeasureEntityStateTransition OK");

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

		return result;
	}


	/**
	 * Deletes the passed MeasuredEntity State Transition in our internal database of Measured Entities.
	 * @param Json representation of the measured entity state transition to delete.
	 * 
	 * @return null.
	 * @throws SQLException 
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Delete("json")
	public Representation deleteMeasuredEntityStateTransition() throws SQLException {

		Representation result;

		// Get the Measured Entity's uniqueID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Get the Transition Id
		String transitionIdStr = (String)this.getRequestAttributes().get("TransitionID");
		if (transitionIdStr != null){

			try{
				Integer transitionId = Integer.valueOf(transitionIdStr);

				ConfigurationManager confManager = ConfigurationManager.getInstance();

				// Look for it in the Measured Entity database.
				MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();

				// Get the measuring entity facade. 
				MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

				((MeasuredEntity) measuredEntityFacade.getEntity()).removeStateTransition(transitionId);

				getResponse().setStatus(Status.SUCCESS_OK);

			} catch (NumberFormatException e) {
				String error = "The value given in state transition is not a valid number";
				logger.error(error);
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);

			}

		}

		else {
			String error = "transitionId was not provided";
			logger.error(error);
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
		}

		result = new JsonRepresentation("");	
		return result;
	}
}
