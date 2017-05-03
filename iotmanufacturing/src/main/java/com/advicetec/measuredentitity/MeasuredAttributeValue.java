package com.advicetec.measuredentitity;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.configuration.LocalDateTimeDeserializer;
import com.advicetec.configuration.LocalDateTimeSerializer;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeValue;
import com.advicetec.persistence.Storable;

@JsonIgnoreProperties({"preparedInsertText","preparedDeleteText"})
@JsonTypeName("MeasuredAttributeValue")
public class MeasuredAttributeValue extends AttributeValue implements Storable
{

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	LocalDateTime timeStamp;

	@JsonIgnore
	public final static String SQL_Insert = "INSERT INTO MeasuredAttributeValue(id_owner, timestamp, owner_type, attribute_name, value_decimal, value_datetime, value_string, value_int) " + "VALUES(?,?,?,?,?,?,?,?)";
	@JsonIgnore
	public final static String SQL_Delete = "DELETE INTO MeasuredAttributeValue(id_owner, timestamp, owner_type, attribute_name) " + "VALUES(?,?,?,?)";

	@JsonCreator
	public MeasuredAttributeValue(
			@JsonProperty("attr")Attribute type, 
			@JsonProperty("value")Object value, 
			@JsonProperty("generator")Integer parent, 
			@JsonProperty("generatorType")MeasuredEntityType parentType, 
			@JsonProperty("timeStamp") LocalDateTime timeStamp) 
	{
		// key = generator + attrName + timeStamp
		super(Integer.toString(parent)+ ":" + type.getName()+":"+ timeStamp.toString(), 
				type, value, parent, parentType );
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
			switch ( getAttr().getType().getValue() )
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
			pstmt.setInt(1, getGenerator());   // id_owner
			pstmt.setTimestamp(2, Timestamp.valueOf(getTimeStamp()) );    // timestamp
			pstmt.setInt(3, getGeneratorType().getValue());          // owner_type
			pstmt.setString(4, getAttr().getName());      // Attribute Name
			pstmt.addBatch();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   							// id_owner		
	}

	public void toCache(){

	}

	public String toString(){
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(", timestamp: ").append(timeStamp.toString());
		return sb.toString();
	}

	public boolean store() {
		// TODO Auto-generated method stub
		return false;
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
