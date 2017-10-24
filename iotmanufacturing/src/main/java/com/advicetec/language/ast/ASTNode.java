package com.advicetec.language.ast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.advicetec.measuredentitity.MeasuringState;

/**
 * This class represents any value that is evaluated during language interpretation.
 * 
 * @author Andres Marentes
 */
public class ASTNode 
{

    /**
     * Value taken by void expressions.
     */
    public static ASTNode VOID = new ASTNode(new Object());

    /**
     * Value 
     */
    private Object value;

    /**
     * Constructor for the class, it takes an generic object 
     * 	and save it on the internal object attribute 
     * 
     * @param value  value which this ASTNode maintains.
     */
    public ASTNode(Object value) {
        this.value = value;
    }
    
    /**
     * Gets the class name of the value being maintained.
     * @return values class name
     */
    public String getObjectClassName()
    {
    	return value.getClass().getName();
    }

    /**
     * Establishes if the value maintained is void 
     * 
     * @return true if void, false otherwise.
     */
    public boolean isVOID(){
    	return this.value == VOID;
    }

    /**
     * Returns the boolean object maintained in the Node
     * 
     * @return boolean object
     */
    public Boolean asBoolean() {
    	return (Boolean)value;
    }

    /**
     * Returns the boolean array object maintained in the Node
     * 
     * @return boolean array object
     */
    public Boolean[] asBooleanVector(){
    	return (Boolean[])value;
    }

    /**
     * Returns the Double object maintained in the Node
     * 
     * @return Double object
     */
    public Double asDouble() {
    	return (Double)value;
    }

    /**
     * Returns the Double array object maintained in the Node
     * 
     * @return Double array object
     */
    public Double[] asDoubleVector(){
    	return (Double[])value; 
    }

    /**
     * Returns the String object maintained in the Node
     * 
     * @return String object
     */
    public String asString() {
    	return String.valueOf(value);
    }

    /**
     * Returns the String array object maintained in the Node
     * 
     * @return String array object
     */
    public String[] asStringVector(){
    	return (String[])value;
    }

    /**
     * Returns the Integer object maintained in the Node
     * 
     * @return Integer object
     */
    public Integer asInterger() {
    	return (Integer)value;
    }

    /**
     * Returns the Integer array object maintained in the Node 
     * 
     * @return Integer array object
     */
    public Integer[] asIntegerVector(){
    	return (Integer[])value;
    }

    /**
     * Returns the Date object maintained in the Node
     * 
     * @return Date object
     */
    public LocalDate asDate(){
    	return (LocalDate)value;
    }

    /**
     * Returns the Date array object maintained in the Node
     * 
     * @return Date array object
     */
    public LocalDate[] asDateVector(){
    	return (LocalDate[])value;
    }

    /**
     * Returns the Time object maintained in the Node
     * 
     * @return time object
     */
    public LocalTime asTime()
    {
    	return (LocalTime)value;
    }

    /**
     * Returns the Time array object maintained in the Node
     * 
     * @return Time array object
     */
    public LocalTime[] asTimeVector(){
    	return (LocalTime[])value;
    }

    /**
     * Returns the DateTime object maintained in the Node
     * 
     * @return DateTime object
     */
    public LocalDateTime asDateTime(){
    	return (LocalDateTime)value;
    }

    /**
     * Returns the DateTime array object maintained in the Node
     * 
     * @return DateTime array object
     */
    public LocalDateTime[] asDateTimeVector(){
    	return (LocalDateTime[])value; 
    }

    /**
     * Returns the measuring state maintained in the node
     * 
     * @return a measuring state object
     */
    public MeasuringState asMeasuringState() {
    	return (MeasuringState) value;
    }
    
    /**
     * Establishes if the value maintained is Double
     * 
     * @return true if Double, false otherwise.
     */
    public boolean isDouble() {
    	return value instanceof Double;
    }

    /**
     * Establishes if the value maintained is Double array
     * 
     * @return true if Double array, false otherwise.
     */
    public boolean isDoubleVector(){
    	return value instanceof Double[];
    }

    /**
     * Establishes if the value maintained is String
     * 
     * @return true if string, false otherwise.
     */
    public boolean isString(){
    	return value instanceof String;
    }

    /**
     * Establishes if the value maintained is String array
     * 
     * @return true if string array, false otherwise.
     */
    public boolean isStringVector(){
    	return value instanceof String[];
    }

    /**
     * Establishes if the value maintained is Integer
     * 
     * @return true if Integer, false otherwise.
     */
    public boolean isInteger(){
    	return value instanceof Integer;
    }

    /**
     * Establishes if the value maintained is Integer array
     * 
     * @return true if Integer array, false otherwise.
     */
    public boolean isIntegerVector(){
    	return value instanceof Integer[];
    }

    /**
     * Establishes if the value maintained is Boolean
     * 
     * @return true if Boolean, false otherwise.
     */
    public boolean isBoolean(){
    	return value instanceof Boolean;
    }

    /**
     * Establishes if the value maintained is Boolean array
     * 
     * @return true if Boolean array, false otherwise.
     */
    public boolean isBooleanVector(){
    	return value instanceof Boolean[];
    }

    /**
     * Establishes if the value maintained is Date
     * 
     * @return true if Date, false otherwise.
     */
    public boolean isDate(){
    	return value instanceof LocalDate;
    }

    /**
     * Establishes if the value maintained is Date array
     * 
     * @return true if Date array, false otherwise.
     */
    public boolean isDateVector(){
    	return value instanceof LocalDate[];
    }

    /**
     * Establishes if the value maintained is Time
     * 
     * @return true if Time, false otherwise.
     */
    public boolean isTime()
    {
    	return value instanceof LocalTime;
    }

    /**
     * Establishes if the value maintained is Time array
     * 
     * @return true if Time array, false otherwise.
     */
    public boolean isTimeVector()
    {
    	return value instanceof LocalTime[];
    }

    /**
     * Establishes if the value maintained is DateTime
     * 
     * @return true if DateTime, false otherwise.
     */
    public boolean isDateTime(){
    	return value instanceof LocalDateTime;
    }

    /**
     * Establishes if the value maintained is DateTime array
     * 
     * @return true if DateTime array, false otherwise.
     */
   public boolean isDateTimeVector(){
    	return value instanceof LocalDateTime[];
    }

   public boolean isMeasuringState() {
	   return value instanceof MeasuringState;
   }
   
   /**
    * Gets a Hashcode from the value maintained
    * 
    * @return Hashcode
    */
    @Override
    public int hashCode() {

    	if(value == null) {
    		return 0;
    	}

    	return this.value.hashCode();
    }

    /**
     * Implements the equals method between two ASTNodes 
     * 
     * Two AST nodes are equal if they have the same class and 
     * are equal according to the equals method of the underlying value class. 
     * 
     * @return true equals, false otherwise. 
     */
    @Override
    public boolean equals(Object o) {

    	if(value == o) {
    		return true;
    	}

    	if(value == null || o == null || o.getClass() != value.getClass()) {
    		return false;
    	}

    	ASTNode that = (ASTNode)o;

    	return this.value.equals(that.value);
    }

    /**
     * Serialized string for the ASTNode.
     * 
     * @return Serialization string 
     */
    @Override
    public String toString() {
    	if (isBoolean())
    		return asBoolean().toString();
    	else
    		return String.valueOf(value);
    }
}