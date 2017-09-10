package com.advicetec.persistence;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.MeasuredEntityType;

public class ReadMeasureAttributeValues implements Runnable
{

	MeasureAttributeValueCache attValueCache = null;
	int measuredEntity;
	MeasuredEntityType mType;
	Attribute attribute;
	LocalDateTime from;
	LocalDateTime to;
	
	public ReadMeasureAttributeValues(int measuredEntity, MeasuredEntityType mType, Attribute attribute, LocalDateTime from,  LocalDateTime to)
	{
		attValueCache = MeasureAttributeValueCache.getInstance();
		this.measuredEntity = measuredEntity;
		this.mType = mType;
		this.attribute = attribute;
		this.from = from;
		this.to = to;
	}
	
	@Override
	public void run() {
		
		System.out.println("started at:" + LocalDateTime.now());
		ArrayList<AttributeValue> values = attValueCache.getFromDatabase(new Integer(measuredEntity), mType, attribute, from, to);
		System.out.println("ended at:" + LocalDateTime.now() + " num registers:" + values.size());
		
	}
	
}
