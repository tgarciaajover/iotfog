package com.advicetec.measuredentitity;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;
import com.advicetec.persistence.Storable;

@JsonIgnoreProperties({"preparedInsertText","preparedDeleteText"})
@JsonTypeName("MeasuredAttributeValue")
public class MeasuredAttributeValue extends AttributeValue implements Storable
{

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	LocalDateTime timeStamp;

	@JsonIgnore
	public final static String SQL_Insert = "INSERT INTO measuredattributevalue(id_owner, timestamp, owner_type, attribute_name, value_decimal, value_datetime, value_string, value_int, value_boolean, value_date, value_time) " + " VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	@JsonIgnore
	public final static String SQL_Delete = "DELETE INTO measuredattributevalue(id_owner, timestamp, owner_type, attribute_name) " + " VALUES (?,?,?,?)";

	@JsonCreator
	public MeasuredAttributeValue(
			@JsonProperty("attr")Attribute type, 
			@JsonProperty("value")Object value, 
			@JsonProperty("generator")Integer parent, 
			@JsonProperty("generatorType")MeasuredEntityType parentType, 
			@JsonProperty("timeStamp") LocalDateTime timeStamp) 
	{
		super(type.getName() + ":" + timeStamp.toString(), type, value, parent, parentType );
		this.timeStamp = timeStamp;
	}

	
	public LocalDateTime getTimeStamp() 
	{
		return timeStamp;
	}

	@JsonIgnore
	public String getPreparedInsertText() {
		return SQL_Insert;
	}

	public String getPreparedDeleteText() {
		return SQL_Delete;
	}


	public void dbInsert(PreparedStatement pstmt) {
		try 
		{
			pstmt.setInt(1, getGenerator());							// Set the parent
			pstmt.setTimestamp(2, Timestamp.valueOf(getTimeStamp()));   // timestamp
			pstmt.setInt(3, getGeneratorType().getValue());          		// owner_type
			pstmt.setString(4, getAttr().getName());      			// Attribute Name
			switch ( getAttr().getType() )
			{
			case DOUBLE :  // Double
				pstmt.setDouble(5, (Double) getValue());		 // value_Decimal
				pstmt.setNull(6, java.sql.Types.TIMESTAMP);      // value_datetime
				pstmt.setString(7, null);                        // value_string
				pstmt.setNull(8, java.sql.Types.INTEGER);        // value_int
				pstmt.setNull(9, java.sql.Types.BOOLEAN);        // value_boolean
				pstmt.setNull(10, java.sql.Types.DATE);          // value_date
				pstmt.setNull(11, java.sql.Types.TIME);          // value_time
				
				break;				
			case DATETIME:  // Datetime
				pstmt.setNull(5, java.sql.Types.DOUBLE);		 // value_Decimal
				pstmt.setTimestamp(6, Timestamp.valueOf( (LocalDateTime) getValue()));		 // value_Decimal				
				pstmt.setString(7, null);
				pstmt.setNull(8, java.sql.Types.INTEGER);
				pstmt.setNull(9, java.sql.Types.BOOLEAN);        // value_boolean
				pstmt.setNull(10, java.sql.Types.DATE);          // value_date
				pstmt.setNull(11, java.sql.Types.TIME);          // value_time
				break;

			case STRING:  // String
				pstmt.setNull(5, java.sql.Types.DOUBLE);		 // value_Decimal
				pstmt.setNull(6, java.sql.Types.TIMESTAMP);
				pstmt.setString(7, (String) getValue());	     // value_string
				pstmt.setNull(8, java.sql.Types.INTEGER);
				pstmt.setNull(9, java.sql.Types.BOOLEAN);        // value_boolean
				pstmt.setNull(10, java.sql.Types.DATE);          // value_date
				pstmt.setNull(11, java.sql.Types.TIME);          // value_time
				break;

			case BOOLEAN:  // Boolean
				pstmt.setNull(5, java.sql.Types.DOUBLE);		 // value_Decimal
				pstmt.setNull(6, java.sql.Types.TIMESTAMP);
				pstmt.setString(7, null);	     				 // value_string
				pstmt.setNull(8, java.sql.Types.INTEGER);
				pstmt.setBoolean(9, (Boolean) getValue());       // value_boolean
				pstmt.setNull(10, java.sql.Types.DATE);          // value_date
				pstmt.setNull(11, java.sql.Types.TIME);          // value_time
				break;
				
			case INT:  // Integer
				pstmt.setNull(5, java.sql.Types.DOUBLE);		 // value_Decimal
				pstmt.setNull(6, java.sql.Types.TIMESTAMP);
				pstmt.setString(7, null);						// value_string
				pstmt.setInt(8, (Integer) getValue());	     
				pstmt.setNull(9, java.sql.Types.BOOLEAN);        // value_boolean
				pstmt.setNull(10, java.sql.Types.DATE);          // value_date
				pstmt.setNull(11, java.sql.Types.TIME);          // value_time
				break;

			case DATE:  // Date
				pstmt.setNull(5, java.sql.Types.DOUBLE);		 // value_Decimal
				pstmt.setNull(6, java.sql.Types.TIMESTAMP);
				pstmt.setString(7, null);
				pstmt.setNull(8, java.sql.Types.INTEGER);	     // value_string
				pstmt.setNull(9, java.sql.Types.BOOLEAN);        // value_boolean
				pstmt.setDate(10, Date.valueOf((LocalDate) getValue()));         // value_date
				pstmt.setNull(11, java.sql.Types.TIME);          // value_time
				break;

			case TIME:  // Time
				pstmt.setNull(5, java.sql.Types.DOUBLE);		 // value_Decimal
				pstmt.setNull(6, java.sql.Types.TIMESTAMP);
				pstmt.setString(7, null);
				pstmt.setNull(8, java.sql.Types.INTEGER);	     // value_string
				pstmt.setNull(9, java.sql.Types.BOOLEAN);        // value_boolean
				pstmt.setNull(10, java.sql.Types.DATE);          // value_date
				pstmt.setTime(11, Time.valueOf((LocalTime) getValue()));          // value_time
				break;
			default:
				// TODO: Error case
				break;
				
			}

			pstmt.addBatch();		

		} catch (SQLException e) {
			Logger logger = LogManager.getLogger(MeasuredAttributeValue.class.getName());
			logger.error(e.getMessage());
			e.printStackTrace();
		}   							// id_owner
	}

	public void setValueFromDatabase(ResultSet rs)
	{
		try{
			switch ( getAttr().getType() )
			{
			case DOUBLE :  // Double
				setValue(new Double( rs.getDouble("value_decimal"))); 
				break;				
			case DATETIME:  // Datetime
				setValue(rs.getTimestamp("value_datetime").toLocalDateTime());
				break;

			case STRING:  // String
				setValue(rs.getString("value_string"));
				break;

			case BOOLEAN:  // Boolean
				setValue(new Boolean(rs.getBoolean("value_boolean")));
				break;

			case INT:  // Integer
				setValue(new Integer(rs.getInt("value_int")));
				break;

			case DATE:  // Date
				setValue(rs.getDate("value_date").toLocalDate());
				break;

			case TIME:  // Time
				setValue(rs.getTime("value_time").toLocalTime());
				break;
			default:
				// TODO error case.
				break;

			}		
		} catch (SQLException e) {
			Logger logger = LogManager.getLogger(MeasuredAttributeValue.class.getName());
			logger.error(e.getMessage());
			e.printStackTrace();
		}   							// id_owner

	}

	public void dbDelete(PreparedStatement pstmt) {

		try 
		{
			pstmt.setInt(1, getGenerator());   // id_owner
			pstmt.setTimestamp(2, Timestamp.valueOf(getTimeStamp()) );    // timestamp
			pstmt.setInt(3, getGeneratorType().getValue());          // owner_type
			pstmt.setString(4, getAttr().getName());      // Attribute Name
			pstmt.addBatch();

		} catch (SQLException e) {
			Logger logger = LogManager.getLogger(MeasuredAttributeValue.class.getName());
			logger.error(e.getMessage());
			e.printStackTrace();
		}   							// id_owner		
	}

	public void toCache(){

	}

	public void setValue(Object value){
		this.value = value;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(", timestamp: ").append(timeStamp.toString());
		return sb.toString();
	}

	public String toJson(){
		String json = null;
		ObjectMapper mapper = new ObjectMapper();
		//mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		try {
			json = mapper.writeValueAsString(this);
		} catch (IOException e) {
			System.err.println("Cannot export this Mesured Attribute Value as the json object.");
			e.printStackTrace();
		}
		return json;
	}
}
