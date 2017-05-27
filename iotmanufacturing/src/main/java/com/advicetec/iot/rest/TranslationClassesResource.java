package com.advicetec.iot.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.advicetec.monitorAdapter.protocolconverter.MqttDigital;

public class TranslationClassesResource extends ServerResource  
{
	
	static Logger logger = LogManager.getLogger(TranslationClassesResource.class.getName());

	  /**
	   * Returns the Signal instance requested by the URL. 
	   * 
	   * @return The JSON representation of the Signal, or CLIENT_ERROR_NOT_ACCEPTABLE if the 
	   * unique ID is not present.
	   * 
	   * @throws Exception If problems occur making the representation. Shouldn't occur in 
	   * practice but if it does, Restlet will set the Status code. 
	   */
	  @Get("json")
	  public Representation getSignal() throws Exception {
  		  
		// Create an empty JSon representation.
		Representation result;

	    // The requested classes name.
		MqttDigital mqttDigital = new MqttDigital();
		
		logger.debug("Starting GetSignal");
		
		String packageStr = mqttDigital.getClass().getPackage().getName();
		String classesNames[] = getClasses(packageStr);
		String json = new ObjectMapper().writeValueAsString(classesNames);
		
		logger.debug("Classes:" + json);
		
	    result = new JsonRepresentation(json);

	    // Return the representation.  The Status code tells the client if the representation is valid.
	    return result;
	  }
	
	
	/**
	 * Scans all classes accessible from the context class loader. Classes belong to the given package and subpackages.
	 *
	 * @param packageName The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private static String[] getClasses(String packageName)
	        throws ClassNotFoundException, IOException {
		
	    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	    assert classLoader != null;
	    String path = packageName.replace('.', '/');
	    
	    logger.debug("getClasses:" + path);
	    
	    ArrayList<File> fileList = new ArrayList<File>();
	    listf( path, fileList );

	    String strRet[] = new  String[fileList.size()]; 
	    
	    int i = 0;
	    for(File elem : fileList)
	    {
	    	strRet[i] = elem.getName();
	    	i++;
	    }
	    
	    return strRet;
	}

	
	public static void listf(String directoryName, ArrayList<File> files)
	{
		
		ArrayList<String> excludeFiles = new ArrayList<String>();
		excludeFiles.add("MqttDigital.java");
		excludeFiles.add("Translator.java");
		directoryName = "src/main/java/" + directoryName;
		
		Path p1 = Paths.get(directoryName);
						
		File directory = new File(p1.toUri());
		
		if (directory.isDirectory()){
		
		    // get all the files from a directory
		    File[] fList = directory.listFiles();
		    for (File file : fList)
		    {
		        if ((file.isFile()) && (excludeFiles.contains(file.getName()) == false))
		        {
		            files.add(file);
		        }
		    }
		}
	    
	}
	
}
