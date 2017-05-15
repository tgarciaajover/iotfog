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

public class DisplayEventProcessor implements Processor
{

	static Logger logger = LogManager.getLogger(DisplayEventProcessor.class.getName());
	DisplayEvent event;
	
	public DisplayEventProcessor(DisplayEvent event) {
		super();
		this.event = event;
	}

	public List<DelayEvent> process() throws SQLException 
	{
		
		String name = this.event.getDisplayName();
		String text = this.event.getDisplayText();
		
        logger.debug("process - display:" + name + "Text:" + text);
		

        ConfigurationManager confManager = ConfigurationManager.getInstance();
        DisplayDevice displayDevice = confManager.getDisplayDeviceContainer().getDisplayDevice(name);
        
		ArrayList<DelayEvent> ret = new ArrayList<DelayEvent>();  

		if (displayDevice != null){
		
			LedSignDisplay led = new LedSignDisplay();
			led.setDstPort(displayDevice.getPort());
			led.setNetAddress(displayDevice.getIpAddress());
			led.setBackColor(displayDevice.getDisplayType().getBackColor());
			led.setInMode(displayDevice.getDisplayType().getInMode());
			led.setOutMode(displayDevice.getDisplayType().getOutMode());
			led.setLetterSize(displayDevice.getDisplayType().getLetterSize());
			led.setLineSpacing(displayDevice.getDisplayType().getLineSpacing());
			led.setSignalHeight(displayDevice.getDisplayType().getPixelsHeight());
			led.setSignalWidth(displayDevice.getDisplayType().getPixelsWidth());
			led.setSpeed(displayDevice.getDisplayType().getSpeed());
			led.setTextColor(displayDevice.getDisplayType().getTextColor());
			led.setVerticalAlign(displayDevice.getDisplayType().getVerticalAlignment());
			led.setHorizontalAlign(displayDevice.getDisplayType().getHorizontalAlignment());
			led.publishMessage(text);


		} else {
			// TODO: put the log error saying that there is not facade.
		}

		return ret;

	}

	/**
	 * This function get the behavior from list of names given as parameter. 
	 * We expect to have machinegroup.machine.behaviorid as the name 
	 * @param names
	 * @return
	 */
	
	public String getBehavior(List<String> names)
	{
		// TODO: create the method.
		return null;
	}
	
}