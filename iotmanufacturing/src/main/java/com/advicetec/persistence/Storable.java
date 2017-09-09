package com.advicetec.persistence;

import java.sql.PreparedStatement;
/**
 * This interface defines required signatures to interact with the database.
 * It describes only <i>INSERT</i> and <i>DELETE</i> functions.
 * @author advicetec
 *
 */
public interface Storable 
{

	/**
	 * Returns the prepared statement to INSERT data into the database.
	 * @return  a String with the prepared statement to INSERT data into the 
	 * database.
	 */
	public String getPreparedInsertText();
	
	/**
	 * Returns the prepared statement to DELETE data from the database.
	 * @return a String with the prepared statement to DELETE data from the 
	 * database.
	 */
	public String getPreparedDeleteText();
	
	/**
	 * Executes INSERT statement into the database.
	 * @param pstmt SQL insert statement.
	 */
	public void dbInsert(PreparedStatement pstmt);
	
	/**
	 * Executes DELETE statement into the database.
	 * @param pstmt SQL delete statement.
	 */
	public void dbDelete(PreparedStatement pstmt);
		
}
