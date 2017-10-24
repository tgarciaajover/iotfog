package com.advicetec.mpmcqueue;

/** 
 * Enumeration that defines the type of elements that will be wrapper under
 * <code>Queable</code> object.
 * <p>
 * GENERIC object. <br>
 * MQTT_DEV_MESSAGE : messages in MQTT protocol from the device.<br>
 * MODBUS_DEV_MESSAGE : messages in Modbus protocol from the device.<br>
 * UNIFIED_MESSAGE : all messages are translated into an agnostic representation.<br>
 * EVENT : also events can be queued, this type represent a FOG's event.<br>
 * 
 * @author advicetec
 *
 */
public enum QueueType {

	GENERIC, MQTT_DEV_MESSAGE, MODBUS_DEV_MESSAGE, MODBUS_ERR_MESSAGE, UNIFIED_MESSAGE, EVENT;
}
