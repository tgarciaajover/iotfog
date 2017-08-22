package com.advicetec.applicationAdapter;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.advicetec.core.Configurable;
import com.advicetec.persistence.MeasureAttributeValueCache;

/**
 * This class manages the list of entities.
 * 
 * @author user
 *
 */
public class ProductionOrderManager extends Configurable {
	
	static Logger logger = LogManager.getLogger(ProductionOrderManager.class.getName());
	
	private static ProductionOrderManager instance=null;
	private ProductionOrderContainer productionOrders;
	
	private Map<Integer, ProductionOrderFacade> pOrders;
	
	private String productionRateId;
	private String unit1PerCycles;
	private String unit2PerCycles;
	private String actualProductionCountId;
	
	private ProductionOrderManager() throws SQLException{
		super("ProductionOrderManager");
		
		pOrders = new HashMap<Integer, ProductionOrderFacade>();
		
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
		this.productionRateId = properties.getProperty("productionRateField");
		this.unit1PerCycles = properties.getProperty("productionUnit1PerCycles");
		this.unit2PerCycles = properties.getProperty("productionUnit2PerCycles");
		this.actualProductionCountId = properties.getProperty("actualProductionCountField");

		productionOrders = new ProductionOrderContainer(driver, server, user, password);
				
	}

	public static ProductionOrderManager getInstance() throws SQLException{
		if(instance == null){
			instance = new ProductionOrderManager();
		}
		return instance;
	}
	
	/**
	 * Returns TRUE if the entity parameter is already into the list of facades.
	 * @param entity
	 * @return TRUE if the entity already exist into the list, FALSE otherwise.
	 */
	private boolean pOrderAlreadyExists(final ProductionOrder pOrder){
		if (pOrder != null) {
			return this.pOrders.containsKey(pOrder.getId());
		} else {
			return false;
		}
	}
	
	/**
	 * Inserts a new entity in the list and creates its facade.
	 * @param entity The new measured entity.
	 * @return
	 */
	public boolean addProductionOrder(ProductionOrder pOrder){
		if(pOrderAlreadyExists(pOrder)){
			return false;
		}
		
		if (pOrders.put(pOrder.getId(), 
				new ProductionOrderFacade(pOrder, productionRateId, unit1PerCycles, unit2PerCycles, actualProductionCountId)) != null)
			return true;
		else
			return false;
	}
	
	/**
	 * Returns the Production Order facade given the production order id.
	 * @param pOrderId The production order id to search.
	 * @return NULL if there is not a production order with the given id.
	 */
	public ProductionOrderFacade getFacadeOfPOrderById(final Integer pOrderId){
		return this.pOrders.get(pOrderId);
	}

	public ProductionOrderContainer getProductionOrderContainer()
	{
		return this.productionOrders;
	}
	
	public void removeFacade(Integer idProduccion)
	{
		
    	ProductionOrderFacade productionOrderFacade = this.pOrders.remove(idProduccion);
    	if (productionOrderFacade != null){
        	// Store all data associated with the production order.
        	productionOrderFacade.storeAllMeasuredAttributeValues();
        	productionOrderFacade.storeAllStateIntervals();    		
    	}
	}

	public Integer getProductionOrderId(String company, String location, String plant, String machineGroup,
			String machineId, int year, int month, String productionOrder) {
		return  this.productionOrders.getCanonicalObject(company, location, plant, machineGroup, machineId, year, month, productionOrder);
	}
}
