package com.advicetec.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeType;
import com.advicetec.core.MeasuringUnit;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.measuredentitity.MeasuredAttributeValue;

/**
 * This class stores the measured entity status.
 * @author user
 *
 */
public class StatusStore {

	private String measuredEntity;
	private HashMap<String, Attribute> store; 

	public StatusStore(String measuredEntity){
		this.measuredEntity = measuredEntity;
		store = new HashMap<String, Attribute>();
	}

	/**
	 * Stores the Measured Attribute into the status store.
	 * 
	 * @param entityName Name or id for the measured entity.
	 * @param attrName Name or Id for the attribute.
	 * @param value Attribute Value.
	 * @return The previous value for that Attribute of null if there is not previous.
	 */
	public Attribute setAttribute( Attribute value)throws Exception{

		// if the attribute already exists in the list, verify units and type
		if(store.containsKey(value.getName())){
			Attribute old = store.get(value.getName());
			if( !value.getType().equals(old.getType()) || !value.getUnit().equals(old.getUnit())){
				throw new Exception("Error -- attribute has different unit or type");
			}
		}
		return store.put(value.getName(), value);
	}


	/**
	 * Returns a collection of Measured Attribute Values
	 * @return
	 */
	public Collection<Attribute> getStatus(){
		return store.values();
	}


	public void setAttributes( Collection<Attribute> attributes) throws Exception{
		for (Attribute attribute : attributes) {
			setAttribute(attribute);
		}
	}

	/**
	 * Imports a symbol table from the interpreter to the Attribute List.
	 * @param measuringEntity
	 * @param map 
	 * @param attrMap
	 */
	public void importSymbols(String measuringEntity, Map<String, Symbol> symbols ) {

		Map<String, Attribute> attributes = new HashMap<String, Attribute>();

		for (Map.Entry<String, Symbol> entry : symbols.entrySet()) {
			if(entry.getValue() instanceof AttributeSymbol){
				AttributeSymbol attSymbol = (AttributeSymbol) entry.getValue(); 
				String attrName = attSymbol.getName();
				
				String attrUnitName = attSymbol.getUnitOfMeasure();
				
				// MeasuringUnit
				MeasuringUnit measurinUnit = null;
				if(symbols.containsKey(attrUnitName) && 
						(symbols.get(attrUnitName) instanceof UnitMeasureSymbol)){
					UnitMeasureSymbol unitMeasureSymbol = (UnitMeasureSymbol) symbols.get(attrUnitName); 
					measurinUnit = new MeasuringUnit(unitMeasureSymbol.getName(), unitMeasureSymbol.getDescription());
				}
				
				// AttributeType
				AttributeType attributeType = null;
				switch (attSymbol.getType()) {
				case tINT:
					attributeType = AttributeType.INT;
					break;

				case tFLOAT:
					attributeType = AttributeType.DOUBLE;
					break;
					
				case tDATETIME:
					attributeType = AttributeType.DATETIME;
					break;
					
				case tSTRING:
					attributeType = AttributeType.STRING;
					break;
				default:
					break;
				}
				Attribute newAttr = new Attribute(attrName, attributeType, measurinUnit);
				
			}
			store.put(measuringEntity, attributes);
		}
	}
	
	
}




