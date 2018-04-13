package com.advicetec.configuration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class AggregateMethodContainer extends Container 
{

	static Logger logger = LogManager.getLogger(AggregateMethodContainer.class.getName());

	/**
	 *  SQL statement to select the signal type configuration data  
	 */
	static String sqlSelect = "select am.id , am.name , am.descr , am.method , am.attribute , am.create_date  , am.period_id , pr.name as name_period , pr.type , pr.type_child , pr.scale from setup_aggregatemethod am , setup_period pr where pr.id = am.period_id ";

	/**
	 * Constructor for the class, it takes as parameters data required to connect to the database.
	 * 
	 * @param driver		: driver string used to connect to the database.
	 * @param server		: Ip address of the database server
	 * @param user			: database user
	 * @param password		: password of the user's database.
	 */
	public AggregateMethodContainer(String driver, String server, String user, String password) 
	{	
		super(driver, server, user, password);	
	}
	
	/**
	 * Loads all signal types registered in the database into the container.
	 * 
	 * @throws SQLException
	 */
	public void loadContainer() throws SQLException
	{

		try 
		{
			super.connect();
			super.configuationObjects.clear();

			
			ResultSet rs = super.pst.executeQuery(sqlSelect);
			while (rs.next())
			{
				Integer id				= rs.getInt("id");
				String name             = rs.getString("name");
				String descr            = rs.getString("descr");
				String method           = rs.getString("method");
				String attribute        = rs.getString("attribute");
				Timestamp create_date   = rs.getTimestamp("create_date");
				String period_id        = rs.getString("period_id");
				String name_period      = rs.getString("name_period");
				String type             = rs.getString("type");
				String type_child       = rs.getString("type_child");
				Integer scale           = rs.getInt("scale");
				
				AggregateMethod object = new AggregateMethod(id);
		        object.setName(name);       
		        object.setDescr(descr);      
		        object.setMethod(method);     
		        object.setAttribute(attribute);  
		        object.setCreate_date(create_date.toLocalDateTime());
		        object.setPeriod_id(period_id);
		        object.setName_period(name_period);
		        object.setType(type);       
		        object.setType_child(type_child);
		        object.setScale(scale);
		        super.configuationObjects.put(id, object);
		      
			}
			
			rs.close();

			super.disconnect();
			
		} catch (ClassNotFoundException e){
        	String error = "Could not find the driver class - Error" + e.getMessage(); 
        	logger.error(error);
        	e.printStackTrace();
        	throw new SQLException(error);
        } catch (SQLException e) {
        	String error = "Container:" + this.getClass().getName() +  "Error connecting to the database - error:" + e.getMessage();
        	logger.error(error);
        	e.printStackTrace();        	
        	throw new SQLException(error);
        }
		
		
	}

	/**
	 * Gets a AggregateMethod  from its alternative identifier
	 * 
	 * @param referenceCd  alternative identifier
	 * @return  AggregateMethod  object with the alternative identifier.
	 */
	public synchronized AggregateMethod getAggregateMethod(String name){
		
		for (int aggregateMethodId : this.configuationObjects.keySet()){
			String gerReference = ((AggregateMethod) this.configuationObjects.get(aggregateMethodId)).getName();
			if (gerReference.compareTo(name) == 0) {
				return (AggregateMethod) super.configuationObjects.get(aggregateMethodId);
			}
		}
		
		return null;
	}

	
	/**
	 * Builds a signal type object from Json object representation. Once it creates the new instance, it is inserted in the container
	 * 
	 * @param json  json representation.
	 */
	public void fromJSON(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		SignalType sTypeTemp;
		try {
		
			sTypeTemp = mapper.readValue(json, SignalType.class);
			super.configuationObjects.put(sTypeTemp.getId(), sTypeTemp);
		
		} catch (JsonParseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
		
}
