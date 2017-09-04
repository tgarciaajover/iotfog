package com.advicetec.configuration;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Container class for Display Types.
 * 
 * @author Andres Marentes
 *
 */
public class DisplayTypeContainer extends Container 
{

	static Logger logger = LogManager.getLogger(DisplayTypeContainer.class.getName());
	
	/**
	 * SQL Statement for reading the configuration data of display types.
	 */
	static String sqlSelect1 = "SELECT id, descr, pixels_width, pixels_height, text_color, back_color, in_mode, out_mode, speed, line_spacing, letter_size, vertical_alignment, horizontal_alignment, create_date FROM setup_displaytype";
	
	/**
	 * Constructor for the class, it takes as parameters data required to connect to the database.
	 * 
	 * @param driver	: driver string used to connect to the database.
	 * @param server	: Ip address of the database server
	 * @param user		: database user
	 * @param password	: password of the user's database.
	 */
	public DisplayTypeContainer(String driver, String server, String user, String password) 
	{	
		super(driver, server, user, password);	
	}
	
	/**
	 * Loads all display types registered in the database into the container.
	 * 
	 * @throws SQLException
	 */
	public void loadContainer() throws SQLException
	{

		try 
		{
			super.connect();
			super.configuationObjects.clear();
						
			ResultSet rs1 = super.pst.executeQuery(sqlSelect1);
			while (rs1.next())
			{
				Integer id     			= rs1.getInt("id");  
		        String descr   			= rs1.getString("descr");
		        Integer	pixelsWidth		= rs1.getInt("pixels_width"); 
		        Integer	pixelsHeight	= rs1.getInt("pixels_height"); 
		        String textColor		= rs1.getString("text_color");
		        String backColor		= rs1.getString("back_color");
		        String inMode         	= rs1.getString("in_mode");
		        String outMode			= rs1.getString("out_mode"); 
		        String speed			= rs1.getString("speed");
		        Integer lineSpacing		= rs1.getInt("line_spacing"); 
		        String letterSize		= rs1.getString("letter_size"); 
		        String verticalAlign	= rs1.getString("vertical_alignment"); 
		        String horizontalAlign	= rs1.getString("horizontal_alignment"); 
		        Timestamp timestamp 	= rs1.getTimestamp("create_date");
		        		        		        
		        DisplayType object = new DisplayType(id);
		        object.setDescr(descr);
		        object.setPixelsWidth(pixelsWidth);
		        object.setPixelsHeight(pixelsHeight);
		        object.setTextColor(textColor);
		        object.setBackColor(backColor);
		        object.setInMode(inMode);
		        object.setOutMode(outMode);
		        object.setSpeed(speed);
		        // TODO line spacing is integer or string
		        object.setLineSpacing(lineSpacing);
		        object.setLetterSize(letterSize);
		        object.setVerticalAlignment(verticalAlign);
		        object.setHorizontalAlignment(horizontalAlign);
		        object.setCreateDate(timestamp.toLocalDateTime());
		        
		        super.configuationObjects.put(id, object);
		      
			}

			rs1.close();

			super.disconnect();

		} catch (ClassNotFoundException e){
        	String error = "Could not find the driver class - Error" + e.getMessage(); 
        	logger.error(error);
        	e.printStackTrace();
        	throw new SQLException(error);
        } catch (SQLException e) {
        	String error = "Container:" + this.getClass().getName() +  "Error connecting to the database - error:" + e.getMessage();
        	logger.error(error);
        	e.printStackTrace();        	
        	throw new SQLException(error);
        }
		
	}

	/**
	 * Delete a display type from the container
	 * 
	 * @param uniqueID  Identifier of the display type to remove.
	 */
	public synchronized void deleteDisplayType(int uniqueID)
	{
		super.configuationObjects.remove(uniqueID);
	}
	
	/**
	 * Builds a display type from Json object representation. Once it creates the new instance, that instance is inserted in the container
	 * 
	 * @param json	json representation.
	 */
	public synchronized void fromJSON(String json){
		
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string and pretty print
		DisplayType displayTypeTemp;
		try {
		
			displayTypeTemp = mapper.readValue(json, DisplayType.class);
			
			super.configuationObjects.put(displayTypeTemp.getId(), displayTypeTemp);
		
		} catch (JsonParseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	
	}
}
