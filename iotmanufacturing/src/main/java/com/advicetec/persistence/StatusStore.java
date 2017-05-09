package com.advicetec.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.advicetec.configuration.DeviceTypeContainer;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.AttributeType;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.MeasuringUnit;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.advicetec.measuredentitity.MeasuredEntityType;

/**
 * This class stores the measured entity status which comprises
 * the list of attributes and the attribute values.
 * 
 * @author user
 *
 */
public class StatusStore {

	
	static Logger logger = LogManager.getLogger(StatusStore.class.getName());
	
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
	public void setAttribute( Attribute attribute) throws Exception{

		// if the attribute already exists in the list, then verify units and type
		if(attributes.containsKey(attribute.getName()))
		{
			Attribute old = attributes.get(attribute.getName());
							
			if( !attribute.getType().equals(old.getType())){
				String error = "Error -- attribute has different unit or type";
				logger.error(error);
				throw new Exception(error);
			} else if ((attribute.getUnit() == null) && (old.getUnit() != null)){
				String error = "Error -- attribute has different unit or type";
				logger.error(error);
				throw new Exception(error);
			} else if (
					(attribute.getUnit() != null) && 
					 (old.getUnit() != null)){
				     boolean equal = (attribute.getUnit()).equals(old.getUnit());
					 if (equal==false){
						String error = "Error -- attribute has different unit or type";
						logger.error(error);
						throw new Exception(error);
					 }
				
			}  else {
				old.update(attribute);
			}
		} 
		else { 
			// insert the value
			attributes.put(attribute.getName(), attribute);
		}
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

	
	public Attribute getAttribute(String name){
		return  attributes.get(name);
	}
	/**
	 * Imports a symbol table from the interpreter to the Attribute List.
	 * @param measuringEntity
	 * @param map 
	 * @param attrMap
	 * @throws Exception 
	 */
	public void importSymbols( Map<String, Symbol> symbols, AttributeOrigin origin ) throws Exception {

		logger.debug("Importing # symbols:" + symbols.size());
		
		for (Map.Entry<String, Symbol> entry : symbols.entrySet()) 
		{
			logger.debug("importing symbol:" + entry.getKey());
			
			if(entry.getValue() instanceof AttributeSymbol){
				AttributeSymbol attSymbol = (AttributeSymbol) entry.getValue(); 
				String attrName = attSymbol.getName();

				String attrUnitName = attSymbol.getUnitOfMeasure();
				
				// MeasuringUnit
				MeasuringUnit measurinUnit = null;
				if(symbols.containsKey(attrUnitName)){
					if ((symbols.get(attrUnitName) instanceof UnitMeasureSymbol)){
						UnitMeasureSymbol unitMeasureSymbol = (UnitMeasureSymbol) symbols.get(attrUnitName); 
						measurinUnit = new MeasuringUnit(unitMeasureSymbol.getName(), unitMeasureSymbol.getDescription());
					} 
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
				newAttr.setOrigin(origin);
				setAttribute(newAttr);
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
			attributes.put(attributeValue.getKey(), attributeValue.getAttr());
		}
		values.put(attributeValue.getKey(), attributeValue);
	}


	/**
	 * Returns all attribute values stored into the status.
	 * @return
	 */
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
	
	
	public Document toXml() throws ParserConfigurationException, JAXBException{
		 Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().newDocument();
		Element root = doc.createElement("status");
		JAXBContext jc = JAXBContext.newInstance(Attribute.class);
		Marshaller m = jc.createMarshaller();
		for (Attribute attr : attributes.values()) {
			Node node = doc.createElement("attribute");
			m.marshal(attr, node);
		}
		doc.appendChild(root);
		
		return doc;
	}

	public int getAttributeSize() {
		return attributes.size();
	}

	/**
	 * Returns the Attribute Value associated to this name.
	 * @param attrName AttributeValue name.
	 * @return The correspondent attribute value or NULL if this element 
	 * does not exist in the status.
	 */
	public AttributeValue getAttributeValueByName(String attrName){
		return values.get(attrName);
	}
}
