package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.configuration.ReasonCodeContainer;
import com.advicetec.configuration.SystemConstants;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.Configurable;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.core.TimeInterval;
import com.advicetec.measuredentitity.DowntimeReason;
import com.advicetec.measuredentitity.Machine;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuringState;
import com.advicetec.measuredentitity.StateInterval;
import com.advicetec.utils.MapUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Represents a cache for storing State Interval elements. This class stores 
 * the intervals where a machine/device remains in a state. It also extends 
 * from Configurable to read the configuration file <code>properties</code>.
 * 
 * @see StateInterval
 * @author advicetec
 *
 */ 
public class StateIntervalCache extends Configurable {

	static Logger logger = LogManager.getLogger(StateIntervalCache.class.getName());

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
	 * Cache limit
	 */
	private static Integer MAX_SIZE = 10000;
	/**
	 * Time the cache keeps entries before write over database.
	 */
	private static Integer WRITE_TIME = 10;
	/**
	 * Time the cache keeps entries before delete them.
	 */
	private static Integer DELETE_TIME = 0;	
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
	private static StateIntervalCache instance = null;
	/**
	 * Database connection object
	 */
	private static Connection conn  = null; 
	/**
	 * Database prepared statement object
	 */
	private static PreparedStatement pst = null;
	/**
	 * SQL expression for multiple database driver.
	 */
	private String sqlDownTimeReasons;
	/**
	 * SQL to select a set of Status Interval given a time range and owner_id. 
	 */
	final private static String sqlStatusIntervalRangeSelect = "SELECT datetime_from, datetime_to, status, reason_code, executed_object, executed_object_type, executed_object_canonical, production_rate, conversion1, conversion2, actual_production_rate, qty_defective FROM measuringentitystatusinterval WHERE id_owner = ? and owner_type = ? and ((datetime_from >= ? AND datetime_from <= ?) or (datetime_to >= ? and datetime_to <= ?))";
	/**
	 * SQL sentence to update an interval over dataabse.
	 */
	final private static String sqlUpdateInterval = "UPDATE measuringentitystatusinterval SET reason_code = ? WHERE id_owner = ? and owner_type = ? and datetime_from =?";
	/**
	 * A cache is a map with a key and the stateInterval. 
	 */		
	private static Cache<String, StateInterval> cache;
	/**
	 * Thread pool to save data in the database.
	 */
	private static ExecutorService threadPool = null;


	/**
	 * Constructs this cache with the parameters from .properties file.
	 */
	private StateIntervalCache(){
		// loads values from StateIntervalCache.properties
		super("StateIntervalCache");
		// database connection properties
		DB_DRIVER = properties.getProperty("driver");
		DB_URL = properties.getProperty("server");
		DB_USER = properties.getProperty("user");
		DB_PASS = properties.getProperty("password");
		// cache initialization properties
		// initial cache size
		INIT_CAPACITY = Integer.valueOf(properties.getProperty("init_capacity"));
		// limit cache size
		MAX_SIZE = Integer.valueOf(properties.getProperty("max_size"));
		// time before store StateInterval into database
		WRITE_TIME = Integer.valueOf(properties.getProperty("write_time"));
		// time before delete entries from the cache.
		DELETE_TIME = Integer.valueOf(properties.getProperty("delete_time"));

		// Thread related information to store data into the database.
		INSERT_THREADS = Integer.parseInt(properties.getProperty("insert_threads"));
		BATCH_ROWS = Integer.parseInt(properties.getProperty("batch_rows"));

		threadPool = Executors.newFixedThreadPool(INSERT_THREADS);
		
		// depends on database driver, assigns the needed query.
		if (StateIntervalCache.DB_DRIVER.compareTo("org.postgresql.Driver") == 0){
			sqlDownTimeReasons = "SELECT reason_code, COUNT(*) AS counter, SUM(DATE_PART('minute',datetime_to - datetime_from)) AS duration FROM measuringentitystatusinterval WHERE id_owner = ? and owner_type = ? AND ((datetime_from >= ? AND datetime_from <= ?) or (datetime_to >= ? AND datetime_to <= ?)) GROUP BY reason_code";
		} else if (StateIntervalCache.DB_DRIVER.compareTo("com.microsoft.sqlserver.jdbc.SQLServerDriver") == 0){
			sqlDownTimeReasons = "SELECT reason_code, COUNT(*) AS counter, SUM(DATEDIFF(minute,datetime_from,datetime_to)) AS duration FROM measuringentitystatusinterval WHERE id_owner = ? and owner_type = ? AND ((datetime_from >= ? AND datetime_from <= ?) or (datetime_to >= ? AND datetime_to <= ?)) GROUP BY reason_code";
		}
		// This part inserts any pending data in the cache to the database in case of shutdown.  
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				try {
					
					threadPool.awaitTermination(WRITE_TIME, TimeUnit.SECONDS);
					
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
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
				// limit cache size
				.maximumSize(maxSize)
				.writer(new WriteBehindCacheWriter.Builder<String, StateInterval>()
						// time before execute WriteAction over database.
						.bufferTime(WRITE_TIME, TimeUnit.SECONDS)
						// action to decide which value get in multiple entries
						.coalesce(BinaryOperator.maxBy(StateInterval::compareTo))
						// WriteAction
						.writeAction(entries -> {
							if (entries.size() > 0) {
								logger.info("to storage num entries:" + entries.size());
								StateIntervalDatabaseStore storedatabase = new StateIntervalDatabaseStore(entries, DB_DRIVER, DB_URL, DB_USER, DB_PASS,BATCH_ROWS);
								threadPool.submit(storedatabase);
							}
						}).build())
						.build();

	}

	/**
	 * Returns a singleton instance of StateIntervalCache.
	 * @return the singleton instance. 
	 */
	public static synchronized StateIntervalCache getInstance(){
		if(instance == null){
			instance = new StateIntervalCache();
			setCache(INIT_CAPACITY,MAX_SIZE); // default values
		}
		return instance;
	}

	/**
	 * Returns the cache map.
	 * @return the cache map.
	 */
	public Cache<String, StateInterval> getCache(){
		return cache; 
	}

	/**
	 * Stores an state interval and its measuring entity name. 
	 * @param interval element to be stored into cache.
	 */
	public void storeToCache(StateInterval interval){
		// the key is composed of entity name + start time + end time
		cache.put(interval.getKey(), interval);
	}

	/**
	 * Given a key, it returns an element from the cache if it is there.
	 * If the value is not in the cache, this method returns NULL.
	 * @param key string that identifies an element in the map.
	 * @return the StateInterval stored into the cache, or 
	 * <code>NULL</code> if the element is not present.
	 */
	public synchronized StateInterval getFromCache(String key){
		return cache.getIfPresent(key);
	}


	/**
	 * Turns over the database all State Interval values remain into cache.
	 * @param keys List of keys to write into database.
	 */
	public void bulkCommit(List<String> keys){

		logger.debug("In bulk commit Db Url:" + DB_URL + 
				"Number of elements to remove from cache:" + keys.size());

		Map<String,StateInterval> subSet = cache.getAllPresent(keys);

		// Splits the entries in batches of batchRows  
		List<Map<String, StateInterval>> listofMaps =
						subSet.entrySet().stream().collect(MapUtils.mapSize(BATCH_ROWS));

		// Loop through split lists and insert in the database 
		for (Map<String, StateInterval> entry : listofMaps) {
			
			if (entry.size() > 0){
				try {
					Class.forName(DB_DRIVER);
					conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
					conn.setAutoCommit(false);
					// prepare statement
					pst = conn.prepareStatement(StateInterval.SQL_Insert);

					// Store the values in the database.
					for (StateInterval interval :entry.values()) {
						interval.dbInsert(pst);
					}
					pst.executeBatch();
					conn.commit();

					// Discard those values obtained as they would be inserted in the database.
					cache.invalidateAll(entry.keySet());

				} catch (ClassNotFoundException | SQLException e) {
					logger.error("Error: "+ e.getMessage());
					e.printStackTrace();
				}
				finally{
					if(pst!=null)
					{
						try
						{
							pst.close();
						} catch (SQLException e) {
							logger.error("Error: "+ e.getMessage());
							e.printStackTrace();
						}
					}

					if(conn!=null) 
					{
						try
						{
							conn.close();
						} catch (SQLException e) {
							logger.error("Error: "+ e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the time of the oldest entry in the cache.
	 * This value is calculated with the DELETE_TIME
	 * @return the time of the oldest entry in the cache.
	 */
	public LocalDateTime getOldestTime(){
		return LocalDateTime.now().minusSeconds(DELETE_TIME);
	}

	/**
	 * Updates a state interval into database with the given parameters. 
	 * 
	 * @param entityId measured entity id.
	 * @param mType describes the type of measured entity.
	 * @param startDttm initial time to query.
	 * @param reasonCode downtime reason, not NULL.
	 * @return <code>TRUE</code> if updates any register, <code>FALSE</code>
	 * otherwise.  
	 */
	public synchronized boolean updateStateInterval(Integer entityId, MeasuredEntityType mType, 
			LocalDateTime startDttm, ReasonCode reasonCode) {

		logger.info("In updateStateInverval reasonCd:" + reasonCode.getId() );

		boolean ret = false;
		Connection connDB  = null; 
		PreparedStatement pstDB = null;
		try{
			Class.forName(DB_DRIVER);
			connDB = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			connDB.setAutoCommit(false);
			// prepares the statement
			pstDB = connDB.prepareStatement(StateIntervalCache.sqlUpdateInterval);
			// set parameters to the query
			pstDB.setString(1, reasonCode.getId().toString() );
			pstDB.setInt(2, entityId);
			pstDB.setInt(3, mType.getValue());
			pstDB.setTimestamp(4, Timestamp.valueOf(startDttm));
			if (pstDB.executeUpdate() > 0){
				ret = true;
				connDB.commit();
			}

		}catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally{
			if(pstDB!=null){
				try {
					pstDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}

			if(connDB!=null) {
				try {
					connDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}			

		logger.info("In updateStateInverval return:" + ret);
		return ret;
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
	
	/**
	 * Updates a state interval into cache with the given parameters. 
	 * @param stateKey key of downtime reason code to update.
	 * @param reasonCode downtime reason code to update.
	 * @return <code>TRUE</code> if the given downtime reason code is successfully
	 * updated, <code>FALSE</code> otherwise.
	 */
	public synchronized boolean updateCacheStateInterval(String stateKey, ReasonCode reasonCode) {

		logger.debug("In updateCacheStateInterval");
		
		StateInterval stateInterval = cache.getIfPresent(stateKey);
		if (stateInterval == null){		
			return false;
		} else {
			stateInterval.setReason(reasonCode); 
			cache.put(stateKey, stateInterval);
			return true;
		}
	}
	
	public synchronized String getSqlStatusIntervalRangeSelect() {
		return sqlStatusIntervalRangeSelect;
	}
	
	public synchronized String getSqlDownTimeReasons() {
		return sqlDownTimeReasons;
	}

	/**
	 * Returns a list of Attribute values from the database. The query is 
	 * composed of the device/machine, the name of the attribute and the time
	 * range (from,to).
	 * 
	 * @param entityId measured entity id.
	 * @param mType describes the type of measured entity.
	 * @param from initial time to query.
	 * @param to final time to query.
	 * @return a list of interval states from the database. If the query does 
	 * not return data, this method returns an empty list.
	 * 
	 * @see StateInterval
	 * @see MeasuredEntityType
	 */
	public ArrayList<StateInterval> getFromDatabase(
			Integer entityId, MeasuredEntityType mType,
			LocalDateTime from, LocalDateTime to) {

		logger.info("getFromDatabase:" + Integer.toString(entityId) + " MeasureEntityType:" + mType + " from:" + from.toString() + " to:" + to.toString());
		// database connection
		Connection connDB  = null; 
		PreparedStatement pstDB = null;

		ArrayList<StateInterval> list = new ArrayList<StateInterval>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SystemConstants.DATETIME_FORMAT);
		Calendar cal = Calendar.getInstance();
		TimeZone utcTimeZone = TimeZone.getTimeZone(SystemConstants.TIMEZONE);
		cal.setTimeZone(utcTimeZone);

		try {
			Class.forName(getDB_DRIVER());
			connDB = DriverManager.getConnection(getDB_URL(), getDB_USER(), getDB_PASS());
			connDB.setAutoCommit(false);
			pstDB = connDB.prepareStatement(getSqlStatusIntervalRangeSelect());
			pstDB.setInt(1, entityId);
			pstDB.setInt(2, mType.getValue());
			pstDB.setTimestamp(3, Timestamp. valueOf(from));
			pstDB.setTimestamp(4, Timestamp.valueOf(to));
			pstDB.setTimestamp(5, Timestamp.valueOf(from));
			pstDB.setTimestamp(6, Timestamp.valueOf(to));
			// execute the query
			ResultSet rs =  pstDB.executeQuery();

			ConfigurationManager manager = ConfigurationManager.getInstance();
			ReasonCodeContainer reasonCont =  manager.getReasonCodeContainer();

			while (rs.next())
			{

				// datetime_from, datetime_to, status, reason_code
				Timestamp dsTimeFrom = rs.getTimestamp("datetime_from", cal);
				long timestampTimeFrom = dsTimeFrom.getTime();
				cal.setTimeInMillis(timestampTimeFrom);
				LocalDateTime dTimeFrom = LocalDateTime.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 
						cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
						cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), 
						cal.get(Calendar.MILLISECOND));

				Timestamp dsTimeTo = rs.getTimestamp("datetime_to", cal);
				long timestampTimeTo = dsTimeTo.getTime();
				cal.setTimeInMillis(timestampTimeTo);
				LocalDateTime dTimeTo = LocalDateTime.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 
						cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
						cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), 
						cal.get(Calendar.MILLISECOND));

				logger.debug("State interval found from:" + dTimeFrom.format(formatter) + 
						" to:" + dTimeTo.format(formatter));
				
				// retrieve data from the query
				String status = rs.getString("status");
				String reasonCode = rs.getString("reason_code");
				Integer executedObject = rs.getInt("executed_object");
				Integer executedObjectType = rs.getInt("executed_object_type");
				String executedObjectCanonicalKey = rs.getString("executed_object_canonical");
				Double productionRate = rs.getDouble("production_rate");
				Double conversion1 = rs.getDouble("conversion1");
				Double conversion2 = rs.getDouble("conversion2");
				Double actualProductionRate = rs.getDouble("actual_production_rate");
				Double qtyDefective = rs.getDouble("qty_defective");

				MeasuringState measuringState = MeasuringState.getByName(status);
				ReasonCode rCode = null;
				if (reasonCode != null) {
					rCode = (ReasonCode) reasonCont.getObject(Integer.valueOf(reasonCode));
				}

				TimeInterval timeInterval = new TimeInterval(dTimeFrom, dTimeTo); 
				StateInterval sInt = new StateInterval(measuringState, rCode, 
						timeInterval, entityId, mType, executedObject, 
						executedObjectType, executedObjectCanonicalKey, productionRate, 
						conversion1, conversion2, actualProductionRate, qtyDefective);
				list.add(sInt);

			}
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		finally{
			if(pstDB!=null)	{
				try	{
					pstDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}

			if(connDB!=null){
				try {
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
	 * Returns a list of Downtime Reasons from database.
	 * 
	 * @param entity measured entity id.
	 * @param from initial time to query.
	 * @param to final time to query.
	 * @return a list of downtime reason from the database. If the query does 
	 * not return data, this method returns an empty list.
	 */
	public Map<Integer,DowntimeReason> getDownTimeReasonsByInterval(
			MeasuredEntity entity,LocalDateTime from, LocalDateTime to) {

		Connection connDB  = null; 
		PreparedStatement pstDB = null;

		logger.info("in getDownTimeReasonsByInterval MeasuredEntity:" + entity.getId() + " from:" + from.toString() + " to:" + to.toString());

		Map<Integer,DowntimeReason> map = new HashMap<Integer,DowntimeReason>();

		try{
			// database connection 
			Class.forName(getDB_DRIVER());
			connDB = DriverManager.getConnection(getDB_URL(), getDB_USER(), getDB_PASS());
			connDB.setAutoCommit(false);
			pstDB = connDB.prepareStatement(getSqlDownTimeReasons());
			pstDB.setString(1, Integer.toString(entity.getId()));
			pstDB.setInt(2, entity.getType().getValue());
			pstDB.setTimestamp(3, Timestamp.valueOf(from));
			pstDB.setTimestamp(4, Timestamp.valueOf(to));
			pstDB.setTimestamp(5, Timestamp.valueOf(from));
			pstDB.setTimestamp(6, Timestamp.valueOf(to));
			ResultSet rs = pstDB.executeQuery();

			ConfigurationManager manager = ConfigurationManager.getInstance();
			ReasonCodeContainer reasonCont =  manager.getReasonCodeContainer();

			while (rs.next())
			{
				// datetime_from, datetime_to, status, reason_code
				Integer reasonCode = rs.getInt("reason_code");
				Integer counter = rs.getInt("counter");
				Double duration = rs.getDouble("duration");

				if(entity.getType() == MeasuredEntityType.MACHINE){
					ReasonCode reason;
					reason = (ReasonCode) reasonCont.getObject(reasonCode);
					if(reason != null){
						map.put(reasonCode,new DowntimeReason(((Machine)entity).getCannonicalMachineId(), 
								reason.getCannonicalReasonId(), reason.getDescription(), counter, duration) );
					} else {
						map.put(0, new DowntimeReason(((Machine)entity).getCannonicalMachineId(), 
								"0", "Desconocida", counter, duration));
					}
				}
			}
		}catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		finally{
			if(pstDB!=null)	{
				try	{
					pstDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}

			if(connDB!=null) 
			{
				try
				{
					connDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return map;
	}

	
}
