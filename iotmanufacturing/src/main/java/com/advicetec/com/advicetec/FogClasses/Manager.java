package com.advicetec.FogClasses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.advicetec.mpmcqueue.PriorityQueue;
import com.advicetec.mpmcqueue.Queueable;

public abstract class Manager 
{
	Properties properties;
	PriorityQueue queue;
	
	
	public Manager(String filename) {
		super();
		this.properties = new Properties();
		this.queue = new PriorityQueue(Queueable.class);
		
		// Load the configuration file.
		loadConfigurationFile(filename);
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
	
	
	public PriorityQueue getQueue()
	{
		return this.queue;
	}
}
