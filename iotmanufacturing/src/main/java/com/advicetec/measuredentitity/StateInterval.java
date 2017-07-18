package com.advicetec.measuredentitity;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import com.advicetec.configuration.Container;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.core.TimeInterval;
import com.advicetec.persistence.Storable;

@JsonIgnoreProperties({"preparedInsertText","preparedDeleteText"})
public final class StateInterval implements Storable
{
	private String key;
	private MeasuringState state;
	private ReasonCode reason;
	private TimeInterval interval;
	
	// information about the parent.
	private Integer parent;
	private MeasuredEntityType parentType;
	private Double productionRate; 
	private Double actualProductionRate;
	private Double qtyDefective;
	
	public static final String SQL_Insert = "INSERT INTO measuringentitystatusinterval(id_owner, owner_type, datetime_from, datetime_to, status, reason_code, production_rate, actual_production_rate, qty_defective)" + "VALUES(?,?,?,?,?,?,?,?,?)";
	public static final String SQL_Delete = "DELETE FROM measuringentitystatusinterval WHERE id_owner = ? AND owner_type = ? AND datetime_from = ? AND datetime_to =?";
			
	
	public StateInterval(
			@JsonProperty("state")MeasuringState state, 
			@JsonProperty("reason")ReasonCode reason,
			@JsonProperty("interval")TimeInterval timeInterval,
			@JsonProperty("origin")Integer parent, 
			@JsonProperty("originType")MeasuredEntityType parentType,
			@JsonProperty("productionRate")Double productionRate,
			@JsonProperty("actualProductionRate")Double actualProductionRate,
			@JsonProperty("qtyDefective")Double qtyDefective
			) {
		super();
		
		this.key = timeInterval.toString();
		this.state = state;
		this.reason = reason;
		this.interval = timeInterval;
		this.parent = parent;
		this.parentType = parentType;
		this.productionRate = productionRate;
		this.actualProductionRate = actualProductionRate;
		this.qtyDefective = qtyDefective;
	}

	public Double getProductionRate() {
		return productionRate;
	}

	public void setProductionRate(Double productionRate) {
		this.productionRate = productionRate;
	}
    
	public void setActualProductionRate(Double actualProductionRate) {
		this.actualProductionRate = actualProductionRate;
	}

	public MeasuringState getState() {
		return state;
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

	public Integer getParent() {
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
			pstmt.setInt(1, getParent());
			pstmt.setInt(2, getParentType().getValue());          					// owner_type
			pstmt.setTimestamp(3, Timestamp.valueOf(getInterval().getStart()) );   // timestamp
			pstmt.setTimestamp(4, Timestamp.valueOf(getInterval().getEnd()) );   // timestamp
			pstmt.setString(5, getState().getName() );      			// Measuring Status
			
			// Reason Code 
			if (getReason() != null) {
				pstmt.setString(6, getReason().getId().toString() );      			
			} else { 
				pstmt.setString(6, null);
			}
			
			// Production rate
			pstmt.setDouble(7, getProductionRate());
			
			// actual production rate
			pstmt.setDouble(8, getActualProductionRate());
	
			// qty defective
			pstmt.setDouble(9, getQtyDefective());
			
			pstmt.addBatch();

		} catch (SQLException e) {
			Logger logger = LogManager.getLogger(StateInterval.class.getName());
			logger.error(e.getMessage());
			e.printStackTrace();
		}			
		
	}

	public void dbDelete(PreparedStatement pstmt) {

		try 
		{
			pstmt.setInt(1, getParent());
			pstmt.setInt(2, getParentType().getValue());          					// owner_type
			pstmt.setTimestamp(3, Timestamp.valueOf(getInterval().getStart()) );   // timestamp
			pstmt.setTimestamp(4, Timestamp.valueOf(getInterval().getEnd()) );   // timestamp
			
			pstmt.addBatch();

		} catch (SQLException e) {
			Logger logger = LogManager.getLogger(StateInterval.class.getName());
			logger.error(e.getMessage());
			e.printStackTrace();
		}			
		
	}

	public void setKey(String newKey) {
		this.key = newKey;
	}
	
	public String toJson()
	{
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("key:").append(key).append(",");
		sb.append("state:").append(state).append(",");
		sb.append("reason:").append(reason).append(",");
		sb.append("interval:").append(interval).append(",");
		sb.append("origin:").append(parent).append(",");
		sb.append("originType:").append(parentType).append(",");
		sb.append("productionRate:").append(productionRate);
		return sb.toString();
	}
	
	public int compareTo(StateInterval a)
	{
		return this.toString().compareTo(a.toString());
	}

	@JsonIgnore
	public Double getDurationMin() {
		return (double) ChronoUnit.MINUTES.between(interval.getStart(),interval.getEnd());
	}

	
	public Double getActualProductionRate() {
		return this.actualProductionRate;
	}

	public double getQtyDefective() {
		return this.qtyDefective;
	}	
	

}
