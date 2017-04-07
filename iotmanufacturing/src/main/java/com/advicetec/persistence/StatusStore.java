package com.advicetec.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.Symbol;
import com.advicetec.measuredentitity.MeasuredAttributeValue;

/**
 * This class implements 
 * @author user
 *
 */
public class StatusStore {
	
	private static StatusStore instance = null;
	private static HashMap<String, HashMap<String, MeasuredAttributeValue>> store; 

	private StatusStore(){	}
	
	public static StatusStore getInstance(){
		if(instance == null){
			instance = new StatusStore();
			store = new HashMap<String, HashMap<String, MeasuredAttributeValue>>();
		}
		return instance;
	}
	
	/**
	 * Stores the Measured Attribute into the status store.
	 * 
	 * @param entityName Name or id for the measured entity.
	 * @param attrName Name or Id for the attribute.
	 * @param value Attribute Value.
	 * @return The previous value for that Attribute of null if there is not previous.
	 */
	public MeasuredAttributeValue setAttribute( String entityName, MeasuredAttributeValue value){
		if(!store.containsKey(entityName)){
			store.put(entityName, new HashMap<String, MeasuredAttributeValue>());
		}
		HashMap<String, MeasuredAttributeValue> internalMap = store.get(entityName);
		
		return internalMap.put(value.getAttribute().getName(), value);
	}
	
	
	/**
	 * Returns a collection of Measured Attribute Values
	 * @param entityName
	 * @return
	 */
	public Collection<MeasuredAttributeValue> getStatusByEntityName(String entityName){
		if(!store.containsKey(entityName)){
			return null;
		}
		return store.get(entityName).values();
	}
	
	
	public void setAttribute( String entityName, Collection<MeasuredAttributeValue> values){
		if(!store.containsKey(entityName)){
			store.put(entityName, new HashMap<String, MeasuredAttributeValue>());
		}
		HashMap<String, MeasuredAttributeValue> internalMap = store.get(entityName);
		
		for (Iterator it = values.iterator(); it.hasNext();) {
			MeasuredAttributeValue v = (MeasuredAttributeValue) it.next();	
			internalMap.put(v.getAttribute().getName(), v);
		}
	}

	/**
	 * Imports a symbol table from the interpreter to a Attributes
	 * @param measuringEntity
	 * @param map
	 */
	public void importSymbols(String measuringEntity, Map<String, Symbol> map) {
		
		
	}
}
