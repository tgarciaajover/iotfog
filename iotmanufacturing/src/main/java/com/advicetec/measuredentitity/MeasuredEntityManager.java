package com.advicetec.measuredentitity;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.advicetec.configuration.MonitoringDeviceContainer;
import com.advicetec.configuration.SignalUnitContainer;
import com.advicetec.core.Configurable;
import com.advicetec.eventprocessor.EventManager;
import com.advicetec.eventprocessor.ModBusTcpEvent;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.mpmcqueue.QueueType;
import com.advicetec.mpmcqueue.Queueable;
import com.advicetec.persistence.MeasureAttributeValueCache;

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
	private int modBusPort;
	
	private MeasuredEntityManager() throws SQLException{
		
		super("MeasuredEntity");
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
		this.modBusPort = Integer.valueOf(properties.getProperty("ModBusPort"));

		measuredEntities = new MeasuredEntityContainer(driver, server, user, password);
		measuredEntities.loadContainer();
		
		for (Integer i : measuredEntities.getKeys()) {
			MeasuredEntity m = (MeasuredEntity) measuredEntities.getObject(i);
			MeasuredEntityFacade f = new MeasuredEntityFacade(m);
			entities.add(f);
		}
		
		logger.info("Num facades that have been read:" + Integer.toString(this.entities.size()) );
		
		List<ModBusTcpEvent> events = measuredEntities.getModBusEvents( modBusPort );
		
		for (ModBusTcpEvent evt : events){
			Queueable obj = new Queueable(QueueType.EVENT, evt);
			try {
				
				EventManager.getInstance().getQueue().enqueue(6, obj);
				
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		
	}

	public static MeasuredEntityManager getInstance() throws SQLException{
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
	private boolean entityAlreadyExists(final MeasuredEntity entity){	
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
	public boolean addNewEntity(final MeasuredEntity entity){
		if(entityAlreadyExists(entity)){
			return false;
		}
		return entities.add(new MeasuredEntityFacade(entity));
	}
	
	/**
	 * Returns the Entity facade of a given entity id.
	 * @param entityId The entity id to search.
	 * @return NULL if there is not an entity with the given id.
	 */
	public MeasuredEntityFacade getFacadeOfEntityById(final Integer entityId){	
		
		logger.debug("getFacadeOfEntityById" + Integer.toString(entityId) );
		
		for (MeasuredEntityFacade facade : entities) {
			
			if (facade.getEntity() == null)
				logger.error("entity registered is null");
			else{ 
				logger.debug("facade:" + Integer.toString(facade.getEntity().getId()));
				
				if(facade.getEntity().getId() == entityId){
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
	
}
