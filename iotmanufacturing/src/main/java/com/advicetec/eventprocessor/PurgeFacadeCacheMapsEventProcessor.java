package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.applicationAdapter.ProductionOrderManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.DisplayDevice;
import com.advicetec.core.EntityFacade;
import com.advicetec.core.Processor;
import com.advicetec.displayadapter.LedSignDisplay;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.MeasuredEntityType;

/**
 * This class process purge facade cache maps events, it takes as parameter the measured entity and its type. 
 * it gets the facade and removes the required data. 
 * 
 * @author Andres Marentes
 *
 */
public class PurgeFacadeCacheMapsEventProcessor implements Processor
{

	static Logger logger = LogManager.getLogger(PurgeFacadeCacheMapsEventProcessor.class.getName());
	
	/**
	 * 
	 */
	PurgeFacadeCacheMapsEvent event;
	
	/**
	 * @param event
	 */
	public PurgeFacadeCacheMapsEventProcessor(PurgeFacadeCacheMapsEvent event) {
		super();
		this.event = event;
	}

	/**
	 * This method takes the event parameters, connects to the display and publish the message.
	 * 
	 * Returns an empty list of delayed events.
	 */
	public List<DelayEvent> process() throws SQLException 
	{
		
		Integer entity = this.event.getEntity();
		
		MeasuredEntityType type = this.event.getOwnerType();
		
        logger.debug("process - purge:" + entity );
		
        List<DelayEvent> ret = new ArrayList<DelayEvent>();

        EntityFacade facade = null;
        if ((MeasuredEntityType.COMPANY == type) || 
        	 (MeasuredEntityType.FACILITY == type) || 
        	  (MeasuredEntityType.MACHINE == type) ||
        	  (MeasuredEntityType.PLANT == type)) {
        	
        	MeasuredEntityManager measuredEntManager = MeasuredEntityManager.getInstance();
        	facade = (EntityFacade) measuredEntManager.getFacadeOfEntityById(entity);
        
        } else if ((MeasuredEntityType.JOB == type)) {
        	
        	ProductionOrderManager productionOrderManager = ProductionOrderManager.getInstance();
        	facade = (EntityFacade) productionOrderManager.getFacadeOfPOrderById(entity);
        	
        } else {
        	logger.error("The event's entity associated is undefined");
        }
        
        if (facade == null){
        	logger.error("The requested facade: " + Integer.toString(entity) + " was not found");
		} else {
			
			facade.removeOldCacheReferences();
			logger.debug("finish executing purge facade event for  measured entity:" + entity);
		}

		return ret;

	}
	
}
