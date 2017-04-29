package com.advicetec.iot.rest;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.data.Status;
import org.json.JSONObject;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.configuration.ReasonCodeContainer;

public class ReasonCodeResource extends ServerResource  
{

	  /**
	   * Returns the Reason Code instance requested by the URL. 
	   * 
	   * @return The JSON representation of the Reason Code, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	   * unique ID is not present.
	   * 
	   * @throws Exception if problems occur making the representation. Shouldn't occur in 
	   * practice but if it does, Restlet will set the Status code. 
	   */
	  @Get("json")
	  public Representation getReasonCode() throws Exception {

		// Create an empty JSon representation.
		Representation result;

		// Get the reason code's uniqueID from the URL.
	    int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));
	    
	    // Look for it in the Reason Code database.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    ReasonCodeContainer reasonCodeCon = confManager.getReasonCodeContainer();
	    
	    ReasonCode reasonCode = (ReasonCode) reasonCodeCon.getObject(uniqueID);
	    if (reasonCode == null) {
	      // The requested Reason Code was not found, so set the Status to indicate this.
	      getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
	      result = new JsonRepresentation("");
	    } 
	    else {
	      // The requested Reason Code was found, so add the Reason Code's JSON representation to the response.
	    	result = new JsonRepresentation(reasonCode.toJson());
	      // Status code defaults to 200 if we don't set it.
	      }
	    // Return the representation.  The Status code tells the client if the representation is valid.
	    return result;
	  }
	  
	  /**
	   * Adds the passed Reason Code to our internal database of Reason Codes.
	   * @param representation The Json representation of the new Reason Code to add.
	   * 
	   * @return null.
	   * 
	   * @throws Exception If problems occur unpacking the representation.
	   */
	  @Put("json")
	  @Post("json")
	  public Representation putReasonCode(Representation representation) throws Exception {
		   
		// Get the Json representation of the ReasonCode.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Convert the Json representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();
		
	    // Look for it in the Reason Code database.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    ReasonCodeContainer reasonCodeCon = confManager.getReasonCodeContainer();
	    
	    reasonCodeCon.fromJSON(jsonText);
	    
	    getResponse().setStatus(Status.SUCCESS_OK);
	    
	    Representation result = new JsonRepresentation("");
	    return result;
	  }
	  
	  /**
	   * Deletes the unique ID from the internal database. 
	   * @return null.
	   */
	  @Delete("json")
	  public Representation deleteReasonCode() {
	    // Get the requested Contact ID from the URL.
	    int uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));

	    // Make sure it is no longer present in the Signal Unit database.
	    ConfigurationManager confManager = ConfigurationManager.getInstance();
	    ReasonCodeContainer reasonCodeCon = confManager.getReasonCodeContainer();
	    
	    reasonCodeCon.deleteReasonCode(uniqueID);
	    return null;
	  }	
}
