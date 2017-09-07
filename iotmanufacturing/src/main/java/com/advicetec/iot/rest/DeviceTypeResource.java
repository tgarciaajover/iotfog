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

/**
 * This class exposes all device type instances that are configured in the device type container.
 * 
 * The user of this interface can retry the device type definition, inserts a new device type or deletes a registered one.
 * 
 * In the case of adding a new object, it verifies whether the dependent objects where previously created. If those are not created,
 * then the system creates them in their containers. 
 * 
 * @author Andres Marentes
 *
 */
public class DeviceTypeResource extends ServerResource  
{

	static Logger logger = LogManager.getLogger(DeviceTypeResource.class.getName());
	
	  /**
	   * Returns the Device Type instance requested by the URL. 
	   * 
	   * @return The JSON representation of the Device Type, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	   * unique ID is not present.
	   * 
	   * @throws Exception If problems occur making the representation. Shouldn't occur in 
	   * practice but if it does, Restlet will set the Status code. 
	   */
	  @Get("json")
	  public Representation getDeviceType() throws Exception {

		// Create an empty JSon representation.
		Representation result;

		// Get the contact's uniqueID from the URL.
	    int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));
	    
	    // Look for it in the Signal database.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    DeviceTypeContainer deviceTypeCon = confManager.getDeviceTypeContainer();
	    
	    DeviceType deviceType = (DeviceType) deviceTypeCon.getObject(uniqueID);
	    if (deviceType == null) {
	      // The requested contact was not found, so set the Status to indicate this.
	      getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
	      result = new JsonRepresentation("");
	    } 
	    else {
	      // The requested contact was found, so add the Contact's XML representation to the response.
	    	result = new JsonRepresentation(deviceType.toJson());
	      // Status code defaults to 200 if we don't set it.
	      }
	    // Return the representation.  The Status code tells the client if the representation is valid.
	    return result;
	  }
	  
	  /**
	   * Adds the given Device Type to the internal Device Type container.
	   * @param representation The Json representation of the new Device Type to be added.
	   * 
	   * @return null.
	   * 
	   * @throws Exception If problems occur unpacking the representation.
	   */
	  @Put("json")
	  public Representation putDeviceType(Representation representation) throws Exception {
		   
		// Get the Json representation of the Device Type.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Convert the Json representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();
		
		logger.debug("Json:" + jsonText);
		
	    // Look for it in the Device Type Database.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    DeviceTypeContainer deviceTypeCon = confManager.getDeviceTypeContainer();
	    
	    deviceTypeCon.fromJSON(jsonText);
	    
	    logger.debug("numElements:" + deviceTypeCon.size());
	    
	    getResponse().setStatus(Status.SUCCESS_OK);
	    
	    Representation result = new JsonRepresentation("");
	    return result;
	  }
	  
	  /**
	   * Deletes the unique device type ID from the internal Device Type container. 
	   * @return null.
	   */
	  @Delete("json")
	  public Representation deleteDeviceType() {
	    // Get the requested ID from the URL.
	    int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

	    // Make sure it is no longer present in the Signal Unit database.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    DeviceTypeContainer deviceTypeCon = confManager.getDeviceTypeContainer();
	    
	    deviceTypeCon.deleteDeviceType(uniqueID);
	    return null;
	  }
}
