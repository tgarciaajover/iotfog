package com.advicetec.measuredentitity;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.advicetec.core.TimeInterval;
import com.advicetec.persistence.Storable;

public class StateInterval implements Storable
{
  
	private String key;
	private MeasuringStatus status;
	private ReasonCode reason;
	private TimeInterval interval;
	
	// information about the parent.
	private String parent;
	private MeasuredEntityType parentType;
	
	public static final String SQL_Insert = "INSERT INTO measuringentitystatusinterval(id_owner, owner_type, datetime_from, datetime_to, status, reason_code)" + "VALUES(?,?,?,?,?,?)";
	public static final String SQL_Delete = "DELETE FROM measuringentitystatusinterval(id_owner, owner_type, datetime_from, datetime_to)" + "VALUES(?,?,?,?)";
			
	
	public StateInterval(MeasuringStatus status, ReasonCode reason,
			TimeInterval timeInterval,String parent, MeasuredEntityType parentType ) {
		super();
		
		this.key = timeInterval.toString();
		this.status = status;
		this.reason = reason;
		this.interval = timeInterval;
		this.parent = parent;
		this.parentType = parentType;
	}

	public MeasuringStatus getStatus() {
		return status;
	}

	public ReasonCode getReason() {
		return reason;
	}

	public TimeInterval getInterval() {
		return interval;
	}
	
	public String getKey(){
		return this.key;
	}

	public String getParent() {
		return parent;
	}

	public MeasuredEntityType getParentType() {
		return parentType;
	}

	public String getPreparedInsertText() {
		return SQL_Insert;
	}

	public String getPreparedDeleteText() {
		return SQL_Delete;
	}

	public void dbInsert(PreparedStatement pstmt) 
	{
		try 
		{
			pstmt.setString(1, getParent());
			pstmt.setInt(2, getParentType().getValue());          					// owner_type
			pstmt.setTimestamp(3, Timestamp.valueOf(getInterval().getStartDateTime()) );   // timestamp
			pstmt.setTimestamp(4, Timestamp.valueOf(getInterval().getEndDateTime()) );   // timestamp
			pstmt.setString(5, getStatus().getName() );      			// Measuring Status
			pstmt.setString(6, getReason().getId() );      			// Measuring Status
			
			pstmt.addBatch();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		
	}

	public void dbDelete(PreparedStatement pstmt) {

		try 
		{
			pstmt.setString(1, getParent());
			pstmt.setInt(2, getParentType().getValue());          					// owner_type
			pstmt.setTimestamp(3, Timestamp.valueOf(getInterval().getStartDateTime()) );   // timestamp
			pstmt.setTimestamp(4, Timestamp.valueOf(getInterval().getEndDateTime()) );   // timestamp
			
			pstmt.addBatch();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		
	}

	public boolean store() {
		// TODO Auto-generated method stub
		return false;
	}

	void setKey(String newKey) {
		this.key = newKey;
	}	
	
}
