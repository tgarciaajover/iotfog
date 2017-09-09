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
	 * @param origin is one of the originator of the attribute.
	 * @throws Exception if the Status cannot insert an attribute from the given
	 * parameter.
	 * @see {@link AttributeOrigin}
	 * @see {@link Symbol}
	 */
	public void importSymbols( Map<String, Symbol> symbols, 
			AttributeOrigin origin ) throws Exception {

		logger.debug("Importing # symbols:" + symbols.size());
		
		for (Map.Entry<String, Symbol> entry : symbols.entrySet()) 
		{
			logger.debug("importing symbol:" + entry.getKey());
			// verifies each map entry
			if(entry.getValue() instanceof AttributeSymbol){
				AttributeSymbol attSymbol = (AttributeSymbol) entry.getValue(); 
				// gets name, unit, and measuring 
				String attrName = attSymbol.getName();
				String attrUnitName = attSymbol.getUnitOfMeasure();
				
				MeasuringUnit measurinUnit = null;
				if(symbols.containsKey(attrUnitName)){
					if ((symbols.get(attrUnitName) instanceof UnitMeasureSymbol)){
						UnitMeasureSymbol unitMeasureSymbol = 
								(UnitMeasureSymbol) symbols.get(attrUnitName); 
						measurinUnit = new MeasuringUnit(
								unitMeasureSymbol.getName(), 
								unitMeasureSymbol.getDescription());
					} 
			    } 

				// sets the attribute type
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
				// sets the new attribute into the cache.
				setAttribute(newAttr);
			}
		}		
	}



	/**
	 * Adds a new Attribute Value to the STATUS.
	 * @param attributeValue The attribute value to be set.
	 */
	public void setAttributeValue(AttributeValue attributeValue) {
		// check if the attribute is already in the attribute list.
		if(!attributes.containsKey(attributeValue.getKey())){
			// sets the attribute
			attributes.put(attributeValue.getKey(), attributeValue.getAttr());
		}
		// sets the value
		values.put(attributeValue.getKey(), attributeValue);
	}


	/**
	 * Returns all attribute values stored into the <i>status</i>.
	 * @return list of all attribute values stored into the <i>status</i>.
	 */
	public Collection<AttributeValue> getAttributeValues(){
		return values.values();
	}
	
	/**
	 * Returns a JSON array of attribute values in the Status.
	 * @return a JSON array of attribute values in the Status.
	 */
	public JSONArray getJsonAtrributesValues(){
		return new JSONArray(getAttributeValues());
	}
	
	/**
	 * Returns a XML representation of the all attribute values stored into the
	 * Status.<br>
	 * XML has the structure: <br><code> &lt;status&gt; <br> &lt;attribute&gt; 
	 * ... &lt;/attribute&gt; <br>&lt;/status&gt; </code>.
	 * 
	 * @return XML representation of the all attribute values stored in the
	 * Status.
	 * @throws ParserConfigurationException if there is a configuration error.
	 * @throws JAXBException if the XML marshaling fails.
	 */
	public Document toXml() throws ParserConfigurationException, JAXBException{
		// creates the document's root
		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().newDocument();
		Element root = doc.createElement("status");
		// XML - Java binding with the Attribute class
		JAXBContext jc = JAXBContext.newInstance(Attribute.class);
		Marshaller m = jc.createMarshaller();
		// creates a node for each value on the status
		for (Attribute attr : attributes.values()) {
			Node node = doc.createElement("attribute");
			m.marshal(attr, node);
		}
		doc.appendChild(root);
		
		return doc;
	}

	/**
	 * Returns the attribute status length.
	 * @return the attribute status length.
	 */
	public int getAttributeSize() {
		return attributes.size();
	}

	/**
	 * Returns the Attribute Value associated to a given attribute name.
	 * @param attrName AttributeValue name.
	 * @return the correspondent attribute value to the given attribute name 
	 * or <code>NULL</code> if the named element does not exist in the status.
	 */
	public AttributeValue getAttributeValueByName(String attrName){
		return values.get(attrName);
	}

	/**
	 * Returns the list of values marked as trend attributes.
	 * If the <code>attribute</code> object has the trend variable as 
	 * <code>TRUE</code>.
 	 * @return the list of values marked as trend attributes.
	 * @see Attribute#getTrend()
	 */
	public List<Attribute> getTrendAttributes() {
		
		List<Attribute> ret = new ArrayList<Attribute>();
		for (Map.Entry<String, Attribute> pair : attributes.entrySet()) {
			logger.debug("Attribute:" + pair.getKey() + " trend:" + pair.getValue().getTrend());
			// checks if the trend variable is set and adds it to the list
			if (pair.getValue().getTrend()){
				logger.debug("inserting in the return list");
				ret.add(pair.getValue());
			}
		}
		
		logger.info("Nbr Attributes returned:" + ret.size());
		return ret;
	}
}
