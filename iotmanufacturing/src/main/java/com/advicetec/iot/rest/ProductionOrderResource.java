package com.advicetec.iot.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.advicetec.applicationAdapter.ProductionOrderManager;
import com.advicetec.measuredentitity.MeasuredEntityManager;

/**
 * This class exposes Production Order Information started in measured Entities
 * 
 * This interface obtain the production order start date in a measured Entity
 *   
 * @author Fernando Chitiva
 */
public class ProductionOrderResource extends ServerResource {

	static final Logger logger = LogManager.getLogger(ProductionOrderResource.class.getName());

	/**
	 *  canonical machine identifier 
	 */
	private String canMachineId;
	
	/**
	 * canonical company identifier
	 */
	private String canCompany;
	
	/**
	 * canonical location identifier
	 */
	private String canLocation;	
	
	/**
	 * canonical plant identifier
	 */
	private String canPlant;
	
	/**
	 * canonical machine group identifier
	 */
	private String canMachineGroup;	

	/**
	 * canonical year identifier
	 */
	private String canYear;
	
	/**
	 * canonical month identifier
	 */
	private String canMonth;
	

	/**
	 * canonical Production Order identifier
	 */
	private String canProdOrder;

	/**
	 * Obtains and verifies the parameters from a JSON representation.
	 * 
	 * @param representation  JSON representation that maintains the parameters for the interface.
	 * 
	 * It does not have a return value, but it registers the parameters in the class's attributes.   
	 */
	private void getParamsFromJson(Representation representation) {
		
		try {			
			// Gets the Json representation of the ReasonCode.
			JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

			// Converts the Json representation to the Java representation.
			JSONObject jsonobject = jsonRepresentation.getJsonObject();
			
			this.canMachineId = jsonobject.getString("machineId");
			this.canCompany = jsonobject.getString("company");
			this.canLocation = jsonobject.getString("location");
			this.canPlant = jsonobject.getString("plant");
			this.canMachineGroup = jsonobject.getString("machineGroup");
			this.canYear = jsonobject.getString("year");
			this.canMonth = jsonobject.getString("month");
			this.canProdOrder = jsonobject.getString("productionOrder");

		} catch (IOException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		}		
	}
	
	
	/**
	 * Get the Production Order start date in a Measured Entity
	 * 
	 * @param representation  Optional JSON representation of the measured entity requested and production order started.
	 * 
	 * @return Representation of JSON value of production order start date in a measured entity.
	 * 
	 * @throws ResourceException
	 * @throws IOException If the representation is not a valid JSON.
	 */
	@Get("json")
	public Representation getProductionOrderStartDate(Representation representation) throws ResourceException, IOException{
		Representation result = null;
		logger.debug("in getOverallEquipmentEffectiveness");
		this.canMachineId = getQueryValue("machineId");
		this.canCompany = getQueryValue("company");
		this.canLocation = getQueryValue("location");
		this.canMachineGroup = getQueryValue("machineGroup");
		this.canPlant = getQueryValue("plant");
		this.canYear = getQueryValue("year");
		this.canMonth = getQueryValue("month");
		this.canProdOrder = getQueryValue("productionOrder");
		
		// JSON request
		if (canMachineId == null) {
			try {
				
				getParamsFromJson(representation);
				
			} catch (JSONException e) {
				String error = e.getMessage();
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
				result = new JsonRepresentation("");
				return result;				
			}
		}		
		
		if (getYear().equals(new Integer(0))) {
			String error = "The year provided is invalid";
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
			result = new JsonRepresentation("");
			return result;				
		}
		
		if (getMonth().equals(new Integer(0))) {
			String error = "The month provided is invalid";
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
			result = new JsonRepresentation("");
			return result;
		}
		
		try {
			
			Integer year = getYear();
			Integer month = getMonth();
			
			// Get the contact's uniqueID from the URL.
			Integer uniqueID = ProductionOrderManager.getInstance().getProductionOrderId( this.canCompany, this.canLocation, this.canPlant, this.canMachineGroup, this.canMachineId, year, month, this.canProdOrder );

			if (uniqueID == null) {
				logger.error("Executed Entity for company:" + this.canCompany +
						" location:" + this.canLocation + " Plant:" + this.canPlant +
						" machineGroup:" + this.canMachineGroup + 
						" machineId:" + this.canMachineId + 
						" year:" + year.toString() + 
						" month:" + month.toString() +
						" production order:" + this.canProdOrder + 
						" was not found");
				
				result = new JsonRepresentation("");
			} else {
			
				Integer measuredEntityID = MeasuredEntityManager.getInstance().getMeasuredEntityId(this.canCompany,this.canLocation,this.canPlant,this.canMachineGroup, this.canMachineId);
				
				if (measuredEntityID == null) {
					logger.error("Measured Entity for company:" + this.canCompany +
							" location:" + this.canLocation + " Plant:" + this.canPlant +
							"machineGroup" + this.canMachineGroup + " machineId:" + 
							this.canMachineId + " was not found");
					
					result = new JsonRepresentation("");
				} else {
					ProductionOrderManager productionOrderManager = ProductionOrderManager.getInstance();
					JSONObject jsob = (JSONObject) productionOrderManager.getProductionOrderContainer().getProductionOrderStartDate(measuredEntityID, uniqueID);
					
					result = new JsonRepresentation(jsob);
				}
			}

		} catch (JSONException e) {
			logger.error("Parsing json object failure:"+e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error("SQL failure."+e.getMessage());
			e.printStackTrace();
		}
		return result;

	}

	private Integer getYear() {
		try {
			
			return Integer.valueOf(this.canYear);
			
		} catch (NumberFormatException e) {
			logger.error("The year provided is invalid");
			return new Integer(0);
		}
	}
	
	private Integer getMonth() {
		try {
			
			return Integer.valueOf(this.canMonth);
			
		} catch (NumberFormatException e) {
			logger.error("The month provided is invalid");
			return new Integer(0);
		}
	}
}
