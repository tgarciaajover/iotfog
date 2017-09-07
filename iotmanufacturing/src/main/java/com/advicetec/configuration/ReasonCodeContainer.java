package com.advicetec.configuration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Container for reason codes.
 * 
 * @author Andres Marentes
 *
 */
public class ReasonCodeContainer extends Container 
{

	static Logger logger = LogManager.getLogger(ReasonCodeContainer.class.getName());
	
	/**
	 * SQL Statement for selecting the reason code configuration data.
	 */
	static String sqlSelect = "SELECT id, descr, classification, down, create_date, group_cd FROM setup_idlereason";
	
	/**
	 * SQL Statement for selecting the canonical information of reason codes.
	 */
	static String sqlReasonSelect = "SELECT idlereason_ptr_id, id_compania, id_sede, id_planta, id_razon_parada FROM setup_idlereasonhostsystem WHERE idlereason_ptr_id = ";

	/**
	 * This map help to index reason codes by canonical codes. The first string corresponds to the canonical reason code key, the second to the identifier of the 
	 * reason code assigned by the configuration database.
	 */
	private Map<String, Integer> canonicalMapIndex;
	
	/**
	 * Constructor for the class, it takes as parameters data required to connect to the database.
	 * 
	 * @param driver		: driver string used to connect to the database.
	 * @param server		: Ip address of the database server
	 * @param user			: database user
	 * @param password		: password of the user's database.
	 */
	public ReasonCodeContainer(String driver, String server, String user, String password) 
	{	
		super(driver, server, user, password);	
		
		canonicalMapIndex = new HashMap<String, Integer>();
	}

	/**
	 * Gets a consolidated canonical key from its parts.
	 * 
	 * @param company   	canonical company code
	 * @param location		canonical location code
	 * @param plant			canonical plant code
	 * @param stopReasonId	canonical reason code 
	 * @return	a consolidated key built from its parts.
	 */
	private String getCanonicalKey(String company, String location, String plant, String stopReasonId)
	{
		return company + "-" + location + "-" + plant + "-" + stopReasonId;
	}
	
	/**
	 * Loads all reason codes registered in the database into the container.
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
				Integer id     			= rs.getInt("id");
		        String descr   			= rs.getString ("descr");
		        String classification   = rs.getString ("classification");
		        String down			    = rs.getString ("down");
		        Timestamp timestamp     = rs.getTimestamp("create_date");
		        String group_cd			= rs.getString ("group_cd");
		        
		        ReasonCode reasonCode = new ReasonCode(id, descr);
		        reasonCode.setClassification(classification);
		        reasonCode.setGroup(group_cd);
		        
		        if (down.trim().equals("Y")){
		        	reasonCode.setIdleDown(true);
		        } else {
		        	reasonCode.setIdleDown(false);
		        }
		        reasonCode.setCreateDate(timestamp.toLocalDateTime());
		        
		        super.configuationObjects.put(id, reasonCode);
		      
			}
			
			rs.close();
			
			// loop through the measured entities and load their behaviors
			for( Integer id : this.configuationObjects.keySet()){
				ReasonCode reasonCode = (ReasonCode) this.configuationObjects.get(id);
				loadCannonicalReasonCodes(reasonCode);
			}
			

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
	 * Loads the canonical information registered in the database into the container for a particular reason code.
	 * 
	 * @param reasonCode  reason code to obtain its canonical information.
	 */
	private void loadCannonicalReasonCodes(ReasonCode reasonCode) {
		try 
		{
			String sqlSelect = sqlReasonSelect + String.valueOf(reasonCode.getId());  
			ResultSet rs = super.pst.executeQuery(sqlSelect);

			while (rs.next()) 
			{
				String company 		= rs.getString("id_compania");
				String location     = rs.getString("id_sede");
				String plant   		= rs.getString("id_planta");
				String reason = rs.getString("id_razon_parada");

				reasonCode.setCannonicalCompany(company);
				reasonCode.setCannonicalLocation(location);
				reasonCode.setCannonicalPlant(plant);
				reasonCode.setCannonicalReasonId(reason);
				
				canonicalMapIndex.put(getCanonicalKey(company, location, plant, reason) , reasonCode.getId());
			}

			rs.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
	}

	/**
	 * Deletes a reason code from the container
	 * 
	 * @param uniqueID  Identifier of the reason code to remove.
	 */
	public void deleteReasonCode(int uniqueID)
	{
		
		ReasonCode reasonCode = (ReasonCode)super.configuationObjects.get(uniqueID);
		
		if (reasonCode != null){ 
			canonicalMapIndex.remove(getCanonicalKey(reasonCode.getCannonicalCompany(), 
					   								 reasonCode.getCannonicalLocation(),
					   								 reasonCode.getCannonicalPlant(),
					   								 reasonCode.getCannonicalReasonId()));
			super.configuationObjects.remove(uniqueID);
		}
	
	}
	
	/**
	 * Builds a reason code from json object representation. Once it creates the new instance, that instance is inserted in the container
	 * 
	 * @param json  json representation.
	 * 
	 * @return true if it could parse and add the object in the container.
	 */
	public synchronized boolean fromJSON(String json){
		
		boolean ret = false;
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		ReasonCode reasonCode;
		try {
		
			reasonCode = mapper.readValue(json, ReasonCode.class);
			
			super.connect();
			
			// Remove from the canonical map.
			if (canonicalMapIndex.get(reasonCode.getId()) != null)
				canonicalMapIndex.remove(reasonCode.getId());
			
			loadCannonicalReasonCodes(reasonCode);
			super.disconnect();
			super.configuationObjects.put(reasonCode.getId(), reasonCode);
			ret = true;
			return ret;
		
		} catch (JsonParseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
        	String error = "Could not find the driver class - Error" + e.getMessage(); 
        	logger.error(error);
			e.printStackTrace();
		} catch (SQLException e) {
        	String error = "Container:" + this.getClass().getName() +  "Error connecting to the database - error:" + e.getMessage();
        	logger.error(error);
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 * Includes a reason code in the container. This method is for testing. 
	 * 
	 * @param reasonObj  Reason code object to include.
	 * @param company   	canonical company code
	 * @param location		canonical location code
	 * @param plant			canonical plant code
	 * @param reason		canonical reason code 
	 */
	public synchronized void insertReason(ReasonCode reasonObj, String company, String location, String plant, String reason)
	{
		canonicalMapIndex.put(getCanonicalKey(company, location, plant, reason) , reasonObj.getId());
		super.configuationObjects.put(reasonObj.getId(), reasonObj);
	}

	/**
	 * Gets the identifier of the reason code from the canonical information 
	 * 
	 * @param canCompany		canonical company code
	 * @param canLocation		canonical location code
	 * @param canPlant			canonical plant code
	 * @param canStopReason		canonical reason code 
	 * 
	 * @return Reason code identifier
	 */
	public Integer getReasonCodeId(String canCompany, String canLocation, String canPlant, String canStopReason) {
		logger.info("Number of reason codes registered:" + Integer.toString(this.canonicalMapIndex.size()));
		return this.canonicalMapIndex.get(getCanonicalKey(canCompany, canLocation, canPlant, canStopReason));
	}
		
}
