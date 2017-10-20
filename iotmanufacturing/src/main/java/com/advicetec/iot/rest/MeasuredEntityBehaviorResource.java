package com.advicetec.iot.rest;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.data.Status;

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.eventprocessor.MeasuredEntityEvent;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityBehavior;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

/**
 * This class exposes measured entity behavior instances which are configured in the container.
 * 
 * The user of this interface can retry a measured entity behavior definition, inserts a new behavior or deletes a registered one.
 *  
 * @author Andres Marentes
 *
 */
public class MeasuredEntityBehaviorResource extends ServerResource  
{

	static Logger logger = LogManager.getLogger(MeasuredEntityBehaviorResource.class.getName());  

	/**
	 * Returns the MeasuredEntityBehavior instance requested by the URL. 
	 * 
	 * @return The JSON representation of the Measured Entity Behavior, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	 * unique ID is not present.
	 * 
	 * @throws Exception If problems occur making the representation. Shouldn't occur in 
	 * practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getMeasuredEntityBehavior() throws Exception {

		logger.info("In getMeasuredEntityBehavior");
		
		// Creates an empty JSON representation.
		Representation result;

		// Gets the Measured Entity's uniqueID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));
		
		// Gets the Behavior name 
		Integer behaviorId = Integer.valueOf((String)this.getRequestAttributes().get("BehaviorID"));

		// Looks for the requested behavior in the Measured Entity database.
		MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

		if (measuredEntityFacade == null) {
			String error = "Measure entity:" + Integer.toString(uniqueID) + " given is not registered"; 
			logger.error(error);
			// The requested measured entity was not found, so set the status to indicate this condition.
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
			result = new JsonRepresentation("");
		} else {
			MeasuredEntityBehavior behavior = ((MeasuredEntity) measuredEntityFacade.getEntity()).getBehavior(behaviorId);
			
			if (behavior != null){
				String jsonTxt = behavior.toJson();
				// The requested behavior was found, so we send the requested behavior.
				logger.debug(jsonTxt);
				result = new JsonRepresentation(jsonTxt);
			} else {
				String error = "behavior with id:" + Integer.toString(behaviorId) + "was not found";
				// The requested behavior was not found, so set the status to indicate this condition.
				logger.error(error);
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
				result = new JsonRepresentation("");
			}
			// Status code defaults to 200 if we don't set it.
		}
		// Return the representation.  The Status code tells the client if the representation is valid.
		return result;
	}

	/**
	 * Adds the given MeasuredEntityBehavior to our internal database of Measured Entities.
	 * 
	 * @param representation The JSON representation of the new behavior to add.
	 * 
	 * @return null.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Put("json")
	public Representation putMeasuredEntityBehavior(Representation representation) throws Exception {

		logger.debug("in putMeasuredEntityBehavior");

		// Creates an empty JSon representation.
		Representation result;

		// Gets the JSON representation of the measured entity behavior.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Converts the JSON representation to Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();

		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		String jsonText = jsonobject.toString();

		// Looks for the measured entity in the measured entity manager.
		MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

		if (measuredEntityFacade == null) {
			// The requested measured entity was not found, so we set the status to indicate this condition.
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			result = new JsonRepresentation("");
		} else {

			MeasuredEntityBehavior behavior = ((MeasuredEntity) measuredEntityFacade.getEntity()).behaviorFromJSON(jsonText);

			if (behavior == null){
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			} else { 
				// The requested measured entity was found, so we add the behavior to the measured entity
				getResponse().setStatus(Status.SUCCESS_OK);
			}

			result = new JsonRepresentation("");
		}

		return result;
	}

	/**
	 * Deletes the given measured entity behavior in the measured entity.
	 * 
	 * @param Json representation of the measured entity behavior to delete.
	 * 
	 * @return null.
	 * @throws SQLException 
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Delete("json")
	 public Representation deleteMeasuredEntityBehavior() throws SQLException {
		
		Representation result;
		
		// Gets the measured entity's uniqueID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));
		
		// Gets the behavior name
		String behaviorIdStr = (String)this.getRequestAttributes().get("BehaviorID");
		if (behaviorIdStr != null){
			
			try{
				Integer behaviorId = Integer.valueOf(behaviorIdStr);
							     
				// Looks for the measured entity in the container.
				MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
				
				// Gets the measuring entity facade. 
				MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);
			    
				// Removes the behavior event
				EventManager eventManager = EventManager.getInstance();
				
				MeasuredEntityBehavior measuredBehavior = ((MeasuredEntity) measuredEntityFacade.getEntity()).getBehavior(behaviorId);
				
				// Creates the behavior event to delete
				MeasuredEntityEvent measuredEvent = new MeasuredEntityEvent(measuredBehavior.getName(), uniqueID, measuredEntityFacade.getEntity().getType(), 0,0, new ArrayList<InterpretedSignal>()) ;
				DelayEvent delEvent = new DelayEvent(measuredEvent, 0);
				eventManager.removeEvent(delEvent);
				
				// Removes the behavior from the facade.
				((MeasuredEntity) measuredEntityFacade.getEntity()).removeBehavior(behaviorId);
			    
				getResponse().setStatus(Status.SUCCESS_OK);

			} catch (NumberFormatException e) {
				String error = "The value given in behaviorId is not a valid number";
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
