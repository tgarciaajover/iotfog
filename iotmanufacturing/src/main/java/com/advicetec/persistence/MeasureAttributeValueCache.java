package com.advicetec.persistence;

import java.beans.PropertyVetoException;
import java.sql.Connection;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * This class is the cache for Measured Attribute Values.
 * It is a singleton object that implements a map to store attribute names and 
 * values. It also extends from Configurable to read the configuration file 
 * <code>properties</code>.
 * @author advicetec
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
	private static int INIT_CAPACITY = 1000;
	/**
	 * Cache maximun size 
	 */
	private static long MAX_SIZE = 10000L;
	/**
	 * Time the cache keeps entries before write over database.
	 */
	private static long WRITE_TIME = 10L;
	/**
	 * Time the cache keeps entries before delete them.
	 */
	private static long DELETE_TIME = 60L;	
	/**
	 * Number of threads used in order to perform inserts
	 */
	private static int INSERT_THREADS = 10;
	
	/**
	 * Number of rows inserted within each batch
	 */
	private static int BATCH_ROWS = 4000;
	
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
	 * A cache is a map with a key and the attribute value. 
	 */
	private static Cache<String, AttributeValue> cache;
	/**
	 * Thread pool to save data in the database.
	 */
	private static ExecutorService threadPool = null;
	/**
	 * SQL to select a set of AttributeValue given owner id and type, attribute
	 * value name, and time range.  
	 */	
	final private static String sqlMeasureAttributeValueRangeSelect = "select timestamp, value_decimal, value_datetime, value_string, value_int, value_boolean, value_date, value_time from measuredattributevalue where id_owner = ? and owner_type = ? and attribute_name = ? and timestamp >= ? and timestamp <= ?";  

	/**
	 * Column name from the query
	 */
	final private static String timestamp = "timestamp";
	
	private static int MIN_DB_THREAD_POOL = 5;
	
	private static int MAX_DB_THREAD_POOL = 30; 
	
	/**
	 * Connection pool for managing database connections.
	 */
	private static ComboPooledDataSource cpds = null;

	/**
	 * last object's date-time written to the database
	 */
	private static LocalDateTime lastDateTimeStore = null;
	
	/**
	 * Constructs this cache with the parameters from .properties file.
	 * This method also defines the shutdown hook if the process is canceled 
	 * and writes all data from the cache to the database.
	 * @throws PropertyVetoException 
	 */
	private MeasureAttributeValueCache()
	{
		super("MeasureAttributeValueCache");

		try {

		// loads values from MeasureAttributeValueCache.properties
		DB_DRIVER = properties.getProperty("driver");
		DB_URL = properties.getProperty("server");
		DB_USER = properties.getProperty("user");
		DB_PASS = properties.getProperty("password");
		
		// Cache data.
		if (properties.getProperty("init_capacity") != null)
			INIT_CAPACITY = Integer.parseInt(properties.getProperty("init_capacity"));
		
		if (properties.getProperty("max_size") != null)
			MAX_SIZE = Long.parseLong(properties.getProperty("max_size"));
		
		// time to store at database
		if (properties.getProperty("write_time") != null)
			WRITE_TIME = Long.parseLong(properties.getProperty("write_time"));
		// time to delete an entry from the cache once it is store.
		
		if (properties.getProperty("delete_time") != null)
			DELETE_TIME = Long.parseLong(properties.getProperty("delete_time"));
		
		// Thread related information to store data into the database.
		if (properties.getProperty("insert_threads") != null)
			INSERT_THREADS = Integer.parseInt(properties.getProperty("insert_threads"));
		
		if (properties.getProperty("batch_rows") != null)
			BATCH_ROWS = Integer.parseInt(properties.getProperty("batch_rows"));
		
		logger.info("Write time:" + WRITE_TIME + "Delete Time:" + DELETE_TIME);
		
		threadPool = Executors.newFixedThreadPool(INSERT_THREADS);

		if (properties.getProperty("min_db_thread_pool") != null)
			MIN_DB_THREAD_POOL = Integer.parseInt(properties.getProperty("min_db_thread_pool"));

		if (properties.getProperty("max_db_thread_pool") != null)
			MAX_DB_THREAD_POOL = Integer.parseInt(properties.getProperty("max_db_thread_pool"));

		// Establishes the pool of connection to the database
		cpds = new ComboPooledDataSource();
		cpds.setDriverClass( DB_DRIVER );
		cpds.setJdbcUrl( DB_URL );
		cpds.setUser(DB_USER);                                  
		cpds.setPassword(DB_PASS); 
		
		// the settings below are optional -- c3p0 can work with defaults
		cpds.setMinPoolSize(MIN_DB_THREAD_POOL);                                     
		cpds.setAcquireIncrement(5);
		cpds.setMaxPoolSize(MAX_DB_THREAD_POOL);

		// Starts the last date-time when the cache writes in the database as the current date-time. 
		lastDateTimeStore = LocalDateTime.now();
		
		/**
		 *  This part inserts any pending data in the cache to the database, in case of shutdown.
		 */
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			
			
			@Override
			public void run()
			{
				
				
				try {
					System.out.println("closing the thread pool");
					threadPool.awaitTermination(WRITE_TIME, TimeUnit.SECONDS);
					
					System.out.println("closing the database connection pool");
					if (cpds != null){
						cpds.close();
					}
					
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}

			}
		});  

		} catch (PropertyVetoException e1) {
			logger.error(e1.getMessage());
			e1.printStackTrace();
			System.exit(0);
		}             

	}

	/**
	 * Sets cache initial capacity and limit size.
	 * @param initialCapacity minimum total entries of internal hash tables
	 * @param maxSize the maximum number of entries in the cache
	 * @see Caffeine#build()
	 */
	public static void setCache(){
		// cache is implemented by Caffeine
		cache = Caffeine.newBuilder()
				// time to delete an entry from cache
				.expireAfterWrite(DELETE_TIME, TimeUnit.SECONDS)
				// initial cache size
				.initialCapacity(10_000)
				// max size
				.maximumSize(100_000)
				.writer(new WriteBehindCacheWriter.Builder<String, AttributeValue>()
						// time before execute WriteAction over database.
						.bufferTime(WRITE_TIME, TimeUnit.SECONDS)
						// action to decide which value get in multiple entries
						.coalesce(BinaryOperator.maxBy(AttributeValue::compareTo))
						// WriteAction
						.writeAction(entries -> {
							if (entries.size() > 0) {
								logger.debug("to storage num entries:" + entries.size());
								MeasureAttributeDatabaseStore storedatabase;
								storedatabase = new MeasureAttributeDatabaseStore(entries, BATCH_ROWS);
								threadPool.submit(storedatabase);
							} // if
						}).build()) // writeAction
						.build(); // writeBehindCache
	}

	/**
	 * Returns a singleton instance of MeasureAttributeValueCache.
	 * @return the singleton instance. 
	 * @throws PropertyVetoException 
	 */
	public synchronized static MeasureAttributeValueCache getInstance(){
		if(instance == null){
			instance = new MeasureAttributeValueCache();
			setCache();// default values
		}
		return instance;
	}

	/**
	 * Updates the last object's date time written in the database.
	 * @param lastDtStore last date time written.
	 */
	public synchronized static void UpdateLastDateTimeStore(LocalDateTime lastDtStore)
	{
		if (lastDtStore != null){

			lastDateTimeStore = lastDtStore;
		}
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
	 * Returns the time of the oldest entry in the cache.
	 * @return the time of the oldest entry in the cache.
	 */
	public LocalDateTime getOldestTime(){
		return lastDateTimeStore;
	}


	/**
	 * Turns over the database all the MeasuredAttribute values remain into cache.
	 * 
	 * @param keys List of keys to write into database.
	 */
	public void bulkCommit(List<String> keys) {

		// Gets a copy of all attributes in the cache. 
		Map<String,AttributeValue> subSet = cache.getAllPresent(keys);
		
		// Invalidates all cache entries found.
		cache.invalidateAll(keys);

		// Saves measured attribute values in the database.
		MeasureAttributeDatabaseStore storedatabase;
		
		storedatabase = new MeasureAttributeDatabaseStore(subSet, BATCH_ROWS);
		
		threadPool.submit(storedatabase);
		
	}

	/**
	 * Gets the url used to connect to the database 
	 * 
	 * @return database url.
	 */
	public synchronized String getDB_URL() {
		return DB_URL;
	}

	/**
	 * Gets the database user 
	 * @return database user
	 */
	public synchronized String getDB_USER() {
		return DB_USER;
	}

	/**
	 * Gets the database password
	 * @return database password
	 */
	public synchronized String getDB_PASS() {
		return DB_PASS;
	}

	/**
	 * Gets the database driver
	 * @return database driver
	 */
	public synchronized String getDB_DRIVER() {
		return DB_DRIVER;
	}
	

	public synchronized String getSqlMeasureAttributeValueRangeSelect()
	{
		return sqlMeasureAttributeValueRangeSelect;
	}
	
	public synchronized String getTimeStampField()
	{
		return timestamp;
	}
	
	public synchronized static Connection getConnection() throws SQLException{
		return cpds.getConnection();
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
	public ArrayList<AttributeValue> getFromDatabase(
			Integer entityId, MeasuredEntityType mType, Attribute attribute, 
			LocalDateTime from, LocalDateTime to) {

		logger.debug("In getFromDatabase");
		Connection connDB  = null; 
		PreparedStatement pstDB = null;

		ArrayList<AttributeValue> list = new ArrayList<AttributeValue>();
		
		ResultSet rs = null;

		Calendar cal = Calendar.getInstance();
		TimeZone utcTimeZone = TimeZone.getTimeZone(SystemConstants.TIMEZONE);
		cal.setTimeZone(utcTimeZone);

		String timestapfield = getTimeStampField();
		try {
			
			// get the database connection from the pool
			connDB = getConnection();
			connDB.setAutoCommit(false);
			// prepare the statement
			pstDB = connDB.prepareStatement(getSqlMeasureAttributeValueRangeSelect());
			pstDB.setInt(1, entityId);
			pstDB.setInt(2, mType.getValue());
			pstDB.setString(3, attribute.getName());
			pstDB.setTimestamp(4, Timestamp.valueOf(from));
			pstDB.setTimestamp(5, Timestamp.valueOf(to));
			rs =  pstDB.executeQuery();

			// brings the attribute data
			while (rs.next())
			{
				logger.debug("In getFromDatabase" + "current thread:" + Thread.currentThread().getName());
				
				Timestamp dtstime = rs.getTimestamp(timestapfield, cal);
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
			
			logger.debug("Ending getFromDatabase");

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally{
			if (rs!=null){
				try{
					logger.debug("result set close");
					rs.close();
				} catch (SQLException e) {  }
			}
			
			if(pstDB!=null){
				try{
					logger.debug("prepared statement close");
					pstDB.close();
				} catch (SQLException e) { 	}
			}

			if(connDB!=null){
				try	{
					logger.debug("close connection");
					connDB.close();
				} catch (SQLException e) { 	}
			}
		}
		
		return list;
	}


}
