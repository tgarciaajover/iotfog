package com.advicetec.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeType;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.MeasuringUnit;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.measuredentitity.MeasuredEntityType;

/**
 * This class stores the measured entity status.
 * @author user
 *
 */
public class StatusStore {

	private HashMap<String, AttributeValue> values;
	private HashMap<String, Attribute> attributes; 

	public StatusStore(){
		attributes = new HashMap<String, Attribute>();
		values = new HashMap<String, AttributeValue>();
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
		if(attributes.containsKey(value.getName())){
			Attribute old = attributes.get(value.getName());
			if( !value.getType().equals(old.getType()) || !value.getUnit().equals(old.getUnit())){
				throw new Exception("Error -- attribute has different unit or type");
			}
		}
		return attributes.put(value.getName(), value);
	}


	/**
	 * Returns a collection of Measured Attribute Values
	 * @return
	 */
	public Collection<Attribute> getStatus(){
		return attributes.values();
	}


	public void setAttributes( Collection<Attribute> atts) throws Exception{
		for (Attribute attribute : atts) {
			setAttribute(attribute);
		}
	}

	/**
	 * Imports a symbol table from the interpreter to the Attribute List.
	 * @param measuringEntity
	 * @param map 
	 * @param attrMap
	 */
	public void importSymbols( Map<String, Symbol> symbols ) {

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

				case tBOOL:
					attributeType = AttributeType.BOOLEAN;
					break;


				default:
					break;
				}

				// finally creates the attribute.
				Attribute newAttr = new Attribute(attrName, attributeType, measurinUnit);
				attributes.put(attrName, newAttr);
			}
		}
	}

	/**
	 * This method 
	 * @param valueMap
	 */
	public void importAttributeValues(Map<String, ASTNode> valueMap, String parent, MeasuredEntityType parentType) {

		for (Attribute att : attributes.values()) {
			if(valueMap.containsKey(att.getName())){
				ASTNode node = valueMap.get(att.getName());
				switch(att.getType()){
				case BOOLEAN:
					values.put(att.getName(), new AttributeValue(att.getName(), att, node.asBoolean(), parent, parentType));
					break;

				case INT:
					values.put(att.getName(), new AttributeValue(att.getName(), att, node.asInterger(), parent, parentType));
					break;

				case DOUBLE:
					values.put(att.getName(), new AttributeValue(att.getName(), att, node.asDouble(), parent, parentType));
					break;

				case STRING:
					values.put(att.getName(), new AttributeValue(att.getName(), att, node.asString(), parent, parentType));
					break;

				case DATETIME:
					values.put(att.getName(), new AttributeValue(att.getName(), att, node.asDateTime(), parent, parentType));
					break;

				case DATE:
					values.put(att.getName(), new AttributeValue(att.getName(), att, node.asDate(), parent, parentType));
					break;

				case TIME:
					values.put(att.getName(), new AttributeValue(att.getName(), att, node.asTime(), parent, parentType));
					break;

				default:
					break;
				}
			}
		}
	}


	public Collection<AttributeValue> getAttributeValues(){
		return values.values();
	}
}




