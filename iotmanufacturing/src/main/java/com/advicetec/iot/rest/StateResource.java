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
 * This class exposes the state of a measured entity to users. The user should specified the measured
 * entity by their canonical codes.
 * 
 * The user of this interface can retry the state for a measured entity.
 *   
 * @author Andres Marentes
 */
public class StateResource extends ServerResource {

	static final Logger logger = LogManager.getLogger(StateResource.class.getName());

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
	 * Obtains and verifies the parameters from a JSON representation.
	 * 
	 * @param representation  JSON representation that maintains the parameters for the interface.
	 * 
	 * It does not have a return value, but it registers parameters in the class's attributes.   
	 */
	private void getParamsFromJson(Representation representation) {

		try {

			// Gets the JSON representation of the State request.
			JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

			// Convert the JSON representation to the Java representation.
			JSONObject jsonobject = jsonRepresentation.getJsonObject();

			this.canMachineId = jsonobject.getString("machineId");
			this.canCompany = jsonobject.getString("company");
			this.canLocation = jsonobject.getString("location");
			this.canPlant = jsonobject.getString("plant");
			this.canMachineGroup = jsonobject.getString("machineGroup");
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
	 * Gets states registered for a measured entity in the given date time interval.
	 * 
	 * @param representation  Optional JSON representation of the measured entity requested and the time interval.
	 * 
	 * @return Representation of JSON array of States in a measured entity.
	 * 
	 * @throws ResourceException
	 * @throws IOException If the representation is not a valid JSON.
	 */
	@Get("json")
	public Representation getEntityInterval(Representation representation) throws ResourceException, IOException{

		Representation result = null;
		
		// Gets the request attributes	
		this.canMachineId = getQueryValue("machineId");
		this.canCompany = getQueryValue("company");
		this.canLocation = getQueryValue("location");
		this.canPlant = getQueryValue("plant");
		this.canMachineGroup = getQueryValue("machineGroup");
		this.reqStartDateTime = getQueryValue("startDttm");
		this.reqEndDateTime = getQueryValue("endDttm");

		if (canMachineId == null) {
			getParamsFromJson(representation);
		}
		
		try {
			Integer uniqueID = MeasuredEntityManager.getInstance()
					.getMeasuredEntityId(this.canCompany,this.canLocation,this.canPlant,this.canMachineGroup, this.canMachineId);
			
			// Looks for the measured entity in the manager.

			if (uniqueID == null) {
				logger.error("Measured Entity for company:" + this.canCompany +
						" location:" + this.canLocation + " Plant:" + this.canPlant +
						"machineGroup" + this.canMachineGroup + " machineId:" + 
						this.canMachineId + " was not found");
				result = new JsonRepresentation("");
			}
			MeasuredEntityFacade facade = MeasuredEntityManager.getInstance()
					.getFacadeOfEntityById(uniqueID);

			if(facade == null)
			{
				result = new JsonRepresentation("");
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
				logger.error("Facade:"+uniqueID+" is not found.");
				result = new JsonRepresentation("");
			}
			else
			{
				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s.n");
				LocalDateTime dttmFrom = LocalDateTime.parse(reqStartDateTime,format); 
				LocalDateTime dttmTo = LocalDateTime.parse(reqEndDateTime,format);

				// get the array from the facade.
				JSONArray jsonArray = facade.getJsonStates(dttmFrom, dttmTo);

				// from jsonarray to Representation
				result = new JsonRepresentation(jsonArray);
			}
		} catch (SQLException e) {
			logger.error("SQL failure:"+e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
}
