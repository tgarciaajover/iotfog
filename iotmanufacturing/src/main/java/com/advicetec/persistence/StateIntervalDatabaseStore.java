package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.StateInterval;

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
	 * Default number of rows per batch  
	 */
	private int batchRows = 4000;
	
	public StateIntervalDatabaseStore(Map<String, StateInterval> entries, int batchRows)
	{
		this.entries = entries;
		this.batchRows = batchRows;
	}
	
	public static List<List< StateInterval>> split(Map<String, StateInterval> map, int limit){
	    
		List<List<StateInterval>> ret = new ArrayList<List<StateInterval>>();
		
		List<StateInterval> items = new ArrayList<StateInterval>(map.values());
		
	    int pages = (int) Math.ceil((double) items.size() / limit);
	    
	    logger.info("Pages:" + pages + "Items:" + items.size());
	    for (int i = 0; i < pages; i++) {
	        List<StateInterval> sub = items.subList(i * limit, ((i+1) * limit > items.size() ? items.size() : (i+1) * limit));
	        logger.info("Num items in sublist:" + sub.size());
	        ret.add(sub);
	    }
	    
	    return ret;
	}
	
	@Override
	public void run() {

		logger.info("Starting Executing database insert State Interval" + " current Thread:" + Thread.currentThread().getName());
		// Splits the entries in batches of batchRows  
		List<List<StateInterval>> lists = split(entries, batchRows);

		logger.info("Number of list:" + lists.size() + " current Thread:" + Thread.currentThread().getName());
		
		// Loop through split lists and insert in the database 
		for (List<StateInterval> entry : lists) {
			try {

				logger.info("number of rows to insert withlin list:" + entry.size() + " current Thread:" + Thread.currentThread().getName() );
				// connect to database
				conn = StateIntervalCache.getConnection();
				conn.setAutoCommit(false);
				pst = conn.prepareStatement(StateInterval.SQL_Insert);
				// prepares the statement
				entry.forEach((v)-> {
					logger.debug( "db write key:" + ((StateInterval)v).getKey() );
					((StateInterval)v).dbInsert(pst);

				});
				// execute the insertion
				int ret[] = pst.executeBatch();
				logger.debug("Number of State Intervals inserted:" + ret.length);
				conn.commit();

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
