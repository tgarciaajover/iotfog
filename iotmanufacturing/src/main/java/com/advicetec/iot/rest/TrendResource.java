package com.advicetec.iot.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

/**
 * This class exposes the evolution through time of a trend variable registered 
 * in a measured entity. 
 * 
 * The user should specify the measured entity by its canonical code.
 *   
 * @author Andres Marentes
 */
public class TrendResource extends ServerResource
{
	static final Logger logger = LogManager.getLogger(TrendResource.class.getName());

	/**
	 * Canonical machine identifier 
	 */
	private String canMachineId;
	
	/**
	 * Canonical company identifier
	 */
	private String canCompany;
	
	/**
	 * Canonical location identifier
	 */
	private String canLocation;
	
	/**
	 * Canonical plant identifier
	 */
	private String canPlant;
	
	/**
	 * Canonical machine group identifier
	 */
	private String canMachineGroup; 
	
	/**
	 * requested start date time
	 */
	private String reqStartDateTime;
	
	/**
	 * requested end date time
	 */
	private String reqEndDateTime;
	
	/**
	 * Name of the requested variable 
	 */
	private String trendVar;
	
	/**
	 * Obtains and verifies the parameters from a JSON representation.
	 * 
	 * @param representation  JSON representation that maintains the parameters for the interface.
	 * 
	 * It does not have a return value, but it registers parameters in the class's attributes.   
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
			this.reqStartDateTime = jsonobject.getString("startDttm");
			this.reqEndDateTime = jsonobject.getString("endDttm");
			this.trendVar = jsonobject.getString("variable");

		} catch (JSONException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Gets the measured attribute values registered for a trend variable within a measured 
	 * 	entity in the given date time interval.
	 * 
	 * @param representation  Optional JSON representation of the 
	 * 		measured entity requested, the time interval and variable name.
	 * 
	 * @return Representation of JSON array of measured attribute values in a measured entity.
	 * 
	 * @throws ResourceException
	 * @throws IOException If the representation is not a valid JSON.
	 */
	@Get("json")
	public Representation getTrendVariable(Representation rep){
		Representation result = null;

		// get parameters
		this.canMachineId = getQueryValue("machineId");
		this.canCompany = getQueryValue("company");
		this.canLocation = getQueryValue("location");
		this.canPlant = getQueryValue("plant");
		this.canMachineGroup = getQueryValue("machineGroup");
		this.reqStartDateTime = getQueryValue("startDttm");
		this.reqEndDateTime = getQueryValue("endDttm");
		this.trendVar = getQueryValue("variable");

		if (canMachineId == null) {
			getParamsFromJson(rep);
		}
				
		try {
			Integer uniqueID = MeasuredEntityManager.getInstance()
					.getMeasuredEntityId(this.canCompany,this.canLocation,this.canPlant, 
													this.canMachineGroup,this.canMachineId);
			// Looks for the measured entity in the database.
			
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
				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s.n");
				LocalDateTime dttmFrom = LocalDateTime.parse(reqStartDateTime,format); 
				LocalDateTime dttmTo = LocalDateTime.parse(reqEndDateTime,format);
								
				// gets the array from the facade.
				JSONArray jsonArray = facade.getJsonTrend(trendVar,dttmFrom, dttmTo);
				result = new JsonRepresentation(jsonArray);
			}
		} catch (SQLException e) {
			logger.error("SQL failure.");
			e.printStackTrace();
		}
		return result;
	}
}
