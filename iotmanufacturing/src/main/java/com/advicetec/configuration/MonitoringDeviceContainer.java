package com.advicetec.configuration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.advicetec.eventprocessor.ModBusTcpEvent;
import com.advicetec.measuredentitity.Machine;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.Plant;

public class MonitoringDeviceContainer extends Container
{

	static Logger logger = LogManager.getLogger(MonitoringDeviceContainer.class.getName());
	
	/**
	 * SQL statement to select configured data of monitoring devices. 
	 */
	static String sqlSelect1 = "SELECT id, device_type_id, type, descr, ip_address, mac_address, serial, create_date FROM setup_monitoringdevice";
	
	/**
	 * SQL statement to select configured data of modbus input output port related to monitoring devices. 
	 */
	static String sqlSelect2_modbus = " SELECT a.id, a.transformation_text, a.signal_type_id, a.port_label, " +
									  " a.refresh_time_ms,  a.measured_entity_id, b.device_id, b.port, b.unit_id, " +
									  " b.offset, b.nbr_read, b.object_type, b.access " +
									  " FROM setup_inputoutputport a, setup_modbusinputoutputport b " +
									  " WHERE a.id = b.inputoutputport_ptr_id";
	
	/**
	 * SQL statement to select configured data of mqtt input output port related to monitoring devices. 
	 */
	static String sqlSelect2_mqtt = " SELECT a.id, a.transformation_text, a.signal_type_id, a.port_label, " + 
									"		 a.refresh_time_ms,  a.measured_entity_id, b.device_id, b.topic_name " + 
									" FROM setup_inputoutputport a, setup_mqttinputoutputport b " + 
									" WHERE a.id = b.inputoutputport_ptr_id";		
	
	/**
	 * SQL statement to select modbus ports that must create new events. 
	 */
	static String sqlSelect3 = "SELECT d.ip_address, c.measured_entity_id, c.port_label, c.refresh_time_ms " +
			                   " from setup_signal a, setup_signaltype b, setup_inputoutputport c, setup_monitoringdevice d " +
			                   " where b.protocol = 'M' and a.type_id = b.id and c.signal_type_id = a.id and d.id = c.device_id and d.id = ";

	static String sqlSelect4 = "SELECT is_concentrator, port, unit_id, \"offset\", \"access\", nbr_read, object_type, refresh_time_ms " + 
							   " from setup_modbusmonitoringdevice " + 
							   " where monitoringdevice_ptr_id = ";
	
	static String sqlSelect5 = "SELECT port " + 
							   " from setup_mqttmonitoringdevice " + 
							   " where monitoringdevice_ptr_id = ";

	static String sqlSelect6 = "select object_type, \"access\", nbr_read, \"offset\", port, unit_id" + 
				" from setup_modbusinputoutputport " + 
				" where inputoutputport_ptr_id = ";

	static String sqlSelect7 = "SELECT topic_name " + 
			   " from setup_mqttinputoutputport " + 
			   " where inputoutputport_ptr_id = ";

	/**
	 * Maps to make faster lookups by macaddresses. Given the mac address, it returns the identifier of the measuring device configured with that address.
	 */
	private Map<String, Integer> indexByMac;
	
	/**
	 * Maps to make faster lookups by Ip address. Given the Ip address, it returns the identifier of the measuring device configured with that address.
	 */
	private Map<String, Integer> indexByIpAddress;
	
	/**
	 * Maps to make faster lookups by serial. Given the serial, it returns the identifier of the measuring device configured with that serial.
	 */
	private Map<String, Integer> indexBySerial;

	
	/**
	 * Constructor for the class, it takes as parameters data required to connect to the database.
	 * 
	 * @param driver		: driver string used to connect to the database.
	 * @param server		: Ip address of the database server
	 * @param user			: database user
	 * @param password		: password of the user's database.
	 */
	public MonitoringDeviceContainer(String driver, String server, String user, String password) 
	{	
		super(driver, server, user, password);
		indexByMac = new HashMap<String, Integer>();
		indexByIpAddress = new HashMap<String, Integer>();
		indexBySerial = new HashMap<String, Integer>();
	}
	
	/**
	 * Loads all measuring devices registered in the database into the container.
	 * 
	 * @throws SQLException
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
				Integer id     				= rs1.getInt("id");
		        String descr   				= rs1.getString("descr");
		        Integer deviceTypeId    	= rs1.getInt("device_type_id");
		        String monitoringDeviceType = rs1.getString("type");
		        String ipAddress   			= rs1.getString("ip_address");
		        String macAddress 			= rs1.getString("mac_address");
		        String serial	 			= rs1.getString("serial");
		        Timestamp timestamp 		= rs1.getTimestamp("create_date");
		        		        
		        DeviceType deviceType = (DeviceType) this.getReferencedObject("DeviceType", deviceTypeId);
		        MonitoringDeviceType monitoringDType = MonitoringDeviceType.from(Integer.parseInt(monitoringDeviceType));
		        
		        MonitoringDevice object = null;
		        
		        switch (monitoringDType)
		        {
		        	case MODBUS:
		        		object = new ModbusMonitoringDevice(id);
		        		break;
		        	case MQTT:
		        		object = new MqttMonitoringDevice(id);
		        		break;
		        	case INVALID:
		        		logger.error("Invalid monitoring device type - value:" + monitoringDType.getValue() );
		        		break;
		        }
		        
		        if (object != null) {
			        object.setDescr(descr);
			        object.setIp_address(ipAddress);
			        object.setMac_addres(macAddress);
			        object.setSerial(serial);
			        object.setType(deviceType);
			        object.setCreate_date(timestamp.toLocalDateTime());
			        object.setMonitoringDeviceType(monitoringDType);
			        
			        super.configuationObjects.put(id, object);
			        
			        if (macAddress != null){
				        if (!macAddress.isEmpty())
				        	indexByMac.put(macAddress,id);
			        }
			        
			        if (ipAddress != null){
				        if (!ipAddress.isEmpty())
				        	indexByIpAddress.put(ipAddress, id);
			        }
			        
			        if (serial != null){
				        if (!serial.isEmpty())
				        	indexBySerial.put(serial,id);
			        }
		        }
			}
			
			rs1.close();
			
			// loop through the measuring devices and load their data
			for( Integer id : this.configuationObjects.keySet()){
				MonitoringDevice measuringDevice = (MonitoringDevice) this.configuationObjects.get(id);
				if (measuringDevice.getMonitoringDeviceType() == MonitoringDeviceType.MODBUS){
					// load modbus measuring device information
					loadModBusMonitoringDevice((ModbusMonitoringDevice) measuringDevice);
				} else if (measuringDevice.getMonitoringDeviceType() == MonitoringDeviceType.MQTT){
					// load Mqtt measuring device information
					loadMqttMonitoringDevice((MqttMonitoringDevice) measuringDevice);
				} else {
					logger.error("Measuring device: " + Integer.toString(id) + "has not a valid measuring device type");
				}				
			}
		
			
			ResultSet rs2 = super.pst.executeQuery(sqlSelect2_modbus);
			while (rs2.next())
			{		     

				Integer id     			= rs2.getInt("id"); 
		        String transformation	= rs2.getString("transformation_text");
		        Integer signalTypeId	= rs2.getInt("signal_type_id");
		        String portLabel		= rs2.getString("port_label");
		        Integer refreshTimeMs   = rs2.getInt("refresh_time_ms");
		        Integer measuredEntityId = rs2.getInt("measured_entity_id");
		        Integer deviceId    	= rs2.getInt("device_id");
		        Integer port			= rs2.getInt("port");
		        Integer unitId			= rs2.getInt("unit_id");
		        Integer offset			= rs2.getInt("offset");
		        Integer nbrRead			= rs2.getInt("nbr_read");
		        String objectType		= rs2.getString("object_type");
		        String access			= rs2.getString("access");
		          
		        ModbusMonitoringDevice device = (ModbusMonitoringDevice) this.getObject(deviceId);
		        Signal signal = (Signal) this.getReferencedObject("Signal", signalTypeId);
		        
		        ModbusInputOutputPort inputOutputPort = new ModbusInputOutputPort(id);		        
		        inputOutputPort.setSignalType(signal);
		        inputOutputPort.setTransformationText(transformation);
		        inputOutputPort.setPortLabel(portLabel);
		        inputOutputPort.setMeasuringEntity(measuredEntityId);
		        inputOutputPort.setRefreshTimeMs(refreshTimeMs);
		        inputOutputPort.setPort(port);
		        inputOutputPort.setUnit_id(unitId);
		        inputOutputPort.setOffset(offset);
		        inputOutputPort.setNbr_read(nbrRead);
		        
		        if ((objectType.isEmpty()) || (objectType == null)) {
		        	inputOutputPort.setObjectType(ModbusObjectType.INVALID);
		        } else {
		        	inputOutputPort.setObjectType(ModbusObjectType.from(Integer.parseInt(objectType)));
		        }
		        
		        if ((access.isEmpty()) || (access == null)) {
		        	inputOutputPort.setAccess(ModbusAccess.INVALID);
		        } else {
		        	inputOutputPort.setAccess(ModbusAccess.from(Integer.parseInt(access)));
		        }

		        device.putInputOutputPort(inputOutputPort);
			}
			
			rs2.close();

			
			rs2 = super.pst.executeQuery(sqlSelect2_mqtt);
			while (rs2.next())
			{		     

				Integer id     			= rs2.getInt("id"); 
		        String transformation	= rs2.getString("transformation_text");
		        Integer signalTypeId	= rs2.getInt("signal_type_id");
		        String portLabel		= rs2.getString("port_label");
		        Integer refreshTimeMs   = rs2.getInt("refresh_time_ms");
		        Integer measuredEntityId = rs2.getInt("measured_entity_id");
		        Integer deviceId    	= rs2.getInt("device_id");
		        String topicName		= rs2.getString("topic_name");
		          
		        MqttMonitoringDevice device = (MqttMonitoringDevice) this.getObject(deviceId);
		        Signal signal = (Signal) this.getReferencedObject("Signal", signalTypeId);
		        
		        MqttInputOutputPort inputOutputPort = new MqttInputOutputPort(id);		        
		        inputOutputPort.setSignalType(signal);
		        inputOutputPort.setTransformationText(transformation);
		        inputOutputPort.setPortLabel(portLabel);
		        inputOutputPort.setMeasuringEntity(measuredEntityId);
		        inputOutputPort.setRefreshTimeMs(refreshTimeMs);
		        inputOutputPort.setTopicName(topicName);
		        
		        device.putInputOutputPort(inputOutputPort);
			}
			
			rs2.close();
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
	 * Inserts the monitoring device given as parameter in the container.
	 * 
	 * If the monitoring device already exists, then replace it.
	 * 
	 * @param mDevice  Monitoring device to add.
	 */
	public synchronized void addMonitoringDevice(MonitoringDevice mDevice) {
		
		// Adds the monitoring device monitoring device container
		super.configuationObjects.put(mDevice.getId(), mDevice);

        // Update indexes created to references the monitoring device
		if (mDevice.getMac_addres() != null){
	        if (!mDevice.getMac_addres().isEmpty())
	        	indexByMac.put(mDevice.getMac_addres(), mDevice.getId());
        }
        
        if (mDevice.getIp_address() != null){
	        if (!mDevice.getIp_address().isEmpty())
	        	indexByIpAddress.put(mDevice.getIp_address(), mDevice.getId());
        }
        
        if (mDevice.getSerial() != null){
	        if (!mDevice.getSerial().isEmpty())
	        	indexBySerial.put(mDevice.getSerial(),mDevice.getId());
        }

	}

	/**
	 * Delete a monitoring device from the container
	 * 
	 * @param uniqueID  monitoring device identifier to remove.
	 */
	public synchronized void deleteMonitoringDevice(int uniqueID)
	{
		super.configuationObjects.remove(uniqueID);
	}
	
	
	/**
	 * Obtains a measuring device configured with the parameter mac address.  
	 * 
	 * @param macAddress  mac address to find.
	 * 
	 * @return measuring device object. If the macaddress is inexistent, the null is returned.
	 */
	public synchronized MonitoringDevice getByMacAddress(String macAddress)
	{
		Integer id = this.indexByMac.get(macAddress);
		if (id != null)
			return (MonitoringDevice) super.getObject(id);
		else
			return null;
		
	}
	
	/**
	 * Obtains a measuring device configured with the parameter IP address.  
	 * 
	 * @param ipAddress  IP address to find.
	 * 
	 * @return measuring device object. If the ipaddress is inexistent, the null is returned.
	 */
	public synchronized MonitoringDevice getByIpAddress(String ipAddress)
	{
		logger.debug("serach by ipaddress:" + ipAddress);
		Integer id = this.indexByIpAddress.get(ipAddress);
		if (id != null)
			return (MonitoringDevice) super.getObject(id);
		else
			return null;
		
	}

	/**
	 * Obtains a measuring device configured with the serial parameter.  
	 * 
	 * @param serial  serial to find.
	 * 
	 * @return measuring device object. If the serial is inexistent, the null is returned.
	 */
	public synchronized MonitoringDevice getBySerial(String serial)
	{
		logger.debug("search by serial:" + serial);
		Integer id = this.indexBySerial.get(serial);
		if (id != null)
			return (MonitoringDevice) super.getObject(id);
		else
			return null;
		
	}

	/**
	 * Builds a Measuring device from a json representation
	 * 
	 * @param json  json object representing the measuring device. 
	 * 
	 */
	public synchronized ModbusMonitoringDevice FromJSONModbus(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		// Convert object to JSON string and pretty print
		ModbusMonitoringDevice mDeviceTemp = null;
		try 
		{
		
			mDeviceTemp = mapper.readValue(json, ModbusMonitoringDevice.class);
			
			logger.debug("Json object read:" + mDeviceTemp.toJson());
			logger.debug("Monitoring device id given:" + mDeviceTemp.getId()  );
			logger.debug("Monitoring address given:" + mDeviceTemp.getIp_address()  );
			
			DeviceTypeContainer deviceTypeContainer = (DeviceTypeContainer) this.getReferenceContainer("DeviceType");			
			DeviceType deviceTypeTmp = (DeviceType) deviceTypeContainer.getObject(mDeviceTemp.getType().getId());
			
			if (deviceTypeTmp != null) {
				mDeviceTemp.setType(deviceTypeTmp);
			} else {
				deviceTypeContainer.fromJSON(mDeviceTemp.getType().toJson());
			}
			
			SignalContainer signalContainer = (SignalContainer) this.getReferenceContainer("Signal");
			for (int i=0; i < mDeviceTemp.inputOutputPorts.size(); i++){
				ModbusInputOutputPort inputOutputPort = (ModbusInputOutputPort) mDeviceTemp.inputOutputPorts.get(i);
				Signal signal = (Signal) signalContainer.getObject(inputOutputPort.getSignalType().getId());
				
				if (signal == null){
					signalContainer.fromJSON(inputOutputPort.getSignalType().toJson());
				} else { 
					inputOutputPort.setSignalType(signal);
				}
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
		
		return mDeviceTemp;
	}
	
	/**
	 * Builds a Mqtt Measuring device from a json representation
	 * 
	 * @param json  json object representing the measuring device. 
	 * 
	 */
	public synchronized MqttMonitoringDevice fromJSONMqtt(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		// Convert object to JSON string and pretty print
		MqttMonitoringDevice mDeviceTemp = null;
		try {
		
			mDeviceTemp = mapper.readValue(json, MqttMonitoringDevice.class);
			
			logger.debug("Json object read:" + mDeviceTemp.toJson());
			logger.debug("Monitoring device id given:" + mDeviceTemp.getId());
			logger.debug("Monitoring address given:" + mDeviceTemp.getIp_address());
			
			DeviceTypeContainer deviceTypeContainer = (DeviceTypeContainer) this.getReferenceContainer("DeviceType");			
			DeviceType deviceTypeTmp = (DeviceType) deviceTypeContainer.getObject(mDeviceTemp.getType().getId());
			
			if (deviceTypeTmp != null) {
				mDeviceTemp.setType(deviceTypeTmp);
			} else {
				deviceTypeContainer.fromJSON(mDeviceTemp.getType().toJson());
			}
			
			SignalContainer signalContainer = (SignalContainer) this.getReferenceContainer("Signal");
			for (int i=0; i < mDeviceTemp.inputOutputPorts.size(); i++){
				MqttInputOutputPort inputOutputPort = (MqttInputOutputPort) mDeviceTemp.inputOutputPorts.get(i);
				Signal signal = (Signal) signalContainer.getObject(inputOutputPort.getSignalType().getId());
				
				if (signal == null){
					signalContainer.fromJSON(inputOutputPort.getSignalType().toJson());
				} else { 
					inputOutputPort.setSignalType(signal);
				}
			}

			mDeviceTemp.updateIndexes();

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
		
		return mDeviceTemp;
	}

	/**
	 * Reads information from the database and sets that data into the 
	 * ModBus Monitoring Device object given by parameter.
	 * 
	 * @param Monitoring Device to store read data.
	 * @see Mod Bus Monitoring Device
	 */
	private void loadModBusMonitoringDevice(ModbusMonitoringDevice modbusDevice) {

		try 
		{
			String sqlSelect = sqlSelect4 + String.valueOf(modbusDevice.getId());  
			ResultSet rs = super.pst.executeQuery(sqlSelect);

			while (rs.next()) 
			{
				
				boolean isConcentrator 	= rs.getBoolean("is_concentrator");
				int port      			= rs.getInt("port");
				int unit_id 			= rs.getInt("unit_id");				
				int offset 				= rs.getInt("offset");
				String access 			= rs.getString("access");				
				int nbrRead 			= rs.getInt("nbr_read");
				String objectType 		= rs.getString("object_type");
				int refreshTimeMs  		= rs.getInt("refresh_time_ms");

				modbusDevice.setConcentrator(isConcentrator);
				modbusDevice.setPort(port);
				modbusDevice.setUnit_id(unit_id);
				modbusDevice.setOffset(offset);
				modbusDevice.setNbr_read(nbrRead);
				modbusDevice.setRefreshTime(refreshTimeMs);
				
				if ((access != null) && (access.compareTo("") != 0))
					modbusDevice.setAccess(ModbusAccess.from(Integer.parseInt(access)));
				else
					modbusDevice.setAccess(ModbusAccess.INVALID);
				
				if ((objectType != null) && (objectType.compareTo("") != 0))
					modbusDevice.setObject_type(ModbusObjectType.from(Integer.parseInt(objectType)));
				else
					modbusDevice.setObject_type(ModbusObjectType.INVALID);

			}
			
			rs.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}


	/**
	 * Reads information from the database and sets that data into the 
	 * Mqtt Monitoring Device object given by parameter.
	 * 
	 * @param Mqtt Monitoring Device to store read data.
	 * @see Mqtt Monitoring Device
	 */
	private void loadMqttMonitoringDevice(MqttMonitoringDevice mqttDevice) {

		try 
		{
			String sqlSelect = sqlSelect5 + String.valueOf(mqttDevice.getId());  
			ResultSet rs = super.pst.executeQuery(sqlSelect);

			while (rs.next()) 
			{
				
				int port      			= rs.getInt("port");

				mqttDevice.setPort(port);
				
			}
			rs.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Reads information from the database and sets that data into the 
	 * Modbus Input Output Port object given by parameter.
	 * 
	 * @param Modbus Input Output Port to store read data.
	 * @see Modbus Input Output Port
	 */
	private void loadModbusInputOutputPort(ModbusInputOutputPort modbusInputOutputPort) {

		try 
		{
			String sqlSelect = sqlSelect6 + String.valueOf(modbusInputOutputPort.getId());  
			ResultSet rs = super.pst.executeQuery(sqlSelect);

			while (rs.next()) 
			{
				
				int port      			= rs.getInt("port");
				int unitId 				= rs.getInt("unit_id");				
				int offset 				= rs.getInt("offset");
				String access 			= rs.getString("access");				
				int nbrRead 			= rs.getInt("nbr_read");
				String objectType 		= rs.getString("object_type");

				modbusInputOutputPort.setPort(port);
				modbusInputOutputPort.setUnit_id(unitId);
				modbusInputOutputPort.setOffset(offset);
				modbusInputOutputPort.setNbr_read(nbrRead);
				
				if ((objectType != null) && (objectType.compareTo("") != 0))
					modbusInputOutputPort.setObjectType(ModbusObjectType.from(Integer.parseInt(objectType)));
				else
					modbusInputOutputPort.setObjectType(ModbusObjectType.INVALID);
				
				if ((access != null) && (access.compareTo("") != 0))
					modbusInputOutputPort.setAccess(ModbusAccess.from(Integer.parseInt(access)));
				else
					modbusInputOutputPort.setAccess(ModbusAccess.INVALID);
			}
			
			rs.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}


	/**
	 * Reads information from the database and sets that data into the 
	 * Mqtt Input Output Port object given by parameter.
	 * 
	 * @param Mqtt Input Output Port to store read data.
	 * @see Mqtt Input Output Port
	 */
	private void loadMqttInputOutputPort(MqttInputOutputPort mqttInputOutputPort) {

		try 
		{
			String sqlSelect = sqlSelect7 + String.valueOf(mqttInputOutputPort.getId());  
			ResultSet rs = super.pst.executeQuery(sqlSelect);

			while (rs.next()) 
			{
				
				String topicName	= rs.getString("topic_name");
				mqttInputOutputPort.setTopicName(topicName);
				
			}
			
			rs.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	/*
	 public List<ModBusTcpEvent> getModbusEvents(MonitoringDevice monitoring) throws SQLException {
		
		List<ModBusTcpEvent> events = new ArrayList<ModBusTcpEvent>();

		try 
		{
			super.connect();
			logger.debug("in getModbusEvents by monitoring device - id:" + monitoring.getId() );
			
			String sqlSelect = sqlSelect3 + String.valueOf(monitoring.getId());  
			ResultSet rs3 = super.pst.executeQuery(sqlSelect);

			while (rs3.next()) 
			{
				String ipaddress        	= rs3.getString("ip_address");
				Integer measured_entity_id  = rs3.getInt("measured_entity_id");  
				String portLabel        	= rs3.getString("port_label");
				Integer refreshTimeMs       = rs3.getInt("refresh_time_ms");
				
				logger.debug("new modbus event found");
				
				if (refreshTimeMs > 0){
					ModBusTcpEvent modBusEvent = ModBusTcpEvent.createModbusEvent(ipaddress, measured_entity_id, portLabel, refreshTimeMs);
					if (modBusEvent != null)
						events.add(modBusEvent);
				} else {
					logger.error("Refresh time is zero for Port label:" + portLabel + " which is invalid");
				}
			}
			
			rs3.close();

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
	*/
	
	public Map<Integer, List<InputOutputPort>> getInputOutputPortReferingMeasuredEntity(Integer measuredEntity) {
				
		Map<Integer, List<InputOutputPort>> ports = new HashMap<Integer,List<InputOutputPort>>();

		Set<Integer> monitoringKeys = this.configuationObjects.keySet();

		for (Integer key : monitoringKeys) {
			MonitoringDevice mDevice = (MonitoringDevice) this.configuationObjects.get(key);
			ports.put(key, mDevice.getInputOutputPortReferingMeasuredEntity(measuredEntity));
		}
		
		return ports;
	}
	
}
