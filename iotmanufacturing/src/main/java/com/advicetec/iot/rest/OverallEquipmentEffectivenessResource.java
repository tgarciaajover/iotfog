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

public class OverallEquipmentEffectivenessResource extends ServerResource {

	static final Logger logger = LogManager.getLogger(OverallEquipmentEffectivenessResource.class.getName());

	private String canMachineId;
	private String canCompany;
	private String canLocation;
	private String canPlant;
	private String reqStartDateTime;
	private String reqEndDateTime;

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
		this.canPlant = getQueryValue("plant");
		this.reqStartDateTime = getQueryValue("startDttm");
		this.reqEndDateTime = getQueryValue("endDttm");

		// json request
		if (canMachineId == null) {
			getParamsFromJson(representation);
		}		
		
		try {
			// Get the contact's uniqueID from the URL.
			Integer uniqueID = MeasuredEntityManager.getInstance()
					.getMeasuredEntityId(canCompany,canLocation,canPlant,canMachineId);

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
				JSONArray jsonArray = facade.getOverallEquipmentEffectiveness(dttmFrom, dttmTo);
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
