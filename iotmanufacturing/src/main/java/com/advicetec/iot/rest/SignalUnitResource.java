package com.advicetec.iot.rest;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.data.Status;
import org.json.JSONObject;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.SignalUnit;
import com.advicetec.configuration.SignalUnitContainer;

/**
 * This class exposes all signal units instances that are configured in the signal unit container.
 * 
 * The user of this interface can retry a signal unit definition, 
 *   inserts a new signal unit or deletes a registered one.
 *   
 * @author Andres Marentes
 */
public class SignalUnitResource extends ServerResource  
{

	/**
	 * Returns the Signal Unit instance requested by the URL. 
	 * 
	 * @return The JSON representation of the Signal Unit, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	 * unique ID is not present.
	 * 
	 * @throws Exception If problems occur making the representation. Shouldn't occur in 
	 * practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getSignaUnit() throws Exception {

		// Creates an empty JSON representation.
		Representation result;

		// Get the signal unit uniqueID from the URL.
		int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		// Looks for the signal unit in the Signal Unit container.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		SignalUnitContainer signalUnitCon = confManager.getSignalUnitContainer();

		SignalUnit signalUnit = (SignalUnit) signalUnitCon.getObject(uniqueID);
		if (signalUnit == null) {
			// The requested signal unit was not found, so we set the status to indicate this failing condition.
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			result = new JsonRepresentation("");
		} 
		else {
			// The requested signal unit was found, so add the signal unit to the JSON response representation.
			result = new JsonRepresentation(signalUnit.toJson());
			// Status code defaults to 200 if we don't set it.
		}
		// Return the representation.  The Status code tells the client if the representation is valid.
		return result;
	}

	/**
	 * Adds the given signal unit to the interval container of signal units.
	 * @param representation The JSON representation of the new signal unit to add.
	 * 
	 * @return null.
	 * 
	 * @throws Exception If problems occur unpacking the representation.
	 */
	@Put("json")
	public Representation putSignalUnit(Representation representation) throws Exception {

		// Gets the JSON representation of the SignalUnit.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Converts the JSON representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();

		// Looks for the signal unit in the Signal Unit database.
		ConfigurationManager confManager = ConfigurationManager.getInstance();
		SignalUnitContainer signalUnitCon = confManager.getSignalUnitContainer();

		signalUnitCon.fromJSON(jsonText);

		getResponse().setStatus(Status.SUCCESS_OK);

		Representation result = new JsonRepresentation("");
		return result;
	}

	/**
	 * Deletes the signal unit unique ID from the internal container.
	 *  
	 * @return null.
	 */
	@Delete("json")
	public Representation deleteSignalUnit() {
		
		// Gets the requested signal unit ID from the URL.
		int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

		ConfigurationManager confManager = ConfigurationManager.getInstance();

		// Make sure it is no longer present in the Signal Unit database.
		SignalUnitContainer signalUnitCon = confManager.getSignalUnitContainer();

		signalUnitCon.deleteSignalUnit(uniqueID);
		return null;
	}



}
