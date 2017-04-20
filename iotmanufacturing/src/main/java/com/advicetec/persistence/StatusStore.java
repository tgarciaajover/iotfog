package com.advicetec.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
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
 * This class stores the measured entity status which comprises
 * the list of attributes and the attribute values.
 * 
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
	 * Stores the Measured Attribute into the Status.
	 * 
	 * @param entityName Name or id for the measured entity.
	 * @param attrName Name or Id for the attribute.
	 * @param attribute Attribute Value.
	 * @return The previous value for that Attribute of null if there is not previous.
	 */
	public void setAttribute( Attribute attribute)throws Exception{

		// if the attribute already exists in the list, then verify units and type
		if(attributes.containsKey(attribute.getName())){
			Attribute old = attributes.get(attribute.getName());
			if( !attribute.getType().equals(old.getType()) || !attribute.getUnit().equals(old.getUnit())){
				throw new Exception("Error -- attribute has different unit or type");
			}
		}
		// updates the value
		attributes.put(attribute.getName(), attribute);
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
	 * 
	 * @param valueMap 
	 * @param parent Identificator from the MeasuredEntity
	 * @param parentType Type of the Measured Entity.
	 */
	public void importAttributeValues(Map<String, ASTNode> valueMap, String parent, MeasuredEntityType parentType) {

		for (Attribute att : attributes.values()) {
			if(valueMap.containsKey(att.getName())){
				ASTNode node = valueMap.get(att.getName());
				switch(att.getType()){
				case BOOLEAN:
					setAttributeValue(att, node.asBoolean(), parent, parentType);
					break;

				case INT:
					setAttributeValue(att, node.asInterger(), parent, parentType);
					break;

				case DOUBLE:
					setAttributeValue(att, node.asDouble(), parent, parentType);
					break;

				case STRING:
					setAttributeValue(att, node.asString(), parent, parentType);
					break;

				case DATETIME:
					setAttributeValue(att, node.asDateTime(), parent, parentType);
					break;

				case DATE:
					setAttributeValue(att, node.asDate(), parent, parentType);
					break;

				case TIME:
					setAttributeValue(att, node.asTime(), parent, parentType);
					break;

				default:
					break;
				}
			}
		}
	}


	/**
	 * Adds to the STATUS a new Attribute Value.
	 * @param attributeValue The attribute value to be set.
	 */
	public void setAttributeValue(AttributeValue attributeValue) {
		// check if the attribute is already in the attribute list.
		if(!attributes.containsKey(attributeValue.getKey())){
			attributes.put(attributeValue.getKey(), attributeValue.getAttribute());
		}
		values.put(attributeValue.getKey(), attributeValue);
	}

	/**
	 * Adds to the STATUS a new Attribute Value.
	 * @param att The Attribute
	 * @param value The Value
	 * @param parent Id of the measured entity
	 * @param parentType Type of measured entity.
	 */
	public void setAttributeValue(Attribute att, Object value,String parent, MeasuredEntityType parentType) {
		setAttributeValue(new AttributeValue(att.getName(), att, value, parent, parentType));
	}

	public Collection<AttributeValue> getAttributeValues(){
		return values.values();
	}
	
	/**
	 * Returns the Entity Status as JSON array.
	 * @return
	 */
	public JSONArray getJsonAtrributesValues(){
		return new JSONArray(getAttributeValues());
	}
}




