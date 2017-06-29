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
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

public class DowntimeReasonResource extends ServerResource {

	static final Logger logger = LogManager.getLogger(DowntimeReasonResource.class.getName());

	/**
	 * Handle a POST http request.<br>
	 * @param rep 
	 * @return Representation of Json array of downtime reasons from a device.
	 * @throws ResourceException
	 * @throws IOException If the representation is not a valid json.
	 */
	@Post("json")
	public Representation getDowntimeReasonsInterval() throws ResourceException, IOException{
		Representation result = null;

		String canMachineId = getQueryValue("machineId");
		String canCompany = getQueryValue("company");
		String canLocation = getQueryValue("location");
		String canPlant = getQueryValue("plant");
		String reqStartDateTime = getQueryValue("startDttm");
		String reqEndDateTime = getQueryValue("endDttm");

		try {
			// Get the contact's uniqueID from the URL.
			Integer uniqueID = MeasuredEntityManager.getInstance()
					.getMeasuredEntityId(canCompany,canLocation,canPlant,canMachineId);
			// Look for it in the database.
			MeasuredEntityFacade facade = MeasuredEntityManager.getInstance()
					.getFacadeOfEntityById(uniqueID);

			if(facade == null){
				result = new JsonRepresentation("");
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
				logger.error("Facade:"+uniqueID+" is not found");
			}else{
				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MMM-dd H:m:s.n");
				LocalDateTime dttmFrom = LocalDateTime.parse(reqStartDateTime,format); 
				LocalDateTime dttmTo = LocalDateTime.parse(reqEndDateTime,format);
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
