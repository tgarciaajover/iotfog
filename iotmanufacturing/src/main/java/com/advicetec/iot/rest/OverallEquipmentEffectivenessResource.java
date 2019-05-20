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
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.utils.PeriodUtils;

/**
 * This class exposes Overall Equipment Effectiveness 
 *  instances that are previously calculated for a measured entity.
 * 
 * The user of this interface can retry the OEE calculation for a measured entity in a given time interval.
 *   
 * @author Andres Marentes
 */
public class OverallEquipmentEffectivenessResource extends ServerResource {

	static final Logger logger = LogManager.getLogger(OverallEquipmentEffectivenessResource.class.getName());

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
	 * requested start date time 
	 */
	private String reqStartDateTime;
	
	/**
	 * requested end date time
	 */
	private String reqEndDateTime;
	
	/**
	 * 
	 */
	private String reqInterval;

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
	 * Get the OEE registered for a measured entity in given date time interval.
	 * 
	 * @param representation  Optional JSON representation of the measured entity requested and the time interval.
	 * 
	 * @return Representation of JSON array of OEEs in a measured entity.
	 * 
	 * @throws ResourceException
	 * @throws IOException If the representation is not a valid JSON.
	 */
	@Get("json")
	public Representation getOverallEquipmentEffectiveness(Representation representation) throws ResourceException, IOException{
		Representation result = null;
		logger.debug("in getOverallEquipmentEffectiveness");
		this.canMachineId = getQueryValue("machineId");
		this.canCompany = getQueryValue("company");
		this.canLocation = getQueryValue("location");
		this.canMachineGroup = getQueryValue("machineGroup");
		this.canPlant = getQueryValue("plant");
		this.reqStartDateTime = getQueryValue("startDttm");
		this.reqEndDateTime = getQueryValue("endDttm");
		this.reqInterval = getQueryValue("reqInterval");
		
		// JSON request
		if (canMachineId == null) {
			getParamsFromJson(representation);
		}		
		
		// Verifies that the reqInterval given is valid.
		if (!validReqInterval()){
			logger.error("Invalid request interval - valid values (H Hours ,D Days, M months, Y Years, MN Minutes)");
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

	/**
	 * Verifies if the requested OEE interval is valid. Valid values are: 
	 * 	H --> Hours
	 *  D --> Days
	 *  M --> Months
	 *  Y --> Years 
	 *  
	 * This method also verifies the coherence between the requested time interval
	 * and the requested time granularity expressed in reqInterval field. In other words, 
	 * for example, that the user provides a month period and request the result 
	 * in days and not in years.  
	 *  
	 * @return  true if the requested interval is valid, false otherwise.
	 */
	private boolean validReqInterval() {
		
		if (this.reqInterval == null)
			return true;
		
		if ((this.reqInterval.compareTo("H") == 0) || 
			 (this.reqInterval.compareTo("D") == 0) || 
			   (this.reqInterval.compareTo("M") == 0) || 
			   (this.reqInterval.compareTo("Y") == 0) ||
			   (this.reqInterval.compareTo("MN") == 0)){
			
			// Verifies the coherence of the request time interval and the interval 
			DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s.n");
			LocalDateTime dttmFrom = LocalDateTime.parse(this.reqStartDateTime,format); 
			LocalDateTime dttmTo = LocalDateTime.parse(this.reqEndDateTime,format);

			long minutes = ChronoUnit.MINUTES.between(dttmFrom, dttmTo);
			
			if (minutes < PeriodUtils.MINUTESPERHOUR) {
				if (reqInterval.compareTo("MN") != 0) {
					logger.error("The time interval given should be expressed in minutes and not in" + this.reqInterval);
					return false;
				} else {
					return true;
				}
			} else if ((minutes >= PeriodUtils.MINUTESPERHOUR) && (minutes < PeriodUtils.MINUTESPERDAY)) {
				if ((reqInterval.compareTo("D") == 0) || (reqInterval.compareTo("M") == 0) || (reqInterval.compareTo("Y") == 0)) {
					logger.error("The time interval given should be expressed in minutes or hours and not in" + this.reqInterval);
					return false;
				} else {
					return true;
				}
			} else if ((minutes >= PeriodUtils.MINUTESPERDAY) && (minutes < PeriodUtils.MINUTESPERMONTH)) {
				if ((reqInterval.compareTo("M") == 0) || (reqInterval.compareTo("Y") == 0)) {
					logger.error("The time interval given should be expressed in minutes or hours or days and not in" + this.reqInterval);
					return false;
				} else {
					return true;
				}
			} else if ((minutes >= PeriodUtils.MINUTESPERMONTH) && (minutes < PeriodUtils.MINUTESPERYEAR)) {
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
