package com.advicetec.FogClasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.advicetec.persistence.Storable;

public class MeasuredAttributeValue extends AttributeValue implements Storable
{
	LocalDateTime timeStamp;
	
	static String SQL_Insert = "INSERT INTO MeasuredAttributeValue(id_owner, timestamp, owner_type, attribute_name, value_decimal, value_datetime, value_string, value_int) " + "VALUES(?,?,?,?,?,?,?,?)";
	static String SQL_Delete = "DELETE INTO MeasuredAttributeValue(id_owner, timestamp, owner_type, attribute_name) " + "VALUES(?,?,?,?)";
		
	public MeasuredAttributeValue(Attribute type, Object value, String parent, MeasuredEntityType parentType, 
			LocalDateTime timeStamp) 
	{
		super(type.getName(), type, value, parent, parentType );
		this.timeStamp = timeStamp;
	}

	public LocalDateTime getTimeStamp() 
	{
		return timeStamp;
	}

	public String getPreparedInsertText() {
		return SQL_Insert;
	}

	public String getPreparedDeleteText() {
		return SQL_Delete;
	}


	public void dbInsert(PreparedStatement pstmt) {
		try 
		{
			pstmt.setString(1, getParent());							// Set the parent
			pstmt.setTimestamp(2, Timestamp.valueOf(getTimeStamp()));   // timestamp
			pstmt.setInt(3, getParentType().getValue());          		// owner_type
			pstmt.setString(4, getAttribute().getName());      			// Attribute Name
			switch ( getAttribute().getType().getValue() )
			{
				case 0:  // Double
					pstmt.setDouble(5, (Double) getValue());		 // value_Decimal
					pstmt.setNull(6, java.sql.Types.TIMESTAMP);
					pstmt.setString(7, null);
					pstmt.setNull(8, java.sql.Types.INTEGER);
					
					break;				
				case 1:  // Datetime
					pstmt.setDouble(5, java.sql.Types.DOUBLE);		 // value_Decimal
					pstmt.setTimestamp(6, Timestamp.valueOf( (LocalDateTime) getValue()));		 // value_Decimal				
					pstmt.setString(7, null);
					pstmt.setNull(8, java.sql.Types.INTEGER);
					break;
					
				case 2:  // String
					pstmt.setDouble(5, java.sql.Types.DOUBLE);		 // value_Decimal
					pstmt.setNull(6, java.sql.Types.TIMESTAMP);
					pstmt.setString(7, (String) getValue());	     // value_string
					pstmt.setNull(8, java.sql.Types.INTEGER);
					break;
					
				case 3:  // Integer
					pstmt.setDouble(5, java.sql.Types.DOUBLE);		 // value_Decimal
					pstmt.setNull(6, java.sql.Types.TIMESTAMP);
					pstmt.setString(7, null);
					pstmt.setInt(8, (Integer) getValue());	     // value_string
					break;
			}

			pstmt.addBatch();		

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   							// id_owner
	}


	public void dbDelete(PreparedStatement pstmt) {
		
		try 
		{
			pstmt.setString(1, getParent());   // id_owner
			pstmt.setTimestamp(2, Timestamp.valueOf(getTimeStamp()) );    // timestamp
			pstmt.setInt(3, getParentType().getValue());          // owner_type
			pstmt.setString(4, getAttribute().getName());      // Attribute Name
			pstmt.addBatch();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   							// id_owner		
	}
	
	public void toCache(){
		
	}
	
	public String toString(){
		//TODO
		return "Parent: "+ getParent()
				+", Parent type: "+ getParentType().getValue()
				+", When: "+ Timestamp.valueOf(getTimeStamp())
				+", Value: "+ getValue();
	}

	public boolean store() {
		// TODO Auto-generated method stub
		return false;
	}
}
