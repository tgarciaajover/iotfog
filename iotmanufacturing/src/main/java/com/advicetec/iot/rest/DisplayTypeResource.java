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
import com.advicetec.configuration.DisplayType;
import com.advicetec.configuration.DisplayTypeContainer;

/**
 * This class exposes all display type instances that are configured in the container.
 * 
 * The user of this interface can retry a display type definition, inserts a new display type or deletes a registered one.
 * 
 * In the case of adding a new display type, it verifies whether the dependent objects where previously created. If those are not created,
 * then the system creates them in their containers. 
 * 
 * @author Andres Marentes
 *
 */
public class DisplayTypeResource extends ServerResource  
{

	static Logger logger = LogManager.getLogger(DisplayTypeResource.class.getName());
	
	   /**
	   * Returns the display type instance requested by the URL. 
	   * 
	   * @return The JSON representation of the display type, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	   * unique ID is not present.
	   * 
	   * @throws Exception If problems occur making the representation. Shouldn't occur in 
	   * practice but if it does, Restlet will set the Status code. 
	   */
	  @Get("json")
	  public Representation getDisplayType() throws Exception 
	  {

		// Create an empty JSon representation.
		Representation result;

		// Get the contact's uniqueID from the URL.
	    int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));
	    
	    // Look for it in the Display database.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    DisplayTypeContainer displayTypeCon = confManager.getDisplayTypeContainer();
	    
	    DisplayType displayType = (DisplayType) displayTypeCon.getObject(uniqueID);
	    if (displayType == null) {
	      // The requested contact was not found, so set the Status to indicate this.
	      getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
	      result = new JsonRepresentation("");
	    } 
	    else {
	      // The requested contact was found, so add the Contact's XML representation to the response.
	    	result = new JsonRepresentation(displayType.toJson());
	      // Status code defaults to 200 if we don't set it.
	      }
	    // Return the representation.  The Status code tells the client if the representation is valid.
	    return result;
	  }
	  
	  /**
	   * Adds the given display type to the internal display type container.
	   * @param representation the Json representation of the new display Type to add.
	   * 
	   * @return null.
	   * 
	   * @throws Exception If problems occur unpacking the representation.
	   */
	  @Put("json")
	  public Representation putDisplayType(Representation representation) throws Exception 
	  {
		   
		// Get the Json representation of the Device Type.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Convert the Json representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();
		
		logger.debug("Json:" + jsonText);
		
	    // Look for it in the Device Type Database.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    DisplayTypeContainer displayTypeCon = confManager.getDisplayTypeContainer();
	    
	    displayTypeCon.fromJSON(jsonText);
	    
	    logger.debug("numElements:" + displayTypeCon.size());
	    
	    getResponse().setStatus(Status.SUCCESS_OK);
	    
	    Representation result = new JsonRepresentation("");
	    return result;
	  }
	  
	  /**
	   * Deletes the unique display type Identifier given as parameter from the internal container. 
	   * @return null.
	   */
	  @Delete("json")
	  public Representation deleteDisplayType() 
	  {
	    // Get the requested ID from the URL.
	    int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

	    // Make sure it is no longer present in the Display Type database.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    DisplayTypeContainer displayTypeCon = confManager.getDisplayTypeContainer();
	    
	    displayTypeCon.deleteDisplayType(uniqueID);
	    return null;
	  }

	
	
}
