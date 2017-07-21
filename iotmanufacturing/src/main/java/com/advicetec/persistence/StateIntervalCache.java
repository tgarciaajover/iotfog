package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.configuration.ReasonCodeContainer;
import com.advicetec.core.Configurable;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.core.TimeInterval;
import com.advicetec.measuredentitity.Machine;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuringState;
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
	
	static Logger logger = LogManager.getLogger(StateIntervalCache.class.getName());
	
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

	private String sqlDownTimeReasons;
	
	final private static String sqlStatusIntervalRangeSelect = "SELECT datetime_from, datetime_to, status, reason_code, production_rate, actual_production_rate, qty_defective FROM measuringentitystatusinterval WHERE id_owner = ? and owner_type = ? and ((datetime_from >= ? AND datetime_from <= ?) or (datetime_to >= ? and datetime_to <= ?))";
			
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

		if (this.DB_DRIVER.compareTo("org.postgresql.Driver") == 0){
			sqlDownTimeReasons = "SELECT reason_code, COUNT(*) AS counter, SUM(DATE_PART('minute',datetime_to - datetime_from)) AS duration FROM measuringentitystatusinterval WHERE id_owner = ? and owner_type = ? AND ((datetime_from >= ? AND datetime_from <= ?) or (datetime_to >= ? AND datetime_to <= ?)) GROUP BY reason_code";
		} else if (this.DB_DRIVER.compareTo("com.microsoft.sqlserver.jdbc.SQLServerDriver") == 0){
			sqlDownTimeReasons = "SELECT reason_code, COUNT(*) AS counter, SUM(DATEDIFF(minute,datetime_from,datetime_to)) AS duration FROM measuringentitystatusinterval WHERE id_owner = ? and owner_type = ? AND ((datetime_from >= ? AND datetime_from <= ?) or (datetime_to >= ? AND datetime_to <= ?)) GROUP BY reason_code";
		}

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
		                		logger.debug("In writeaction:" + entries.size());
			        			try {
									Class.forName(DB_DRIVER);
				        			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
				        			conn.setAutoCommit(false);
				        			pst = conn.prepareStatement(StateInterval.SQL_Insert);
		
				                	entries.forEach((k,v)-> {
				                		((StateInterval)v).dbInsert(pst);

				                	});
		
				                	int ret[] = pst.executeBatch();
				                	logger.debug("Number of commands executed:" + ret.length);
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
	public LocalDateTime getOldestTime(){
		return LocalDateTime.now().minusSeconds(WRITE_TIME + DELETE_TIME);
	}
	
	
	public synchronized ArrayList<StateInterval> getFromDatabase(Integer entityId, MeasuredEntityType mType,
			LocalDateTime from, LocalDateTime to) {
		
		logger.info("getFromDatabase:" + Integer.toString(entityId) + " MeasureEntityType:" + mType + " from" + from.toString() + " to:" + to.toString());
		
		Connection connDB  = null; 
		PreparedStatement pstDB = null;
		
		ArrayList<StateInterval> list = new ArrayList<StateInterval>();
		
		try {
			Class.forName(DB_DRIVER);
			connDB = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			connDB.setAutoCommit(false);
			pstDB = connDB.prepareStatement(this.sqlStatusIntervalRangeSelect);
			pstDB.setString(1, Integer.toString(entityId));
			pstDB.setInt(2, mType.getValue());
			pstDB.setTimestamp(3, Timestamp.valueOf(from));
			pstDB.setTimestamp(4, Timestamp.valueOf(to));
			pstDB.setTimestamp(5, Timestamp.valueOf(from));
			pstDB.setTimestamp(6, Timestamp.valueOf(to));
			ResultSet rs =  pstDB.executeQuery();

			ConfigurationManager manager = ConfigurationManager.getInstance();
			ReasonCodeContainer reasonCont =  manager.getReasonCodeContainer();
			
			while (rs.next())
			{
				
				logger.info("state interval found");
				
				// datetime_from, datetime_to, status, reason_code
				LocalDateTime dTimeFrom = rs.getTimestamp("datetime_from").toLocalDateTime();
				LocalDateTime dTimeTo = rs.getTimestamp("datetime_to").toLocalDateTime();
				String status = rs.getString("status");
				String reasonCode = rs.getString("reason_code");
				Double productionRate = rs.getDouble("production_rate");
				Double actualProductionRate = rs.getDouble("actual_production_rate");
				Double qtyDefective = rs.getDouble("qty_defective");
				
				
				MeasuringState measuringState = MeasuringState.getByName(status);
				ReasonCode rCode = null;
				if (reasonCode != null) {
					rCode = (ReasonCode) reasonCont.getObject(Integer.valueOf(reasonCode));
				}
				
				TimeInterval timeInterval = new TimeInterval(dTimeFrom, dTimeTo); 
				StateInterval sInt = new StateInterval(measuringState, rCode, timeInterval, entityId, mType, productionRate, actualProductionRate, qtyDefective);
				
				list.add(sInt);
						      
			}
			

			// Bring the attribute 
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			if(pstDB!=null)
			{
				try
				{
					pstDB.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if(connDB!=null) 
			{
				try
				{
					connDB.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return list;	
	}
	
	
	/**
	 * Returns a list of Downtime Reasons from database.
	 * @param entity
	 * @param from
	 * @param to
	 * @return
	 */
	public synchronized Map<Integer,DowntimeReason> getDownTimeReasonsByInterval(MeasuredEntity entity,
			LocalDateTime from, LocalDateTime to)
	{

		Connection connDB  = null; 
		PreparedStatement pstDB = null;

		logger.info("in getDownTimeReasonsByInterval MeasuredEntity:" + entity.getId() + " from:" + from.toString() + " to:" + to.toString());
		
		Map<Integer,DowntimeReason> map = new HashMap<Integer,DowntimeReason>();
		
		try{
			Class.forName(DB_DRIVER);
			connDB = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			connDB.setAutoCommit(false);
			pstDB = connDB.prepareStatement(this.sqlDownTimeReasons);
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
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			if(pstDB!=null)
			{
				try
				{
					pstDB.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if(connDB!=null) 
			{
				try
				{
					connDB.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return map;
	}
}
