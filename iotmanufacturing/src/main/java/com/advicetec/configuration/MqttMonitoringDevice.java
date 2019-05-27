package com.advicetec.configuration;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author PowerUser
 *
 */
public class MqttMonitoringDevice extends MonitoringDevice {
	
	/**
	 * This property establishes the port to be connected
	 */
	protected int port;
	
	/**
	 * Gets the port for the monitoring device. This value is used when the monitoring device is a
	 * concentrator.
	 * 
	 * @return
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port for the monitoring device. This value is used when the monitoring device is a
	 * concentrator.
	 * 
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * Constructor for the class. It receives the identifier of the measuring entity.
	 * @param id Identifier of the measuring entity.
	 */
	@JsonCreator
	public MqttMonitoringDevice(@JsonProperty("id") Integer id) {
		super(id);
	}

	/**
	 * Gets the input output port by identifier
	 * @param id identifier of the input - output port.
	 * @return input output port object or null if there is not a port with that identifier.
	 */
	@JsonIgnore
	public MqttInputOutputPort getInputOutputPort(Integer id){

		for (int i = 0; i < this.inputOutputPorts.size(); i++){
			if (this.inputOutputPorts.get(i).getId().equals(id)){
				return (MqttInputOutputPort) this.inputOutputPorts.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * Gets an input output port by port label
	 * 
	 * @param portLabel  port label to find.
	 * @return input output port object or null if there is not a port with that port label.
	 */
	@JsonIgnore
	public MqttInputOutputPort 
	getInputOutputPort(String portLabel){
		Integer id = this.portsByLabel.get(portLabel);
		return (MqttInputOutputPort) getInputOutputPort(id);
	}



	
}
