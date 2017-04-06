package com.advicetec.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigurationManager
{
	
	private static ConfigurationManager instance=null;
	
	private Properties properties;
	
	private SignalUnitContainer signalUnits;
	private SignalTypeContainer signalTypes;
	private SignalContainer signals;
	private DeviceTypeContainer deviceTypes;
	private MonitoringDeviceContainer monitoringDevices;

    public static ConfigurationManager getInstance()
    {
    	if (instance == null){
    		instance = new ConfigurationManager();
    	}
    	
    	return instance;
    }

	
	private ConfigurationManager() 
	{
		super();
		
		this.properties = new Properties();
		loadConfigurationFile("ConfigurationManager");
		
		String server = this.properties.getProperty("server");
		String user = this.properties.getProperty("user");
		String password = this.properties.getProperty("password");
		
		signalUnits = new SignalUnitContainer(server, user,password);
		signalTypes = new SignalTypeContainer(server, user, password);
		signals = new SignalContainer(server, user, password);
		deviceTypes = new DeviceTypeContainer(server, user, password);
		monitoringDevices = new MonitoringDeviceContainer(server, user, password);
	}

	private void loadConfigurationFile(String filename)
	{
		String filenamerel = "resources/" + filename;
		File configFile = new File(filenamerel);
		 
		try 
		{
		    FileReader reader = new FileReader(configFile);
		    this.properties.load(reader);		 		 
		    reader.close();
		} catch (FileNotFoundException ex) {
		    // TODO include in the log file
			System.out.println("File" + filename + " not found");
		} catch (IOException ex) {
			// TODO include in the log file
			System.out.println("Input Output error reading the file:" + filename );
		}
	}
	
	public String getProperty (String propName)
	{
		return this.properties.getProperty(propName);
	}
	
	public synchronized void loadConfiguration()
	{
		this.signalUnits.loadContainer();
		this.signalTypes.loadContainer();
		this.signals.loadContainer();
		this.deviceTypes.loadContainer();
		this.monitoringDevices.loadContainer();
	}
	
	public MonitoringDevice getMonitoringDevice(String macAddress)
	{
		return this.monitoringDevices.getByMacAddress(macAddress);
	}
}
