package com.advicetec.measuredentitity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
	private MeasuredEntityContainer measuredEntities;
	
	private List<MeasuredEntityFacade> entities;
	
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

		measuredEntities = new MeasuredEntityContainer(driver, server, user, password);
		measuredEntities.loadContainer();
		
		for (Integer i : measuredEntities.getKeys()) {
			MeasuredEntity m = (MeasuredEntity) measuredEntities.getObject(i);
			MeasuredEntityFacade f = new MeasuredEntityFacade(m);
			entities.add(f);
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
		for (MeasuredEntityFacade facade : entities) {
			if (facade.getEntity() == null)
				System.out.println("error - entity is null");
			else{ 
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

	
	public Integer getMeasuredEntityId(String company, String location,
			String plant, String machineId) {
		
		// TODO Marenetes hacer la consulta del modelo canonico y retornar el ID de la máquina
		return null;
	}
	
	public String getCannonicalById(Integer id){
		// TODO Marentes retornal el id canonico dado el measure entity id
		return null;
	}
	
}
