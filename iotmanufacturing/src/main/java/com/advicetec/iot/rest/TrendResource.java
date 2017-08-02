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
import org.restlet.resource.ServerResource;

import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

public class TrendResource extends ServerResource
{
	static final Logger logger = LogManager.getLogger(TrendResource.class.getName());

	private String canMachineId;
	private String canCompany;
	private String canLocation;
	private String canPlant;
	private String canMachineGroup; 
	private String reqStartDateTime;
	private String reqEndDateTime;
	private String trendVar;
	
	
	private void getParamsFromJson(Representation representation) {
		
		try {
			// Get the information from the request json object
			
			// Get the Json representation of the ReasonCode.
			JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

			// Convert the Json representation to the Java representation.
			JSONObject jsonobject = jsonRepresentation.getJsonObject();
			String jsonText = jsonobject.toString();
			
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

	
	
	@Get("json")
	public Representation getTrendVariable(Representation rep){
		Representation result = null;

		// get the parameters
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
		
		logger.info("here we are 01");
		
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
				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s.n");
				LocalDateTime dttmFrom = LocalDateTime.parse(reqStartDateTime,format); 
				LocalDateTime dttmTo = LocalDateTime.parse(reqEndDateTime,format);
								
				// get the array from the facade.
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
