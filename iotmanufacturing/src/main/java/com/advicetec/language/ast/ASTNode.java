package com.advicetec.language.ast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ASTNode 
{

    public static ASTNode VOID = new ASTNode(new Object());

    private Object value;

    public ASTNode(Object value) {
        this.value = value;
    }
    
   public String getObjectClassName()
   {
	   return value.getClass().getName();
   }

    public boolean isVOID(){
    	return this.value == VOID;
    }
    
    public Boolean asBoolean() {
        return (Boolean)value;
    }
    
    public Boolean[] asBooleanVector(){
    	return (Boolean[])value;
    }

    public Double asDouble() {
        return (Double)value;
    }

    public Double[] asDoubleVector(){
    	return (Double[])value; 
    }
    
    public String asString() {
        return String.valueOf(value);
    }

    public String[] asStringVector(){
    	return (String[])value;
    }
    
    public Integer asInterger() {
    	return (Integer)value;
    }
    
    public Integer[] asIntegerVector(){
    	return (Integer[])value;
    }
    
    public LocalDate asDate(){
    	return (LocalDate)value;
    }
    
    public LocalDate[] asDateVector(){
    	return (LocalDate[])value;
    }
    
    public LocalTime asTime()
    {
    	return (LocalTime)value;
    }
    
    public LocalTime[] asTimeVector(){
    	return (LocalTime[])value;
    }
    
    public LocalDateTime asDateTime(){
    	return (LocalDateTime)value;
    }
    
    public LocalDateTime[] asDateTimeVector(){
    	return (LocalDateTime[])value; 
    }
    
    public boolean isDouble() {
        return value instanceof Double;
    }
    
    public boolean isDoubleVector(){
    	return value instanceof Double[];
    }

    public boolean isString(){
    	return value instanceof String;
    }
    
    public boolean isStringVector(){
    	return value instanceof String[];
    }
    
    public boolean isInteger(){
    	return value instanceof Integer;
    }
    
    public boolean isIntegerVector(){
    	return value instanceof Integer[];
    }
    
    public boolean isBoolean(){
    	return value instanceof Boolean;
    }

    public boolean isBooleanVector(){
    	return value instanceof Boolean[];
    }
    
    public boolean isDate(){
    	return value instanceof LocalDate;
    }
    
    public boolean isDateVector(){
    	return value instanceof LocalDate[];
    }
    
    public boolean isTime()
    {
    	return value instanceof LocalTime;
    }
    
    public boolean isTimeVector()
    {
    	return value instanceof LocalTime[];
    }
    
    public boolean isDateTime(){
    	return value instanceof LocalDateTime;
    }
    
    public boolean isDateTimeVector(){
    	return value instanceof LocalDateTime[];
    }
    
    @Override
    public int hashCode() {

        if(value == null) {
            return 0;
        }

        return this.value.hashCode();
    }

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

    @Override
    public String toString() {
    	if (isBoolean())
    		return asBoolean().toString();
    	else
    		return String.valueOf(value);
    }
}