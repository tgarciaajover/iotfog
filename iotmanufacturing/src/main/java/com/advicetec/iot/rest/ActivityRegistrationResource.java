package com.advicetec.iot.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.advicetec.applicationAdapter.ProductionOrderFacade;
import com.advicetec.applicationAdapter.ProductionOrderManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.ReasonCodeContainer;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuringState;
import com.advicetec.monitorAdapter.protocolconverter.MqttDigital;

public class ActivityRegistrationResource extends ServerResource  
{
	
	static Logger logger = LogManager.getLogger(ActivityRegistrationResource.class.getName());

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
	public Representation putActivityRegister(Representation representation) throws Exception {

		// Create an empty JSon representation.
		Representation result;
		
		// Get the Json representation of the ReasonCode.
		JsonRepresentation jsonRepresentation = new JsonRepresentation(representation);

		// Convert the Json representation to the Java representation.
		JSONObject jsonobject = jsonRepresentation.getJsonObject();
		String jsonText = jsonobject.toString();

		// Get the information from the activity registration
		Integer idCompania = jsonobject.getInt("id_compania");
		Integer idSede = jsonobject.getInt("id_sede");
		Integer idPlanta = jsonobject.getInt("id_planta");
		Integer idGrupoMaquina = jsonobject.getInt("id_grupo_maquina");
		Integer idMaquina = jsonobject.getInt("id_maquina");
		Integer ano = jsonobject.getInt("ano");
		Integer mes = jsonobject.getInt("mes");
		String tipoActividad = jsonobject.getString("tipo_actividad");
		Integer idRazonParada = jsonobject.getInt("id_razon_parada");
		Integer idProduccion = jsonobject.getInt("id_produccion");
		
		// Bring the measured entity
	    MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
	    MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(idMaquina);
	    
	    if (measuredEntityFacade == null) {
	      // The requested contact was not found, so set the Status to indicate this.
	      getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
	      result = new JsonRepresentation("");
	    } 
	    else {

	        if (tipoActividad.compareTo("S") == 0){
	        	ProductionOrderManager productionOrderManager = ProductionOrderManager.getInstance(); 

	        	// Bring the production order from the container.
	        	
	        	// Start of the production order
	        	ProductionOrderFacade productionOrderFacade = productionOrderManager.getFacadeOfPOrderById(idProduccion);
	        	
	        	if (measuredEntityFacade.getCurrentState() == MeasuringState.OPERATING){
		        	// start production
		        	measuredEntityFacade.startExecutedObject(productionOrderFacade.getProductionOrder());
		        	
		        	// This function searches the actual status of the production order 
		        	// and based on that it creates a previous interval. 
		        	productionOrderFacade.start();
		        	
	        	} else {
	        		logger.error("The measured entity is not running");
	        	}
	        	        	
	        	
	        } else if (tipoActividad.compareTo("E") == 0) {
	        	// End of the production order
	        	ProductionOrderManager productionOrderManager = ProductionOrderManager.getInstance(); 
	        		        		        	
	        	// remove the facade from the Manager
	        	productionOrderManager.removeFacade(idProduccion);
	        	
	        	productionOrderManager.getProductionOrderContainer().removeObject(idProduccion);
	        	
	        	// Remove the production order from the measured entity.
	        	measuredEntityFacade.removeExecutedObject(idProduccion);
	        		        	
	        	
	        } else if (tipoActividad.compareTo("C") == 0) {
	        	// Start of a new stop, call the behavior.
	        	
	    		// Look for it in the Reason Code database.
	    		ConfigurationManager confManager = ConfigurationManager.getInstance();
	    		ReasonCodeContainer reasonCodeCon = confManager.getReasonCodeContainer();
	    		reasonCodeCon.getObject(idRazonParada);
	    		
	        	// ask if there are production orders already in execution. 
	        	List<ExecutingObject> objects = measuredEntityFacade.getEntity().getExecutingObjects();

	        	// End of the production order
	        	ProductionOrderManager productionOrderManager = ProductionOrderManager.getInstance(); 

	        	// loop through the objects and put them stopped.
	        	for (int i = 0; i < objects.size(); i++){
	        		ProductionOrderFacade productionOrderFacade = productionOrderManager.getFacadeOfPOrderById(objects.get(i).getId());
	        		productionOrderFacade.stop();
	        	}
	        	
	        	// If there is a new stop by the application, the machine should be stopped. In case that it is not stopped, we should 
	        	// report the error.
	    		
	        } else  {
	        	logger.error("The activity type received is not valid" + tipoActividad);
	        }
	    
	    }
		
				

		reasonCodeCon.fromJSON(jsonText);

		getResponse().setStatus(Status.SUCCESS_OK);

		result = new JsonRepresentation("");
		return result;
	}
	
}
