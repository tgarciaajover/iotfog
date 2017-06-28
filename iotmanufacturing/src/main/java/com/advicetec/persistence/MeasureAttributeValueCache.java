package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;

import com.advicetec.configuration.DeviceType;
import com.advicetec.configuration.IOSignalDeviceType;
import com.advicetec.configuration.Signal;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.Configurable;
import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.advicetec.measuredentitity.MeasuredEntityType;
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

	final private static String sqlMeasureAttributeValueRangeSelect = "select timestamp, value_decimal, value_datetime, value_string, value_int, value_boolean, value_date, value_time from measuredattributevalue where id_owner = ? and attribute_name = ? and timestamp >= ? and timestamp <= ?";  

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
		// time to store at database
		WRITE_TIME = Integer.valueOf(properties.getProperty("write_time"));
		// time to delete from the cache
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
	                .coalesce(BinaryOperator.maxBy(AttributeValue::compareTo))
	                .writeAction(entries -> {

	                	if (entries.size() > 0) {

		        			try {
								Class.forName(DB_DRIVER);
			        			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			        			conn.setAutoCommit(false);
			        			pst = conn.prepareStatement(MeasuredAttributeValue.SQL_Insert);
	
			                	entries.forEach((k,v)-> {
			                		((MeasuredAttributeValue)v).dbInsert(pst);

			                	});
	
			                	int ret[] = pst.executeBatch();
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

	public LocalDateTime getOldestTime(){
		return LocalDateTime.now().minusSeconds(WRITE_TIME + DELETE_TIME);
	}

	/**
	 * 
	 * @param entityId
	 * @param attrName
	 * @param from
	 * @param oldest
	 * @return
	 */
	public ArrayList<AttributeValue> getFromDatabase(Integer entityId, MeasuredEntityType mType,
			Attribute attribute, LocalDateTime from, LocalDateTime to) {
		
		
		ArrayList<AttributeValue> list = new ArrayList<AttributeValue>();
		
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			conn.setAutoCommit(false);
			pst = conn.prepareStatement(this.sqlMeasureAttributeValueRangeSelect);
			pst.setString(1, String.valueOf(entityId));
			pst.setString(2, attribute.getName());
			pst.setTimestamp(3, Timestamp.valueOf(from));
			pst.setTimestamp(4, Timestamp.valueOf(to));
			ResultSet rs =  pst.executeQuery();

			while (rs.next())
			{

				LocalDateTime dtime = rs.getTimestamp("timestamp").toLocalDateTime();
				MeasuredAttributeValue mav = new MeasuredAttributeValue(attribute,new Object(), entityId, mType, dtime); 
				mav.setValueFromDatabase(rs);
				list.add(mav);
						      
			}
			

			// Bring the attribute 
			
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
		
		return list;
	}
}
