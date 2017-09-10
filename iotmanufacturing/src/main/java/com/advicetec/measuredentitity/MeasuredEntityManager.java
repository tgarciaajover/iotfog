package com.advicetec.measuredentitity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.core.Configurable;
import com.advicetec.eventprocessor.AggregationEvent;
import com.advicetec.eventprocessor.Event;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.eventprocessor.ModBusTcpEvent;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;
import com.advicetec.persistence.MeasureAttributeValueCache;
import com.advicetec.utils.MqttSubscriber;

/**
 * This class manages the list of entities.
 * 
 * @author user
 *
 */
public class MeasuredEntityManager extends Configurable {
	
	private static MeasuredEntityManager instance=null;
	private MeasuredEntityContainer measuredEntities;
	
	static Logger logger = LogManager.getLogger(MeasuredEntityManager.class.getName());
	
	private List<MeasuredEntityFacade> entities;
	
	// Field that maintains the machine speed in case of no job on the machine. 
	private String productionRateId;
	
	// Field that establishes the conversion Product Unit 1 / Cycle
	private String unit1PerCycles;
	
	// Field that establishes the conversion Product Unit 2 / Cycle
	private String unit2PerCycles;
	
	// This field is the attribute name for the production counter.
	private String actualProductionCountId;
	
	private MeasuredEntityManager() throws SQLException{
		
		super("MeasuredEntity");
		
		logger.info("In MeasuredEntityManager constructor"  );
		
		entities = new ArrayList<MeasuredEntityFacade>();
		
		// String[] machines = properties.getProperty("machines").split(",");
		
		String initCapacity = properties.getProperty("cache_initialCapacity");
		String maxSize = properties.getProperty("cache_maxSize");
		
		// creates an instance if it is not exists
		MeasureAttributeValueCache.getInstance();
		// sets cache parameters
		MeasureAttributeValueCache.setCache(
				Integer.parseInt(initCapacity), Integer.parseInt(maxSize));

		String driver = properties.getProperty("driver");
		String server = properties.getProperty("server");
		String user = properties.getProperty("user");
		String password = properties.getProperty("password");

		this.productionRateId = properties.getProperty("machineRateField");
		this.unit1PerCycles = properties.getProperty("machineUnit1PerCycles");
		this.unit2PerCycles = properties.getProperty("machineUnit2PerCycles");
		this.actualProductionCountId = properties.getProperty("actualProductionCountField");

		measuredEntities = new MeasuredEntityContainer(driver, server, user, password);
		measuredEntities.loadContainer();
		
		for (Integer i : measuredEntities.getKeys()) {
			MeasuredEntity m = (MeasuredEntity) measuredEntities.getObject(i);
			MeasuredEntityFacade f = new MeasuredEntityFacade(m, this.productionRateId, 
																this.unit1PerCycles, this.unit2PerCycles, 
																this.actualProductionCountId);
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
		List<Event> scheduledEvents = new ArrayList<Event>();
		for (Integer i : measuredEntities.getKeys()) {
			MeasuredEntity m = (MeasuredEntity) measuredEntities.getObject(i);
			scheduledEvents.addAll(measuredEntities.getScheduledEvents(m));
		}
		
		logger.info("num scheduled events:" + scheduledEvents.size());

		// Put to execute all scheduled events.
		int numEvent = 0;
		for (Event evt : scheduledEvents){
			long seconds = ((AggregationEvent) evt).getSecondsToNextExecution();
			
			logger.info("Next Recurrence to occur in: " + seconds + " seconds");
			
			DelayEvent dEvent = new DelayEvent(evt,seconds*1000);
			
			try {
				
				EventManager.getInstance().getDelayedQueue().put(dEvent);
				
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			
			numEvent = numEvent + 1;
		}
		
		logger.info("Number of scheduled events that have been read:" + numEvent );
		
	}

	public synchronized static MeasuredEntityManager getInstance() throws SQLException{
		if(instance == null){
			instance = new MeasuredEntityManager();
		}
		return instance;
	}
	
	/**
	 * Returns TRUE if the entity parameter is already into the list of facades.
	 * @param entity
	 * @return TRUE if the entity already exist into the list, FALSE otherwise.
	 */
	private synchronized boolean entityAlreadyExists(final MeasuredEntity entity){	
		for (MeasuredEntityFacade facade : entities) {
			if(facade.getEntity().getId().equals(entity)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Inserts a new entity in the list and creates its facade.
	 * @param entity The new measured entity.
	 * @return
	 */
	public synchronized boolean addNewEntity(final MeasuredEntity entity){
		if(entityAlreadyExists(entity)){
			return false;
		}
		return entities.add(new MeasuredEntityFacade(entity, this.productionRateId, 
							this.unit1PerCycles, this.unit2PerCycles, 
							this.actualProductionCountId));
	}
	
	/**
	 * Returns the Entity facade of a given entity id.
	 * @param entityId The entity id to search.
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

	public MeasuredEntityContainer getMeasuredEntityContainer()
	{
		return this.measuredEntities;
	}

	
	public synchronized Integer getMeasuredEntityId(String company, String location,
			String plant, String machineGroup, String machineId) {
		
		return this.measuredEntities.getCanonicalObject(company, location, plant, machineGroup, machineId);
	}
	
	public String getCanonicalById(Integer id){
		if (this.measuredEntities.getObject(id) == null) {
			return null;
		}
		else {
			return ((MeasuredEntity) this.measuredEntities.getObject(id)).getCanonicalIdentifier();
		}
	}
	
	public synchronized boolean removeMeasuredEntity(Integer entityId)
	{

		logger.debug("getFacadeOfEntityById" + Integer.toString(entityId) );
	
		// Removes the events related with this measured entity
		EventManager.getInstance().removeMeasuredEntityEvents(entityId);
		
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
					
					// Stores pending data in caches.
					facade.storeAllMeasuredAttributeValues();
					facade.storeAllStateIntervals();
					
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
	
}
