package com.advicetec.measuredentitity;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.advicetec.configuration.Container;
import com.advicetec.configuration.DeviceType;
import com.advicetec.configuration.IOSignalDeviceType;
import com.advicetec.configuration.Signal;
import com.advicetec.configuration.SignalUnitContainer;
import com.advicetec.eventprocessor.ModBusTcpDiscreteDataInputEvent;
import com.advicetec.eventprocessor.ModBusTcpDiscreteDataOutputEvent;
import com.advicetec.eventprocessor.ModBusTcpEvent;
import com.advicetec.eventprocessor.ModBusTcpEventType;
import com.advicetec.eventprocessor.ModBusTcpInputRegisterEvent;
import com.advicetec.eventprocessor.ModBusTcpReadHoldingRegisterEvent;
import com.advicetec.utils.ModBusUtils;

public class MeasuredEntityContainer extends Container 
{

	static Logger logger = LogManager.getLogger(MeasuredEntityContainer.class.getName());

	static String sqlSelect1 = "SELECT id, code, descr, create_date, type FROM setup_measuredentity";
	static String sqlSelect2 = "SELECT id, name, descr, behavior_text, create_date, last_updttm, measure_entity_id FROM setup_measuredentitybehavior WHERE measure_entity_id = ";
	static String sqlSelect3 = "SELECT id, state_behavior_type, descr, behavior_text, create_date, last_updttm from setup_measuredentitystatebehavior WHERE measure_entity_id = ";
	static String sqlSelect4 = "SELECT id, state_from, behavior_id, measure_entity_id, reason_code_id, create_date, last_updttm FROM setup_measuredentitytransitionstate WHERE measure_entity_id = ";
	static String sqlSelect5 = "SELECT d.ip_address, c.measured_entity_id, c.port_label, c.refresh_time_ms from setup_signal a, setup_signaltype b, setup_inputoutputport c, setup_monitoringdevice d where b.protocol = 'M' and a.type_id = b.id and c.signal_type_id = a.id and d.id = c.device_id";

	static String sqlMachineSelect = "SELECT measuredentity_ptr_id, id_compania, id_sede, id_planta, id_grupo_maquina, id_maquina FROM setup_machinehostsystem WHERE measuredentity_ptr_id =";
	static String sqlPlantSelect = "SELECT measuredentity_ptr_id, id_compania, id_sede, id_planta FROM setup_planthostsystem WHERE measuredentity_ptr_id =";

	private Map<String, Integer> canonicalMapIndex;

	public MeasuredEntityContainer(String driver, String server, String user, String password) {
		super(driver, server, user, password);
		
		canonicalMapIndex = new HashMap<String, Integer>();
	}

	private String getCanonicalKey(String company, String location, String plant, String machineGroup, String machineId)
	{
		if (machineId != null)
			return company + "-" + location + "-" + plant + "-" + machineGroup + "-" + machineId;
		else
			return company + "-" + location + "-" + plant;
	}
	
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

				logger.info("measured Entity id:" + id.toString() +  " Entity Category:" + entityCategory);
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

	private void loadPlantInformation(Plant plant) {
		
		try 
		{
			String sqlSelect = sqlMachineSelect + String.valueOf(plant.getId());  
			ResultSet rs = super.pst.executeQuery(sqlSelect);

			while (rs.next()) 
			{
				String company 		= rs.getString("id_compania");
				String location     = rs.getString("id_sede");
				String plant_id 	= rs.getString("id_planta");
				String machineGroup =  rs.getString("id_grupo_maquina");
				plant.setCannonicalCompany(company);
				plant.setCannonicalLocation(location);
				plant.setCannonicalPlant(plant_id);
				
				logger.info("registering plant " + getCanonicalKey(company, location, plant_id, null, null));
				canonicalMapIndex.put(getCanonicalKey(company, location, plant_id, null, null) , plant.getId());
			}

			rs.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

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

				entity.putStateBehavior(id, stateBehaviorType, descr, behaviorText);
			}

			rs3.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

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
	
	
	private void loadMachineInformation(Machine machine){
		try 
		{
			String sqlSelect = sqlMachineSelect + String.valueOf(machine.getId());  
			ResultSet rs4 = super.pst.executeQuery(sqlSelect);

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
				
				logger.info("registering machine " + getCanonicalKey(company, location, plant, machineGroup, machine_id) + " Id:" + Integer.toString(machine.getId()) );
				canonicalMapIndex.put(getCanonicalKey(company, location, plant, machineGroup,  machine_id) , machine.getId());
			}

			rs4.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public List<ModBusTcpEvent> getModBusEvents( int port ) throws SQLException {

		List<ModBusTcpEvent> events = new ArrayList<ModBusTcpEvent>();

		try 
		{

			super.connect();

			logger.info("in getModbusEvents:" + port );
			
			String sqlSelect = sqlSelect5;  
			ResultSet rs5 = super.pst.executeQuery(sqlSelect);

			while (rs5.next()) 
			{
				String ipaddress        	= rs5.getString("ip_address");
				Integer measured_entity_id  = rs5.getInt("measured_entity_id");  
				String portLabel        	= rs5.getString("port_label");
				Integer refreshTimeMs       = rs5.getInt("refresh_time_ms");

				if (refreshTimeMs > 0){
				
					if (ModBusUtils.isPortLabelValid(portLabel) == false){
						logger.error("Port label" + portLabel + " is invalid");
					} else {
						ModBusTcpEventType type = ModBusUtils.getModBusType(portLabel);
						Integer unitId = ModBusUtils.getUnitId(portLabel);
						Integer offset = ModBusUtils.getOffset(portLabel);
						Integer count = ModBusUtils.getCount(portLabel);
	
						switch (type){
						case READ_DISCRETE:
							ModBusTcpDiscreteDataInputEvent evnt1 = new ModBusTcpDiscreteDataInputEvent(ipaddress, port,
									unitId, offset, count, true);
							// TODO: Establish where to put this parameter.
							evnt1.setMilliseconds(refreshTimeMs); 
							events.add(evnt1);
							break;
						case READ_REGISTER:
							ModBusTcpInputRegisterEvent evnt2 = new ModBusTcpInputRegisterEvent(ipaddress, port,
									unitId, offset, count, true);
							evnt2.setMilliseconds(refreshTimeMs); 
							events.add(evnt2);
							break;
						case WRITE_DISCRETE:
							ModBusTcpDiscreteDataOutputEvent evnt3 = new ModBusTcpDiscreteDataOutputEvent(ipaddress, port,
									unitId, offset, count, true);
							// TODO: Establish where to put this parameter.
							evnt3.setMilliseconds(refreshTimeMs); 
							events.add(evnt3);
							break;
						case WRITE_REGISTER:
							ModBusTcpReadHoldingRegisterEvent evnt4 = new ModBusTcpReadHoldingRegisterEvent(ipaddress, port,
									unitId, offset, count, true);
							evnt4.setMilliseconds(refreshTimeMs); 
							events.add(evnt4);
							break;
						case READ_HOLDING_REGISTER:
							ModBusTcpReadHoldingRegisterEvent evnt5 = new ModBusTcpReadHoldingRegisterEvent(ipaddress, port,
									unitId, offset, count, true);
							evnt5.setMilliseconds(refreshTimeMs); 
							events.add(evnt5);
							break;
						case INVALID:
							logger.error("The type of action in the port is invalid - Port label:" + portLabel);
							break;
						}
					}
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

	public Integer getCanonicalObject(String company, String location, String plant, String machineGroup, String machineId) 
	{
		
		logger.info("Number of measuredEntities registered:" + Integer.toString(this.canonicalMapIndex.size()));
		return this.canonicalMapIndex.get(getCanonicalKey(company, location, plant, machineGroup, machineId));
	}
}
