package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class MeasureAttributeValueStore {

	private static MeasureAttributeValueStore instance = null;
	Connection conn  = null; 
	PreparedStatement pst = null;

	private static Cache<String, AttributeValue> cache;
	PreparedStatement preparedStatement;

	private MeasureAttributeValueStore(){
	}
	
	
	public static void setCache(int initialCapacity, int maxSize){
		cache = Caffeine.newBuilder()
				.initialCapacity(initialCapacity)
				.maximumSize(maxSize)
				.recordStats()
				.build();
	}

	public static MeasureAttributeValueStore getInstance(){
		if(instance == null){
			instance = new MeasureAttributeValueStore();
			setCache(1000,1000);// default values
		}
		return instance;
	}

	public Cache<String, AttributeValue> getCache(){
		return cache; 
	}



	public void cacheStore(AttributeValue mav){
		cache.put(mav.getKey(), mav);
	}

	/**
	 * Given a key, it returns an element from the cache if it is there.
	 * If the value is not in the cache, this method returns NULL.
	 * @param key 
	 * @return
	 */
	public AttributeValue getFromCache(String key){
		AttributeValue fromCache = cache.getIfPresent(key);
		if(fromCache != null)
			return fromCache;
		return null;
	}

	
	public void commit(AttributeValue value){
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/iotajover", "iotajover", "iotajover");
			conn.setAutoCommit(false);
			pst = conn.prepareStatement(value.getPreparedInsertText());
			value.dbInsert(pst);
			pst.executeBatch();
			conn.commit();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			if(pst!=null)
			{
				try
				{
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if(conn!=null) 
			{
				try
				{
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
