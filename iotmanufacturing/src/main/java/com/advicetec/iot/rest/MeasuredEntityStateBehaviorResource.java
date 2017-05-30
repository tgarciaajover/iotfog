package com.advicetec.iot.rest;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.json.JSONObject;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.SignalUnit;
import com.advicetec.configuration.SignalUnitContainer;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityContainer;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

public class MeasuredEntityStateBehaviorResource extends ServerResource  
{

	  /**
	   * Returns the MeasuredEntity instance requested by the URL. 
	   * 
	   * @return The JSON representation of the Measured Entity, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	   * unique ID is not present.
	   * 
	   * @throws Exception If problems occur making the representation. Shouldn't occur in 
	   * practice but if it does, Restlet will set the Status code. 
	   */
	  @Get("json")
	  public Representation getMeasuredEntity() throws Exception {

		// Create an empty JSon representation.
		Representation result;

		// Get the contact's uniqueID from the URL.
	    Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));
	    
	    // Look for it in the Measured Entity database.
	    MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
	    MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);
	    
	    if (measuredEntityFacade == null) {
	      // The requested contact was not found, so set the Status to indicate this.
	      getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
	      result = new JsonRepresentation("");
	    } 
	    else {
	      // The requested contact was found, so add the Contact's XML representation to the response.
	    	result = new JsonRepresentation(measuredEntityFacade.getEntity().toJson());
	      // Status code defaults to 200 if we don't set it.
	      }
	    // Return the representation.  The Status code tells the client if the representation is valid.
	    return result;
	  }
	  
	  /**
	   * Adds the passed MeasuredEntity to our internal database of Measured Entities.
	   * @param representation The Json representation of the new Contact to add.
	   * 
	   * @return null.
	   * 
	   * @throws Exception If problems occur unpacking the representation.
	   */
	  @Put("json")
	  public Representation putMeasuredEntity(Representation representation) throws Exception {
		   
		// Get the Json representation of the SignalUnit.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Get the contact's uniqueID from the URL.
	    String uniqueID = (String)this.getRequestAttributes().get("uniqueID");
		
		// Convert the Json representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();
		
	    // Look for it in the Signal Unit database.
	    MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
	    MeasuredEntityContainer container = measuredEntityManager.getMeasuredEntityContainer();
	    
	    MeasuredEntity measuredEntity = container.fromJSON(jsonText);
	    
	    if (measuredEntityManager.getFacadeOfEntityById(measuredEntity.getId()) == null){
	    	measuredEntityManager.addNewEntity(measuredEntity);
	    }
	    	    
	    getResponse().setStatus(Status.SUCCESS_OK);
	    
	    Representation result = new JsonRepresentation("");
	    return result;
	  }
	  
}
