package com.advicetec.configuration;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.Configurable;

public class ConfigurationManager extends Configurable
{
	private static ConfigurationManager instance=null;
	static Logger logger = LogManager.getLogger(ConfigurationManager.class.getName());
	
	private SignalUnitContainer signalUnits;
	private SignalTypeContainer signalTypes;
	private SignalContainer signals;
	private DeviceTypeContainer deviceTypes;
	private MonitoringDeviceContainer monitoringDevices;
	private ReasonCodeContainer reasonCodes;
	private DisplayTypeContainer displayTypes;
	private DisplayDeviceContainer displayDevices;
	
    public static ConfigurationManager getInstance()
    {
    	if (instance == null){
    		instance = new ConfigurationManager();
    	}
    	
    	return instance;
    }

	
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
		displayDevices.addReference("DisplayType", displayDevices);
	}

	
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
	}
	
	public SignalUnitContainer getSignalUnitContainer()
	{
		return this.signalUnits;
	}
	
	public SignalTypeContainer getSignalTypeContainer()
	{
		return this.signalTypes;
	}
	
	public SignalContainer getSignalContainer()
	{
		return this.signals;
	}
	
	public DeviceTypeContainer getDeviceTypeContainer()
	{
		return this.deviceTypes;
	}
	
	public MonitoringDeviceContainer getMonitoringDeviceContainer()
	{
		return this.monitoringDevices;
	}
	
	public ReasonCodeContainer getReasonCodeContainer()
	{
		return this.reasonCodes;
	}
	
	public DisplayTypeContainer getDisplayTypeContainer()
	{
		return this.displayTypes;
	}
	
	public DisplayDeviceContainer getDisplayDeviceContainer()
	{
		return this.displayDevices;
	}
	
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
	
	
	public String getTransformation(String macAddress, String portLabel){
		logger.debug("In getTransformation macAddress:" + macAddress + "portLabel:" + portLabel );
		
		if (this.getMonitoringDevice(macAddress) == null){
			logger.error("Monitoring Device with address:" + macAddress +" not Found");
			return null;
		} else { 
			return	this.getMonitoringDevice(macAddress).getTranformation(portLabel);
		}
	}
		
	public String getClassName(String macAddress, String portLabel){
		return this.getMonitoringDevice(macAddress).getClassName(portLabel);
	}
	
	/**
	 * 
	 * @param macAddress
	 * @param portLabel
	 * @return
	 */
	public Integer getMeasuredEntity(String macAddress, String portLabel){
		logger.debug("start getMeasuredEntity params:" + macAddress + "|" + portLabel);
		MonitoringDevice mDevice = this.getMonitoringDevice(macAddress); 
		if ( mDevice == null){
			logger.error("Monitoring Device with address:"+ macAddress +" not found");
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
