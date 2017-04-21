package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.advicetec.measuredentitity.StateInterval;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class StateIntervalCache {
	
	private final static String DB_URL = "jdbc:postgresql://localhost:5432/iotajover";
	private final static String DB_USER = "iotajover";
	private final static String DB_PASS = "iotajover";
	private final static String DB_DRIVER = "org.postgresql.Driver";

	private static StateIntervalCache instance = null;
	Connection conn  = null; 
	PreparedStatement pst = null;

	private static Cache<String, StateInterval> cache;
	PreparedStatement preparedStatement;

	private StateIntervalCache(){
	}
	
	
	public static void setCache(int initialCapacity, int maxSize){
		cache = Caffeine.newBuilder()
				.initialCapacity(initialCapacity)
				.maximumSize(maxSize)
				.recordStats()
				.build();
	}

	public static StateIntervalCache getInstance(){
		if(instance == null){
			instance = new StateIntervalCache();
			setCache(1000,1000);// default values
		}
		return instance;
	}

	public Cache<String, StateInterval> getCache(){
		return cache; 
	}

	/**
	 * Stores an state interval and its measuring entity name. 
	 * @param interval
	 */
	public void storeToCache(StateInterval interval){
		// the key is composed of entity name + start time + end time
		cache.put(interval.getKey(), interval);
	}

	/**
	 * Given a key, it returns an element from the cache if it is there.
	 * If the value is not in the cache, this method returns NULL.
	 * @param key 
	 * @return
	 */
	public StateInterval getFromCache(String key){
		return cache.getIfPresent(key);
	}

	/**
	 * Stores the interval into the database.
	 * @param interval
	 */
	public void commit(StateInterval interval){
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			conn.setAutoCommit(false);
			
			pst = conn.prepareStatement(interval.getPreparedInsertText());
			interval.dbInsert(pst);
			
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
	
	public void bulkCommit(List<String> keys){
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			conn.setAutoCommit(false);
			pst = conn.prepareStatement(StateInterval.SQL_Insert);
			Map<String,StateInterval> subSet = cache.getAllPresent(keys);
			for (StateInterval interval :subSet.values()) {
				interval.dbInsert(pst);
				pst.addBatch();
			}
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
