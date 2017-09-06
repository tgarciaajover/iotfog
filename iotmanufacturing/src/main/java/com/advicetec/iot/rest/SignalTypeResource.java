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
import com.advicetec.configuration.SignalType;
import com.advicetec.configuration.SignalTypeContainer;

/**
 * This class exposes all signal types instances that are configured in the signal type container.
 * 
 * The user of this interface can retry a signal type definition, 
 *   inserts a new signal type or deletes a registered one.
 *   
 * @author Andres Marentes
 */
public class SignalTypeResource extends ServerResource  
{

	  /**
	   * Returns the signal Type instance requested by the URL. 
	   * 
	   * @return The JSON representation of the signal Type, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	   * unique ID is not present.
	   * 
	   * @throws Exception If problems occur making the representation. Shouldn't occur in 
	   * practice but if it does, Restlet will set the Status code. 
	   */
	  @Get("json")
	  public Representation getSignaType() throws Exception {

		// Creates an empty JSON representation.
		Representation result;

		// Gets the signal type uniqueID from the URL.
	    int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));
	    
	    // Looks for the signal type in the Signal Type container.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    SignalTypeContainer signalTypeCon = confManager.getSignalTypeContainer();
	    
	    SignalType signalType = (SignalType) signalTypeCon.getObject(uniqueID);
	    if (signalType == null) {
	      // The requested signal type was not found, so we set the status to indicate this fail condition.
	      getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
	      result = new JsonRepresentation("");
	    } 
	    else {
	      // The requested signal type was found, so we add the signal type into a JSON representation to the response.
	    	result = new JsonRepresentation(signalType.toJson());
	      // Status code defaults to 200 if we don't set it.
	      }
	    // Return the representation.  The Status code tells the client if the representation is valid.
	    return result;
	  }
	  
	  /**
	   * Adds the given Signal Type to the internal container of Signal Types.
	   * @param representation The JSON representation of the new Contact to add.
	   * 
	   * @return null.
	   * 
	   * @throws Exception If problems occur unpacking the representation.
	   */
	  @Put("json")
	  public Representation putSignalType(Representation representation) throws Exception {
		   
		// Gets the JSON representation of the SignalType.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Converts the JSON representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();
		
	    // Looks for the signal type in the Signal Type container.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    SignalTypeContainer signalTypeCon = confManager.getSignalTypeContainer();
	    
	    signalTypeCon.fromJSON(jsonText);
	    	    
	    getResponse().setStatus(Status.SUCCESS_OK);
	    
	    Representation result = new JsonRepresentation("");
	    return result;
	  }
	  
	  /**
	   * Deletes the signal type unique ID from the internal container.
	   *  
	   * @return null.
	   */
	  @Delete("json")
	  public Representation deleteSignalType() {
	    // Gets the requested Contact ID from the URL.
	    int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

	    // Makes sure it is no longer present in the Signal Unit database.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    SignalTypeContainer signalTypeCon = confManager.getSignalTypeContainer();
	    
	    signalTypeCon.deleteSignalType(uniqueID);
	    return null;
	  }

	
	
}
