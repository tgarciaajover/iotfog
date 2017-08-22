package com.advicetec.iot.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.stax2.ri.typed.ValueEncoderFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.advicetec.applicationAdapter.ProductionOrder;
import com.advicetec.applicationAdapter.ProductionOrderFacade;
import com.advicetec.applicationAdapter.ProductionOrderManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.configuration.ReasonCodeContainer;
import com.advicetec.core.AttributeType;
import com.advicetec.core.TimeInterval;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.eventprocessor.MeasuredEntityEvent;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuringState;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.monitorAdapter.protocolconverter.MqttDigital;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

public class ActivityRegistrationResource extends ServerResource  
{
	
	static Logger logger = LogManager.getLogger(ActivityRegistrationResource.class.getName());

	private String canCompany;
	private String canLocation;
	private String canPlant;
	private String canMachineGroup;
	private String canMachineId;
	private Integer canYear;
	private Integer canMonth;
	private String canActivityType;
	private String canStopReason;
	private String canProductionOrder;
	private String canStartDttm;
	
	private static String[] activityTypes = {"S", "E", "C", "N"};
	
	private void getParamsFromJson(Representation representation) {
		
		try {
			
			// Get the Json representation of the ReasonCode.
			JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

			// Convert the Json representation to the Java representation.
			JSONObject jsonobject = jsonRepresentation.getJsonObject();
			String jsonText = jsonobject.toString();
			
			
			this.canCompany = jsonobject.getString("company");
			this.canLocation = jsonobject.getString("location");
			this.canPlant = jsonobject.getString("plant");
			this.canMachineGroup = jsonobject.getString("machineGroup");
			this.canMachineId = jsonobject.getString("machineId");
			this.canActivityType = jsonobject.getString("activityType");
			
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
	 * Adds the passed Reason Code to our internal database of Reason Codes.
	 * @param representation The Json representation of the new Reason Code to add.
	 * 
	 * @return null.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
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
	
	
	private void executeStartProduction(MeasuredEntityFacade measuredEntityFacade, int idProduction) throws SQLException{
    	logger.info("in register production order start");
        
    	ProductionOrderManager productionOrderManager = ProductionOrderManager.getInstance(); 
        	
    	// Start of the production order
    	ProductionOrderFacade productionOrderFacade = productionOrderManager.getFacadeOfPOrderById(idProduction);
    	
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
    		
    		
    		// Stop all other executed Objects
    		measuredEntityFacade.stopExecutedObjects();
        	
    		// put the production order in execution.
    		productionOrderFacade.start();
    		
        	// start production
        	measuredEntityFacade.addExecutedObject(productionOrderFacade.getProductionOrder());
        	
        	getResponse().setStatus(Status.SUCCESS_OK);
	        	
    	}		
	}
	
	
	private void executeStopProduction(MeasuredEntityFacade measuredEntityFacade, int idProduction) throws SQLException{

    	logger.info("in register production order end");
    	
    	// End of the production order
    	ProductionOrderManager productionOrderManager = ProductionOrderManager.getInstance(); 
    		        		        	
    	// remove the facade from the Manager
    	productionOrderManager.removeFacade(idProduction);
    	
    	productionOrderManager.getProductionOrderContainer().removeObject(idProduction);
    	
    	// Remove the production order from the measured entity.
    	measuredEntityFacade.removeExecutedObject(idProduction);
    	
    	getResponse().setStatus(Status.SUCCESS_OK);

	}
	
	
	private void executeUpdateStop(MeasuredEntityFacade measuredEntityFacade, int idStopReason, String startDttm) {

    	logger.info("in updating a measured entity stop");

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
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
		}
			

	}
	
	
	private void executeStartNewStop(MeasuredEntityFacade measuredEntityFacade, int idStopReason){
		
		logger.info("in starting a measured entity stop");

    	// Look for it in the Reason Code database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		ReasonCodeContainer reasonCodeCon = confManager.getReasonCodeContainer();
		ReasonCode reasonCode = (ReasonCode) reasonCodeCon.getObject(idStopReason);

		// Update the current stop, call the behavior.
		MeasuringState state =  measuredEntityFacade.getEntity().getCurrentState();
	
		// Update the reason code.
		measuredEntityFacade.getEntity().setCurrentReasonCode(reasonCode);

		String behavior = measuredEntityFacade.getEntity().getBehaviorText(state, idStopReason);

		if ((behavior!= null) && (!behavior.isEmpty())){
			ArrayList<InterpretedSignal> signals = new ArrayList<InterpretedSignal>();
			InterpretedSignal reasonSignal = new InterpretedSignal(AttributeType.INT, new Integer(idStopReason));
			signals.add(reasonSignal);

			MeasuredEntityEvent event = new MeasuredEntityEvent(behavior, measuredEntityFacade.getEntity().getId(),0, 0, signals );
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
	
	private boolean isValidActivityType(String activityType){
		List<String> activities = Arrays.asList(activityTypes);
		return activities.contains( activityType );
	}
}
