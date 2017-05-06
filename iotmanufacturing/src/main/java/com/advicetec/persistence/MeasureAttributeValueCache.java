package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;

import com.advicetec.core.AttributeValue;
import com.advicetec.core.Configurable;
import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
/**
 * This is the cache for Measured Attribute Values. <br>
 * It is a singleton object.
 * @author user
 *
 */
public class MeasureAttributeValueCache extends Configurable {
	
	private static String DB_URL = null;
	private static String DB_USER = null;
	private static String DB_PASS = null;
	private static String DB_DRIVER = null;
	private static Integer INIT_CAPACITY = 0;
	private static Integer MAX_SIZE = 0;
	private static Integer WRITE_TIME = 0;
	private static Integer DELETE_TIME = 0;
	
	private static MeasureAttributeValueCache instance = null;
	private static Connection conn  = null; 
	private static PreparedStatement pst = null;
	

	private static Cache<String, AttributeValue> cache;
	PreparedStatement preparedStatement;

	private MeasureAttributeValueCache()
	{
		super("MeasureAttributeValueCache");
		
		DB_DRIVER = properties.getProperty("driver");
		DB_URL = properties.getProperty("server");
		DB_USER = properties.getProperty("user");
		DB_PASS = properties.getProperty("password");
		INIT_CAPACITY = Integer.valueOf(properties.getProperty("init_capacity"));
		MAX_SIZE = Integer.valueOf(properties.getProperty("max_size"));
		WRITE_TIME = Integer.valueOf(properties.getProperty("write_time"));
		DELETE_TIME = Integer.valueOf(properties.getProperty("delete_time"));

	}
		
	public static void setCache(int initialCapacity, int maxSize){
		
		// Write behind implementation.
		//cache = Caffeine.newBuilder()
		//		.initialCapacity(initialCapacity)
		//		.maximumSize(maxSize)
		//		.recordStats()
		//		.build();
		
	     cache = Caffeine.newBuilder()
	            .expireAfterWrite(DELETE_TIME, TimeUnit.SECONDS)
	            .initialCapacity(initialCapacity)
	            .maximumSize(maxSize)
	        	.writer(new WriteBehindCacheWriter.Builder<String, AttributeValue>()
	                .bufferTime(WRITE_TIME, TimeUnit.SECONDS)
	                // .coalesce(BinaryOperator.maxBy(AttributeValue::compareTo))
	                .writeAction(entries -> {

	                	
	        			try {
							Class.forName(DB_DRIVER);
		        			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
		        			conn.setAutoCommit(false);
		        			pst = conn.prepareStatement(MeasuredAttributeValue.SQL_Insert);

		                	entries.forEach((k,v)-> {
		                		((MeasuredAttributeValue)v).dbInsert(pst);
									try {
										pst.addBatch();
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
			                	});

		                	pst.executeBatch();
		        			conn.commit();

	        			} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
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

	                }).build())
	            .build();
	}

	public static MeasureAttributeValueCache getInstance(){
		if(instance == null){
			instance = new MeasureAttributeValueCache();
			setCache(INIT_CAPACITY,MAX_SIZE);// default values
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
			int[] count =pst.executeBatch();
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
