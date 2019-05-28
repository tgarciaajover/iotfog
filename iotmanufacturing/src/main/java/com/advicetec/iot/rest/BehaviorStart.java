package com.advicetec.iot.rest;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.advicetec.applicationAdapter.ProductionOrderManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.configuration.ReasonCodeContainer;
import com.advicetec.core.AttributeType;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.eventprocessor.MeasuredEntityEvent;
import com.advicetec.measuredentitity.BehaviorType;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuringState;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

/**
 * This class is used to process the Rest API for Starting a behavior in the measured entity.
 * 
 * 
 * @author Andres Marentes
 *
 */
public class BehaviorStart extends ServerResource  
{

	static Logger logger = LogManager.getLogger(ActivityRegistrationResource.class.getName());

	/**
	 * Canonical company code
	 */
	private String canCompany;
	
	/**
	 * Canonical company location
	 */
	private String canLocation;
	
	/**
	 * Canonical company plant
	 */
	private String canPlant;
	
	/**
	 * Canonical machine group
	 */
	private String canMachineGroup;
	
	/**
	 * Canonical machine identifier
	 */
	private String canMachineId;

	/**
	 * Canonical stateBehavior
	 * 
	 */
	private String stateBehaviorType;

	/**
	 * Obtains and verifies the parameters from a JSON representation.
	 * 
	 * @param representation  JSON representation that maintains the parameters for the interface.
	 * 
	 * It does not have a return value, but it registers the parameters in the class's attributes.   
	 */
	private void getParamsFromJson(Representation representation) { 
		try {
			
			// Get the Json representation of the ReasonCode.
			JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

			// Convert the Json representation to a Java representation.
			JSONObject jsonobject = jsonRepresentation.getJsonObject();
			
			// Gets the parameters required to process the interface.
			this.canCompany = jsonobject.getString("company");
			this.canLocation = jsonobject.getString("location");
			this.canPlant = jsonobject.getString("plant");
			this.canMachineGroup = jsonobject.getString("machineGroup");
			this.canMachineId = jsonobject.getString("machineId");
			this.stateBehaviorType = jsonobject.getString("stateBehaviorType");
			
		} catch (JSONException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Processes the state behavior for a machine. 
	 * 
	 * The request can be called in two ways:
	 * 	 A json object with all parameters or as query parameters in the request  
	 *   
	 * The following are the parameters required depending on the registration activity:
	 * 
	 * 	Registration activity types: production Order start and end. All these field are canonical fields.
	 *	company	
	 *	location
	 *	plant
	 *	machineGroup
	 *	machineId
	 *  stateBehaviorName 
	 *  
	 * @param representation: The Json representation of the state behavior.
	 * 
	 * @return a reply message saying if we could process successfully or not.
	 * 
	 * @throws Exception It is triggered if problems occur unpacking the representation.
	*/
	@Put("json")
	@Post("json")
	public Representation putActivityRegister(Representation representation) throws Exception {
 
		// Create an empty JSon representation.
		Representation result;
		
		// Get the information from the activity registration
		this.canCompany = getQueryValue("company");
		this.canLocation = getQueryValue("location"); 
		this.canPlant = getQueryValue("plant");
		this.canMachineGroup = getQueryValue("machineGroup");
		this.canMachineId = getQueryValue("machineId");
		this.stateBehaviorType = getQueryValue("stateBehaviorType");

		if (canMachineId == null) {
			getParamsFromJson(representation);
		} 
	
		logger.debug("idMaquina:" + this.canCompany + 
				"idSede:" + this.canLocation +
				"idPlanta:" + this.canPlant +
				"idGrupoMaquina :" + this.canMachineGroup +
				"idMaquina :" + this.canMachineId +
				"stateBehaviorType :" + this.stateBehaviorType );

		// brings the measured entity.
		Integer uniqueID = MeasuredEntityManager.getInstance()
				.getMeasuredEntityId(canCompany,canLocation,canPlant,canMachineGroup,canMachineId);

		if (uniqueID == null) {
			String error = "Measured Entity for company:" + this.canCompany +
					" location:" + this.canLocation + " Plant:" + this.canPlant +
					" machineId:" + this.canMachineId + " was not found"; 
			logger.error(error);
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
			result = new JsonRepresentation("");
			return result;
		}

		MeasuredEntityFacade measuredEntityFacade = MeasuredEntityManager.getInstance().getFacadeOfEntityById(uniqueID);

		if (measuredEntityFacade == null) {
			// The requested contact was not found, so set the Status to indicate this.
			String error = "Meaured Entity requested: " + canMachineId + " was not found"; 
			logger.error(error);
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
			result = new JsonRepresentation("");
			return result;
		} else {
			String getStateBehaviorText = ((MeasuredEntity)measuredEntityFacade.getEntity()).getStateBehaviorText(stateBehaviorType);
			if (getStateBehaviorText ==  null) {
				String error = "The State Behavior type given as parameter: " + stateBehaviorType + " was not found"; 
				logger.error(error);
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
				result = new JsonRepresentation("");
				return result;
			}
		}



		// If there is a new stop by the application, the machine should be stopped. In case that it is not stopped, we should 
		// report the error.
		executeStartStateBehavior(measuredEntityFacade, stateBehaviorType);
		result = new JsonRepresentation("");
		return result;

	}

	/**
	 * Method to register a new stop in the measured entity. It updates the current state interval putting a reason code
	 * for the interval.
	 * 
	 * @param measuredEntityFacade	Measured entity where the state interval should be updated
	 * @param stateBehaviorType		Type of state behavior to execute (the behavior type uniquely identifies it)
	 */
	private void executeStartStateBehavior(MeasuredEntityFacade measuredEntityFacade, String stateBehaviorType) {
		logger.debug("in Starting execute Start State Behavior");

    	// Look for it in the Reason Code database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();

		// Update the current stop, call the behavior.
		String getStateBehaviorText = ((MeasuredEntity)measuredEntityFacade.getEntity()).getStateBehaviorText(stateBehaviorType);
	
		if ((getStateBehaviorText!= null) && (!getStateBehaviorText.isEmpty())){
			ArrayList<InterpretedSignal> signals = new ArrayList<InterpretedSignal>();

			MeasuredEntityEvent event = new MeasuredEntityEvent(null, measuredEntityFacade.getEntity().getId(), measuredEntityFacade.getEntity().getType(), 0, 0, signals, BehaviorType.STATE_BEHAVIOR, this.stateBehaviorType );
			event.setRepeated(false);
			event.setMilliseconds(0); // To be executed now.

			EventManager eventManager = EventManager.getInstance();

			try {
				Queueable obj = new Queueable(QueueType.EVENT, event);
				eventManager.getQueue().enqueue(6, obj);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}				
		}

		getResponse().setStatus(Status.SUCCESS_OK);


	}

	
	
}
