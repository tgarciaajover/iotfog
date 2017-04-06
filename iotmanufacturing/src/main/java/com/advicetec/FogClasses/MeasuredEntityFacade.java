package com.advicetec.FogClasses;

import java.time.LocalDateTime;

import com.advicetec.persistence.MeasureAttributeValueStore;


/**
 * This class is a MeasuredEntity facade.
 * It allows the language processor to access some functionality
 * from the MeasuredEntity without expose all its methods.
 *   
 * @author maldofer
 *
 */
public class MeasuredEntityFacade {
	
	private MeasuredEntity entity;

	public MeasuredEntityFacade(){
		
	}
	
	public MeasuredEntityFacade(MeasuredEntity entity) {
		super();
		this.entity = entity;
	}

	public MeasuredEntity getEntity() {
		return entity;
	}

	public void setEntity(MeasuredEntity entity) {
		this.entity = entity;
	}
	
	public MeasuredEntityType getType(){
		return entity.getType();
	}

	/**
	 * Returns TRUE if the attrib
	 * @param attr
	 * @return
	 */
	public boolean existsAttribute(AttributeMeasuredEntity attr){
		return entity.getAttributeList().contains(attr);
	}
	
	public boolean registerMeasureEntityAttibute(AttributeMeasuredEntity newAttribute){
		return entity.registerMeasureEntityAttibute(newAttribute);
	}
	/**
	 * Stores a new value for the attribute.
	 * @param attribute
	 * @param value
	 * @param timeStamp
	 * @throws Exception
	 */
	public void registerAttributeValue(Attribute attribute, Object value, LocalDateTime timeStamp) throws Exception{
		MeasuredAttributeValue measure = entity.getMeasureAttributeValue(attribute, value, timeStamp);
			MeasureAttributeValueStore.getInstance().cacheStore(measure);
	}
	
	
	public void getLastOf(AttributeMeasuredEntity attrMeasureEntity, int lastN){
		
	}
}
