package com.advicetec.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.Container;

/**
 * This class implements a reader for properties file
 * @author user
 *
 */
public abstract class Configurable {

	static Logger logger = LogManager.getLogger(Configurable.class.getName());
	
	protected Properties properties;
	
	public Configurable(String filename){
		this.properties = new Properties();
		// Load the configuration file.
		loadConfigurationFile(filename);
	}
	
	private void loadConfigurationFile(String filename)
	{
		String filenamerel = "resources/" + filename + ".properties";
		File configFile = new File(filenamerel);
		 
		try 
		{
		    FileReader reader = new FileReader(configFile);
		    this.properties.load(reader);		 		 
		    reader.close();
		} catch (FileNotFoundException ex) {
			logger.error("File" + filename + " not found");
		} catch (IOException ex) {
			logger.error("Input Output error reading the file:" + filename );
		}
	}
	
	public String getProperty (String propName)
	{
		return this.properties.getProperty(propName);
	}
}
