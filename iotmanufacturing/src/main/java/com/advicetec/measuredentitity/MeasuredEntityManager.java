package com.advicetec.measuredentitity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.advicetec.core.Configurable;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.persistence.MeasureAttributeValueCache;

/**
 * This class manages the list of entities.
 * 
 * @author user
 *
 */
public class MeasuredEntityManager extends Configurable {
	
	private static MeasuredEntityManager instance=null;
	
	private List<MeasuredEntityFacade> entities;
	
	private MeasuredEntityManager(){
		super("MeasuredEntity");
		entities = new ArrayList<MeasuredEntityFacade>();
		
		String[] machines = properties.getProperty("machines").split(",");
		String initCapacity = properties.getProperty("cache_initialCapacity");
		String maxSize = properties.getProperty("cache_maxSize");
		
		// creates an instance if it is not exists
		MeasureAttributeValueCache.getInstance();
		// sets cache parameters
		MeasureAttributeValueCache.setCache(
				Integer.parseInt(initCapacity), Integer.parseInt(maxSize));

		for (int i = 0; i < machines.length; i++) {
			Machine m = new Machine(machines[i]);
			MeasuredEntityFacade f = new MeasuredEntityFacade(m);
			entities.add(f);
		}
	}

	public static MeasuredEntityManager getInstance(){
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
	public MeasuredEntityFacade getFacadeOfEntityById(final String entityId){	
		for (MeasuredEntityFacade facade : entities) {
			if(facade.getEntity().getId().equals(entityId)){
				return facade;
			}
		}
		return null;
	}

	public MeasuredEntity fromJSON(String json) {

		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		MeasuredEntity measuredEntity;
		try {
		
			measuredEntity = mapper.readValue(json, MeasuredEntity.class);
			return measuredEntity;
		
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}	

	
}
