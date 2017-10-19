package com.advicetec.iot.rest;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.data.Status;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.InputOutputPort;
import com.advicetec.configuration.MonitoringDevice;
import com.advicetec.configuration.MonitoringDeviceContainer;
import com.advicetec.language.transformation.SyntaxChecking;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityBehavior;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

/**
 * This class exposes measured entity behavior checks which must be executed against the configuration repository.
 * 
 * The user of this interface can check if the behavior is being used in other transformation.
 *  
 * @author Andres Marentes
 *
 */
public class MeasuredEntityBehaviorCheckResource extends ServerResource  
{

	static Logger logger = LogManager.getLogger(MeasuredEntityBehaviorCheckResource.class.getName());  

	/**
	 * Returns the MeasuredEntityBehavior instance requested by the URL. 
	 * 
	 * @return The JSON representation of the Measured Entity Behavior, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	 * unique ID is not present.
	 * 
	 * @throws Exception If problems occur making the representation. Shouldn't occur in 
	 * practice but if it does, Restlet will set the Status code. 
	 */
	@Get("json")
	public Representation getTransformations() throws Exception {

		logger.info("In getTransformations");
				
		// Creates an empty JSON representation.
		Representation result;
		
		// Gets the Measured Entity's uniqueID from the URL.
		Integer uniqueID = Integer.valueOf((String)this.getRequestAttributes().get("uniqueID"));
		
		// Gets the Behavior name 
		Integer behaviorId = Integer.valueOf((String)this.getRequestAttributes().get("BehaviorID"));

		// Looks for the requested behavior in the Measured Entity database.
		MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
		MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(uniqueID);

		if (measuredEntityFacade == null) {
			String error = "Measure entity:" + Integer.toString(uniqueID) + " given is not registered"; 
			logger.error(error);
			// The requested measured entity was not found, so set the status to indicate this condition.
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, error);
			result = new JsonRepresentation("");
		} else {
			
			MeasuredEntityBehavior behavior = ((MeasuredEntity) measuredEntityFacade.getEntity()).getBehavior(behaviorId);
			
			String behaviorName = behavior.getName();
			
			// Gets all ports referring to this measuring entity
			MonitoringDeviceContainer mDevicecontainer = ConfigurationManager.getInstance().getMonitoringDeviceContainer();
			Map<Integer, List<InputOutputPort>> mDevices = mDevicecontainer.getInputOutputPortReferingMeasuredEntity(uniqueID);
			
			SyntaxChecking sintaxChecking = new SyntaxChecking();
			
			JSONArray array = new JSONArray();
			
			// Look at the transformation code and search for the behavior name
			for (Integer mDevice : mDevices.keySet()) {
				List<InputOutputPort> ports = mDevices.get(mDevice);
				
				for (InputOutputPort port : ports) {
					if (sintaxChecking.referencesImport(port.getTransformationText(), uniqueID, behaviorName )) {
						
						JSONObject jsob = new JSONObject();
						jsob.append("monitoring_device", ((MonitoringDevice) mDevicecontainer.getObject(mDevice)).getDescr());
						jsob.append("port", port.getPortLabel());
						
						array.put(jsob);
					}
				}
			}
			
			result = new JsonRepresentation(array);
			
		}
		// Return the representation.  The Status code tells the client if the representation is valid.
		return result;
	}

}
