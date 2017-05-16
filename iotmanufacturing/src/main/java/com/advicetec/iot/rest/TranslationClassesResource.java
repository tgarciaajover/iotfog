package com.advicetec.iot.rest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.Signal;
import com.advicetec.configuration.SignalContainer;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;
import com.advicetec.monitorAdapter.protocolconverter.MqttDigital;

public class TranslationClassesResource extends ServerResource  
{

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
		
		String packageStr = mqttDigital.getClass().getPackage().getName() + ".protocolconverter";
		String classesNames[] = getClasses(packageStr);
		String json = new ObjectMapper().writeValueAsString(classesNames);
		
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
	    Enumeration<URL> resources = classLoader.getResources(path);
	    List<File> dirs = new ArrayList<File>();
	    while (resources.hasMoreElements()) {
	        URL resource = resources.nextElement();
	        dirs.add(new File(resource.getFile()));
	    }
	    ArrayList<String> classes = new ArrayList<String>();
	    for (File directory : dirs) {
	        classes.addAll(findClasses(directory, packageName));
	    }
	    return classes.toArray(new String[classes.size()]);
	}

	/**
	 * Recursive method used to find all classes name in a given directory and subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base directory
	 * @return The classes names
	 * @throws ClassNotFoundException
	 */
	private static List<String> findClasses(File directory, String packageName) throws ClassNotFoundException {
	    List<String> classes = new ArrayList<String>();
	    if (!directory.exists()) {
	        return classes;
	    }
	    File[] files = directory.listFiles();
	    for (File file : files) {
	        if (file.isDirectory()) 
	        {
	            assert !file.getName().contains(".");
	            classes.addAll(findClasses(file, packageName + "." + file.getName()));
	        } else if (file.getName().endsWith(".java")) {
	            classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)).getName());
	        }
	    }
	    return classes;
	}
	
}
