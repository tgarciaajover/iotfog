package com.advicetec.iot.rest;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.data.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.DeviceType;
import com.advicetec.configuration.DeviceTypeContainer;
import com.advicetec.configuration.MonitoringDevice;
import com.advicetec.configuration.MonitoringDeviceContainer;

public class MonitoringDeviceResource extends ServerResource  
{

	static Logger logger = LogManager.getLogger(MonitoringDeviceResource.class.getName());    

	/**
	 * Returns the Monitoring Device instance requested by the URL. 
	 * 
	 * @return The JSON representation of the Monitoring Device, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	 * unique ID is not present.
	 * 
	 * @throws Exception If problems occur making the representation. Shouldn't occur in 
	 * practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getMonitoringDevice() throws Exception {

		// Create an empty JSon representation.
		Representation result;

		// Get the contact's uniqueID from the URL.
		int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Look for it in the Signal database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		MonitoringDeviceContainer monitoringDeviceCon = confManager.getMonitoringDeviceContainer();

		MonitoringDevice monitoringDevice = (MonitoringDevice) monitoringDeviceCon.getObject(uniqueID);
		if (monitoringDevice == null) {
			// The requested contact was not found, so set the Status to indicate this.
			String error = "Monitoring device with Id:" + Integer.toString(uniqueID) + " was not found";
			logger.error(error);
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
			result = new JsonRepresentation("");
		} 
		else {
			// The requested contact was found, so add the Contact's XML representation to the response.
			result = new JsonRepresentation(monitoringDevice.toJson());
			// Status code defaults to 200 if we don't set it.
		}
		// Return the representation.  The Status code tells the client if the representation is valid.
		return result;
	}

	/**
	 * Adds the passed Device Type to our internal database of Monitoring Devices.
	 * @param representation The Json representation of the new Monitoring Device to add.
	 * 
	 * @return null.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Put("json")
	public Representation putMonitoringDevice(Representation representation) throws Exception {

		// Get the Json representation of the Monitoring Device.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Convert the Json representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();

		logger.debug("Json:" + jsonText);

		// Look for it in the Monitoring Device Database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		MonitoringDeviceContainer monitoringDeviceCon = confManager.getMonitoringDeviceContainer();

		monitoringDeviceCon.fromJSON(jsonText);

		logger.info("numElements:" + monitoringDeviceCon.size());

		getResponse().setStatus(Status.SUCCESS_OK);

		Representation result = new JsonRepresentation("");
		return result;
	}

	/**
	 * Deletes the unique ID from the internal database. 
	 * @return null.
	 */
	@Delete("json")
	public Representation deleteMonitoringDevice() {
		// Get the requested ID from the URL.
		int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Make sure it is no longer present in the Monitoring Device database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		MonitoringDeviceContainer monitoringDeviceCon = confManager.getMonitoringDeviceContainer();

		monitoringDeviceCon.deleteMonitoringDevice(uniqueID);
		return null;
	}



}
