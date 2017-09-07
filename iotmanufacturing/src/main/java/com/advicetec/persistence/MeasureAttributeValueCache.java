package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.SystemConstants;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.Configurable;
import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * This class is the cache for Measured Attribute Values. <br>
 * It is a singleton object.
 * @author user
 *
 */
public class MeasureAttributeValueCache extends Configurable {

	static Logger logger = LogManager.getLogger(MeasureAttributeValueCache.class.getName());

	/**
	 * Database url
	 */
	private static String DB_URL = null;
	/**
	 * Database user
	 */
	private static String DB_USER = null;
	/**
	 * Database password
	 */
	private static String DB_PASS = null;
	/**
	 * Database driver
	 */
	private static String DB_DRIVER = null;
	/**
	 * Initial available space for entries
	 */
	private static Integer INIT_CAPACITY = 1000;
	/**
	 * Cache maximun size
	 */
	private static Integer MAX_SIZE = 10000;
	/**
	 * Time the cache keeps entries before write over database.
	 */
	private static Integer WRITE_TIME = 10;
	/**
	 * Time the cache keeps entries before delete them.
	 */
	private static Integer DELETE_TIME = 60;
	/**
	 * Singleton cache instance
	 */
	private static MeasureAttributeValueCache instance = null;
	/**
	 * Database connection object
	 */
	private static Connection conn  = null;
	/**
	 * Database prepared statement object
	 */
	private static PreparedStatement pst = null;

	/**
	 * SQL to select a set of AttributeValue given owner id and type, attribute
	 * value name, and time range. 
	 *  
	 */
	final private static String sqlMeasureAttributeValueRangeSelect = "select timestamp, value_decimal, value_datetime, value_string, value_int, value_boolean, value_date, value_time from measuredattributevalue where id_owner = ? and owner_type = ? and attribute_name = ? and timestamp >= ? and timestamp <= ?";  

	/**
	 * Column name from the query
	 */
	final private static String timestamp = "timestamp";
	
	final private static String timezone = "UTC";
	/**
	 * A cache is a map with a key and the attribute value. 
	 */
	private static Cache<String, AttributeValue> cache;

	/**
	 * Constructs this cache with the parameters from .properties file.
	 * This method also defines the shutdown hook if the process is canceled 
	 * and writes all data from the cache to the database.
	 */
	private MeasureAttributeValueCache()
	{
		super("MeasureAttributeValueCache");
		// loads values from MeasureAttributeValueCache.properties
		DB_DRIVER = properties.getProperty("driver");
		DB_URL = properties.getProperty("server");
		DB_USER = properties.getProperty("user");
		DB_PASS = properties.getProperty("password");
		INIT_CAPACITY = Integer.valueOf(properties.getProperty("init_capacity"));
		MAX_SIZE = Integer.valueOf(properties.getProperty("max_size"));
		// time to store at database
		WRITE_TIME = Integer.valueOf(properties.getProperty("write_time"));
		// time to delete an entry from the cache once it is store.
		DELETE_TIME = Integer.valueOf(properties.getProperty("delete_time"));
		logger.info("Write time:" + WRITE_TIME + "Delete Time:" + DELETE_TIME);

		// This part inserts any pending data in the cache to the database, in case of shutdown.
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				List<String> keys = new ArrayList<>();
				keys.addAll(cache.asMap().keySet());
				bulkCommit(keys);
			}
		});  

	}

	/**
	 * Sets cache initial capacity and limit size.
	 * @param initialCapacity minimum total entries of internal hash tables
	 * @param maxSize the maximum number of entries in the cache
	 * @see Caffeine#build()
	 */
	public static void setCache(int initialCapacity, int maxSize){
		// cache is implemented by Caffeine
		cache = Caffeine.newBuilder()
				// time to delete an entry from cache
				.expireAfterWrite(DELETE_TIME, TimeUnit.SECONDS)
				// initial cache size
				.initialCapacity(initialCapacity)
				// max size
				.maximumSize(maxSize)
				.writer(new WriteBehindCacheWriter.Builder<String, AttributeValue>()
						// time before execute WriteAction over database.
						.bufferTime(WRITE_TIME, TimeUnit.SECONDS)
						// action to decide which value get in multiple entries
						.coalesce(BinaryOperator.maxBy(AttributeValue::compareTo))
						// WriteAction
						.writeAction(entries -> {
							if (entries.size() > 0) {
								try {
									// connect to database
									Class.forName(DB_DRIVER);
									conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
									conn.setAutoCommit(false);
									pst = conn.prepareStatement(MeasuredAttributeValue.SQL_Insert);
									// prepares the statement
									entries.forEach((k,v)-> {
										logger.debug( "db write key:" + ((MeasuredAttributeValue)v).getKey() );
										((MeasuredAttributeValue)v).dbInsert(pst);

									});
									// execute the insertion
									int ret[] = pst.executeBatch();
									logger.debug("Number of Attribute Values inserted:" + ret.length);
									conn.commit();
								} catch (ClassNotFoundException e) {
									logger.error(e.getMessage());
									e.printStackTrace();
								} catch (SQLException e) {
									logger.error(e.getMessage());
									e.printStackTrace();
								}

								finally{
									if(pst!=null){
										try{
											pst.close();
										} catch (SQLException e) {
											logger.error(e.getMessage());
											e.printStackTrace();
										}
									}

									if(conn!=null) {
										try {
											conn.close();
										} catch (SQLException e) {
											logger.error(e.getMessage());
											e.printStackTrace();
										}
									}
								} // finally
							} // if
						}).build()) // writeAction
						.build(); // writeBehindCache
	}

	/**
	 * Returns a singleton instance of MeasureAttributeValueCache.
	 * @return the singleton instance. 
	 */
	public static MeasureAttributeValueCache getInstance(){
		if(instance == null){
			instance = new MeasureAttributeValueCache();
			setCache(INIT_CAPACITY,MAX_SIZE);// default values
		}
		return instance;
	}

	/**
	 * Returns the cache map.
	 * @return the cache map.
	 */
	protected Cache<String, AttributeValue> getCache(){
		return cache; 
	}

	/**
	 * Stores an attribute value into cache.
	 * @param mav AttributeValue to store.
	 */
	public void cacheStore(AttributeValue mav){
		logger.debug("Storing:" + mav.getKey());
		cache.put(mav.getKey(), mav);
	}

	/**
	 * Given a key, it returns an element from the cache if it is there.
	 * If the value is not in the cache, this method returns NULL.
	 * @param key string that identifies an element in the map.
	 * @return the attributeValue stored into the cache, or 
	 * <code>NULL</code> if the element is not present.
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
			// database connection
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			conn.setAutoCommit(false);
			// sql statement
			pst = conn.prepareStatement(value.getPreparedInsertText());
			value.dbInsert(pst);
			// execution
			pst.executeBatch();
			conn.commit();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		finally{
			if(pst!=null) {
				try {
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
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Returns the time of the oldest entry in the cache.
	 * @return the time of the oldest entry in the cache.
	 */
	public LocalDateTime getOldestTime(){
		// creates a temporary map
		Map<String, AttributeValue> map = cache.policy().eviction().get().coldest(1);
		// This code at most returns one value.
		LocalDateTime ret = LocalDateTime.now();
		if (map.size() > 0){
			for (String key : map.keySet()){
				ret = ((MeasuredAttributeValue) map.get(key)).getTimeStamp();
				break;
			}
		}

		return ret;

	}

	/**
	 * Returns a list of Attribute values from the database. The query is 
	 * composed of the device/machine, the name of the attribute and the time
	 * range (from,to).
	 * 
	 * @param entityId measured entity id.
	 * @param mType describes the type of device 
	 * @param attribute attribute name.
	 * @param from initial time to query.
	 * @param to final time to query.
	 * @return a list of attribute values from the database. If the query does 
	 * not return data, this method returns an empty list.
	 * 
	 * @see MeasuredEntityType
	 */
	public synchronized ArrayList<AttributeValue> getFromDatabase(
			Integer entityId, MeasuredEntityType mType, Attribute attribute, 
			LocalDateTime from, LocalDateTime to) {

		Connection connDB  = null; 
		PreparedStatement pstDB = null;

		ArrayList<AttributeValue> list = new ArrayList<AttributeValue>();
		ResultSet rs = null;

		Calendar cal = Calendar.getInstance();
		TimeZone utcTimeZone = TimeZone.getTimeZone(SystemConstants.TIMEZONE);
		cal.setTimeZone(utcTimeZone);

		try {
			// get the database driver
			Class.forName(DB_DRIVER);
			connDB = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			connDB.setAutoCommit(false);
			// prepare the statement
			pstDB = connDB.prepareStatement(MeasureAttributeValueCache.sqlMeasureAttributeValueRangeSelect);
			pstDB.setString(1, String.valueOf(entityId));
			pstDB.setInt(2, mType.getValue());
			pstDB.setString(3, attribute.getName());
			pstDB.setTimestamp(4, Timestamp.valueOf(from));
			pstDB.setTimestamp(5, Timestamp.valueOf(to));
			rs =  pstDB.executeQuery();

			// brings the attribute data
			while (rs.next())
			{
				Timestamp dtstime = rs.getTimestamp(timestamp, cal);
				long timestampTime = dtstime.getTime();
				cal.setTimeInMillis(timestampTime);
				LocalDateTime dTime = LocalDateTime.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 
						cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
						cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), 
						cal.get(Calendar.MILLISECOND));
				MeasuredAttributeValue mav = new MeasuredAttributeValue(attribute,new Object(), entityId, mType, dTime); 
				mav.setValueFromDatabase(rs);
				list.add(mav);
			}

		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		finally{
			if(pstDB!=null){
				try{
					pstDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}

			if(connDB!=null){
				try	{
					connDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}

		return list;
	}

	/**
	 * Turns over the database all the MeasuredAttribute values remain into cache.
	 * 
	 * @param keys List of keys to write into database.
	 */
	public void bulkCommit(List<String> keys) {
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			conn.setAutoCommit(false);
			// prepare statement
			pst = conn.prepareStatement(MeasuredAttributeValue.SQL_Insert);
			// Get the keys to insert in the database.
			Map<String,AttributeValue> subSet = cache.getAllPresent(keys);
			for (AttributeValue value :	subSet.values()) {
				if(value instanceof MeasuredAttributeValue){
					MeasuredAttributeValue mav = (MeasuredAttributeValue) value;
					mav.dbInsert(pst);
				}
			}
			// execute the query
			pst.executeBatch();
			conn.commit();
			// Remove all keys inserted in the database.
			cache.invalidateAll(keys);

		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		finally {
			if(pst!=null) {
				try {
					pst.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}

			if(conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

}
