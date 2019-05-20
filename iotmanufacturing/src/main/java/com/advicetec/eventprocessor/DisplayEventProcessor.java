package com.advicetec.eventprocessor;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;

import com.advicetec.MessageProcessor.DelayEvent;
import com.advicetec.applicationAdapter.ProductionOrderManager;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.DisplayDevice;
import com.advicetec.core.Processor;
import com.advicetec.displayadapter.LedSignDisplay;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.measuredentitity.StateInterval;

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
	
	ArrayList<String> messages;
	
	ArrayList<String> colors;
	
	/**
	 * @param event
	 */
	public DisplayEventProcessor(DisplayEvent event) {
		super();
		this.event = event;
		messages = new ArrayList<String>();
		colors = new ArrayList<String>();
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
		Integer uniqueID = this.event.getEntity();
		Integer counter;
		String filename;
		       
		
        ConfigurationManager confManager = ConfigurationManager.getInstance();
        DisplayDevice displayDevice = confManager.getDisplayDeviceContainer().getDisplayDevice(name);
        uniqueID = displayDevice.getEntityId();
        
        //logger.debug("process - display:" + name + "Text:" + text + " uniqueID: "+uniqueID);
        
		ArrayList<DelayEvent> ret = new ArrayList<DelayEvent>(); 
		try{
			this.getMessageToPublish(uniqueID, confManager, name);
		}catch (Exception e) {
			logger.error("Error getting message to publish " + name);
			e.printStackTrace();
		}
		
		
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
			counter = 1;
			//logger.debug("***************************************************");
			for(String texttopublish : this.messages){				
				filename = "temp"+counter.toString()+".Nmg";
				//logger.debug("Mensaje al display: "+texttopublish+" - "+ filename);
				led.setLanguageTextColor(this.colors.get(counter-1));
				led.publishMessage(texttopublish, filename);
				counter += 1;
			}
			
			try{
				led.publishPlayList();
			} catch(Exception e) {
				logger.error("Error in publish PlayList" + e.getMessage());
				e.printStackTrace();
			}
			
			
		} else {
			logger.error("No display with name:" + name + " was found registered in the system");
		}
		
		return ret;

	}
	
	public void getMessageToPublish(Integer uniqueID, ConfigurationManager confManager, String name){
				
		ArrayList<String> itemDetail, variables;
		String messagetemp = "", totduration = "";
		Double actualProductionRate;
		Double duration;
		MeasuredEntityFacade facade;
		this.messages.clear();
		this.colors.clear();
		
		variables = confManager.getDisplayDeviceContainer().getDisplayDeviceVariables(name);
		String label1 = confManager.getDisplayDeviceContainer().getDisplayDeviceLabels(name);
		
		try {
			facade = MeasuredEntityManager.getInstance()
					.getFacadeOfEntityById(uniqueID);
			if(facade == null)
			{			
				logger.error("Facade:"+uniqueID+" is not found.");			
			}
			else
			{
				// get the array from the facade.
				List<StateInterval> intervals = facade.getCurrentStateInterval();
				for (StateInterval interval : intervals){	
					duration = facade.getCurrentStateDuration();				
					if(duration > 60){
						duration = duration / 60;
						duration = round(duration,1);
						totduration = String.valueOf(duration) + " Hrs";
					}else{
						duration = round(duration,1);
						totduration = String.valueOf(duration)+" Mins";
					}
					
					actualProductionRate = facade.getDBActualProductionRate();
					
					logger.debug("Compañía: " + facade.getEntity().getCanonicalKey().split("-")[0] +
							" Sede: " + facade.getEntity().getCanonicalKey().split("-")[1] +
							" Planta: " + facade.getEntity().getCanonicalKey().split("-")[2] +
							" Grupo Máquina: " + facade.getEntity().getCanonicalKey().split("-")[3] +
							" Máquina: " + facade.getEntity().getCanonicalKey().split("-")[4] +
							" Año: " + String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) +
							" Mes: " + String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1) +
							" Día: " + String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
					//Calculation of online indicators moved to the API. 
					/*facade.calculateOverallEquipmentEffectiveness(facade.getEntity().getCanonicalKey().split("-")[0],
							facade.getEntity().getCanonicalKey().split("-")[1], facade.getEntity().getCanonicalKey().split("-")[2],
							facade.getEntity().getCanonicalKey().split("-")[3], facade.getEntity().getCanonicalKey().split("-")[4],
							String.valueOf(Calendar.getInstance().get(Calendar.YEAR)), String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1), 
							String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)), "%");*/
					
					duration = facade.getEntityAvailability(facade.getBeginDttmCurrentShift());
					duration = round(duration,2);
					logger.info("Shift Availability: " + String.valueOf(duration));
					
					if(interval.getState().getName() == "Operating"){					
						messagetemp = "Produciendo: "+totduration;
						messages.add(messagetemp);
						colors.add("G");
						if(label1 != null && !label1.isEmpty()){
							messagetemp = label1 + ": " + round(actualProductionRate,1);
						} else {
							messagetemp = "Vel: " + round(actualProductionRate,1);
						}
						messages.add(messagetemp);
						colors.add("G");
						messagetemp = "Disponibilidad Turno: "+String.valueOf(duration) + "%";
						messages.add(messagetemp);
						colors.add("G");
						messagetemp = interval.getExecutedObjectCanonical();						
						if("".equals(messagetemp)){
							messagetemp = "ID de Trabajo: NA";
							messages.add(messagetemp);
							colors.add("G");
							messagetemp = "Articulo: NA";
							messages.add(messagetemp);
							colors.add("G");							
						}else{
							String[] parts = messagetemp.split("-");
							messagetemp = "ID de Trabajo: "+ parts[6].toString();
							//messagetemp = "ID de Trabajo: NA";
							messages.add(messagetemp);
							colors.add("G");
							ProductionOrderManager productionOrderManager = ProductionOrderManager.getInstance();
							itemDetail = productionOrderManager.getProductionOrderContainer().getItemDetail(parts[0], parts[1], parts[2], parts[3], Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), parts[6] );						
							messagetemp = "Articulo: "+itemDetail.get(0)+" "+itemDetail.get(1);
							messages.add(messagetemp);
							colors.add("G");
						}																	
						//for(String var: variables){
						//	messages.add(var);
						//	colors.add("G");
						//}
					}
					if(interval.getState().getName() == "UnScheduleDown"){
						messagetemp = "Parada: "+totduration;
						messages.add(messagetemp);
						colors.add("R");
						try{
							if (facade.getCurrentReason() == null) {
								messagetemp = "Parada pendiente por reportar";
							} else {
								messagetemp = "Motivo: "+ facade.getCurrentReason().getDescription();
							}
						} catch (NullPointerException  e){
							messagetemp = "Parada pendiente por reportar";
							logger.debug("The current reason of " + facade.getEntity().getId().toString() + " is null. " + e.getMessage());
							e.printStackTrace();
						}
						messages.add(messagetemp);
						colors.add("R");
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error buscando mensajes");
		}		
	}
	
	private static double round (double value, int precision) {
	    int scale = (int) Math.pow(10, precision);
	    return (double) Math.round(value * scale) / scale;
	}
		
}
