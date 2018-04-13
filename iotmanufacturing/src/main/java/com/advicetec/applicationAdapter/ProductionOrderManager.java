package com.advicetec.applicationAdapter;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.Configurable;
import com.advicetec.measuredentitity.ExecutedEntityFacade;
import com.advicetec.persistence.MeasureAttributeValueCache;

/**
 * Manager for the production order object.
 * 
 * The manager object is represented as singleton. It supports the rest of the SW, so other classes can deal with these kind of objects.
 * 
 * @author Andres Marentes
 *
 */
public class ProductionOrderManager extends Configurable {
	
	static Logger logger = LogManager.getLogger(ProductionOrderManager.class.getName());
	
	/**
	 * Singleton Instance 
	 */
	private static ProductionOrderManager instance=null;
	
	/**
	 * Reference to the Production Order container. Through this object we can select and update Production Orders.
	 */
	private ProductionOrderContainer productionOrders;
	
	/**
	 * Map with production order being used by the software, the Integer corresponds to the key given in the database. The facade let the system
	 * to expose some methods of the production order to other classes. 
	 */
	private Map<Integer, ExecutedEntityFacade> pOrders;
	
	/**
	 * Production rate field.
	 * Form this field we are going to take the production rate. 
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
	 * Production cycle or cycle count registered from the sensor. 
	 */
	private String actualProductionCountId;

	/**
	 * This field establishes how often we have to remove the cache entries (seconds). 
	 */
	private Integer purgeFacadeCacheMapEntries;

	/**
	 * Constructor for the class. The manager uses a properties file with name:  ProductionOrderManager.properties
	 * 
	 * The following are the properties expected to be defined in this file:
	 * 
	 * cache_initialCapacity : initial capacity for the cache 
	 * cache_maxSize		 : Max size of the cache.
	 * driver				 
	 * server
	 * user
	 * password
	 * productionRateField
	 * productionUnit1PerCycles
	 * productionUnit2PerCycles
	 * actualProductionCountField
	 * 
	 * @throws SQLException
	 */
	private ProductionOrderManager() throws SQLException{
		super("ProductionOrderManager");
		
		pOrders = new HashMap<Integer, ExecutedEntityFacade>();
		
		String driver = properties.getProperty("driver");
		String server = properties.getProperty("server");
		String user = properties.getProperty("user");
		String password = properties.getProperty("password");
		this.productionRateId = properties.getProperty("productionRateField");
		this.unit1PerCycles = properties.getProperty("productionUnit1PerCycles");
		this.unit2PerCycles = properties.getProperty("productionUnit2PerCycles");
		this.actualProductionCountId = properties.getProperty("actualProductionCountField");

		if (properties.getProperty("PurgeFacadeCacheMapEntries") != null) {
			try {
				this.purgeFacadeCacheMapEntries =Integer.parseInt(properties.getProperty("PurgeFacadeCacheMapEntries").trim());
			} catch(NumberFormatException ex){ // handle your exception
				logger.error(ex);
			}
		} else {
			this.purgeFacadeCacheMapEntries = new Integer(10); // By default 10 seconds.
		}

		productionOrders = new ProductionOrderContainer(driver, server, user, password);
				
	}

	/**
	 * @return ProductionOrderManager instance
	 * @throws SQLException - This exception is returned when we can not establish the connection to the database.
	 */
	public synchronized static ProductionOrderManager getInstance() throws SQLException{
		if(instance == null){
			instance = new ProductionOrderManager();
		}
		return instance;
	}
	
	/**
	 * Returns TRUE if the production order given as parameter is already into the list of facades.
	 * @param production order object to test.
	 * @return TRUE if the production order already exist into the list, FALSE otherwise.
	 */
	private synchronized boolean pOrderAlreadyExists(final ProductionOrder pOrder){
		if (pOrder != null) {
			return this.pOrders.containsKey(pOrder.getId());
		} else {
			return false;
		}
	}
	
	/**
	 * Inserts a new production order in the list and creates its facade.
	 * 
	 * @param entity The new production order to control.
	 * @return true if it could insert the production order, false otherwise.
	 * @throws PropertyVetoException 
	 */
	public synchronized boolean addProductionOrder(ProductionOrder pOrder) throws PropertyVetoException{
		if(pOrderAlreadyExists(pOrder)){
			return false;
		}
		
		if (pOrders.put(pOrder.getId(), 
				new ExecutedEntityFacade(pOrder, this.productionRateId, this.unit1PerCycles, 
											this.unit2PerCycles, this.actualProductionCountId, 
											  this.purgeFacadeCacheMapEntries)) != null)
			return true;
		else
			return false;
	}
	
	/**
	 * Returns the Production Order facade given the production order id.
	 * 
	 * @param pOrderId The production order id to search.
	 * @return NULL if there is not a production order with the given id.
	 */
	public synchronized ExecutedEntityFacade getFacadeOfPOrderById(final Integer pOrderId){
		return this.pOrders.get(pOrderId);
	}

	/**
	 * @return Return a reference to the production order container.
	 */
	public synchronized ProductionOrderContainer getProductionOrderContainer()
	{
		return this.productionOrders;
	}
	
	/**
	 * Removes a production order. This means that the production order is not anymore controlled.
	 * 
	 * observe that the cache is responsible of storing attribute values and states, so we don't have
	 * to do something special.     
	 * 
	 * @param idProduccion Id of the production order to remove.
	 */
	public synchronized void removeFacade(Integer idProduccion)
	{		
    	ExecutedEntityFacade productionOrderFacade = this.pOrders.remove(idProduccion);
	}

	/**
	 * Get the production order Id from the canonical information given.
	 * 
	 * @param company		: company to which the production order belongs to.
	 * @param location		: location to which the production order belongs to.
	 * @param plant			: plant to which the production order belongs to.
	 * @param machineGroup	: machine group to which the production order belongs to.
	 * @param machineId		: machine the production order is going to be executed.
	 * @param year			: year when the production order is going to be executed.
	 * @param month			: month when the production order is going to be executed.
	 * @param productionOrder	: canonical production order code.
	 * 
	 * @return Returns the identifier of a production order from its canonical data
	 */
	public synchronized Integer getProductionOrderId(String company, String location, String plant, String machineGroup,
														String machineId, int year, int month, String productionOrder) {
		
		return  this.productionOrders.getCanonicalObject(company, location, plant, machineGroup, machineId, year, month, productionOrder);
		
	}
}
