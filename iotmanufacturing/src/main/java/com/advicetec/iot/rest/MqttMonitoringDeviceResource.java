package com.advicetec.iot.rest;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.data.Status;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.MqttMonitoringDevice;
import com.advicetec.configuration.MonitoringDeviceContainer;

public class MqttMonitoringDeviceResource extends ServerResource  
{

	static Logger logger = LogManager.getLogger(MqttMonitoringDeviceResource.class.getName());    

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
		int uniqueID = 0;
		// Get the contact's uniqueID from the URL.
		try{
			
			uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));
		
		} catch (NumberFormatException e) {
			String error = "The value given in measurin device is not a valid number";
			logger.error(error);
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
			result = new JsonRepresentation("");
			return result;
		}

		// Look for it in the Signal database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		MonitoringDeviceContainer monitoringDeviceCon = confManager.getMonitoringDeviceContainer();

		MqttMonitoringDevice monitoringDevice = (MqttMonitoringDevice) monitoringDeviceCon.getObject(uniqueID);
		if (monitoringDevice == null) {
			// The requested contact was not found, so set the Status to indicate this.
			String error = "Monitoring device with Id:" + Integer.toString(uniqueID) + " was not found";
			logger.warn(error);
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
			result = new JsonRepresentation("");
		} 
		else {
			
			// The requested measuring device was found, so add its definition to the JSON representation to the response.
			result = new JsonRepresentation(monitoringDevice.toJson());
			// Status code defaults to 200 if we don't set it.
		}
		// Return the representation.  The Status code tells the client if the representation is valid.
		return result;
	}

	/**
	 * Adds the given monitoring device to the monitoring device container. 
	 * 
	 * Schedule ModBus events associated to the MeasuringDevice.  
	 * 
	 * @param representation The JSON representation of the new Monitoring Device to add.
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

		logger.debug("putMonitoringDevice Json:" + jsonText);

		// Look for it in the Monitoring Device Database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		MonitoringDeviceContainer monitoringDeviceCon = confManager.getMonitoringDeviceContainer();
		MqttMonitoringDevice monitoringDev =  monitoringDeviceCon.fromJSONMqtt(jsonText);

		// Create modbus events.
		if (monitoringDev == null) {
			String error = "The monitoring device could not be deserialized";
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, error);
			
		} else {
						
			// Checks if the monitoring device was previously defined. 
			MqttMonitoringDevice previousMDevice = (MqttMonitoringDevice) monitoringDeviceCon.getObject(monitoringDev.getId());
			
			if (previousMDevice != null) {
				

				// Adds the monitoring device to monitoring device container.
				monitoringDeviceCon.addMonitoringDevice(monitoringDev);

				
			} else {

				// Adds the monitoring device to monitoring device container.
				monitoringDeviceCon.addMonitoringDevice(monitoringDev);

				
			}

			logger.debug("numElements:" + monitoringDeviceCon.size());
			getResponse().setStatus(Status.SUCCESS_OK);
		}
		
		Representation result = new JsonRepresentation("");
		return result;
	}

	/**
	 * Deletes the given monitoring device from the monitoring device container.
	 *  
	 * @return null.
	 * 
	 * @throws SQLException It is triggered if we can not connect to the database. 
	 */
	@Delete("json")
	public Representation deleteMonitoringDevice() throws SQLException {
		// Get the requested ID from the URL.
		int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Make sure it is no longer present in the Monitoring Device database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		MonitoringDeviceContainer monitoringDeviceCon = confManager.getMonitoringDeviceContainer();
		
		// Deletes the monitoring device from the container.
		monitoringDeviceCon.deleteMonitoringDevice(uniqueID);
		return null;
	}

}
