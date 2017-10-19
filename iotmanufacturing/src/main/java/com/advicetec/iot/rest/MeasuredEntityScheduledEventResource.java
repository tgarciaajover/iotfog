package com.advicetec.iot.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.data.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.eventprocessor.AggregationEvent;
import com.advicetec.eventprocessor.AggregationEventType;
import com.advicetec.eventprocessor.Event;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityContainer;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuredEntityScheduledEvent;

/**
 * This class exposes measured entity scheduled events instances which are configured in the container.
 * 
 * The user of this interface can retry a measured entity schedule event definition, inserts a new schedule event or deletes a registered one.
 *  
 * @author Andres Marentes
 *
 */
public class MeasuredEntityScheduledEventResource extends ServerResource  
{
	static Logger logger = LogManager.getLogger(MeasuredEntityScheduledEventResource.class.getName());


	/**
	 * Returns the measured entity scheduled event instance requested by the URL. 
	 * 
	 * @return The JSON representation of the measured entity scheduled event, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	 * unique ID is not present.
	 * 
	 * @throws Exception: if problems occur making the representation. Shouldn't occur in 
	 * 		 practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getMeasuredEntityScheduledEvent() throws Exception {

		// Creates an empty JSON representation.
		Representation result;

		// Gets the measured entity Identifier from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Looks for measured entity in the container.
		MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

		if (measuredEntityFacade == null) {
			// The requested measured entity was not found, so set the status to indicate this condition.
			String error = "The measured entity facade with id: " + Integer.toString(uniqueID) + " was not found";
			logger.error(error);
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
			result = new JsonRepresentation("");
		} 
		else {
			Integer eventID = Integer.valueOf((String)this.getRequestAttributes().get("EventID"));

			// The requested measured entity was found.
			if (measuredEntityFacade.getEntity() != null){
				if (measuredEntityFacade.getEntity().getScheduledEvent(eventID) != null){
					// The schedule event was found, status code defaults to 200 if we don't set it.
					result = new JsonRepresentation(measuredEntityFacade.getEntity().getScheduledEvent(eventID).toJson());
				} else {
					// The schedule event was not found, we set the status code to report the error condition.
					String error = "The scheduled event was not found";
					logger.error(error);
					getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
					result = new JsonRepresentation("");
				}
			} else {
				// The measured entity was not found, we set the status code to report the error condition.
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
	 * Adds the given measured entity scheduled event to measured entity container and schedule the event in the delay queue.
	 * 
	 * @param representation The JSON representation of the new scheduled event to add.
	 * 
	 * @return null.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Put("json")
	public Representation putMeasuredEntityScheduledEvent(Representation representation) throws Exception {

		logger.debug("putMeasuredEntityScheduledEvent");
		Representation result = null;

		// Gets the JSON representation of the SignalUnit.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Gets the measured entity uniqueID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Converts the JSON representation to a Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();

		// Looks for measured entity in the measured entity manager.
		MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();

		logger.debug("jsonTxt:" + jsonText);

		if (measuredEntityManager.getFacadeOfEntityById(uniqueID) != null){
			MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);
			if (measuredEntityFacade.getEntity() != null){
				// The requested measured entity was found, so we can add the measured entity schedule
				logger.debug("MeasuredEntityFacade found");
				try{
					
					List<AggregationEvent> events = new ArrayList<AggregationEvent>();
					
					ObjectMapper mapper = new ObjectMapper();
					MeasuredEntityScheduledEvent event = mapper.readValue(jsonText, MeasuredEntityScheduledEvent.class);
					
					((MeasuredEntity) measuredEntityFacade.getEntity()).putScheduledEvent( event );
					
					events.addAll(measuredEntityFacade.getEntity().getScheduledEvents(event.getId()));
					
					measuredEntityManager.scheduleAggregationEvents(events);
					
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
	 * Deletes the given measured entity scheduled event in the measured entity container 
	 * 	 and removes from the delayed queue. 
	 * 
	 * @param Json representation of the measured entity scheduled event to delete.
	 * 
	 * @return null.
	 * 
	 * @throws SQLException It is triggered whenever the MeasuredEntityManager cannot connect with the database.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Delete("json")
	public Representation deleteMeasuredEntityScheduledEvent() throws SQLException {

		Representation result;

		// Gets the Measured Entity's uniqueID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Gets the Scheduled Event Id
		String eventIdStr = (String) this.getRequestAttributes().get("EventID");
		if (eventIdStr != null){

			try{
				Integer eventId = Integer.valueOf(eventIdStr);

				// Look for the measured entity in the Measured Entity Manager.
				MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();

				// Get the measuring entity facade. 
				MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

				MeasuredEntityScheduledEvent scheduleEvent = measuredEntityFacade.getEntity().getScheduledEvent(eventId);
				
				if (scheduleEvent == null) {
					
					logger.info("Event with Id:" + eventId + " was not found in measured entity:" + uniqueID);
					
					// If not found, we send ok.
					getResponse().setStatus(Status.SUCCESS_OK);
					
				} else {
					
					// Creates the required schedule events. We create an event by each recurrence included.  
					if (scheduleEvent.getScheduledEventType().compareTo("AG") == 0) {

						String lines[] = scheduleEvent.getRecurrence().split("\\r?\\n");
						List<Event> events = new ArrayList<Event>();

						for (String recurrence : lines) {
							AggregationEvent aggEvent = new AggregationEvent(measuredEntityFacade.getEntity().getId(), measuredEntityFacade.getEntity().getType(), AggregationEventType.OEE, recurrence, scheduleEvent.getDayTime());
							events.add(aggEvent);
						}
						
						logger.info("schedule events to delete:" + events.size());
						
						// Removes the events from the delay queue.
						for (Event evt : events){						
							DelayEvent dEvent = new DelayEvent(evt,0);
							
							boolean deleted = EventManager.getInstance().removeEvent(dEvent);
							logger.info( "Event: " + dEvent.getKey() + " deleted:" + deleted); 
						}

					} else {
						logger.error("The Schedule event given cannot be processed - Type given:" +  scheduleEvent.getScheduledEventType() );
					}


					// Deletes the schedule event from the measured entity. 
					((MeasuredEntity) measuredEntityFacade.getEntity()).removeScheduledEvent(eventId);

					getResponse().setStatus(Status.SUCCESS_OK);
				}

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
