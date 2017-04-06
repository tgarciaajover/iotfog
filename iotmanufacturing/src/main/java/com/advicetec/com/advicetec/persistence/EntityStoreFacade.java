package com.advicetec.persistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import com.advicetec.FogClasses.MeasuredAttributeValue;

public class EntityStoreFacade {

	private Store store;
	
	/**
	 * Map with key DATETIME and value String Primary Key for the database.
	 */
	private SortedMap<LocalDateTime,String> map;
	
	
	public EntityStoreFacade(){
		store = Store.getInstance();
		map = new TreeMap<LocalDateTime, String>();
	}
	
	public int size(){
		return map.size();
	}
	
	public void store(MeasuredAttributeValue mav){
		store.cacheStore(mav);
		map.put(mav.getTimeStamp(), mav.getKey());
	}
	
	public LocalDateTime getOldest(){
		return map.firstKey();
	}
	
	public ArrayList<MeasuredAttributeValue> getByInterval(LocalDateTime from, LocalDateTime to){
		
		String[] keyArray = (String[]) map.subMap(from, to).values().toArray();
		return getFromCache(keyArray);
	}
	
	
	public ArrayList<MeasuredAttributeValue> getLast(int n){
		ArrayList<MeasuredAttributeValue> maValues = new ArrayList<MeasuredAttributeValue>();
		String[] keyArray = (String[]) map.values().toArray();
		if(n>= map.size()){
			return getFromCache(keyArray);
		}
		else{
			for (int i = keyArray.length - n - 1; i < keyArray.length; i++) {
				maValues.add(store.getFromCache(keyArray[i]));
			}
			return maValues;
		}
	}
	
	private ArrayList<MeasuredAttributeValue> getFromCache(String[] keyArray){
		ArrayList<MeasuredAttributeValue> maValues = new ArrayList<MeasuredAttributeValue>();
		for (String key : keyArray) {
			maValues.add(store.getFromCache(key));
		}
		return maValues;
	}
}
