package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.DisplayDevice;
import com.advicetec.core.Processor;
import com.advicetec.displayadapter.LedSignDisplay;

/**
 * This class process display events, it takes as parameter the display event to be executed, 
 * then it creates a connection with the display and sends to message to be shown. 
 * 
 * @author Andres Marentes
 *
 */
public class DisplayEventProcessor implements Processor
{

	static Logger logger = LogManager.getLogger(DisplayEventProcessor.class.getName());
	
	/**
	 * 
	 */
	DisplayEvent event;
	
	/**
	 * @param event
	 */
	public DisplayEventProcessor(DisplayEvent event) {
		super();
		this.event = event;
	}

	/**
	 * This method takes the event parameters, connects to the display and publish the message.
	 * 
	 * Returns an empty list of delayed events.
	 */
	public List<DelayEvent> process() throws SQLException 
	{
		
		String name = this.event.getDisplayName();
		String text = this.event.getDisplayText();
		
        logger.info("process - display:" + name + "Text:" + text);
		
        ConfigurationManager confManager = ConfigurationManager.getInstance();
        DisplayDevice displayDevice = confManager.getDisplayDeviceContainer().getDisplayDevice(name);
        
		ArrayList<DelayEvent> ret = new ArrayList<DelayEvent>();  
		
		if (displayDevice != null){
		
			LedSignDisplay led = new LedSignDisplay();
			led.setDstPort(displayDevice.getPort());
			led.setNetAddress(displayDevice.getIpAddress());
			led.setLanguageBackColor(displayDevice.getDisplayType().getBackColor());
			led.setLanguageInMode(displayDevice.getDisplayType().getInMode());
			led.setLanguageOutMode(displayDevice.getDisplayType().getOutMode());
			led.setLanguageLetterSize(displayDevice.getDisplayType().getLetterSize());
			led.setLanguageLineSpacing(displayDevice.getDisplayType().getLineSpacing());
			led.setSignalHeight(displayDevice.getDisplayType().getPixelsHeight());
			led.setSignalWidth(displayDevice.getDisplayType().getPixelsWidth());
			led.setLanguageSpeed(displayDevice.getDisplayType().getSpeed());
			led.setLanguageTextColor(displayDevice.getDisplayType().getTextColor());
			led.setLanguageVerticalAlign(displayDevice.getDisplayType().getVerticalAlignment());
			led.setLanguageHorizontalAlign(displayDevice.getDisplayType().getHorizontalAlignment());
			led.publishMessage(text);


		} else {
			logger.error("No display with name:" + name + " was found registered in the system");
		}
		
		return ret;

	}
	
}
