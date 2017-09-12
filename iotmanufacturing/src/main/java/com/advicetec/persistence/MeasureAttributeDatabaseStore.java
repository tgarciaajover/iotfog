package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.advicetec.utils.MapUtils;

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
	
	public MeasureAttributeDatabaseStore(Map<String, AttributeValue> entries, Connection connection, int batchRows)
	{
		this.entries = entries;
		this.batchRows = batchRows;
		this.conn = connection;
	}
	

	
	@Override
	public void run() {

		logger.info("Starting Executing database insert MeasuringAttributeValue" + " current Thread:" + Thread.currentThread().getName());
		// Splits the entries in batches of batchRows  
		List<Map<String, AttributeValue>> listofMaps =
				entries.entrySet().stream().collect(MapUtils.mapSize(batchRows));

		logger.info("Number of list:" + listofMaps.size() + " current Thread:" + Thread.currentThread().getName());
		// Loop through split lists and insert in the database 
		for (Map<String, AttributeValue> entry : listofMaps) {
			try {

				logger.info("number of rows to insert withlin list:" + entry.size() + " current Thread:" + Thread.currentThread().getName() );
				// connect to database
				conn.setAutoCommit(false);
				pst = conn.prepareStatement(MeasuredAttributeValue.SQL_Insert);
				// prepares the statement
				entry.forEach((k,v)-> {
					logger.debug( "db write key:" + ((MeasuredAttributeValue)v).getKey() );
					((MeasuredAttributeValue)v).dbInsert(pst);

				});
				// execute the insertion
				int ret[] = pst.executeBatch();
				logger.debug("Number of Attribute Values inserted:" + ret.length);
				conn.commit();

			} catch (SQLException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}

			finally{
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
		
		logger.info("Ending Executing database insert MeasuringAttributeValue" + " current Thread:" + Thread.currentThread().getName());
	}
}
