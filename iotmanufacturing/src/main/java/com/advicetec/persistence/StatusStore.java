package com.advicetec.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.AttributeType;
import com.advicetec.core.AttributeValue;
import com.advicetec.core.MeasuringUnit;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.UnitMeasureSymbol;

/**
 * This class keeps attributes and attribute values of a measured entity.
 * This class represents the measured entitiy <i>Status</i>.
 * The <i>Status</i> models the most recent values related to a measured entity.
 * It uses lists of attributes and the attribute values.
 * 
 * @author advicetec
 *
 */
public class StatusStore {

	static Logger logger = LogManager.getLogger(StatusStore.class.getName());
	/**
	 * List of attribute values
	 */
	private HashMap<String, AttributeValue> values;
	/**
	 * List of attributes
	 */
	private HashMap<String, Attribute> attributes; 

	/**
	 * Constructor
	 */
	public StatusStore(){
		attributes = new HashMap<String, Attribute>();
		values = new HashMap<String, AttributeValue>();
	}

	/**

	 * Stores the Measured Attribute into the Status.
	 * 
	 * @param attribute attribute object for the measured entity.
	 * @return The previous value for that Attribute of null if there is not previous.
	 * @throws Exception If the type or unit of the given parameter do not 
	 * match with the Attribute already stored.
	 */
	public void setAttribute( Attribute attribute) throws Exception{

		// if the attribute already exists in the list, then verify units and type
		if(attributes.containsKey(attribute.getName()))
		{
			Attribute old = attributes.get(attribute.getName());
							
			if( !attribute.getType().equals(old.getType())){
				logger.error("Error -- attribute has different unit or type");
				throw new Exception("Error -- attribute has different unit or type");
			} else if ((attribute.getUnit() == null) && (old.getUnit() != null)){
				logger.error("Error -- attribute has different unit or type");
				throw new Exception("Error -- attribute has different unit or type");
			} else if (
					(attribute.getUnit() != null) && 
					 (old.getUnit() != null)){
				     boolean equal = (attribute.getUnit()).equals(old.getUnit());
					 if (equal==false){
						logger.error("Error -- attribute has different unit or type");
						throw new Exception("Error -- attribute has different unit or type");
					 }
				
			}  else {
				// updating the attribute value is safe
				old.update(attribute);
			}
		}
		else { 
			// if the attribute does not exists in the STATUS
			// insert the value
			attributes.put(attribute.getName(), attribute);
		}
	}


	/**
	 * Returns a collection of Measured Attribute Values.
	 * @return a collection of Measured Attribute Values.
	 */
	public Collection<Attribute> getStatus(){
		return attributes.values();
	}

	/**
	 * Sets a collection of attributes.
	 * @param atts List of attributes.
	 * @throws Exception If the type or unit of the given parameter do not 
	 * match with the Attribute already stored.
	 * @see #setAttribute
	 */
	public void setAttributes( Collection<Attribute> atts) throws Exception{
		for (Attribute attribute : atts) {
			setAttribute(attribute);
		}
	}

	/**
	 * Returns an attribute object mapped to the given attribute name. 
	 * @param attrName Attribute name to search for.
	 * @return The specified attribute to which the given key is mapped, or 
	 * <code>NULL</code> if this Store does not contain the given attribute name.
	 * 
	 */
	public Attribute getAttribute(String attrName){
		return  attributes.get(attrName);
	}
	
	/**
	 * Imports a symbol table from the interpreter to the cache of Attribute List.
	 * @param symbols maps names and symbols objects from the language.
	 * @param origin 
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
				newAttr.setTrend(attSymbol.getTrend());
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

	public List<Attribute> getTrendAttributes() {
		
		List<Attribute> ret = new ArrayList<Attribute>();
		for (Map.Entry<String, Attribute> pair : attributes.entrySet()) {
			logger.info("Attribute:" + pair.getKey() + " trend:" + pair.getValue().getTrend());
			if (pair.getValue().getTrend()){
				logger.info("inserting in the return list");
				ret.add(pair.getValue());
			}
		}
		
		logger.info("Nbr Attributes returned:" + ret.size());
		return ret;
	}
}
