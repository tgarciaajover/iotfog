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

/**
 * This class models a machine/device state on an interval.
 * 
 * @author maldofer
 *
 */
@JsonIgnoreProperties({"preparedInsertText","preparedDeleteText"})
public final class StateInterval implements Storable
{

	/**
	 * Stater interval key  
	 */
	private String key;
	
	/**
	 * Measuring entity State 
	 */
	private MeasuringState state;
	
	/**
	 * Reason code 
	 */
	private ReasonCode reason;
	
	/**
	 * The reason code assigned to the measured entity state interval.
	 */
	private TimeInterval interval;
	
	/**
	 * Last date and time when this interval was modified.
	 */
	private LocalDateTime lastUpdttm;
	
	/**
	 * The measure entity internal identifier of the measured entity.
	 */
	private Integer parent;

	/**
	 * The measure entity type
	 */
	private MeasuredEntityType parentType;
	
	/**
	 * Related object 
	 */
	private Integer relatedObject;
	
	/**
	 * Related object type.
	 */
	private Integer relatedObjectType;
	
	/**
	 * Canonical identifier given to the executed object. 
	 */
	private String executedObjectCanonical;
	
	/**
	 * production rate
	 */
	private Double productionRate;
	
	/**
	 * This field maintains the conversion from cycles to product units (unit of measure 1)
	 */
	private Double conversion1;  
	
	/**
	 * This field maintains the conversion from cycles to product units (unit of measure 2)
	 */
	private Double conversion2;  
	
	/**
	 * Actual production rate 
	 */
	private Double actualProductionRate;
	
	/**
	 * Defective Quantity during the interval. 
	 */
	private Double qtyDefective;
	 
	/**
	 * SQl Statement to be used to insert a measured status interval 
	 */
	public static final String SQL_Insert = "INSERT INTO measuringentitystatusinterval(id_owner, owner_type, datetime_from, datetime_to, status, reason_code, related_object, related_object_type, executed_object_canonical, production_rate, conversion1, conversion2, actual_production_rate, qty_defective)" + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	/**
	 * SQl Statement to be used to delete a measured status interval
	 */
	public static final String SQL_Delete = "DELETE FROM measuringentitystatusinterval WHERE id_owner = ? AND owner_type = ? AND datetime_from = ? AND datetime_to =?";
			
	
	/**
	 * Constructor for the measured entity state interval.
	 * 
	 * @param state							the measured entity state  
	 * @param reason						reason code
	 * @param timeInterval					Time interval
	 * @param parent						measured entity
	 * @param parentType					measured entity type
	 * @param executedObject				executed object 
	 * @param executedObjectType			executed object type
	 * @param executedObjectCanonical		executed object canonical identifier
	 * @param productionRate				production rate
	 * @param conversion1					conversion rate 1
	 * @param conversion2					conversion rate 2
	 * @param actualProductionRate			actual production rate
	 * @param qtyDefective					total defective quantity 
	 */
	public StateInterval(
			@JsonProperty("state")MeasuringState state, 
			@JsonProperty("reason")ReasonCode reason,
			@JsonProperty("interval")TimeInterval timeInterval,
			@JsonProperty("origin")Integer parent, 
			@JsonProperty("originType")MeasuredEntityType parentType,
			@JsonProperty("relatedObject")Integer relatedObject,
			@JsonProperty("relatedObjectType")Integer relatedObjectType,
			@JsonProperty("executedObjectCanonical")String executedObjectCanonical,
			@JsonProperty("productionRate")Double productionRate,
			@JsonProperty("conversion1")Double conversion1,
			@JsonProperty("conversion2")Double conversion2,
			@JsonProperty("actualProductionRate")Double actualProductionRate,
			@JsonProperty("qtyDefective")Double qtyDefective
			) {
		super();
		
		this.key = parent + ":" + timeInterval.toString();
		this.state = state;
		this.reason = reason;
		this.interval = timeInterval;
		this.parent = parent;
		this.parentType = parentType;
		this.relatedObject = relatedObject;
		this.relatedObjectType = relatedObjectType;
		this.executedObjectCanonical = executedObjectCanonical;
		this.productionRate = productionRate;
		this.conversion1 = conversion1;
		this.conversion2 = conversion2;
		this.actualProductionRate = actualProductionRate;
		this.qtyDefective = qtyDefective;
		this.lastUpdttm = LocalDateTime.now();
	}

	/**
	 * Gets the last update datetime
	 * 
	 * @return	last update datetime
	 */
	public LocalDateTime getLastUpdttm(){
		return this.lastUpdttm;
	}
	
	/**
	 * Gets the executed object
	 * @return	executed object
	 */
	public Integer getRelatedObject() {
		return relatedObject;
	}

	/**
	 * Sets the related object
	 * @param relatedObject	related object to set
	 */
	public void setRelatedObject(Integer relatedObject) {
		this.relatedObject = relatedObject;
		this.lastUpdttm = LocalDateTime.now();
	}

	/**
	 * Gets the related object type
	 * @return	related object type
	 */
	public Integer getRelatedObjectType() {
		return relatedObjectType;
	}
	
	/**
	 * Sets the related object type
	 * @param relatedObjectType	related object type to set
	 */
	public void setRelatedObjectType(Integer relatedObjectType) {
		this.relatedObjectType = relatedObjectType;
		this.lastUpdttm = LocalDateTime.now();
	}
	
	/**
	 * Gets the executed object canonical identifier 
	 * @return	executed object canonical identifier
	 */
	public String getExecutedObjectCanonical() {
		return executedObjectCanonical;
	}

	/**
	 * Sets the executed object canonical identifier
	 * @param executedObjectCanonical	executed object canonical identifier to set
	 */
	public void setExecutedObjectCanonical(String executedObjectCanonical) {
		this.executedObjectCanonical = executedObjectCanonical;
	}

	/**
	 * Gets the production rate during the state interval
	 * 
	 * @return production rate
	 */
	public Double getProductionRate() {
		return productionRate;
	}

	/**
	 * Sets the production rate during the state interval
	 * 
	 * @param productionRate	production rate to set
	 */
	public void setProductionRate(Double productionRate) {
		this.productionRate = productionRate;
		this.lastUpdttm = LocalDateTime.now();
	}
    
	/**
	 * Sets the actual production rate 
	 * 
	 * @param actualProductionRate	actual production rate
	 */
	public void setActualProductionRate(Double actualProductionRate) {
		this.actualProductionRate = actualProductionRate;
		this.lastUpdttm = LocalDateTime.now();
	}

	/**
	 * Gets the conversion rate from cycles to units 
	 * 
	 * @return	conversion rate 
	 */
	public Double getConversion1() {
		return conversion1;
	}

	/**
	 * Sets the conversion rate one from cycles to units
	 * 
	 * @param conversion1 conversion rate one from cycles to units
	 */
	public void setConversion1(Double conversion1) {
		this.conversion1 = conversion1;
		this.lastUpdttm = LocalDateTime.now();
	}

	/**
	 * Gets the conversion rate two from cycles to units
	 * 
	 * @return	conversion rate two from cycles to units
	 */
	public Double getConversion2() {
		return conversion2;
	}

	/**
	 * Sets the conversion rate two from cycles to units
	 * 
	 * @param conversion2	conversion rate two from cycles to units
	 */
	public void setConversion2(Double conversion2) {
		this.conversion2 = conversion2;
		this.lastUpdttm = LocalDateTime.now();
	}

	/**
	 * Sets the reason code for the interval.
	 * 
	 * @param reason Interval reason code
	 */
	public void setReason(ReasonCode reason) {
		this.reason = reason;
		this.lastUpdttm = LocalDateTime.now();
	}
	
	/**
	 * Gets the measured entity state
	 * 
	 * @return measured entity state
	 */
	public MeasuringState getState() {
		return state;
	}

	/**
	 * Gets the reason code
	 * @return reason code
	 */
	public ReasonCode getReason() {
		return reason;
	}

	/**
	 * Gets the time interval 
	 * 
	 * @return	time interval 
	 */
	public TimeInterval getInterval() {
		return interval;
	}
	
	/**
	 * Gets the key
	 * 
	 * @return key from the state interval
	 */
	public String getKey(){
		return this.key;
	}

	/**
	 * Gets the parent 
	 * 
	 * @return parent identifier 
	 */
	public Integer getParent() {
		return parent;
	}

	/**
	 * Gets the parent type 
	 * 
	 * @return	parent type
	 */
	public MeasuredEntityType getParentType() {
		return parentType;
	}

	/**
	 * Gets the prepared insert statement text
	 */
	public String getPreparedInsertText() {
		return SQL_Insert;
	}

	/**
	 * Gets the prepared delete statement
	 */
	public String getPreparedDeleteText() {
		return SQL_Delete;
	}

	/**
	 * Inserts the state interval in the prepare statement given  
	 */
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
			
			if (getRelatedObject() == null){
				pstmt.setNull(7, java.sql.Types.INTEGER);
			} else{
				// Executed Object
				pstmt.setInt(7, getRelatedObject());
			}
			
			// Executed Object Type
			if (getRelatedObjectType() == null){
				pstmt.setNull(8, java.sql.Types.INTEGER);
			} else {
				pstmt.setInt(8, getRelatedObjectType());
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

	/**
	 * Deletes the state interval in the prepare statement given  
	 */
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

	/**
	 * Sets the key for the state interval.  
	 */
	public void setKey(String newKey) {
		this.key = newKey;
		this.lastUpdttm = LocalDateTime.now();
	}
	
	/**
	 * gets a json representation   
	 */
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
	
	/**
	 * String representation
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("key:").append(key).append(",");
		sb.append("state:").append(state).append(",");
		sb.append("reason:").append(reason).append(",");
		sb.append("interval:").append(interval).append(",");
		sb.append("origin:").append(parent).append(",");
		sb.append("originType:").append(parentType).append(",");
		sb.append("relatedObject:").append(relatedObject).append(",");
		sb.append("relatedObjectType:").append(relatedObjectType).append(",");
		sb.append("productionRate:").append(productionRate);
		sb.append("conversion1:").append(conversion1);
		sb.append("conversion2:").append(conversion2);
		sb.append("actualProductionRate:").append(actualProductionRate);
		sb.append("qtyDefective:").append(qtyDefective);
		
		return sb.toString();
	}
	
	/** 
	 * Compares two state intervals
	 *  
	 * @param a state interval to compare.
	 * 
	 * @return	0 if are equal, minor than zero if less than, greater than zero if greater than.   
	 */
	public int compareTo(StateInterval a)
	{
		return this.lastUpdttm.compareTo(a.getLastUpdttm());
	}

	/**
	 * State interval duration in minutes
	 *    
	 * @return state interval time in minutes 
	 */
	@JsonIgnore
	public Double getDurationMin() {
		return (double) ChronoUnit.MINUTES.between(interval.getStart(),interval.getEnd());
	}

	
	/**
	 * Gets the actual production rate 
	 * 
	 * @return	actual production rate 
	 */
	public Double getActualProductionRate() {
		return this.actualProductionRate;
	}

	/**
	 * Gets the total defective quantity
	 *  
	 * @return	total defective quantity
	 */
	public Double getQtyDefective() {
		return this.qtyDefective;
	}	
	
}
