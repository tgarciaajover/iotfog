package com.advicetec.configuration;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * Map between the label of the part and its identifier. 
	 * This maps is a helper structure to make faster port look ups by port label.
	 */
	@JsonIgnore
	protected Map<String, Integer> portsByTopic; 

	
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
	 * List of ports included in the measuring device.
	 */
	@JsonProperty("io_ports")
	protected List<MqttInputOutputPort> inputOutputPorts;

	
	/**
	 * Constructor for the class. It receives the identifier of the measuring entity.
	 * @param id Identifier of the measuring entity.
	 */
	@JsonCreator
	public MqttMonitoringDevice(@JsonProperty("id") Integer id) {
		super(id);
		portsByTopic = new HashMap<String, Integer>();

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
	public MqttInputOutputPort  getMqttInputOutputPort(String topicName){
		Integer id = this.portsByTopic.get(topicName);
		return (MqttInputOutputPort) getInputOutputPort(id);
	}

	/**
	 * Adds a port into the list of input output ports.
	 * 
	 * @param iop Input - Output port object to add.
	 */
	public void putInputOutputPort(InputOutputPort iop){
		logger.debug("port label:" + iop.getPortLabel() + "Id:" + iop.getId());
		this.inputOutputPorts.add((MqttInputOutputPort)iop);
	}
	
	/**
	 * Gets the transformation text associated to the port, it search the port by port label.  
	 * 
	 * @param topicName  Topic Name to search.
	 * @return transformation text assigned or null if port label not found.
	 */
	@JsonIgnore
	public String getTranformation(String topicName){
		logger.debug("Topic name requested:" + topicName + "Port id:" + id);
		Integer id = this.portsByTopic.get(topicName);
		if (getInputOutputPort(id) == null){
			logger.debug("Error Label Port: " + id + " not found");
			return null;
		} else {
			return  getInputOutputPort(id).getTransformationText();
		}
		
	}
	
	/**
	 * Gets the class name that will be used to interpret the signal. 
	 * This is configured in the port's signal.
	 * 
	 * @param topicName topic name which is being searched.
	 * @return class name associated to the port with topic name. Null if not found.
	 */
	
	@JsonIgnore
	public String getClassName(String topicName){
		Integer id = this.portsByTopic.get(topicName);

		if (getInputOutputPort(id) == null){
			logger.debug("Error Label Port: " + id + " not found");
			return null;
		} else {
			return getInputOutputPort(id).getSignalType().getType().getClassName();
		}
	}
	

	/**
	 * Adds a port into the list of input output ports.
	 * 
	 * @param iop Input - Output port object to add.
	 */
	public void putInputOutputPort(MqttInputOutputPort iop){
		logger.debug("port label:" + iop.getPortLabel() + "Id:" + iop.getId());
		this.inputOutputPorts.add(iop);
		this.portsByTopic.put(iop.getTopicName(), iop.getId());
	}

	/**
	 * Indexes are maps used to speed up looks ups of instances.
	 * 
	 * for now the portsByLabel index is the only one. 
	 */
	public void updateIndexes() {
		
		this.portsByTopic.clear();
		
		for (int i = 0; i < this.inputOutputPorts.size(); i++){
			MqttInputOutputPort inputOutputPort = (MqttInputOutputPort) this.inputOutputPorts.get(i);
			this.portsByTopic.put(inputOutputPort.getTopicName(), inputOutputPort.getId());
		}
		
	}

	/**
	 * Returns all input-output ports within the measured device
	 * 
	 */
	public List<MqttInputOutputPort> getInputOutputPorts(){		
		return this.inputOutputPorts;
	}
	
	public List<InputOutputPort> getInputOutputPortReferingMeasuredEntity(Integer measuredEntity){
		
		List<InputOutputPort> ports = new ArrayList<InputOutputPort>();
		
		for (int i = 0; i < this.inputOutputPorts.size(); i++){
			InputOutputPort inputOutputPort = this.inputOutputPorts.get(i);
			if (inputOutputPort.getMeasuringEntity().equals(measuredEntity)) {
				ports.add(inputOutputPort);
			}
		}
		
		return ports;
	}
	
}
