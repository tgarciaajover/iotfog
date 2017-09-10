package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.measuredentitity.StateInterval;
import com.advicetec.utils.MapUtils;

public class StateIntervalDatabaseStore implements Runnable {
	
	
	static Logger logger = LogManager.getLogger(StateIntervalDatabaseStore.class.getName());
	
	private Map<String, StateInterval> entries = null;

	/**
	 * Database connection object
	 */
	private Connection conn  = null;
	/**
	 * Database prepared statement object
	 */
	private PreparedStatement pst = null;

	/**
	 * Database url
	 */
	private String DB_URL = null;
	/**
	 * Database user
	 */
	private String DB_USER = null;
	/**
	 * Database password
	 */
	private String DB_PASS = null;
	/**
	 * Database driver
	 */
	private String DB_DRIVER = null;

	/**
	 * Default number of rows per batch  
	 */
	private int batchRows = 4000;
	
	public StateIntervalDatabaseStore(Map<String, StateInterval> entries, String driver, String dbUrl, String user, String password, int batchRows)
	{
		this.entries = entries;
		this.DB_DRIVER = driver;
		this.DB_URL = dbUrl;
		this.DB_USER = user;
		this.DB_PASS = password;
		this.batchRows = batchRows;
	}
	

	
	@Override
	public void run() {

		logger.info("Starting Executing database insert State Interval" + " current Thread:" + Thread.currentThread().getName());
		// Splits the entries in batches of batchRows  
		List<Map<String, StateInterval>> listofMaps =
				entries.entrySet().stream().collect(MapUtils.mapSize(batchRows));

		logger.info("Number of list:" + listofMaps.size() + " current Thread:" + Thread.currentThread().getName());
		
		// Loop through split lists and insert in the database 
		for (Map<String, StateInterval> entry : listofMaps) {
			try {

				logger.info("number of rows to insert withlin list:" + entry.size() + " current Thread:" + Thread.currentThread().getName() );
				// connect to database
				Class.forName(DB_DRIVER);
				conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
				conn.setAutoCommit(false);
				pst = conn.prepareStatement(StateInterval.SQL_Insert);
				// prepares the statement
				entry.forEach((k,v)-> {
					logger.debug( "db write key:" + ((StateInterval)v).getKey() );
					((StateInterval)v).dbInsert(pst);

				});
				// execute the insertion
				int ret[] = pst.executeBatch();
				logger.debug("Number of State Intervals inserted:" + ret.length);
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
			}
		}
		
		logger.info("Ending Executing database insert State Intervals" + " current Thread:" + Thread.currentThread().getName());
	}
}
