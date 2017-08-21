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
import com.advicetec.measuredentitity.MeasuredEntityScheduledEvent;
import com.advicetec.measuredentitity.MeasuredEntityStateTransition;

public class MeasuredEntityScheduledEventResource extends ServerResource  
{
	static Logger logger = LogManager.getLogger(MeasuredEntityScheduledEventResource.class.getName());


	/**
	 * Returns the MeasuredEntity Scheduled Event instance requested by the URL. 
	 * 
	 * @return The JSON representation of the Measured Entity Scheduled Event, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	 * unique ID is not present.
	 * 
	 * @throws Exception: if problems occur making the representation. Shouldn't occur in 
	 * 		 practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getMeasuredEntityScheduledEvent() throws Exception {

		// Create an empty JSon representation.
		Representation result;

		// Get the contact's uniqueID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Look for it in the Measured Entity database.
		MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

		if (measuredEntityFacade == null) {
			// The requested contact was not found, so set the Status to indicate this.
			String error = "The measured entity facade with id: " + Integer.toString(uniqueID) + " was not found";
			logger.error(error);
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
			result = new JsonRepresentation("");
		} 
		else {
			Integer eventID = Integer.valueOf((String)this.getRequestAttributes().get("EventID"));

			// The requested contact was found, so add the Contact's XML representation to the response.
			if (measuredEntityFacade.getEntity() != null){
				if (measuredEntityFacade.getEntity().getScheduledEvent(eventID) != null){
					// Status code defaults to 200 if we don't set it.
					result = new JsonRepresentation(measuredEntityFacade.getEntity().getScheduledEvent(eventID).toJson());
				} else {
					String error = "The scheduled event was not found";
					logger.error(error);
					getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
					result = new JsonRepresentation("");
				}
			} else {
				String error = "The measured entity with Id: " + Integer.toString(uniqueID) +  " is invalid";
				logger.error(error);
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
				result = new JsonRepresentation("");
			}

		}
		// Return the representation.  The Status code tells the client if the representation is valid.
		return result;
	}

	/**
	 * Adds the passed MeasuredEntity Scheduled Event to our internal database of Measured Entities.
	 * @param representation The Json representation of the new Scheduled Event to add.
	 * 
	 * @return null.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Put("json")
	public Representation putMeasuredEntityScheduledEvent(Representation representation) throws Exception {

		logger.info("putMeasuredEntityScheduledEvent");
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
					MeasuredEntityScheduledEvent event = mapper.readValue(jsonText, MeasuredEntityScheduledEvent.class);
					measuredEntityFacade.getEntity().putScheduledEvent( event );

					logger.debug("putMeasureEntityScheduleEvent OK");

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
	 * Deletes the passed MeasuredEntity Scheduled Event in our internal database of Measured Entities.
	 * @param Json representation of the measured entity scheduled event to delete.
	 * 
	 * @return null.
	 * @throws SQLException 
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Delete("json")
	public Representation deleteMeasuredEntityScheduledEvent() throws SQLException {

		Representation result;

		// Get the Measured Entity's uniqueID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Get the Scheduled Event Id
		String eventIdStr = (String)this.getRequestAttributes().get("EventID");
		if (eventIdStr != null){

			try{
				Integer eventId = Integer.valueOf(eventIdStr);

				ConfigurationManager confManager = ConfigurationManager.getInstance();

				// Deletes the signal unit from all signals that has it as the unit.

				// Look for it in the Measured Entity database.
				MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();

				// Get the measuring entity facade. 
				MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

				measuredEntityFacade.getEntity().removeScheduledEvent(eventId);

				getResponse().setStatus(Status.SUCCESS_OK);

			} catch (NumberFormatException e) {
				String error = "The value given in Scheduled Event is not a valid number";
				logger.error(error);
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);

			}

		}
		
		else {
			String error = "behaviorId was not provided";
			logger.error(error);
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
		}

		result = new JsonRepresentation("");	
		return result;
	}

}
