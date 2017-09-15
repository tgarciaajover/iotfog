package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.DisplayDevice;
import com.advicetec.core.Processor;
import com.advicetec.displayadapter.LedSignDisplay;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

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
		
		Integer measuredEntity = this.event.getMeasuredEntity();
		
        logger.debug("process - purge:" + measuredEntity );
		
        List<DelayEvent> ret = new ArrayList<DelayEvent>();

        MeasuredEntityManager measuredEntManager = MeasuredEntityManager.getInstance();
        MeasuredEntityFacade facade = measuredEntManager.getFacadeOfEntityById(measuredEntity);
        
        if (facade == null){
        	logger.error("The requested facade: " + Integer.toString(measuredEntity) + " was not found");
		} else {
			
			facade.removeOldCacheReferences();
			logger.debug("finish executing purge facade event for  measured entity:" + measuredEntity);
		}

		return ret;

	}
	
}
