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
 * This class exposes reason code instances that are configured in the container.
 * 
 * The user of this interface can retry a reason code definition, inserts a new reason code or deletes a registered one.
 * 
 * In the case of adding a new reason code, it verifies whether the dependent objects where previously created. If those are not created,
 * then the system creates them in their containers. 
 * 
 * @author Andres Marentes
 *
 */
public class DowntimeReasonResource extends ServerResource {

	static final Logger logger = LogManager.getLogger(DowntimeReasonResource.class.getName());

	/**
	 * canonical machine identifier 
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
	 * Start date time of the downtime intervals
	 */
	private String reqStartDateTime;
	
	/**
	 * End date time of the downtime intervals
	 */
	private String reqEndDateTime;

	/**
	 * Obtains and verifies the parameters from a JSON representation.
	 * 
	 * @param representation  JSON representation that maintain the parameters for the interface.
	 * 
	 * It does not have a return value, but it lets the parameters in the class's attributes.   
	 */
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
			this.reqStartDateTime = jsonobject.getString("startDttm");
			this.reqEndDateTime = jsonobject.getString("endDttm");

		} catch (JSONException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Error:" + e.getMessage() );
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Get the list of downtime intervals registered for a measured entity in given date time interval.
	 * 
	 * @param representation  Optional Json representation of the measured entity requested and the time interval.
	 * 
	 * @return Representation of Json array of downtime reasons for a measured entity.
	 * 
	 * @throws ResourceException
	 * @throws IOException If the representation is not a valid json.
	 */
	@Get("json")
	public Representation getDowntimeReasonsInterval(Representation representation) throws ResourceException, IOException{
		Representation result = null;
		logger.debug("in getDowntimeReasonsInterval");
		this.canCompany = getQueryValue("company");
		this.canLocation = getQueryValue("location");
		this.canPlant = getQueryValue("plant");
		this.canMachineGroup = getQueryValue("machineGroup");
		this.canMachineId = getQueryValue("machineId");
		this.reqStartDateTime = getQueryValue("startDttm");
		this.reqEndDateTime = getQueryValue("endDttm");
		
		if (canMachineId == null) {
			getParamsFromJson(representation);
		}		
		
		try {
			// Get the contact's uniqueID from the URL.
			Integer uniqueID = MeasuredEntityManager.getInstance()
					.getMeasuredEntityId(canCompany,canLocation,canPlant,canMachineGroup,canMachineId);

			logger.info("Measured Entity for company:" + this.canCompany +
					 " location:" + this.canLocation + " Plant:" + this.canPlant +
					 " machineId:" + this.canMachineId);

			
			if (uniqueID == null) {
				logger.error("Measured Entity for company:" + this.canCompany +
						 " location:" + this.canLocation + " Plant:" + this.canPlant +
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
				JSONArray jsonArray = facade.getJsonDowntimeReasons(dttmFrom, dttmTo);
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
		return result;
	}
}
