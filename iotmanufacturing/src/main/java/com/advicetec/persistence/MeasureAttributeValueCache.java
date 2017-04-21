package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.hamcrest.core.IsInstanceOf;

import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
/**
 * This is the cache for Measured Attribute Values. <br>
 * It is a singleton object.
 * @author user
 *
 */
public class MeasureAttributeValueCache {
	
	private final static String DB_URL = "jdbc:postgresql://localhost:5432/iotajover";
	private final static String DB_USER = "iotajover";
	private final static String DB_PASS = "iotajover";
	private final static String DB_DRIVER = "org.postgresql.Driver";
	
	private static MeasureAttributeValueCache instance = null;
	Connection conn  = null; 
	PreparedStatement pst = null;

	private static Cache<String, AttributeValue> cache;
	PreparedStatement preparedStatement;

	private MeasureAttributeValueCache(){
	}
	
	
	public static void setCache(int initialCapacity, int maxSize){
		cache = Caffeine.newBuilder()
				.initialCapacity(initialCapacity)
				.maximumSize(maxSize)
				.recordStats()
				.build();
	}

	public static MeasureAttributeValueCache getInstance(){
		if(instance == null){
			instance = new MeasureAttributeValueCache();
			setCache(1000,1000);// default values
		}
		return instance;
	}

	protected Cache<String, AttributeValue> getCache(){
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
		return cache.getIfPresent(key);
	}

	/**
	 * Stores the Measured Attribute Value into the database.
	 * @param value The value to be committed.
	 */
	public void commit(MeasuredAttributeValue value){
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
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


	public void bulkCommit(List<String> keys) {
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			conn.setAutoCommit(false);
			
			pst = conn.prepareStatement(MeasuredAttributeValue.SQL_Insert);
			Map<String,AttributeValue> subSet = cache.getAllPresent(keys);
			for (AttributeValue value :	subSet.values()) {
				if(value instanceof MeasuredAttributeValue){
					MeasuredAttributeValue mav = (MeasuredAttributeValue) value;
					mav.dbInsert(pst);
					pst.addBatch();
				}
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
