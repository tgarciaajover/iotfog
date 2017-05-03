package com.advicetec.configuration;

import com.advicetec.core.Configurable;

public class ConfigurationManager extends Configurable
{
	private static ConfigurationManager instance=null;
	
	private SignalUnitContainer signalUnits;
	private SignalTypeContainer signalTypes;
	private SignalContainer signals;
	private DeviceTypeContainer deviceTypes;
	private MonitoringDeviceContainer monitoringDevices;
	private ReasonCodeContainer reasonCodes;

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

		String server = properties.getProperty("server");
		String user = properties.getProperty("user");
		String password = properties.getProperty("password");
		
		signalUnits = new SignalUnitContainer(server, user,password);
		signalTypes = new SignalTypeContainer(server, user, password);
		
		signals = new SignalContainer(server, user, password);		
		// Add references to the signal container.
		signals.addReference("SignalType", signalTypes);
		signals.addReference("Unit", signalUnits);

		deviceTypes = new DeviceTypeContainer(server, user, password);
		// Add references to device type container.
		deviceTypes.addReference("Signal", signals);
		
		monitoringDevices = new MonitoringDeviceContainer(server, user, password);
		// Add References to monitoring devices.
		monitoringDevices.addReference("Signal", signals);
		monitoringDevices.addReference("DeviceType", deviceTypes);
		
		reasonCodes = new ReasonCodeContainer(server, user, password);
	}

	
	public synchronized void loadConfiguration()
	{
		this.signalUnits.loadContainer();
		this.signalTypes.loadContainer();
		this.signals.loadContainer();
		this.deviceTypes.loadContainer();
		this.monitoringDevices.loadContainer();
		this.reasonCodes.loadContainer();
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
	
	public MonitoringDevice getMonitoringDevice(String macAddress)
	{
		return this.monitoringDevices.getByMacAddress(macAddress);
	}
	
	public String getTransformation(String macAddress, String portLabel){
		System.out.println("In getTransformation macAddress:" + macAddress + "portLabel:" + portLabel );
		if (this.monitoringDevices.getByMacAddress(macAddress) == null){
			System.out.println("Monitoring Device not Found");
			return null;
		} else { 
			return	this.monitoringDevices.getByMacAddress(macAddress).getTranformation(portLabel);
		}
	}
		
	public String getClassName(String macAddress, String portLabel){
		return this.monitoringDevices.getByMacAddress(macAddress).getClassName(portLabel);
	}
	
	public Integer getMeasuredEntity(String macAddress, String portLabel){
		System.out.println("start getMeasuredEntity params:" + macAddress + "|" + portLabel);
		MonitoringDevice mDevice = this.monitoringDevices.getByMacAddress(macAddress); 
		if ( mDevice == null){
			System.out.println("Monitoring Device not found");
			return null;
		}
		else { 		
			if (mDevice.getInputOutputPort(portLabel) == null){
				System.out.println("Port not found in monitoring device");
				return null;
			} 
			else {
				return mDevice.getInputOutputPort(portLabel).getMeasuringEntity();
			}
		}
	}
}
