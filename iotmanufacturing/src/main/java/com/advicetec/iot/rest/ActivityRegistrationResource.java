package com.advicetec.iot.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.stax2.ri.typed.ValueEncoderFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.advicetec.applicationAdapter.ProductionOrder;
import com.advicetec.applicationAdapter.ProductionOrderFacade;
import com.advicetec.applicationAdapter.ProductionOrderManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.ReasonCodeContainer;
import com.advicetec.core.AttributeType;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.eventprocessor.MeasuredEntityEvent;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuringState;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.monitorAdapter.protocolconverter.MqttDigital;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;

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

		logger.info("json that arrive:" + jsonText);
		try
		{
			// Get the information from the activity registration
			int idCompania = jsonobject.getInt("id_compania");
			int idSede = jsonobject.getInt("id_sede");
			int idPlanta = jsonobject.getInt("id_planta");
			int idGrupoMaquina = jsonobject.getInt("id_grupo_maquina");
			int idMaquina = jsonobject.getInt("id_maquina");
			int ano = jsonobject.getInt("ano");
			int mes = jsonobject.getInt("mes");
			String tipoActividad = jsonobject.getString("tipo_actividad");
			int idRazonParada = jsonobject.getInt("id_razon_parada");
			int idProduccion = jsonobject.getInt("id_produccion");

			logger.debug("idMaquina:" + Integer.toString(idCompania) + 
						"idSede:" + Integer.toString(idSede) +
						"idPlanta:" + Integer.toString(idPlanta) +
						"idGrupoMaquina : " + Integer.toString(idGrupoMaquina) +
						"idMaquina :" + Integer.toString(idMaquina) );
			
			// Bring the measured entity
		    MeasuredEntityManager measuredEntityManager = MeasuredEntityManager.getInstance();
		    MeasuredEntityFacade measuredEntityFacade = measuredEntityManager.getFacadeOfEntityById(idMaquina);
		    
		    if (measuredEntityFacade == null) {
		      
		    	// The requested contact was not found, so set the Status to indicate this.
		      logger.error("Meaured Entity requested: " + Integer.toString(idMaquina) + " was not found");
		      getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);

		    } 
		    else {
	
		        if (tipoActividad.compareTo("S") == 0)
		        {
		        	logger.info("in register production order start");
		        
		        	ProductionOrderManager productionOrderManager = ProductionOrderManager.getInstance(); 
			        	
		        	// Start of the production order
		        	ProductionOrderFacade productionOrderFacade = productionOrderManager.getFacadeOfPOrderById(idProduccion);
		        	
		        	if (productionOrderFacade == null)
		        	{
		        		ProductionOrder oProd = (ProductionOrder) productionOrderManager.getProductionOrderContainer().getObject(idProduccion);
		        		if (oProd != null) {
		        			productionOrderManager.addProductionOrder(oProd);
		        			productionOrderFacade = productionOrderManager.getFacadeOfPOrderById(idProduccion);
		        		}
		        	}
	
		        	if (productionOrderFacade == null) {
		        		getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
		        		logger.error("The production order with number:" + Integer.toString(idProduccion) + " was not found");
		        		
		        	} else {
		        	
		        		logger.info("Production Order found, it is going to be put in execution");
		        		
		        		
		        		// Stop all other executed Objects
		        		measuredEntityFacade.stopExecutedObjects();
			        	
		        		// put the production order in execution.
		        		productionOrderFacade.start();
		        		
			        	// start production
			        	measuredEntityFacade.addExecutedObject(productionOrderFacade.getProductionOrder());
				        				        	
			        	getResponse().setStatus(Status.SUCCESS_OK);
				        	
		        	}
		        	
		        } else if (tipoActividad.compareTo("E") == 0) {
		        
		        	logger.info("in register production order end");
		        	
		        	// End of the production order
		        	ProductionOrderManager productionOrderManager = ProductionOrderManager.getInstance(); 
		        		        		        	
		        	// remove the facade from the Manager
		        	productionOrderManager.removeFacade(idProduccion);
		        	
		        	productionOrderManager.getProductionOrderContainer().removeObject(idProduccion);
		        	
		        	// Remove the production order from the measured entity.
		        	measuredEntityFacade.removeExecutedObject(idProduccion);
		        	
		        	getResponse().setStatus(Status.SUCCESS_OK);
		        		        	
		        	
		        } else if (tipoActividad.compareTo("C") == 0) {
		        	
		        	logger.info("in register measured entity stop");
		        	
		        	// Look for it in the Reason Code database.
		    		ConfigurationManager confManager = ConfigurationManager.getInstance();
		    		ReasonCodeContainer reasonCodeCon = confManager.getReasonCodeContainer();
		    		reasonCodeCon.getObject(idRazonParada);
	
		        	// Start of a new stop, call the behavior.
		    		
		    		// Get the current state of the measured entity
		    		if (measuredEntityFacade.getEntity()== null){
		        		getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
		  		      	result = new JsonRepresentation("");
		    			logger.error("No entity assigned to this measured entity facade");
		  		      	return result;
		    		} else {
		    			MeasuringState state =  measuredEntityFacade.getEntity().getCurrentState();
		    			String behavior = measuredEntityFacade.getEntity().getBehaviorText(state, idRazonParada);
		    			
		    			ArrayList<InterpretedSignal> signals = new ArrayList<InterpretedSignal>();
		    			InterpretedSignal reasonSignal = new InterpretedSignal(AttributeType.INT, new Integer(idRazonParada));
		    			signals.add(reasonSignal);
	
						MeasuredEntityEvent event = new MeasuredEntityEvent(behavior, measuredEntityFacade.getEntity().getId(),0, 0, signals );
						event.setRepeated(false);
						event.setMilliseconds(0); // To be executed now.
						
						EventManager eventManager = EventManager.getInstance();
						
						Queueable obj = new Queueable(QueueType.EVENT, event);
						eventManager.getQueue().enqueue(6, obj);
						
						getResponse().setStatus(Status.SUCCESS_OK);
						
		    		}
		    		
		    		// 
		        	
		        	// If there is a new stop by the application, the machine should be stopped. In case that it is not stopped, we should 
		        	// report the error.
		    		
		        } else  {
	        		getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
		        	logger.error("The activity type received is not valid" + tipoActividad);
		        }
		    
		    }
			
			
		} catch (JSONException e) {
    		getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			logger.error("The json could not be parsed - Text:" + jsonText );
		}

		result = new JsonRepresentation("");
		return result;
	}
	
}
