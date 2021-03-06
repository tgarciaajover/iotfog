package com.advicetec.configuration;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class MqttInputOutputPort extends InputOutputPort {

	
	/**
	 * topic name to use in MQTT.
	 */
	protected String topicName;

	/**
	 * Constructor for a Mqtt input output port
	 * @param id  identifier of this configurable object.
	 */
	@JsonCreator
	public MqttInputOutputPort(@JsonProperty("id") Integer id) {
		super(id);
	}

	/**
	 * Gets topic name used in mqtt
	 * @return topic name
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 * Sets topic name used in mqtt
	 * @param topicName
	 */
	public void setTopicName(String topic_name) {
		this.topicName = topic_name;
	}
		
}
