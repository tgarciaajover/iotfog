package com.advicetec.measuredentitity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.advicetec.core.Attribute;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.Symbol;
import com.advicetec.core.AttributeValue;
import com.advicetec.persistence.MeasureAttributeValueStore;
import com.advicetec.persistence.StatusStore;


/**
 * This class is a MeasuredEntity facade.
 * It allows the language processor to access some functionality
 * from the MeasuredEntity without expose all its methods.
 *   
 * @author maldofer
 *
 */
public class MeasuredEntityFacade {

	private MeasureAttributeValueStore store;
	private MeasuredEntity entity;
	private StatusStore status;

	/**
	 * Map with key DATETIME and value String Primary Key for the database.
	 */
	private Map<String,SortedMap<LocalDateTime,String>> attMap;


	public MeasuredEntityFacade(MeasuredEntity entity) {
		this.entity = entity;
		status = new StatusStore();
	}

	public MeasuredEntity getEntity() {
		return entity;
	}

	public void setEntity(MeasuredEntity entity) {
		this.entity = entity;
	}

	public MeasuredEntityType getType(){
		return entity.getType();
	}

	/**
	 * Returns TRUE if the attrib
	 * @param attr
	 * @return
	 */
	public boolean existsAttribute(AttributeMeasuredEntity attr){
		return entity.getAttributeList().contains(attr);
	}

	public boolean registerMeasureEntityAttibute(AttributeMeasuredEntity newAttribute){
		return entity.registerMeasureEntityAttibute(newAttribute);
	}
	
	public void setAttribute(Attribute attribute) throws Exception{
		// returns the previous value
		status.setAttribute(attribute);

	}

	/**
	 * Sets or updates the attribute value into the status and store.
	 * @param attrValue
	 */
	public void setAttributeValue(AttributeValue attrValue){
		store(new MeasuredAttributeValue(attrValue.getAttribute(), attrValue.getValue(),
				attrValue.getParent(), attrValue.getParentType(), LocalDateTime.now()));
		status.setAttributeValue(attrValue);
	}

	/**
	 * Stores a new value for the attribute.
	 * @param attribute
	 * @param value
	 * @param timeStamp
	 * @throws Exception
	 */
	public void registerAttributeValue(Attribute attribute, Object value, LocalDateTime timeStamp) throws Exception{
		MeasuredAttributeValue measure = entity.getMeasureAttributeValue(attribute, value, timeStamp);
		MeasureAttributeValueStore.getInstance().cacheStore(measure);
		
		// update status
		status.setAttribute(attribute);
		
	}

	
	public MeasuredEntityFacade(){
		store = MeasureAttributeValueStore.getInstance();
		attMap = new HashMap<String, SortedMap<LocalDateTime,String>>();
		status = new StatusStore();
	}

	/**
	 * Returns the amount of attributes stored in the cache.
	 * @return 
	 */
	public int size(){
		return attMap.size();
	}

	
	/**
	 * Stores a Measured Attributed Value into the cache.
	 * @param mav the value to be stored.
	 */
	public void store(MeasuredAttributeValue mav){
		store.cacheStore(mav);
		//map.put(mav.getTimeStamp(), mav.getKey());
		String attName = mav.getAttribute().getName();
		SortedMap<LocalDateTime, String> internalMap = attMap.get(attName);
		if(internalMap == null){
			attMap.put(attName,new TreeMap<LocalDateTime, String>());
			internalMap = attMap.get(attName);
		}
		internalMap.put(mav.getTimeStamp(), mav.getKey());
	}

	
	/**
	 * Returns the oldest Measured Attribute Value for a given Attribute name.
	 * @param attName
	 * @return
	 */
	public AttributeValue getOldestByAttributeName(String attName){
		SortedMap<LocalDateTime, String> internalMap = attMap.get(attName);
		if(internalMap == null){
			return null;
		}
		return store.getFromCache(internalMap.get(internalMap.firstKey()));
	}

	/**
	 * Returns the newest Measured Attribute Value for a given Attribute name.
	 * @param attName
	 * @return
	 */
	public AttributeValue getNewestByAttributeName(String attName){
		SortedMap<LocalDateTime, String> internalMap = attMap.get(attName);
		if(internalMap == null){
			return null;
		}
		return store.getFromCache(internalMap.get(internalMap.lastKey()));
	}


	/**
	 * Returns a list of attribute values for a given interval.
	 * 
	 * @param attrName Attribute name.
	 * @param from Time from.
	 * @param to Time to.
	 * @return
	 */
	public ArrayList<AttributeValue> getByIntervalByAttributeName(
			String attrName, LocalDateTime from, LocalDateTime to){

		if(!attMap.containsKey(attrName)){
			return null;
		}
		SortedMap<LocalDateTime,String> internalMap = attMap.get(attrName);
		String[] keyArray = (String[]) internalMap.subMap(from, to).values().toArray();
		return getFromCache(keyArray);
	}

	
	/**
	 * Returns a list with the last N attribute values for a given
	 * attribute name.
	 * 
	 * @param attrName The name of the attribute.
	 * @param n The number of values for the given attribute.
	 * @return The list of Measured Attribute Values. NULL if there are not 
	 * values for the given attribute name.
	 * 
	 */
	public List<AttributeValue> getLastNbyAttributeName(String attrName, int n){
		ArrayList<AttributeValue> maValues = new ArrayList<AttributeValue>();
		
		if(!attMap.containsKey(attrName)){
			return null;
		}
		
		SortedMap<LocalDateTime,String> internalMap = attMap.get(attrName);
		String[] keyArray = (String[]) internalMap.values().toArray();
		if(n>= internalMap.size()){
			return getFromCache(keyArray);
		}
		else{
			for (int i = keyArray.length - n - 1; i < keyArray.length; i++) {
				maValues.add(store.getFromCache(keyArray[i]));
			}
			return maValues;
		}
	}


	/**
	 * Returns a list of Measured Attribute Values, from the cache,
	 * @param keyArray
	 * @return
	 */
	private ArrayList<AttributeValue> getFromCache(String[] keyArray){
		ArrayList<AttributeValue> maValues = new ArrayList<AttributeValue>();
		for (String key : keyArray) {
			
			maValues.add(store.getFromCache(key));
		}
		return maValues;
	}

	public void importSymbols(Map<String, Symbol> symbolMap) {
		status.importSymbols(symbolMap);
	}
	
	public Collection<Attribute> getStatus(){
		return status.getStatus();
	}

	public void importAttributeValues(Map<String, ASTNode> valueMap) {
		status.importAttributeValues(valueMap,entity.getId(),entity.getType());
	}
	
	public Collection<AttributeValue> getAttributeValues(){
		return status.getAttributeValues();
	}
	
}

