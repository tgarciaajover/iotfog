package com.advicetec.configuration;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.Configurable;

/**
 * This class manages all containers for configurable objects. A configurable object is any objet that can be created, updated, or deleted 
 * in the Django configuration module.
 * 
 * @author Andres Marentes
 *
 */
public class ConfigurationManager extends Configurable
{
	/**
	 * Configuration Manager instance.
	 */
	private static ConfigurationManager instance=null;
	
	static Logger logger = LogManager.getLogger(ConfigurationManager.class.getName());
	
	/**
	 * Reference to the container of Signal Units
	 */
	private SignalUnitContainer signalUnits;
	
	/**
	 * Reference to the container of Signal Types
	 */
	private SignalTypeContainer signalTypes;
	
	/**
	 * Reference to the container of Signals
	 */
	private SignalContainer signals;
	
	/**
	 * Reference to the container of Device Types
	 */
	private DeviceTypeContainer deviceTypes;
	
	/**
	 * Reference to the container of Monitoring Devices
	 */
	private MonitoringDeviceContainer monitoringDevices;
	
	/**
	 * Reference to the container of Reason Codes
	 */
	private ReasonCodeContainer reasonCodes;
	
	/**
	 * Reference to the container of Display Types
	 */
	private DisplayTypeContainer displayTypes;
	
	/**
	 * Reference to the container of Display Devices
	 */
	private DisplayDeviceContainer displayDevices;
	
	private AggregateMethodContainer aggregateMethods;
	
    /**
     * @return Get the instance of the Configuration Manager
     */
    public synchronized static ConfigurationManager getInstance()
    {
    	if (instance == null){
    		instance = new ConfigurationManager();
    	}
    	
    	return instance;
    }

	/**
	 * Constructor for the class. This class read a properties file with name: ConfigurationManager.properties
	 * 
	 * The properties that should be included are those used to connect to the database, which are:
	 * 
	 * 	- driver
	 *  - server
	 *  - user
	 *  - password
	 */
	private ConfigurationManager() 
	{
		super("ConfigurationManager");

		String driver = properties.getProperty("driver");
		String server = properties.getProperty("server");
		String user = properties.getProperty("user");
		String password = properties.getProperty("password");
		
		signalUnits = new SignalUnitContainer(driver, server, user,password);
		signalTypes = new SignalTypeContainer(driver, server, user, password);
		
		signals = new SignalContainer(driver, server, user, password);		
		// Add references to the signal container.
		signals.addReference("SignalType", signalTypes);
		signals.addReference("Unit", signalUnits);

		deviceTypes = new DeviceTypeContainer(driver,server, user, password);
		// Add references to device type container.
		deviceTypes.addReference("Signal", signals);
		
		monitoringDevices = new MonitoringDeviceContainer(driver, server, user, password);
		// Add References to monitoring devices.
		monitoringDevices.addReference("Signal", signals);
		monitoringDevices.addReference("DeviceType", deviceTypes);
		
		// container from configuration
		reasonCodes = new ReasonCodeContainer(driver, server, user, password);
		
		// Add Display Types 
		displayTypes = new DisplayTypeContainer(driver, server, user, password);
		
		// Add Display Devices
		displayDevices = new DisplayDeviceContainer(driver, server, user, password);
		displayDevices.addReference("DisplayType", displayTypes);
		
		//Add Aggregate Methods
		aggregateMethods = new AggregateMethodContainer(driver, server, user, password);
	}

	/**
	 * Load the configuration object´s information. It just reads this information the first time. 
	 * Posterior changes to configuration objects should be done by interface.
	 * 
	 * @throws SQLException This exception is triggered whenever that some container could not connect to the database.
	 */
	public synchronized void loadConfiguration() throws SQLException
	{
		this.signalUnits.loadContainer();
		this.signalTypes.loadContainer();
		this.signals.loadContainer();
		this.deviceTypes.loadContainer();
		this.monitoringDevices.loadContainer();
		this.reasonCodes.loadContainer();
		this.displayTypes.loadContainer();
		this.displayDevices.loadContainer();
		this.aggregateMethods.loadContainer();
	}
	
	/**
	 * Gets the reference to the signal unit container
	 * 
	 * @return Reference to the signal unit container
	 */
	public SignalUnitContainer getSignalUnitContainer()
	{
		return this.signalUnits;
	}
	
	/**
	 * Gets the reference to the signal type container
	 * 
	 * @return reference to the signal type container
	 */
	public SignalTypeContainer getSignalTypeContainer()
	{
		return this.signalTypes;
	}
	
	public AggregateMethodContainer getAggregateMethods() {
		return aggregateMethods;
	}
	
	/**
	 * Gets the reference to the signal container
	 * 
	 * @return reference to the signal container
	 */
	public SignalContainer getSignalContainer()
	{
		return this.signals;
	}
	
	/**
	 * Gets the reference to the device type container
	 * 
	 * @return  reference to the device type container
	 */
	public DeviceTypeContainer getDeviceTypeContainer()
	{
		return this.deviceTypes;
	}
	
	/**
	 * Gets a reference to the monitoring device container
	 * 
	 * @return reference to the monitoring device container
	 */
	public MonitoringDeviceContainer getMonitoringDeviceContainer()
	{
		return this.monitoringDevices;
	}
	
	/**
	 * Gets a reference to the reason code container
	 * 
	 * @return  reference to the reason code container
	 */
	public ReasonCodeContainer getReasonCodeContainer()
	{
		return this.reasonCodes;
	}
	
	/**
	 * Gets a reference to the display type container
	 * 
	 * @return  reference to the device type container
	 */
	public DisplayTypeContainer getDisplayTypeContainer()
	{
		return this.displayTypes;
	}
	
	/**
	 * Gets a reference to the Display Device Container
	 * 
	 * @return  reference to the display device container
	 */
	public DisplayDeviceContainer getDisplayDeviceContainer()
	{
		return this.displayDevices;
	}
	
	/**
	 * Gets the monitoring device that has the given deviceAddress.
	 * 
	 *  A valid deviceAddress can be a MacAddress, Serial or an IP address.
	 *  It is important to note that the macAddress has priority over IPAddress,  
	 *  and the IpAddress over serial field.  
	 * 
	 * @param deviceAddress : an string representing any of the three fields.
	 * 
	 * @return the monitoring device configured with any of those fields. If not found, it returns null. 
	 */
	public MonitoringDevice getMonitoringDevice(String deviceAddress)
	{
		if ( this.monitoringDevices.getByMacAddress(deviceAddress) == null)
		{
			if (this.monitoringDevices.getByIpAddress(deviceAddress) == null)
			{
				if (this.monitoringDevices.getBySerial(deviceAddress) == null)
				{
					return null;
				} else {
					return this.monitoringDevices.getBySerial(deviceAddress);
				}
			} else {
				return this.monitoringDevices.getByIpAddress(deviceAddress);
			}
			
		} else {
			return this.monitoringDevices.getByMacAddress(deviceAddress);
		}
			
	}
	
	/**
	 * Gets the transformation configured in the monitoring device that has the given deviceAddress and portLabel.
	 * 
	 * @param macAddress : an string representing any of the MacAddress, Ip Address, or Serial fields.
	 * @param portLabel : port label identifying the port to which the transformation belongs to.
	 * 
	 * @return Transformation text registered for the monitoring device and port label. If not found returns null.
	 */
	public String getTransformation(String macAddress, String portLabel){
		logger.debug("In getTransformation macAddress:" + macAddress + "portLabel:" + portLabel );
		
		if (this.getMonitoringDevice(macAddress) == null){
			logger.error("Monitoring Device with address:" + macAddress +" not Found");
			return null;
		} else { 
			return	this.getMonitoringDevice(macAddress).getTranformation(portLabel);
		}
	}
		
	/**
	 * Gets the Class Name configured in the monitoring device that has the given deviceAddress and portLabel.
	 * 
	 * @param macAddress : an string representing any of the MacAddress, Ip Address, or Serial fields.
	 * @param portLabel : port label identifying the port to which the transformation belongs to.
	 * 
	 * @return Class name registered for the monitoring device and port label. If not found returns null.
	 */
	public String getClassName(String macAddress, String portLabel){
		return this.getMonitoringDevice(macAddress).getClassName(portLabel);
	}
	
	/**
	 * Gets the measured entity configured in the monitoring device that has the given deviceAddress and portLabel.
	 * 
	 * @param macAddress : an string representing any of the MacAddress, Ip Address, or Serial fields.
	 * @param portLabel : port label identifying the port to which the transformation belongs to.
	 * 
	 * @return measured entity registered for the monitoring device and port label. If not found returns null.
	 */
	public Integer getMeasuredEntity(String monitoringAddress, String portLabel){
		logger.debug("start getMeasuredEntity params:" + monitoringAddress + "|" + portLabel);
		
		MonitoringDevice mDevice = this.getMonitoringDevice(monitoringAddress); 
		if ( mDevice == null){
			logger.error("Monitoring Device with address:"+ monitoringAddress +" not found");
			return null;
		}
		else { 		
			if (mDevice.getInputOutputPort(portLabel) == null){
				logger.error("Port:"+ portLabel + " not found in monitoring device");
				return null;
			} 
			else {
				return mDevice.getInputOutputPort(portLabel).getMeasuringEntity();
			}
		}
	}
}
