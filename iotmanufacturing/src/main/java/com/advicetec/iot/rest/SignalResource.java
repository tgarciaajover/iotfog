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
import com.advicetec.configuration.Signal;
import com.advicetec.configuration.SignalContainer;

/**
 * This class exposes all Signals instances that are configured in the signal container.
 * 
 * The user of this interface can retry the signal definition, 
 *   inserts a new signal or deletes a registered one.
 *   
 * @author Andres Marentes
 */
public class SignalResource extends ServerResource  
{

	static Logger logger = LogManager.getLogger(MonitoringDeviceResource.class.getName());
	
	/**
	 * Returns the Signal instance requested by the URL. 
	 * 
	 * @return The JSON representation of the Signal, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	 * unique ID is not present.
	 * 
	 * @throws Exception If problems occur making the representation. Shouldn't occur in 
	 * practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getSignal() throws Exception {

		// Creates an empty JSon representation.
		Representation result;

		// Gets the signal uniqueID from the URL.
		int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Looks for it in the Signal database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		SignalContainer signalCon = confManager.getSignalContainer();

		Signal signal = (Signal) signalCon.getObject(uniqueID);
		if (signal == null) {
			// The requested signal was not found, so we set the status to indicate this fail condition.
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			result = new JsonRepresentation("");
		} 
		else {
			// The requested signal was found, so we add the signal to the resulting JSON representation
			result = new JsonRepresentation(signal.toJson());
			// Status code defaults to 200 if we don't set it.
		}
		// Return the representation.  The Status code tells the client if the representation is valid.
		return result;
	}

	/**
	 * Adds the given Signal to our internal container of signals.
	 * 
	 * @param representation The JSON representation of the new signal to add.
	 * 
	 * @return null.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Put("json")
	public Representation putSignal(Representation representation) throws Exception {

		// Gets the JSON representation of the Signal.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Converts the JSON representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();

		logger.debug("Json:" + jsonText);

		// Looks for it in the Signal Database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		SignalContainer signalCon = confManager.getSignalContainer();

		signalCon.fromJSON(jsonText);

		logger.debug("numElements:" + signalCon.size());

		getResponse().setStatus(Status.SUCCESS_OK);

		Representation result = new JsonRepresentation("");
		return result;
	}

	/**
	 * Deletes the unique signal ID from the internal database. 
	 * @return null.
	 */
	@Delete("json")
	public Representation deleteSignal() {
		// Get the requested Contact ID from the URL.
		int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Make sure it is no longer present in the Signal Unit database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		SignalContainer signalCon = confManager.getSignalContainer();

		signalCon.deleteSignal(uniqueID);
		return null;
	}
}
