package com.advicetec.measuredentitity;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

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
	private LocalDateTime lastUpdttm;
	
	// information about the parent.
	private Integer parent;
	private MeasuredEntityType parentType;
	private Integer executedObject;
	private Integer executedObjectType;
	private String executedObjectCanonical;
	private Double productionRate;
	private Double conversion1;  // This field maintains the conversion from cycles to product units (unit of measure 1)
	private Double conversion2;  // This field maintains the conversion from cycles to product units (unit of measure 2)
	private Double actualProductionRate;
	private Double qtyDefective;
	 
	public static final String SQL_Insert = "INSERT INTO measuringentitystatusinterval(id_owner, owner_type, datetime_from, datetime_to, status, reason_code, executed_object, executed_object_type, executed_object_canonical, production_rate, conversion1, conversion2, actual_production_rate, qty_defective)" + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String SQL_Delete = "DELETE FROM measuringentitystatusinterval WHERE id_owner = ? AND owner_type = ? AND datetime_from = ? AND datetime_to =?";
			
	
	public StateInterval(
			@JsonProperty("state")MeasuringState state, 
			@JsonProperty("reason")ReasonCode reason,
			@JsonProperty("interval")TimeInterval timeInterval,
			@JsonProperty("origin")Integer parent, 
			@JsonProperty("originType")MeasuredEntityType parentType,
			@JsonProperty("executedObject")Integer executedObject,
			@JsonProperty("executedObjectType")Integer executedObjectType,
			@JsonProperty("executedObjectCanonical")String executedObjectCanonical,
			@JsonProperty("productionRate")Double productionRate,
			@JsonProperty("conversion1")Double conversion1,
			@JsonProperty("conversion2")Double conversion2,
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
		this.executedObject = executedObject;
		this.executedObjectType = executedObjectType;
		this.executedObjectCanonical = executedObjectCanonical;
		this.productionRate = productionRate;
		this.conversion1 = conversion1;
		this.conversion2 = conversion2;
		this.actualProductionRate = actualProductionRate;
		this.qtyDefective = qtyDefective;
		this.lastUpdttm = LocalDateTime.now();
	}

	public LocalDateTime getLastUpdttm(){
		return this.lastUpdttm;
	}
	
	public Integer getExecutedObject() {
		return executedObject;
	}

	public void setExecutedObject(Integer executedObject) {
		this.executedObject = executedObject;
		this.lastUpdttm = LocalDateTime.now();
	}

	public Integer getExecutedObjectType() {
		return executedObjectType;
	}
	
	public void setExecutedObjectType(Integer executedObjectType) {
		this.executedObjectType = executedObjectType;
		this.lastUpdttm = LocalDateTime.now();
	}
	
	public String getExecutedObjectCanonical() {
		return executedObjectCanonical;
	}

	public void setExecutedObjectCanonical(String executedObjectCanonical) {
		this.executedObjectCanonical = executedObjectCanonical;
	}

	public Double getProductionRate() {
		return productionRate;
	}

	public void setProductionRate(Double productionRate) {
		this.productionRate = productionRate;
		this.lastUpdttm = LocalDateTime.now();
	}
    
	public void setActualProductionRate(Double actualProductionRate) {
		this.actualProductionRate = actualProductionRate;
		this.lastUpdttm = LocalDateTime.now();
	}

	public Double getConversion1() {
		return conversion1;
	}

	public void setConversion1(Double conversion1) {
		this.conversion1 = conversion1;
		this.lastUpdttm = LocalDateTime.now();
	}

	public Double getConversion2() {
		return conversion2;
	}

	public void setConversion2(Double conversion2) {
		this.conversion2 = conversion2;
		this.lastUpdttm = LocalDateTime.now();
	}

	public void setReason(ReasonCode reason) {
		this.reason = reason;
		this.lastUpdttm = LocalDateTime.now();
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
			pstmt.setTimestamp(3, Timestamp.valueOf(getInterval().getStart()) );   // Timestamp
			pstmt.setTimestamp(4, Timestamp.valueOf(getInterval().getEnd()) );   // Timestamp
			pstmt.setString(5, getState().getName() );      			// Measuring Status
			
			// Reason Code 
			if (getReason() != null) {
				pstmt.setString(6, getReason().getId().toString() );      			
			} else { 
				pstmt.setString(6, null);
			}
			
			if (getExecutedObject() == null){
				pstmt.setNull(7, java.sql.Types.INTEGER);
			} else{
				// Executed Object
				pstmt.setInt(7, getExecutedObject());
			}
			
			// Executed Object Type
			if (getExecutedObjectType() == null){
				pstmt.setNull(8, java.sql.Types.INTEGER);
			} else {
				pstmt.setInt(8, getExecutedObjectType());
			}

			pstmt.setString(9, getExecutedObjectCanonical());

			// Production rate
			if (getProductionRate() == null){
				pstmt.setDouble(10, new Double(0.0));
			} else {
				pstmt.setDouble(10, getProductionRate());
			}
			
			// Conversion 1
			if (getConversion1() == null){
				pstmt.setDouble(11, new Double(0.0));
			} else {
				pstmt.setDouble(11, getConversion1());
			}

			// Conversion 2
			if (getConversion2() == null) {
				pstmt.setDouble(12, new Double(0.0));
			} else {
				pstmt.setDouble(12, getConversion2());
			}

			// actual production rate
			if (getActualProductionRate() == null){
				pstmt.setDouble(13, new Double(0.0));
			} else {
				pstmt.setDouble(13, getActualProductionRate());
			}
	
			// qty defective
			if (getQtyDefective() == null){
				pstmt.setDouble(14, new Double(0.0));
			} else {
				pstmt.setDouble(14, getQtyDefective());
			}
			
			pstmt.addBatch();

		} catch (SQLException e) {
			Logger logger = LogManager.getLogger(StateInterval.class.getName());
			logger.error(e.getMessage());
			e.printStackTrace();
		}			
		
	}

	public void dbDelete(PreparedStatement pstmt) {

        Calendar cal = Calendar.getInstance();
        TimeZone timeZone1 = TimeZone.getDefault();
        cal.setTimeZone(timeZone1);

		try 
		{
			pstmt.setInt(1, getParent());
			pstmt.setInt(2, getParentType().getValue());          					// owner_type
			pstmt.setTimestamp(3, Timestamp.valueOf(getInterval().getStart()), cal );   // timestamp
			pstmt.setTimestamp(4, Timestamp.valueOf(getInterval().getEnd()), cal );   // timestamp
			
			pstmt.addBatch();

		} catch (SQLException e) {
			Logger logger = LogManager.getLogger(StateInterval.class.getName());
			logger.error(e.getMessage());
			e.printStackTrace();
		}			
		
	}

	public void setKey(String newKey) {
		this.key = newKey;
		this.lastUpdttm = LocalDateTime.now();
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
		sb.append("executedObject:").append(executedObject).append(",");
		sb.append("executedObjectType:").append(executedObjectType).append(",");
		sb.append("productionRate:").append(productionRate);
		sb.append("conversion1:").append(conversion1);
		sb.append("conversion2:").append(conversion2);
		sb.append("actualProductionRate:").append(actualProductionRate);
		sb.append("qtyDefective:").append(qtyDefective);
		
		return sb.toString();
	}
	
	public int compareTo(StateInterval a)
	{
		return this.lastUpdttm.compareTo(a.getLastUpdttm());
	}

	@JsonIgnore
	public Double getDurationMin() {
		return (double) ChronoUnit.MINUTES.between(interval.getStart(),interval.getEnd());
	}

	
	public Double getActualProductionRate() {
		return this.actualProductionRate;
	}

	public Double getQtyDefective() {
		return this.qtyDefective;
	}	
	
}
