package com.advicetec.iot.rest;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import com.advicetec.applicationAdapter.ProductionOrder;
import com.advicetec.applicationAdapter.ProductionOrderManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.configuration.ReasonCodeContainer;
import com.advicetec.core.AttributeType;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.eventprocessor.MeasuredEntityEvent;
import com.advicetec.measuredentitity.ExecutedEntity;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.ExecutedEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuringState;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

/**
 * This class is used to process the Rest API for activity resources.
 * 
 * Activity resources includes production start, production stop, 
 * resource code registration for a state in the history, and resource 
 * registration in the current state interval.
 * 
 * @author Andres Marentes
 *
 */
public class ActivityRegistrationResource extends ServerResource  
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
	 * Year when the registration activity occurs. 
	 */
	private Integer canYear;
	
	/**
	 * Month when the registration activity occurs.
	 */
	private Integer canMonth;
	
	/**
	 * Registration activity type, it has four types:
	 * 		S  -> Start production order
	 * 		E  -> End production order
	 * 		C  -> Register reason code in the state history
	 * 		N  -> Register reason code for the current state interval. 
	 */
	private String canActivityType;
	
	/**
	 * Canonical reason code 
	 */
	private String canStopReason;
	
	/**
	 * Canonical production order code. 
	 */
	private String canProductionOrder;
	
	/**
	 * start date and time for the interval being updated with the reason code. 
	 */
	private String canStartDttm;
	
	/**
	 * Registration activity type, it has four types:
	 * 		S  -> Start production order
	 * 		E  -> End production order
	 * 		C  -> Register reason code in the state history
	 * 		N  -> Register reason code for the current state interval. 
	 */
	private static String[] activityTypes = {"S", "E", "C", "N"};
	
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
			this.canActivityType = jsonobject.getString("activityType");
			
			// Verify required parameters by registration activity type
			if (isValidActivityType(this.canActivityType)) {
				if ((this.canActivityType.compareTo("C") == 0) || (this.canActivityType.compareTo("N") == 0)){
					this.canStopReason = jsonobject.getString("stopReason");					
				}
				
				if (this.canActivityType.compareTo("C") == 0){
					this.canStartDttm = jsonobject.getString("startDttm");
				}
				
				if ((this.canActivityType.compareTo("S") == 0) || (this.canActivityType.compareTo("E") == 0)){
					this.canYear = Integer.valueOf(jsonobject.getString("year"));
					this.canMonth = Integer.valueOf(jsonobject.getString("month"));
					this.canProductionOrder = jsonobject.getString("productionOrder");					
				}
			}
			
		} catch (JSONException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Depending on the type of register activity, it processes the request. 
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
	 *	activityType
	 *	year
	 *	month
	 *	productionOrder
		
	 * 	Registration activity types: reason code in the history and in the current state. All these field are canonical fields.
	 *	company
	 *	location
	 *	plant
	 *	machineGroup
	 *	machineId
	 *	activityType
	 *  stopReason 
	 *  startDttm
	 *  
	 * @param representation The Json representation of the new registration activity to process.
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
		this.canActivityType = getQueryValue("activityType");

		if (canMachineId == null) {
			getParamsFromJson(representation);
		} else {			

			if ((this.canActivityType.compareTo("C") == 0) || (this.canActivityType.compareTo("N") == 0)){
				this.canStopReason = getQueryValue("stopReason");					
			}

			if (this.canActivityType.compareTo("C") == 0){
				this.canStartDttm = getQueryValue("startDttm");
			}

			if ((this.canActivityType.compareTo("S") == 0) || (this.canActivityType.compareTo("E") == 0)){
				this.canYear = Integer.valueOf(getQueryValue("year")); 
				this.canMonth = Integer.valueOf(getQueryValue("month")); 
				this.canProductionOrder = getQueryValue("productionOrder");				
			}
		}

		if (!isValidActivityType(this.canActivityType)) {
			String error = "The activity type given is incorrect - valid types are: S, E, C, N";
			logger.error(error);
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
			result = new JsonRepresentation("");
			return result;				
		}

		if ((this.canActivityType.compareTo("C") == 0) || (this.canActivityType.compareTo("N") == 0)){
			if ((this.canStopReason == null) || (this.canStopReason.isEmpty())){
				String error = "A stop reason must be provided to register a new or an update activity";
				logger.error(error);
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
				result = new JsonRepresentation("");
				return result;					
			}
		}

		if (this.canActivityType.compareTo("C") == 0){
			if ((this.canStartDttm == null) || (this.canStartDttm.isEmpty())){
				String error = "The start datatetime must be provided to register an update stop";
				logger.error(error);
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
				result = new JsonRepresentation("");
				return result;				
			}
		}

		if ((this.canActivityType.compareTo("S") == 0) || (this.canActivityType.compareTo("E") == 0)){
			if ((this.canProductionOrder == null) || (this.canProductionOrder.isEmpty())){
				String error = "The production order must be provided";
				logger.error(error);
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
				result = new JsonRepresentation("");
				return result;
			}
		}


		logger.debug("idMaquina:" + this.canCompany + 
				"idSede:" + this.canLocation +
				"idPlanta:" + this.canPlant +
				"idGrupoMaquina :" + this.canMachineGroup +
				"idMaquina :" + this.canMachineId +
				"ano :" + this.canYear +
				"mes:" + this.canMonth +
				"tipoActividad :" + this.canActivityType +
				"idRazonParada:" + this.canStopReason +
				"idProduccion: " +  this.canProductionOrder +
				"reqStartDateTime:" + this.canStartDttm );

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

		} 

		if (this.canActivityType.compareTo("S") == 0)
		{
			// Brings the internal production id.
			Integer idProduction = ProductionOrderManager.getInstance().getProductionOrderId(canCompany,canLocation,
					canPlant,canMachineGroup,canMachineId, canYear, canMonth, canProductionOrder );

			executeStartProduction(measuredEntityFacade, idProduction);
			result = new JsonRepresentation("");
			return result;

		} else if (this.canActivityType.compareTo("E") == 0) {

			// Brings the internal production id.
			Integer idProduction = ProductionOrderManager.getInstance().getProductionOrderId(canCompany,canLocation,
					canPlant,canMachineGroup,canMachineId, canYear, canMonth, canProductionOrder );

			executeStopProduction( measuredEntityFacade, idProduction);
			result = new JsonRepresentation("");
			return result;

		} else if (this.canActivityType.compareTo("C") == 0) {

			// Brings the internal reason id.
			Integer idStopReason = ConfigurationManager.getInstance().getReasonCodeContainer().getReasonCodeId(canCompany,canLocation,
					canPlant,canStopReason);

			executeUpdateStop(measuredEntityFacade, idStopReason, this.canStartDttm);
			result = new JsonRepresentation("");
			return result;

		} else if (this.canActivityType.compareTo("N") == 0) {

			// Brings the internal reason id.
			Integer idStopReason = ConfigurationManager.getInstance().getReasonCodeContainer().getReasonCodeId(canCompany,canLocation,
					canPlant,canStopReason);

			// If there is a new stop by the application, the machine should be stopped. In case that it is not stopped, we should 
			// report the error.
			executeStartNewStop(measuredEntityFacade, idStopReason);
			result = new JsonRepresentation("");
			return result;

		} else  {
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			logger.error("The activity type received is not valid" + this.canActivityType);
			result = new JsonRepresentation("");
			return result;
		}
	}
	
	/** 
	 * Method to start a production order within a measured entity. 
	 * 
	 * This method stops all production orders being executed in the measured entity. Likewise, 
	 * it puts to run the production order given by parameter.  
	 * 
	 * It is important to say that only one production can be operating in the measured entity. In other words, we can have many
	 * production orders assigned to the measured entity, but only one can be in operating state.  
	 * 
	 * @param measuredEntityFacade Measured entity where the production should be started
	 * @param idProduction    	   Internal production order identifier
	 * @throws SQLException	  	   It is triggered if an error occurs during the production order information retrieval  
	 * @throws PropertyVetoException 
	 */
	private void executeStartProduction(MeasuredEntityFacade measuredEntityFacade, int idProduction) throws SQLException, PropertyVetoException 
	{
    	logger.info("in register production order start");
        
    	ProductionOrderManager productionOrderManager = ProductionOrderManager.getInstance(); 
        	
    	// Start of the production order
    	ExecutedEntityFacade productionOrderFacade = productionOrderManager.getFacadeOfPOrderById(idProduction);
    	
    	if (productionOrderFacade == null)
    	{
    		ProductionOrder oProd = (ProductionOrder) productionOrderManager.getProductionOrderContainer().getObject(idProduction);
    		if (oProd != null) {
    			productionOrderManager.addProductionOrder(oProd);
    			productionOrderFacade = productionOrderManager.getFacadeOfPOrderById(idProduction);
    		}
    	}

    	if (productionOrderFacade == null) {
    		getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
    		logger.error("The production order with number:" + Integer.toString(idProduction) + " was not found");
    		
    	} else {
    	
    		logger.info("Production Order found, it is going to be put in execution");

    		measuredEntityFacade.ExecutedEntityChange();

    		// Add a reference to measured entity facade.  
    		productionOrderFacade.addMeasuredEntity(measuredEntityFacade);

    		// Stop all other executed Objects
    		measuredEntityFacade.stopExecutedObjects();

        	// start production
        	measuredEntityFacade.addExecutedObject((ExecutedEntity) productionOrderFacade.getEntity());

    		// put the production order in execution.
    		productionOrderFacade.start(measuredEntityFacade.getEntity().getId());
        	
        	getResponse().setStatus(Status.SUCCESS_OK);
	        	
    	}		
	}
	
	/** 
	 * Method to ends a production order within a measured entity. 
	 * 
	 * This method ends the production order execution in the measured entity. With the end, the user is reporting that no more 
	 * operations are going to be executed for the production order; and therefore, the system can remove the production order,  
	 * remove its facade, and save any pending data in caches. 
	 * 	 
	 * @param measuredEntityFacade	Measured entity where the production should be ended
	 * @param idProduction			Internal production order identifier to end.
	 * @throws SQLException			It is triggered if an error occurs during the production order information retrieval
	 */
	private void executeStopProduction(MeasuredEntityFacade measuredEntityFacade, Integer idProduction) throws SQLException {

    	logger.debug("in register production order end");
    	
    	// End of the production order
    	ProductionOrderManager productionOrderManager = ProductionOrderManager.getInstance(); 
    	
    	// Get the entity facade for the production order. 
    	ExecutedEntityFacade  pOrderfacade = productionOrderManager.getFacadeOfPOrderById(idProduction);
    	
    	if (pOrderfacade != null) {
    		
    		measuredEntityFacade.ExecutedEntityChange();
    	    		
    		// Stop the production order if was in operation. 
    		pOrderfacade.stop(measuredEntityFacade.getEntity().getId());

    		// Remove the Measured Entity Facade where the production order was being executed.  
    		pOrderfacade.deleteMeasuredEntity(measuredEntityFacade.getEntity().getId());
    		
    		// Remove the production order from the measured entity.
    		measuredEntityFacade.removeExecutedObject(idProduction);

    		// remove the facade from the Manager
    		if (pOrderfacade.isProcessed() == false) {
    			
	    		productionOrderManager.removeFacade(idProduction);
	    		productionOrderManager.getProductionOrderContainer().removeObject(idProduction);
    		}

    		getResponse().setStatus(Status.SUCCESS_OK);
    	
    	} else {

			String error = "The production order:" + Integer.toString(idProduction) + " was not found";
			logger.error(error);
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);

    	}

	}
		
	/**
	 * Method to register a reason code for a past state interval
	 * 
	 * @param measuredEntityFacade	Measured entity where the state interval should be updated
	 * @param idStopReason			Stop reason identifier to set in the state interval
	 * @param startDttm				identifier of the interval to update.
	 */
	private void executeUpdateStop(MeasuredEntityFacade measuredEntityFacade, int idStopReason, String startDttm) {

    	logger.debug("in updating a measured entity stop");

    	// Look for it in the Reason Code database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		ReasonCodeContainer reasonCodeCon = confManager.getReasonCodeContainer();
		ReasonCode reasonCode = (ReasonCode) reasonCodeCon.getObject(idStopReason);

		// look for the correct stop.
		boolean ret = measuredEntityFacade.updateStateInterval(startDttm,reasonCode);
		
		if (ret){ 
			getResponse().setStatus(Status.SUCCESS_OK);
		}
		else{
			String error = "It could not find the interval to update";
			logger.info("It could not find the interval to update");
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
		}
			

	}
	
	/**
	 * Method to register a new stop in the measured entity. It updates the current state interval putting a reason code
	 * for the interval.
	 * 
	 * @param measuredEntityFacade	Measured entity where the state interval should be updated
	 * @param idStopReason			Stop reason identifier to set in the state interval
	 */
	private void executeStartNewStop(MeasuredEntityFacade measuredEntityFacade, int idStopReason) {
		
		logger.debug("in starting a measured entity stop");

    	// Look for it in the Reason Code database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		ReasonCodeContainer reasonCodeCon = confManager.getReasonCodeContainer();
		ReasonCode reasonCode = (ReasonCode) reasonCodeCon.getObject(idStopReason);

		// Update the current stop, call the behavior.
		MeasuringState state = ((MeasuredEntity)measuredEntityFacade.getEntity()).getCurrentState();
	
		// Update the reason code.
		((MeasuredEntity)measuredEntityFacade.getEntity()).setCurrentReasonCode(reasonCode);

		String behavior = ((MeasuredEntity) measuredEntityFacade.getEntity()).getBehaviorText(state, idStopReason);

		if ((behavior!= null) && (!behavior.isEmpty())){
			ArrayList<InterpretedSignal> signals = new ArrayList<InterpretedSignal>();
			InterpretedSignal reasonSignal = new InterpretedSignal(AttributeType.INT, new Integer(idStopReason));
			signals.add(reasonSignal);

			MeasuredEntityEvent event = new MeasuredEntityEvent(behavior, measuredEntityFacade.getEntity().getId(), measuredEntityFacade.getEntity().getType(), 0, 0, signals );
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
	
	/**
	 * Method to evaluate whether the string representing an activity type is valid or not
	 *  
	 * @param activityType	activity type string given as interface parameter
	 * 
	 * @return true if is valid, false otherwise.
	 */
	private boolean isValidActivityType(String activityType) {
		List<String> activities = Arrays.asList(activityTypes);
		return activities.contains( activityType );
	}
}
