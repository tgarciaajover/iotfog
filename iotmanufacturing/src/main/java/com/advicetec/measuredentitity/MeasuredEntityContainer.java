package com.advicetec.measuredentitity;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.advicetec.configuration.Container;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.AttributeType;
import com.advicetec.core.AttributeValue;
import com.advicetec.eventprocessor.ModBusTcpEvent;

/**
 * This class models the data container for Measured Entities.
 * It supports the interaction with the database.
 * 
 * @author advicetec
 * @see Container
 */
public class MeasuredEntityContainer extends Container 
{

	static Logger logger = LogManager.getLogger(MeasuredEntityContainer.class.getName());

	// String queries for data selection from databases
	static String sqlSelect1 = "SELECT id, code, descr, create_date, type FROM setup_measuredentity";
	static String sqlSelect2 = "SELECT id, name, descr, behavior_text, create_date, last_updttm, measure_entity_id FROM setup_measuredentitybehavior WHERE measure_entity_id = ";
	static String sqlSelect3 = "SELECT id, state_behavior_type, descr, behavior_text, create_date, last_updttm from setup_measuredentitystatebehavior WHERE measure_entity_id = ";
	static String sqlSelect4 = "SELECT id, state_from, behavior_id, measure_entity_id, reason_code_id, create_date, last_updttm FROM setup_measuredentitytransitionstate WHERE measure_entity_id = ";
	static String sqlSelect5 = "SELECT d.ip_address, c.measured_entity_id, c.port_label, c.refresh_time_ms from setup_signal a, setup_signaltype b, setup_inputoutputport c, setup_monitoringdevice d where b.protocol = 'M' and a.type_id = b.id and c.signal_type_id = a.id and d.id = c.device_id";
	static String sqlSelect6 = "SELECT id, scheduled_event_type, descr, recurrences, day_time, create_date, last_updttm FROM setup_measuredentityscheduledevent WHERE measure_entity_id =";
	static String sqlSelect7 = "SELECT count(*) from setup_signal a, setup_signaltype b, setup_inputoutputport c, setup_monitoringdevice d where b.protocol = 'Q' and a.type_id = b.id and c.signal_type_id = a.id and d.id = c.device_id";

	static String sqlMachineSelect = "SELECT * FROM setup_machinehostsystem WHERE measuredentity_ptr_id =";
	static String sqlPlantSelect = "SELECT measuredentity_ptr_id, id_compania, id_sede, id_planta FROM setup_planthostsystem WHERE measuredentity_ptr_id =";

	/**
	 * Maps ids from database and names in this model.
	 */
	private Map<String, Integer> canonicalMapIndex;

	/**
	 * Constructor of this container. 
	 * 
	 * @param driver SQL driver.
	 * @param server address to the server.
	 * @param user credential's user.
	 * @param password credential's password.
	 */
	public MeasuredEntityContainer(String driver, String server, String user, String password) {
		super(driver, server, user, password);

		canonicalMapIndex = new HashMap<String, Integer>();
	}

	/**
	 * Builds the canonical key from the given parameters.
	 * 
	 * @param company Identifier for the company.
	 * @param location Identifier for the location.
	 * @param plant Identifier for the plant.
	 * @param machineGroup Identifier for the group of machines. 
	 * @param machineId  Identifier for the machine
	 * @return the canonical key from the given parameters.
	 */
	private String getCanonicalKey(String company, String location, String plant, String machineGroup, String machineId)
	{
		if (machineId != null)
			return company + "-" + location + "-" + plant + "-" + machineGroup + "-" + machineId;
		else
			return company + "-" + location + "-" + plant;
	}

	/**
	 * Loads information from the setup database and creates a set of 
	 * MeasuredEntity objects.
	 * Later, loads behaviors, state behaviors, state transitions, machine data
	 * and scheduled events.
	 * 
	 * @throws SQLException if there are error trying to connect the database.
	 * @see {@link MeasuredEntity}
	 */
	public void loadContainer() throws SQLException
	{
		try 
		{
			super.connect();
			super.configuationObjects.clear();

			ResultSet rs1 = super.pst.executeQuery(sqlSelect1);
			while (rs1.next())
			{
				Integer id     			= rs1.getInt("id");  
				String code   			= rs1.getString("code");
				String descr   			= rs1.getString("descr");
				Timestamp timestamp 	= rs1.getTimestamp("create_date");
				String entityCategory   = rs1.getString("type");

				MeasuredEntity measuredEntity = null;

				logger.debug("measured Entity id:" + id.toString() +  " Entity Category:" + entityCategory);
				switch (entityCategory)
				{
				case "M":
					measuredEntity = new Machine(id);
					break;
				case "P":
					measuredEntity = new Plant(id);
					break;
				default:
					logger.error("Error entity category is not identified");
				}

				measuredEntity.setDescr(descr);
				measuredEntity.setCode(code);
				measuredEntity.setCreateDate(timestamp.toLocalDateTime());

				super.configuationObjects.put(id, measuredEntity);

			}

			rs1.close();

			// loop through the measured entities and load their behaviors
			for( Integer id : this.configuationObjects.keySet()){
				MeasuredEntity measuredEntity = (MeasuredEntity) this.configuationObjects.get(id);
				loadBehaviors(measuredEntity);
			}

			// loop through the measured entities and load their state behaviors
			for( Integer id : this.configuationObjects.keySet()){
				MeasuredEntity measuredEntity = (MeasuredEntity) this.configuationObjects.get(id);
				loadStateBehaviors(measuredEntity);
			}

			// loop through the measured entities and load their state transitions
			for( Integer id : this.configuationObjects.keySet()){
				MeasuredEntity measuredEntity = (MeasuredEntity) this.configuationObjects.get(id);
				loadStateTransitions(measuredEntity);
			}

			// loop through the measured entities and load machine data
			for( Integer id : this.configuationObjects.keySet()){
				MeasuredEntity measuredEntity = (MeasuredEntity) this.configuationObjects.get(id);
				if (measuredEntity.getType() == MeasuredEntityType.MACHINE){
					// load machine information
					loadMachineInformation((Machine)measuredEntity);
				}else if(measuredEntity.getType() == MeasuredEntityType.PLANT){
					// load plant information
					loadPlantInformation((Plant)measuredEntity);
				}
			}

			// loop through the measured entities and load scheduled events
			for( Integer id : this.configuationObjects.keySet()){
				MeasuredEntity measuredEntity = (MeasuredEntity) this.configuationObjects.get(id);
				loadScheduledEvents(measuredEntity);
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
	 * Reads information from the database and sets those data into the 
	 * Plant object given by parameter.
	 * 
	 * @param plant object to store read data.
	 * @see Plant
	 */
	private void loadPlantInformation(Plant plant) {

		try 
		{
			String sqlSelect = sqlPlantSelect + String.valueOf(plant.getId());  
			ResultSet rs = super.pst.executeQuery(sqlSelect);

			while (rs.next()) 
			{
				String company 		 = rs.getString("id_compania");
				String location      = rs.getString("id_sede");
				String plant_id 	 = rs.getString("id_planta");

				plant.setCannonicalCompany(company);
				plant.setCannonicalLocation(location);
				plant.setCannonicalPlant(plant_id);

				logger.debug("registering plant " + getCanonicalKey(company, location, plant_id, null, null));
				canonicalMapIndex.put(getCanonicalKey(company, location, plant_id, null, null) , plant.getId());
			}
			rs.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Reads behavior from the database and sets those data into the 
	 * MeasuredEntity object given by parameter.
	 * 
	 * @param entity object to store read data.
	 */
	public void loadBehaviors(MeasuredEntity entity)
	{
		try 
		{
			String sqlSelect = sqlSelect2 + String.valueOf(entity.getId());  
			ResultSet rs2 = super.pst.executeQuery(sqlSelect);

			while (rs2.next()) 
			{
				Integer id   			= rs2.getInt("id");  
				String name            = rs2.getString("name");
				String descr            = rs2.getString("descr");
				String behaviorText    = rs2.getString("behavior_text");

				entity.putBehavior(id, name, descr, behaviorText);
			}
			rs2.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Reads state behavior from the database and sets those data into the 
	 * MeasuredEntity object given by parameter.
	 * 
	 * @param entity object to store read data.
	 */
	public void loadStateBehaviors(MeasuredEntity entity)
	{
		try 
		{
			String sqlSelect = sqlSelect3 + String.valueOf(entity.getId());  
			ResultSet rs3 = super.pst.executeQuery(sqlSelect);

			while (rs3.next()) 
			{
				Integer id   		     = rs3.getInt("id");  
				String stateBehaviorType = rs3.getString("state_behavior_type");
				String descr             = rs3.getString("descr");
				String behaviorText      = rs3.getString("behavior_text");

				MeasuredEntityStateBehavior measuredEntityStateBehavior = new MeasuredEntityStateBehavior(id, stateBehaviorType);
				measuredEntityStateBehavior.setDescr(descr);
				measuredEntityStateBehavior.setBehaviorText(behaviorText);


				entity.putStateBehavior(measuredEntityStateBehavior);
			}

			rs3.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Reads state transitions from the database and sets those data into the 
	 * MeasuredEntity object given by parameter.
	 * 
	 * @param entity object to store read data.
	 */
	public void loadStateTransitions(MeasuredEntity entity)
	{
		try 
		{
			String sqlSelect = sqlSelect4 + String.valueOf(entity.getId());  
			ResultSet rs4 = super.pst.executeQuery(sqlSelect);

			while (rs4.next()) 
			{

				Integer id   		     = rs4.getInt("id");
				String  stateFromTxt     = rs4.getString("state_from");
				Integer reasonCodeFrom   = rs4.getInt("reason_code_id");
				Integer behavior 		 = rs4.getInt("behavior_id");
				Timestamp timestamp 	 = rs4.getTimestamp("create_date");

				MeasuringState stateFrom = MeasuringState.UNDEFINED;

				if (stateFromTxt.compareTo("O") == 0){
					stateFrom = MeasuringState.OPERATING;
				} else if (stateFromTxt.compareTo("S") == 0){
					stateFrom = MeasuringState.SCHEDULEDOWN;
				} else if (stateFromTxt.compareTo("U") == 0){
					stateFrom = MeasuringState.UNSCHEDULEDOWN;
				} else {
					stateFrom = MeasuringState.UNDEFINED;
				}

				entity.putStateTransition(id, stateFrom, reasonCodeFrom, behavior, timestamp.toLocalDateTime());
			}

			rs4.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Reads machine information from the database and sets those data into the 
	 * Machine object given by parameter.
	 * 
	 * @param machine to store read data.
	 */
	private void loadMachineInformation(Machine machine){
		try 
		{
			String sqlSelect = sqlMachineSelect + String.valueOf(machine.getId());  
			ResultSet rs4 = super.pst.executeQuery(sqlSelect);
			ResultSetMetaData rsmd = rs4.getMetaData();

			while (rs4.next()) 
			{
				String company 		= rs4.getString("id_compania");
				String location     = rs4.getString("id_sede");
				String plant   		= rs4.getString("id_planta");
				String machineGroup = rs4.getString("id_grupo_maquina");
				String machine_id 	= rs4.getString("id_maquina");

				machine.setCannonicalCompany(company);
				machine.setCannonicalLocation(location);
				machine.setCannonicalPlant(plant);
				machine.setCannonicalGroup(machineGroup);
				machine.setCannonicalMachineId(machine_id);

				String[] fixedFieldNames = {"measuredentity_ptr_id", "id_compania", "id_sede", "id_planta", "id_grupo_maquina", "id_maquina"};  

				logger.debug("registering machine " + getCanonicalKey(company, location, plant, machineGroup, machine_id) + " Id:" + Integer.toString(machine.getId()) );

				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					int type = rsmd.getColumnType(i);
					String name = rsmd.getColumnName(i);
					if (!(Arrays.asList(fixedFieldNames).contains(name))) {

						logger.debug("Registering attribute: " + name + " in machine:" + String.valueOf(machine.getId()) );

						switch (type) {

						case java.sql.Types.TINYINT:
						case java.sql.Types.SMALLINT:
						case java.sql.Types.INTEGER:
						{	
							Integer valueInteger = rs4.getInt(i);
							Attribute attrInt = new Attribute(name, AttributeType.INT);
							attrInt.setTrend(false);
							attrInt.setOrigin(AttributeOrigin.ERP);
							machine.registerAttribute(attrInt);
							AttributeValue valueAttrInt = new AttributeValue(name, attrInt, valueInteger, i, MeasuredEntityType.MACHINE );
							machine.registerAttributeValue(valueAttrInt);
							break;
						}

						case java.sql.Types.FLOAT:
						case java.sql.Types.REAL:
						{
							Double valueDouble = 0.0 + rs4.getFloat(i);
							Attribute attrFloat = new Attribute(name, AttributeType.DOUBLE);
							attrFloat.setTrend(false);
							attrFloat.setOrigin(AttributeOrigin.ERP);
							machine.registerAttribute(attrFloat);
							AttributeValue valueAttrFloat = new AttributeValue(name, attrFloat, valueDouble, i, MeasuredEntityType.MACHINE );
							machine.registerAttributeValue(valueAttrFloat);
							break;
						}	
						case java.sql.Types.DOUBLE:
						case java.sql.Types.NUMERIC:
						case java.sql.Types.DECIMAL:
						{
							Double valueDouble2 = rs4.getDouble(i);
							Attribute attrDouble = new Attribute(name, AttributeType.DOUBLE);
							attrDouble.setTrend(false);
							attrDouble.setOrigin(AttributeOrigin.ERP);
							machine.registerAttribute(attrDouble);
							AttributeValue valueAttrDouble = new AttributeValue(name, attrDouble, valueDouble2, i, MeasuredEntityType.MACHINE );
							machine.registerAttributeValue(valueAttrDouble);
							break;
						}

						case java.sql.Types.CHAR:
						case java.sql.Types.VARCHAR:
						case java.sql.Types.NCHAR:
						case java.sql.Types.NVARCHAR:
						{
							String valueString = rs4.getString(i);
							Attribute attrString = new Attribute(name, AttributeType.STRING);
							attrString.setTrend(false);
							attrString.setOrigin(AttributeOrigin.ERP);
							machine.registerAttribute(attrString);
							AttributeValue valueAttrString = new AttributeValue(name, attrString, valueString, i, MeasuredEntityType.MACHINE );
							machine.registerAttributeValue(valueAttrString);
							break;
						}

						case java.sql.Types.DATE:
						{
							Date valueDate = rs4.getDate(i);
							Attribute attrDate = new Attribute(name, AttributeType.DATE);
							attrDate.setTrend(false);
							attrDate.setOrigin(AttributeOrigin.ERP);
							machine.registerAttribute(attrDate);
							AttributeValue valueAttrDate = new AttributeValue(name, attrDate, valueDate.toLocalDate(), i, MeasuredEntityType.MACHINE );
							machine.registerAttributeValue(valueAttrDate);
							break;
						}
						case java.sql.Types.TIME:
						{
							Time valueTime = rs4.getTime(i);
							Attribute attrTime = new Attribute(name, AttributeType.TIME);
							attrTime.setTrend(false);
							attrTime.setOrigin(AttributeOrigin.ERP);
							machine.registerAttribute(attrTime);
							AttributeValue valueAttrTime = new AttributeValue(name, attrTime, valueTime.toLocalTime(), i, MeasuredEntityType.MACHINE );
							machine.registerAttributeValue(valueAttrTime);
							break;					    	
						}
						case java.sql.Types.TIMESTAMP:
						{
							Timestamp valueDateTime = rs4.getTimestamp(i);
							Attribute attrDateTime = new Attribute(name, AttributeType.DATETIME);
							attrDateTime.setTrend(false);
							attrDateTime.setOrigin(AttributeOrigin.ERP);
							machine.registerAttribute(attrDateTime);
							AttributeValue valueAttrDateTime = new AttributeValue(name, attrDateTime, valueDateTime.toLocalDateTime(), i, MeasuredEntityType.MACHINE );
							machine.registerAttributeValue(valueAttrDateTime);
							break;					    						    
						}

						case java.sql.Types.BINARY:
						case java.sql.Types.BOOLEAN:
						{
							Boolean valueBool = rs4.getBoolean(i);
							Attribute attrBool = new Attribute(name, AttributeType.BOOLEAN);
							attrBool.setTrend(false);
							attrBool.setOrigin(AttributeOrigin.ERP);
							machine.registerAttribute(attrBool);
							AttributeValue valueAttrBool = new AttributeValue(name, attrBool, valueBool, i, MeasuredEntityType.MACHINE );
							machine.registerAttributeValue(valueAttrBool);
							break;					    						    					    	
						}

						case java.sql.Types.ROWID:
							// This is the id of the production in the database, which is the id of the object. 
							// So we don't have to do anything.
							break;

						case java.sql.Types.BIT:
						case java.sql.Types.BIGINT:
						case java.sql.Types.LONGVARCHAR:
						case java.sql.Types.VARBINARY:
						case java.sql.Types.LONGVARBINARY:
						case java.sql.Types.NULL:
						case java.sql.Types.OTHER:
						case java.sql.Types.JAVA_OBJECT:
						case java.sql.Types.DISTINCT:
						case java.sql.Types.STRUCT:
						case java.sql.Types.ARRAY:
						case java.sql.Types.BLOB:
						case java.sql.Types.CLOB:
						case java.sql.Types.REF:
						case java.sql.Types.DATALINK:
						case java.sql.Types.LONGNVARCHAR:
						case java.sql.Types.NCLOB:
						case java.sql.Types.SQLXML:
							logger.error("Type is not translatable to attribute");
							break;
						}

					}
				}

				canonicalMapIndex.put(getCanonicalKey(company, location, plant, machineGroup,  machine_id) , machine.getId());
			}

			rs4.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Reads scheduled events from the database and sets those data into the 
	 * MeasuredEntity object given by parameter.
	 * @param entity to store read data.
	 */
	public void loadScheduledEvents(MeasuredEntity entity){

		try 
		{

			super.connect();
			logger.debug("in get Scheduled Events:" );

			String sqlSelect = sqlSelect6 + String.valueOf(entity.getId());
			logger.debug("sqlSelect machines :" + sqlSelect);
			ResultSet rs6 = super.pst.executeQuery(sqlSelect);

			while (rs6.next()) 
			{

				Integer id 					= rs6.getInt("id");
				String scheduleEventType   	= rs6.getString("scheduled_event_type");
				String descr  				= rs6.getString("descr");  
				String recurrences        	= rs6.getString("recurrences");
				Time day_time				= rs6.getTime("day_time");
				Timestamp createDate 		= rs6.getTimestamp("create_date");

				MeasuredEntityScheduledEvent measuredEvent = new MeasuredEntityScheduledEvent(id, scheduleEventType );
				measuredEvent.setDescr(descr);
				measuredEvent.setRecurrence(recurrences);
				measuredEvent.setDayTime(day_time.toLocalTime());
				measuredEvent.setCreateDate(createDate.toLocalDateTime());

				entity.putScheduledEvent(measuredEvent);

			}

			rs6.close();

			super.disconnect();

		} catch (ClassNotFoundException e){
			String error = "Could not find the driver class - Error" + e.getMessage(); 
			logger.error(error);
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Deserializes a JSON string and builds a MeasuredEntity.
	 * 
	 * @param json String representation of a MeasuredEntity in JSON format.
	 * @return the MeasuredEntity object from the JSON string given by parameter
	 */
	public MeasuredEntity fromJSON(String json) {

		ObjectMapper mapper = new ObjectMapper();

		//Convert object to JSON string and pretty print
		MeasuredEntity measuredEntity;
		try {

			measuredEntity = mapper.readValue(json, MeasuredEntity.class);

			MeasuredEntity oldMeasuredEntity = (MeasuredEntity) this.configuationObjects.get(measuredEntity.getId());

			if (oldMeasuredEntity == null){
				this.configuationObjects.put(measuredEntity.getId(), measuredEntity);
				return measuredEntity;

			} else { 
				//Update a measured Entity
				oldMeasuredEntity.updateEntityConfiguration(measuredEntity);
				return oldMeasuredEntity;
			}

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

		return null;
	}	

	/**
	 * Reads Modbus Events from the configuration database, then creates those 
	 * ModbusEvents objects and include them into a list.
	 * 
	 * @return a list of ModbusEvents read from the database.
	 * @throws SQLException if there is an error with the SQL driver.
	 * @see {@link ModBusTcpEvent}
	 */
	public List<ModBusTcpEvent> getModBusEvents( ) throws SQLException {

		List<ModBusTcpEvent> events = new ArrayList<ModBusTcpEvent>();

		try 
		{

			super.connect();
			logger.info("in getModbusEvents:" );

			String sqlSelect = sqlSelect5;  
			ResultSet rs5 = super.pst.executeQuery(sqlSelect);

			while (rs5.next()) 
			{
				String ipaddress        	= rs5.getString("ip_address");
				Integer measured_entity_id  = rs5.getInt("measured_entity_id");  
				String portLabel        	= rs5.getString("port_label");
				Integer refreshTimeMs       = rs5.getInt("refresh_time_ms");

				if (refreshTimeMs > 0){
					ModBusTcpEvent modBusEvent = ModBusTcpEvent.createModbusEvent(ipaddress, measured_entity_id, portLabel, refreshTimeMs);
					if (modBusEvent != null)
						events.add(modBusEvent);
				} else {
					logger.error("Refresh time is zero for Port label:" + portLabel + " which is invalid");
				}
			}
			rs5.close();

			super.disconnect();

		} catch (ClassNotFoundException e){
			String error = "Could not find the driver class - Error" + e.getMessage(); 
			logger.error(error);
			e.printStackTrace();
			throw new SQLException(error);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return events;
	}

	/**
	 * Returns the object identifier from the given parameters.
	 * 
	 * @param company Identifier of the company
	 * @param location Identifier of the location
	 * @param plant Identifier of the plant
	 * @param machineGroup Identifier of the group of machines.
	 * @param machineId Identifier of the machine
	 * @return the object identifier from the given parameters, or NULL if the object does not exist.
	 */
	public Integer getCanonicalObject(String company, String location, String plant, String machineGroup, String machineId) 
	{
		logger.info("Number of measuredEntities registered:" + Integer.toString(this.canonicalMapIndex.size()));
		return this.canonicalMapIndex.get(getCanonicalKey(company, location, plant, machineGroup, machineId));
	}

	/**
	 * This method counts the number of ports that uses MQTT protocol.
	 * If the number of ports that uses MQTT is greater than zero returns TRUE.
	 * @return TRUE if there are ports that uses MQTT protocol, FALSE otherwise.
	 */
	public boolean requireMQTT() {

		boolean ret = false;
		try 
		{
			super.connect();

			logger.info("in requireMQTT:");

			String sqlSelect = sqlSelect7;  
			ResultSet rs7 = super.pst.executeQuery(sqlSelect);

			while (rs7.next()) 
			{
				// The number of ports configured with MQTT is greater than 0.
				Integer count  = rs7.getInt(1);
				if (count > 0){
					ret = true;
					logger.info("MQTT is required");
				}
			}
			rs7.close();

			super.disconnect();

		} catch (ClassNotFoundException e){
			String error = "Could not find the driver class - Error" + e.getMessage(); 
			logger.error(error);
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * Removes an object given its identifier.
	 * @return TRUE if the object existed and was removed, 
	 * FALSE if the object did not exist.
	 */
	public boolean removeObject(Integer uid){
		Map.Entry<String,Integer> pairToDelete = null;
		Iterator<Map.Entry<String,Integer>> it = this.canonicalMapIndex.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String,Integer> pair = it.next();
			if (uid.equals( (Integer) pair.getValue())){
				pairToDelete = pair;
				break;
			}
		}
		if (pairToDelete != null){
			return this.canonicalMapIndex.remove(pairToDelete.getKey(), pairToDelete.getValue());
		}
		return false;
	}
}
