package com.advicetec.measuredentitity;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.core.Configurable;
import com.advicetec.eventprocessor.AggregationEvent;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.eventprocessor.ModBusTcpEvent;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;
import com.advicetec.utils.MqttSubscriber;

/**
 * This class manages the list of measured entities registered.
 * 
 * @author Advicetec
 *
 */
public class MeasuredEntityManager extends Configurable {
	
	/**
	 * Reference to the singleton that is going to be used to reference the object.
	 */
	private static MeasuredEntityManager instance = null;
	
	/**
	 * Container used to retry measured entities from the database.  
	 */
	private MeasuredEntityContainer measuredEntities;
	
	static Logger logger = LogManager.getLogger(MeasuredEntityManager.class.getName());
	
	/**
	 * List of measured entities registered. 
	 */
	private List<MeasuredEntityFacade> entities;
	
	/**
	 *	Field that maintains the machine speed in case of no job on the machine. 
	 */
	private String productionRateId;
	
	/**
	 * Field that establishes the conversion Product Unit 1 / Cycle
	 */
	private String unit1PerCycles;
	
	/**
	 * Field that establishes the conversion Product Unit 2 / Cycle
	 */
	private String unit2PerCycles;
	
	/**
	 * This field is the attribute name for the production counter.
	 */
	private String actualProductionCountId;

	/**
	 * This field establishes how often we have to remove the cache entries (seconds). 
	 */
	private Integer purgeFacadeCacheMapEntries;
	
	/**
	 * Constructor for the class 
	 * 
	 * @throws SQLException  this exception is thrown is case of database connection error.
	 */
	private MeasuredEntityManager() throws SQLException{
		
		super("MeasuredEntity");
		
		logger.info("In MeasuredEntityManager constructor");
		
		entities = new ArrayList<MeasuredEntityFacade>();
		
		String driver = properties.getProperty("driver");
		String server = properties.getProperty("server");
		String user = properties.getProperty("user");
		String password = properties.getProperty("password");

		this.productionRateId = properties.getProperty("machineRateField");
		this.unit1PerCycles = properties.getProperty("machineUnit1PerCycles");
		this.unit2PerCycles = properties.getProperty("machineUnit2PerCycles");
		this.actualProductionCountId = properties.getProperty("actualProductionCountField");

		logger.info("PurgeFacadeCacheMapEntries" + properties.getProperty("PurgeFacadeCacheMapEntries"));
		if (properties.getProperty("PurgeFacadeCacheMapEntries") != null) {
			try {
				this.purgeFacadeCacheMapEntries =Integer.parseInt(properties.getProperty("PurgeFacadeCacheMapEntries").trim());
			} catch(NumberFormatException ex){ // handle your exception
				logger.error(ex);
			}
		} else {
			this.purgeFacadeCacheMapEntries = new Integer(10); // By default 10 seconds.
		}
		
		logger.info("It is going to load the measured entity container");
		
		measuredEntities = new MeasuredEntityContainer(driver, server, user, password);
		measuredEntities.loadContainer();
		
		logger.info("after loading the measured entity container");
		
		for (Integer i : measuredEntities.getKeys()) {
			
			MeasuredEntity m = (MeasuredEntity) measuredEntities.getObject(i);
						
			MeasuredEntityFacade f = new MeasuredEntityFacade(m, this.productionRateId, 
																this.unit1PerCycles, this.unit2PerCycles, 
																this.actualProductionCountId, this.purgeFacadeCacheMapEntries);
			
			entities.add(f);
		}
		
		logger.info("Num facades that have been read:" + Integer.toString(this.entities.size()) );
		
		List<ModBusTcpEvent> events = measuredEntities.getModBusEvents();
		
		for (ModBusTcpEvent evt : events){
			Queueable obj = new Queueable(QueueType.EVENT, evt);
			try {
				logger.info("new: " + events.size() +  " modbus event created");
				EventManager.getInstance().getQueue().enqueue(6, obj);
				
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		
		// Start the MQQ subscriber if there is a MQTT connection configured.
		if (measuredEntities.requireMQTT()){
			MqttSubscriber mqttSubscriber = new MqttSubscriber();
			mqttSubscriber.run();
		}

		
		// Collect scheduled events for all measured entities.
		List<AggregationEvent> scheduledEvents = new ArrayList<AggregationEvent>();
		for (Integer i : measuredEntities.getKeys()) {
			MeasuredEntity m = (MeasuredEntity) measuredEntities.getObject(i);
			scheduledEvents.addAll(m.getScheduledEvents());
		}
		
		scheduleAggregationEvents(scheduledEvents);
		
		
	}

	/**
	 * Gets the reference to singleton instance
	 *  
	 * @return  measure entity manager singleton. 
	 * 
	 * @throws SQLException  it is thrown if the connection to the database could not be established.
	 */
	public synchronized static MeasuredEntityManager getInstance() throws SQLException{
		if(instance == null){
			instance = new MeasuredEntityManager();
		}
		return instance;
	}
	
	/**
	 * Verifies if a particular measured entity is already registered in the database.
	 *   
	 * Returns TRUE if the measured entity given parameter is already into the list of facades.
	 * 
	 * @param entity	Measured entity that is given to verify if it is registered in the manager.
	 * 
	 * @return 			TRUE if the entity already exist into the list, FALSE otherwise.
	 */
	private synchronized boolean entityAlreadyExists(final MeasuredEntity entity){	
		for (MeasuredEntityFacade facade : entities) {
			if(facade.getEntity().getId().equals(entity.getId())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Inserts a new entity in the measured entity list and creates its facade.
	 * 
	 * @param entity 	The new measured entity.
	 * @return			true if the measured entity was registered}, false otherwise.
	 * 
	 * @throws PropertyVetoException 
	 */
	public synchronized boolean addNewEntity(final MeasuredEntity entity){
		if(entityAlreadyExists(entity)){
			return false;
		}
		return entities.add(new MeasuredEntityFacade(entity, this.productionRateId, 
							this.unit1PerCycles, this.unit2PerCycles, 
							this.actualProductionCountId,this.purgeFacadeCacheMapEntries));
	}
	
	/**
	 * Returns the measured entity facade for a given entity id.
	 * 
	 * @param entityId 	The entity id to search.
	 * 
	 * @return NULL if there is not an entity with the given id.
	 */
	public synchronized MeasuredEntityFacade getFacadeOfEntityById(final Integer entityId){	
		
		logger.debug("getFacadeOfEntityById" + Integer.toString(entityId) );
		
		for (MeasuredEntityFacade facade : entities) {
			
			if (facade.getEntity() == null)
				logger.error("entity registered is null");
			else{ 
				logger.debug("facade:" + Integer.toString(facade.getEntity().getId()));
				
				if(facade.getEntity().getId().equals(entityId)){
					return facade;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the reference to the measured entity container
	 * 
	 * @return measured entity container
	 */
	public synchronized MeasuredEntityContainer getMeasuredEntityContainer()
	{
		return this.measuredEntities;
	}

	
	/**
	 * Gets the internal measure identifier from the measured entity canonical identifier. 
	 *  
	 * @param company			Company canonical identifier
	 * @param location			Location canonical identifier
	 * @param plant				Plant canonical identifier
	 * @param machineGroup		Machine group canonical identifier
	 * @param machineId			Machine canonical identifier
	 * 
	 * @return	measured entity internal identifier 
	 */
	public synchronized Integer getMeasuredEntityId(String company, String location,
			String plant, String machineGroup, String machineId) {
		
		return this.measuredEntities.getCanonicalObject(company, location, plant, machineGroup, machineId);
	}
	
	/**
	 * Gets the canonical identifier from a internal measured entity identifier
	 * @param id	internal measured entity identifier
	 * 
	 * @return		canonical identifier		
	 */
	public String getCanonicalById(Integer id){
		if (this.measuredEntities.getObject(id) == null) {
			return null;
		}
		else {
			return ((MeasuredEntity) this.measuredEntities.getObject(id)).getCanonicalKey();
		}
	}
	
	/**
	 * Removes a measured entity from its internal identifier
	 * 
	 * @param entityId	measured entity identifier to remove
	 * 
	 * @return	True if the measured entity was removed, false otherwise.
	 */
	public synchronized boolean removeMeasuredEntity(Integer entityId, MeasuredEntityType entityType)
	{

		logger.debug("getFacadeOfEntityById" + Integer.toString(entityId) );
	
		// Removes the events related with this measured entity
		EventManager.getInstance().removeEntityEvents(entityId, entityType);
		
		// Remove the measured entity from the list of measured entities facades.
		int index = 0;
		MeasuredEntityFacade facade = null;
		while ( index <= entities.size()) {
			facade = entities.get(index);
			
			if (facade.getEntity() == null)
				logger.error("entity registered is null");
			else{ 
				logger.debug("facade:" + Integer.toString(facade.getEntity().getId()));
				
				if(facade.getEntity().getId().equals(entityId)){
					
					// Stops all executed objects
					facade.stopExecutedObjects();
										
					// Removes the facade.
					entities.remove(index);
					break;
				}
			}
			
			index = index + 1;
		}
		
		// Remove from the container
		this.measuredEntities.removeObject(entityId);
			
		// always return true, that means that if not present is like it has been deleted.
		return true;
	}
	
	
	/**
	 * Schedule an aggregation event list in the scheduler.
	 *  
	 * @param scheduledEvents	Lists of scheduled events to schedule.
	 */
	public synchronized void scheduleAggregationEvents(List<AggregationEvent> scheduledEvents) {
		
		logger.info("scheduling # event:" + scheduledEvents.size());
		
		// Put to execute all scheduled events.
		int numEvent = 0;
		for (AggregationEvent evt : scheduledEvents){
			long seconds =  evt.getSecondsToNextExecution();
			
			logger.debug("Next Recurrence to occur in: " + seconds + " seconds");
			
			DelayEvent dEvent = new DelayEvent(evt,seconds*1000);
			
			try {
				
				EventManager.getInstance().getDelayedQueue().put(dEvent);
				
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			
			numEvent = numEvent + 1;
		}

		logger.info("Number of scheduled events: " + numEvent);
	}
}
