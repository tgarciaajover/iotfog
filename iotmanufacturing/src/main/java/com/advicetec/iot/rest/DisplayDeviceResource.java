package com.advicetec.iot.rest;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.DisplayDevice;
import com.advicetec.configuration.DisplayDeviceContainer;

public class DisplayDeviceResource extends ServerResource  
{

	  /**
	   * Returns the Display Device instance requested by the URL. 
	   * 
	   * @return The JSON representation of the Signal, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	   * unique ID is not present.
	   * 
	   * @throws Exception If problems occur making the representation. Shouldn't occur in 
	   * practice but if it does, Restlet will set the Status code. 
	   */
	  @Get("json")
	  public Representation getDisplayDevice() throws Exception 
	  {

		// Create an empty JSon representation.
		Representation result;

		// Get the contact's uniqueID from the URL.
	    int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));
	    
	    // Look for it in the Display database.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    DisplayDeviceContainer displayDeviceCon = confManager.getDisplayDeviceContainer();
	    
	    DisplayDevice displayDevice = (DisplayDevice) displayDeviceCon.getObject(uniqueID);
	    if (displayDevice == null) 
	    {
	      // The requested contact was not found, so set the Status to indicate this.
	      getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
	      result = new JsonRepresentation("");
	    } 
	    else {
	      // The requested contact was found, so add the Contact's XML representation to the response.
	    	result = new JsonRepresentation(displayDevice.toJson());
	      // Status code defaults to 200 if we don't set it.
	      }
	    // Return the representation.  The Status code tells the client if the representation is valid.
	    return result;
	  }
	  
	  /**
	   * Adds the passed Display Device to our internal database of Display Device.
	   * @param representation The Json representation of the new Display Device to add.
	   * 
	   * @return null.
	   * 
	   * @throws Exception If problems occur unpacking the representation.
	   */
	  @Put("json")
	  public Representation putDisplayDevice(Representation representation) throws Exception 
	  {
		   
		// Get the Json representation of the Device Type.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Convert the Json representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();
		
		System.out.println("Json:" + jsonText);
		
	    // Look for it in the Device Type Database.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    DisplayDeviceContainer displayDeviceCon = confManager.getDisplayDeviceContainer();
	    
	    displayDeviceCon.fromJSON(jsonText);
	    
	    System.out.println("numElements:" + displayDeviceCon.size());
	    
	    getResponse().setStatus(Status.SUCCESS_OK);
	    
	    Representation result = new JsonRepresentation("");
	    return result;
	  }
	  
	  /**
	   * Deletes the unique ID from the internal database. 
	   * @return null.
	   */
	  @Delete("json")
	  public Representation deleteDisplaydevice() 
	  {
	    // Get the requested ID from the URL.
	    int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

	    // Make sure it is no longer present in the Display Type database.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    DisplayDeviceContainer displayDeviceCon = confManager.getDisplayDeviceContainer();
	    
	    displayDeviceCon.deleteDisplayDevice(uniqueID);
	    return null;
	  }

}
