package com.advicetec.persistence;

import java.sql.PreparedStatement;

public interface Storable 
{

	public String getPreparedInsertText();
	
	public String getPreparedDeleteText();
	
	public void dbInsert(PreparedStatement pstmt);
	
	public void dbDelete(PreparedStatement pstmt);
	
	public boolean store();
	
}
