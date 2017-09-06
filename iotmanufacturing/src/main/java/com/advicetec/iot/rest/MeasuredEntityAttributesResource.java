package com.advicetec.iot.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.stax2.ri.typed.ValueEncoderFactory;
import org.json.JSONArray;
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
import com.advicetec.configuration.ReasonCodeContainer;
import com.advicetec.core.AttributeType;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.eventprocessor.MeasuredEntityEvent;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuringState;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.monitorAdapter.protocolconverter.MqttDigital;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

public class MeasuredEntityAttributesResource extends ServerResource  
{
	
	static Logger logger = LogManager.getLogger(MeasuredEntityAttributesResource.class.getName());

	private String canCompany;
	private String canLocation;
	private String canPlant;
	private String canMachineGroup; 
	private String canMachineId;
	
	
	private void getParamsFromJson(Representation representation) {
		
		try {
			// Get the information from the request json object
			
			// Get the Json representation of the ReasonCode.
			JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

			// Convert the Json representation to the Java representation.
			JSONObject jsonobject = jsonRepresentation.getJsonObject();
			
			this.canCompany = jsonobject.getString("company");
			this.canLocation = jsonobject.getString("location");
			this.canPlant = jsonobject.getString("plant");
			this.canMachineGroup = jsonobject.getString("machineGroup");
			this.canMachineId = jsonobject.getString("machineId");
			
		} catch (JSONException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		}
		
	}

	
	/**
	 * Gets the attributes marked as trend in the measured entity.
	 * @param representation The Json representation of the measured entity.
	 * 
	 * @return null.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	*/
	@Get("json")
	public Representation getMeasuredAttributes(Representation representation) throws Exception {
 
		Representation result = null;

		
		// get the parameters
		this.canCompany = getQueryValue("company");
		this.canLocation = getQueryValue("location");
		this.canPlant = getQueryValue("plant");
		this.canMachineGroup = getQueryValue("machineGroup");
		this.canMachineId = getQueryValue("machineId");

		if (canMachineId == null) {
			getParamsFromJson(representation);
		}

		
		try {
			Integer uniqueID = MeasuredEntityManager.getInstance()
					.getMeasuredEntityId(this.canCompany,this.canLocation,this.canPlant, 
													this.canMachineGroup,this.canMachineId);
			// Look for it in the database.
			
			if (uniqueID == null) {
				logger.error("Measured Entity for company:" + this.canCompany +
						 " location:" + this.canLocation + " Plant:" + this.canPlant + 
						 "Machine Group:"  + this.canMachineGroup + 
						 " machineId:" + this.canMachineId + " was not found");
				result = new JsonRepresentation("");
			}
			
			MeasuredEntityFacade facade = MeasuredEntityManager.getInstance()
					.getFacadeOfEntityById(uniqueID);

			if(facade == null){
				result = new JsonRepresentation("");
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
				logger.error("Facade does not found:"+uniqueID);
				result = new JsonRepresentation("");
			}else{
								
				// get the array from the facade.
				JSONArray jsonArray = facade.getJsonAttributeTrend();
				result = new JsonRepresentation(jsonArray);
			}
		} catch (SQLException e) {
			logger.error("SQL failure.");
			e.printStackTrace();
		}
		return result;
		
	}
	
}
