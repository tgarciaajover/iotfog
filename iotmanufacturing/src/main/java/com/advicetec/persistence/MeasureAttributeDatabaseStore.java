package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.MeasuredAttributeValue;

public class MeasureAttributeDatabaseStore implements Runnable {
	
	
	static Logger logger = LogManager.getLogger(MeasureAttributeDatabaseStore.class.getName());
	
	private Map<String, AttributeValue> entries = null;

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
	
	/**
	 * last object's date-time written to the database
	 */
	LocalDateTime lastStore = null;
	
	
	public MeasureAttributeDatabaseStore(Map<String, AttributeValue> entries, int batchRows)
	{
		this.entries = entries;
		this.batchRows = batchRows;
	}
	

	public static List<List< AttributeValue>> split(Map<String, AttributeValue> map, int limit){
	    
		List<List<AttributeValue>> ret = new ArrayList<List<AttributeValue>>();
		
		List<AttributeValue> items = new ArrayList<AttributeValue>(map.values());
		
	    int pages = (int) Math.ceil((double) items.size() / limit);
	    
	    logger.debug("Pages:" + pages + "Items:" + items.size());
	    for (int i = 0; i < pages; i++) {
	        List<AttributeValue> sub = items.subList(i * limit, ((i+1) * limit > items.size() ? items.size() : (i+1) * limit));
	        logger.debug("Num items in sublist:" + sub.size());
	        ret.add(sub);
	    }
	    
	    return ret;
	}

	
	@Override
	public void run() {

		logger.debug("Starting Executing database insert MeasuringAttributeValue" + " current Thread:" + Thread.currentThread().getName());
		// Splits the entries in batches of batchRows  
		List<List<AttributeValue>> lists = split(entries, batchRows);
				
		logger.info("Number of list:" + lists.size() + " current Thread:" + Thread.currentThread().getName());
		// Loop through split lists and insert in the database 
		for (List<AttributeValue> entry : lists) {
			try {

				logger.debug("number of rows to insert withlin list:" + entry.size() + " current Thread:" + Thread.currentThread().getName() );
				// connect to database
				conn = MeasureAttributeValueCache.getConnection();
				conn.setAutoCommit(false);
				pst = conn.prepareStatement(MeasuredAttributeValue.SQL_Insert);
				// prepares the statement
				entry.forEach((v)-> {
					logger.debug( "db write key:" + ((MeasuredAttributeValue)v).getKey() );
					if (lastStore == null){
						lastStore = ((MeasuredAttributeValue)v).getTimeStamp(); 
					} else {
						if (lastStore.compareTo(((MeasuredAttributeValue)v).getTimeStamp()) <= 0){
							lastStore = ((MeasuredAttributeValue)v).getTimeStamp();
						}
					}
					
					((MeasuredAttributeValue)v).dbInsert(pst);

				});
				// execute the insertion
				int ret[] = pst.executeBatch();
				logger.info("Number of Attribute Values inserted:" + ret.length);
				conn.commit();

			} catch (SQLException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			} finally{
				if(pst!=null){
					try{
						logger.info("closing prepared statement");
						pst.close();
					} catch (SQLException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}
				}
				
				if(conn!=null) {
					try {
						logger.info("closing connection");
						conn.close();
					} catch (SQLException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}
				}

			}
		}
		
		MeasureAttributeValueCache.getInstance().UpdateLastDateTimeStore(lastStore);
		
		logger.debug("Ending Executing database insert MeasuringAttributeValue" + " current Thread:" + Thread.currentThread().getName());
	}
}
