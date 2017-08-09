package com.advicetec.iot.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.utils.PeriodUtils;

public class OverallEquipmentEffectivenessResource extends ServerResource {

	static final Logger logger = LogManager.getLogger(OverallEquipmentEffectivenessResource.class.getName());

	private String canMachineId;
	private String canCompany;
	private String canLocation;	
	private String canPlant;
	private String canMachineGroup;	
	private String reqStartDateTime;
	private String reqEndDateTime;
	private String reqInterval;

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
			this.reqInterval = jsonobject.getString("reqInterval");

		} catch (JSONException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Handle a POST http request.<br>
	 * @param rep 
	 * @return Representation of Json array of downtime reasons from a device.
	 * @throws ResourceException
	 * @throws IOException If the representation is not a valid json.
	 */
	@Get("json")
	public Representation getOverallEquipmentEffectiveness(Representation representation) throws ResourceException, IOException{
		Representation result = null;

		this.canMachineId = getQueryValue("machineId");
		this.canCompany = getQueryValue("company");
		this.canLocation = getQueryValue("location");
		this.canMachineGroup = getQueryValue("machineGroup");
		this.canPlant = getQueryValue("plant");
		this.reqStartDateTime = getQueryValue("startDttm");
		this.reqEndDateTime = getQueryValue("endDttm");
		this.reqInterval = getQueryValue("reqInterval");
		
		// json request
		if (canMachineId == null) {
			getParamsFromJson(representation);
		}		
		
		// Verifies that the reqInterval given is valid.
		if (!validReqInterval()){
			logger.error("Invalid request interval - valid values (H Hours ,D Days, M months, Y Years)");
			result = new JsonRepresentation("");
		} else {
		
			try {
				// Get the contact's uniqueID from the URL.
				Integer uniqueID = MeasuredEntityManager.getInstance()
						.getMeasuredEntityId(this.canCompany, this.canLocation, this.canPlant,this.canMachineGroup, this.canMachineId);
	
				if (uniqueID == null) {
					logger.error("Measured Entity for company:" + this.canCompany +
							 " location:" + this.canLocation + " Plant:" + this.canPlant +
							 "machineGroup" + this.canMachineGroup + 
							 " machineId:" + this.canMachineId + " was not found");
					result = new JsonRepresentation("");
				}
				
				// Look for it in the database.
				MeasuredEntityFacade facade = MeasuredEntityManager.getInstance()
						.getFacadeOfEntityById(uniqueID);
	
				if(facade == null){
					result = new JsonRepresentation("");
					getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
					logger.error("Facade:"+uniqueID+" is not found");
					result = new JsonRepresentation("");
				}
				else
				{
					DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s.n");
					LocalDateTime dttmFrom = LocalDateTime.parse(this.reqStartDateTime,format); 
					LocalDateTime dttmTo = LocalDateTime.parse(this.reqEndDateTime,format);
					// get the array from the facade.
					JSONArray jsonArray = facade.getOverallEquipmentEffectiveness(dttmFrom, dttmTo, this.reqInterval);
					// from jsonarray to Representation
					result = new JsonRepresentation(jsonArray);
				}
	
			} catch (JSONException e) {
				logger.error("Parsing json object failure:"+e.getMessage());
				e.printStackTrace();
			} catch (SQLException e) {
				logger.error("SQL failure."+e.getMessage());
				e.printStackTrace();
			}
		}
		return result;
	}


	private boolean validReqInterval() {
		
		if (this.reqInterval == null)
			return true;
		
		if ((this.reqInterval.compareTo("H") == 0) || 
			 (this.reqInterval.compareTo("D") == 0) || 
			   (this.reqInterval.compareTo("M") == 0) || 
			   (this.reqInterval.compareTo("Y") == 0)){
			
			// Verifies the coherence of the request time interval and the interval 
			DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s.n");
			LocalDateTime dttmFrom = LocalDateTime.parse(this.reqStartDateTime,format); 
			LocalDateTime dttmTo = LocalDateTime.parse(this.reqEndDateTime,format);

			long hours = ChronoUnit.HOURS.between(dttmFrom, dttmTo);
			
			if ( hours < PeriodUtils.HOURSPERDAY) {
				if (reqInterval.compareTo("H") != 0){
					logger.error("The time interval given should be expressed in hours and not in" + this.reqInterval);
					return false;
				} else {
					return true;
				}
			
			} else if ((hours >= PeriodUtils.HOURSPERDAY) && (hours <= PeriodUtils.HOURSPERMONTH )) {
				if ((reqInterval.compareTo("M") == 0) || (reqInterval.compareTo("Y") == 0)) {
					logger.error("The time interval given should be expressed in days or hours and not in" + this.reqInterval);
					return false;
				} else {
					return true;
				}
			} else if ((hours > PeriodUtils.HOURSPERMONTH) && (hours <= PeriodUtils.HOURSPERYEAR )) {
				if (reqInterval.compareTo("Y") == 0) {
					logger.error("The time interval given should not be expressed in years");
					return false;
				} else {
					return true;
				}

			} else {
				return true;
			}			
		}
		
		return false;
	}
}
