package com.advicetec.monitorAdapter;

import com.advicetec.FogClasses.Manager;
import com.advicetec.configuration.ConfigurationManager;

public class AdapterManager extends Manager implements Runnable
{

	private static AdapterManager instance=null;
	private static ConfigurationManager confManager = null; 
	private static 
	
	public AdapterManager getInstance()
	{
		if (instance==null)
			instance = new AdapterManager();
		
		return instance;
	}
	
	private AdapterManager() 
	{
		super("AdapterManager");	
		confManager = ConfigurationManager.getInstance();
	}
	
	
	public void executeEventHandler()
	{
		
	}


	public void run() {
		
		int number = Integer.valueOf(getProperty("NumAdapterHandlers")); 
		for (int i = 0; i < number; i++) 
		{
			Thread t = new Thread(new AdapterHandler());
			
		}
	}
	
}
