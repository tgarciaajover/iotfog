package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;

import com.advicetec.core.Configurable;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.StateInterval;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;


/**
 * Represents a cache for storing State Interval Objects.<br>
 * 
 * @author user
 *
 */ 
public class StateIntervalCache extends Configurable {
	
	private static String DB_URL = null;
	private static String DB_USER = null;
	private static String DB_PASS = null;
	private static String DB_DRIVER = null;
	private static Integer INIT_CAPACITY = 0;
	private static Integer MAX_SIZE = 0;
	private static Integer WRITE_TIME = 0;
	private static Integer DELETE_TIME = 0;

	private static StateIntervalCache instance = null;
	private static Connection conn  = null; 
	private static PreparedStatement pst = null;

	private static Cache<String, StateInterval> cache;
	PreparedStatement preparedStatement;

	private StateIntervalCache()
	{ 
		super("StateIntervalCache");
		
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
		
		/*cache = Caffeine.newBuilder()
				.initialCapacity(initialCapacity)
				.maximumSize(maxSize)
				.recordStats()
				.build(); */

	     cache = Caffeine.newBuilder()
		            .expireAfterWrite(DELETE_TIME, TimeUnit.SECONDS)
		            .initialCapacity(initialCapacity)
		            .maximumSize(maxSize)
		        	.writer(new WriteBehindCacheWriter.Builder<String, StateInterval>()
		                .bufferTime(WRITE_TIME, TimeUnit.SECONDS)
		                .coalesce(BinaryOperator.maxBy(StateInterval::compareTo))
		                .writeAction(entries -> {

		                	if (entries.size() > 0) {
			                	System.out.println("Entre en writeaction:" + entries.size());
			        			try {
									Class.forName(DB_DRIVER);
				        			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
				        			conn.setAutoCommit(false);
				        			pst = conn.prepareStatement(StateInterval.SQL_Insert);
		
				                	entries.forEach((k,v)-> {
				                		((StateInterval)v).dbInsert(pst);

				                	});
		
				                	int ret[] = pst.executeBatch();
				                	System.out.println("Number of commands executed:" + ret.length);
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
		                	}
		                }).build())
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
	 * Stores a single State Interval into the database.
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
	
	/**
	 * 
	 * @param keys
	 */
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
			// here the 
			
		} catch (ClassNotFoundException | SQLException e) {
			System.err.println("Error: "+ e.getMessage());
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


	public LocalDateTime getOldestTime() {
		return LocalDateTime.now().minusSeconds(WRITE_TIME+DELETE_TIME);
	}


	public ArrayList<StateInterval> getFromDatabase(Integer id,
			MeasuredEntityType type, LocalDateTime from, LocalDateTime oldest) {
		// TODO Auto-generated method stub
		return null;
	}
}
