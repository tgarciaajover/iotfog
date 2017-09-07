package com.advicetec.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class implements a reader for properties file
 * 
 * @author Andres Marentes
 *
 */
public abstract class Configurable {

	static Logger logger = LogManager.getLogger(Configurable.class.getName());
	
	/**
	 *  instance of a properties file 
	 */
	protected Properties properties;
	
	/**
	 * Constructor for the class, it receives as parameter the name of the properties file.  
	 * 
	 * @param filename  properties file name 
	 */
	public Configurable(String filename){
		this.properties = new Properties();
		// Load the configuration file.
		loadConfigurationFile(filename);
	}
	
	/**
	 * Reads the properties from the file.
	 * 
	 * This method looks up the file in resources directory. 
	 * 
	 * @param filename file name to load.
	 */
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
	
	/**
	 * Gets a property from the file.
	 * 
	 * @param propName  property name to be returned
	 * 
	 * @return String value of the property being requested.  
	 */
	public String getProperty (String propName)
	{
		return this.properties.getProperty(propName);
	}
}
