package com.advicetec.measuredentitity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.w3c.dom.Document;

import com.advicetec.configuration.ReasonCode;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.TimeInterval;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.Symbol;
import com.advicetec.core.AttributeValue;
import com.advicetec.persistence.MeasureAttributeValueCache;
import com.advicetec.persistence.StateIntervalCache;
import com.advicetec.persistence.StatusStore;


/**
 * This class is a MeasuredEntity facade.
 * It allows the language processor to access some functionality
 * from the MeasuredEntity without expose all its methods.
 *   
 * @author maldofer
 *
 */
public final class MeasuredEntityFacade {


	static Logger logger = LogManager.getLogger(MeasuredEntityFacade.class.getName());
	
	private MeasuredEntity entity;
	// Keeps an in-memory entity status
	private StatusStore status;
	private Map<String,SortedMap<LocalDateTime,String>> attMap;
	private MeasureAttributeValueCache attValueCache;
	// This map stores endTime, startTime,
		private TreeMap<LocalDateTime,String> intervalMap;
	private StateIntervalCache stateCache;

	
	public MeasuredEntityFacade(MeasuredEntity entity) 
	{
		this.entity = entity;
		status = new StatusStore();
		attValueCache= MeasureAttributeValueCache.getInstance();
		attMap = new HashMap<String,SortedMap<LocalDateTime,String>>();
		intervalMap = new TreeMap<LocalDateTime,String>();
		stateCache = StateIntervalCache.getInstance();	
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
	 * Sets an Attribute.
	 * @param attribute
	 * @throws Exception If the new type of the attribute does not match the 
	 * previous type.
	 */
	public synchronized void setAttribute(Attribute attribute) throws Exception{
		// returns the previous value
		status.setAttribute(attribute);
	}

	/**
	 * Sets or updates the attribute value into the status and store.
	 * @param attrValue
	 */
	public synchronized void setAttributeValue(AttributeValue attrValue){
		
		MeasuredAttributeValue mav = new MeasuredAttributeValue(attrValue.getAttr(), attrValue.getValue(),
				attrValue.getGenerator(), attrValue.getGeneratorType(), LocalDateTime.now());
		// stores this attributeValue into cache
		attValueCache.cacheStore(mav);
		// stores this value into status
		status.setAttributeValue(attrValue);
		

		// The key for a measuredAttributeValue is the name of the attribute plus the timestamp
		// The key for an attributeValue is the name of the attribute. 
		String attName = mav.getAttr().getName();
		SortedMap<LocalDateTime, String> internalMap = attMap.get(attName);
		if(internalMap == null){
			attMap.put(attName,new TreeMap<LocalDateTime, String>());
			internalMap = attMap.get(attName);
		}
		internalMap.put(mav.getTimeStamp(), mav.getKey());
	}

	/**
	 * Returns the newest Measured Attribute Value for a given Attribute name.
	 * @param attName
	 * @return
	 */
	public AttributeValue getNewestByAttributeName(String attName){
		return status.getAttributeValueByName(attName);
	}


	/**
	 * 
	 * @param valueMap 
	 * @param parent Identificator from the MeasuredEntity
	 * @param parentType Type of the Measured Entity.
	 */
	public void importAttributeValues(Map<String, ASTNode> valueMap, Integer parent, MeasuredEntityType parentType) {

		logger.debug("entering importAttributeValues" + valueMap.size() + " attribute status count:" + status.getAttributeSize()); 
		
		for ( String attrName : valueMap.keySet()){
			
			logger.debug("importAttributeValue:" + attrName);
			
			Attribute att = status.getAttribute(attrName);
			
			if( att != null )
			{
				
				ASTNode node = valueMap.get(att.getName());

				if (node.isVOID()){
					logger.warn("The attribute:" + att.getName() +" is declared but it is not initialized !!!");
				} 
				else 
				{ 
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
						logger.error("The type:" + att.getType().getName() + " of the attribute value:" + att.getName() + " is not supported");
						break;
					}
				}
			} else {
				logger.error("The attribute: " + attrName + " is not in the status");
			}
			
		}
	}


	/**
	 * Adds to the STATUS a new Attribute Value.
	 * @param att The Attribute
	 * @param value The Value
	 * @param parent Id of the measured entity
	 * @param parentType Type of measured entity.
	 */
	public void setAttributeValue(Attribute att, Object value,Integer parent, MeasuredEntityType parentType) {

		logger.debug("inserting attribute value -attr:" + att.getName() + " value:" + value.toString() );
		setAttributeValue(new AttributeValue(att.getName(), att, value, parent, parentType));

	}	

	/**
	 * Returns a list of attribute values for a given interval.
	 * 
	 * @param attrName Attribute name.
	 * @param from Time from.
	 * @param to Time to.
	 * @return
	 */
	public ArrayList<AttributeValue> getByIntervalByAttributeName(
			String attrName, LocalDateTime from, LocalDateTime to){
		
		LocalDateTime oldest = attValueCache.getOldestTime();
		
		if(!attMap.containsKey(attrName)){
			System.out.println("attribute is not in facade");
			return null;
		}
		SortedMap<LocalDateTime,String> internalMap = attMap.get(attrName);
		
		// all values are in the cache
		if(oldest.isBefore(from)){
			SortedMap<LocalDateTime,String> subMap = internalMap.subMap(from, to);
			Collection<String> keys = subMap.values();
			String[] keyArray = keys.toArray( new String[keys.size()]);
			return getFromCache(keyArray);
			
		}else if(oldest.isAfter(to)){
			// get all values from database
			return attValueCache.getFromDatabase(entity.getId(),attrName,from, oldest);
		}else{
			SortedMap<LocalDateTime,String> subMap = internalMap.subMap(oldest, to);
			Collection<String> keys = subMap.values();
			String[] keyArray = keys.toArray( new String[keys.size()]);
			
			ArrayList<AttributeValue> newList = attValueCache.getFromDatabase(entity.getId(),attrName,from, oldest);
			newList.addAll(getFromCache(keyArray));
			
			return newList;
		}
	}

	
	
	/**
	 * 
	 * @param oldest
	 */
	public void deleteOldValues(LocalDateTime oldest){
		for(SortedMap<LocalDateTime, String> internalMap : attMap.values()){
			// replace the map with the last entries. 
			internalMap = internalMap.tailMap(oldest);
		}
	}
	
	
	public String getByIntervalByAttributeNameJSON(
			String attrName, LocalDateTime from, LocalDateTime to){

		ArrayList<AttributeValue> ret = getByIntervalByAttributeName(attrName, from, to);

		ObjectMapper mapper = new ObjectMapper();
		String jsonText=null;

		try {
			jsonText = mapper. writeValueAsString(ret);

		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonText;
	}

	/**
	 * Returns a list with the last N attribute values for a given
	 * attribute name.
	 * 
	 * @param attrName The name of the attribute.
	 * @param n The number of values for the given attribute.
	 * @return The list of Measured Attribute Values. NULL if there are not 
	 * values for the given attribute name.
	 * 
	 */
	public List<AttributeValue> getLastNbyAttributeName(String attrName, int n){
		ArrayList<AttributeValue> maValues = new ArrayList<AttributeValue>();

		if(!attMap.containsKey(attrName)){
			return null;
		}

		SortedMap<LocalDateTime,String> internalMap = attMap.get(attrName);
		String[] keyArray = (String[]) internalMap.values().toArray();
		if(n>= internalMap.size()){
			return getFromCache(keyArray);
		}
		else{
			for (int i = keyArray.length - n - 1; i < keyArray.length; i++) {
				maValues.add(attValueCache.getFromCache(keyArray[i]));
			}
			return maValues;
		}
	}


	/**
	 * Returns a list of Measured Attribute Values, from the cache,
	 * @param keyArray
	 * @return
	 */
	private ArrayList<AttributeValue> getFromCache(String[] keyArray){
		ArrayList<AttributeValue> maValues = new ArrayList<AttributeValue>();
		for (String key : keyArray) {
			maValues.add(attValueCache.getFromCache(key));
		}
		return maValues;
	}

	public void importSymbols(Map<String, Symbol> symbolMap, AttributeOrigin origin) throws Exception {
		status.importSymbols(symbolMap, origin);
	}

	public Collection<Attribute> getStatus(){
		return status.getStatus();
	}
	
	/**
	 * Returns the measured Entity status. 
	 * @return json array
	 */
	public JSONArray getStatusJSON(){
		return new JSONArray(getStatusValues());
	}
	
	
	public void importAttributeValues(Map<String, ASTNode> valueMap) {
		importAttributeValues(valueMap,entity.getId(),entity.getType());

	}

	public Collection<AttributeValue> getStatusValues(){
		return status.getAttributeValues();
	}

	public void getJsonStatesByInterval(TimeInterval timeInterval){
		entity.getStateByInterval(timeInterval);
	}

	public void registerInterval(MeasuringState status, ReasonCode reasonCode, TimeInterval interval)
	{
		StateInterval stateInterval = new StateInterval(status, reasonCode, interval, entity.getId(), entity.getType());
		stateInterval.setKey(entity.getId()+stateInterval.getKey());
		// key in the map and the cache must be consistent
		intervalMap.put(interval.getStart(),stateInterval.getKey());
		StateIntervalCache.getInstance().storeToCache(stateInterval);
	}


	/**
	 * Returns a
	 * @param from
	 * @param to
	 * @return
	 */
	public String statesByInterval(LocalDateTime from, LocalDateTime to){

		ArrayList<StateInterval> intervals = getStatesByInterval(from, to);

		ObjectMapper mapper = new ObjectMapper();
		String jsonText=null;

		try {

			jsonText = mapper. writeValueAsString(intervals);

		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return jsonText;

	}

	public String statesByInterval(TimeInterval interval){
		ArrayList<StateInterval> intervals = getStatesByInterval(interval.getStart(), interval.getEnd());

		ObjectMapper mapper = new ObjectMapper();
		String jsonText=null;

		try {

			jsonText = mapper. writeValueAsString(intervals);

		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return jsonText;

	}


	/**
	 * Returns the list of intervals between two datetimes.
	 * @param from Beginning time
	 * @param to Ending time.
	 * @return List of intervals.
	 */
	public ArrayList<StateInterval> getStatesByInterval(LocalDateTime from, LocalDateTime to){
		ArrayList<StateInterval> list = new ArrayList<StateInterval>();
		SortedMap<LocalDateTime, String> subMap = intervalMap.subMap(from, to);
		for (Map.Entry<LocalDateTime, String> entry : subMap.entrySet()) {
			list.add(stateCache.getFromCache(entry.getValue()));
		}
		return list;
	}

	/**
	 * Commands to the cache to store their attribute values into the database 
	 * and clean the cache.
	 */
	public void storeAllAttributeValues(){
		attValueCache.bulkCommit(getAllKeysFromAttributeMap());
	}

	/**
	 * Returns the list of keys from the map of attributes.
	 * @return
	 */
	private List<String> getAllKeysFromAttributeMap() {
		List<String> attKeys = new ArrayList<String>();
		for(Map<LocalDateTime,String> map:attMap.values()){
			attKeys.addAll(map.values());
		}
		return attKeys;
	}

	/**
	 * Commands to the cache to store all intervals into the database 
	 * and clean the cache.
	 */
	public void storeAllStateIntervals(){
		stateCache.bulkCommit(new ArrayList<String>(intervalMap.values()));
	}

	/**
	 * Returns the Entity Status into a XML document.
	 * @return
	 * @throws ParserConfigurationException
	 * @throws JAXBException
	 */
	public Document getXmlStatus() throws ParserConfigurationException, JAXBException{
		return status.toXml();
	}

}

