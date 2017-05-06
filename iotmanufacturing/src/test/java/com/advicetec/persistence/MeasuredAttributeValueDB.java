package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.junit.Test;

import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeType;
import com.advicetec.measuredentitity.MeasuredAttributeValue;
import com.advicetec.core.MeasuringUnit;
import com.advicetec.measuredentitity.MeasuredEntityType;

public class MeasuredAttributeValueDB 
{

	
	@Test
	public void InsertDoubleMeasuredAttributeValue()
	{
		Connection conn  = null; 
        PreparedStatement pst = null;
		
        try
        {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/iotajover", "iotajover", "iotajover");
			
			conn.setAutoCommit(false);
			
			LocalDateTime nowe = LocalDateTime.now(); 
			
			MeasuringUnit measure = new MeasuringUnit("KG", "Kilogram");  
			Attribute atr = new Attribute("vel", AttributeType.DOUBLE, measure);
			
			MeasuredAttributeValue value = new MeasuredAttributeValue(atr, new Double(10.01), 10, MeasuredEntityType.MACHINE, nowe);
			
			String insertSQL = value.getPreparedInsertText();
			
			pst = conn.prepareStatement(insertSQL);
			
			value.dbInsert(pst);
			pst.executeBatch();
			
			conn.commit();
			
        } catch(Exception e){
        	e.printStackTrace();
        } finally{
            if(pst!=null)
            {
                try
                {
                    pst.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
             
            if(conn!=null) 
            {
                try
                {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
	}
	

	
	@Test
	public void InsertIntegerMeasuredAttributeValue()
	{
		Connection conn  = null; 
        PreparedStatement pst = null;
		
        try
        {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/iotajover", "iotajover", "iotajover");
			
			conn.setAutoCommit(false);
			
			LocalDateTime nowe = LocalDateTime.now(); 
			
			MeasuringUnit measure = new MeasuringUnit("KG", "Kilogram");  
			Attribute atr = new Attribute("velint", AttributeType.INT, measure);
			
			MeasuredAttributeValue value = new MeasuredAttributeValue(atr, new Integer(10), 10, MeasuredEntityType.MACHINE, nowe);
			
			String insertSQL = value.getPreparedInsertText();
			
			pst = conn.prepareStatement(insertSQL);
			
			value.dbInsert(pst);
			pst.executeBatch();
			
			conn.commit();
			
        } catch(Exception e){
        	e.printStackTrace();
        } finally{
            if(pst!=null)
            {
                try
                {
                    pst.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
             
            if(conn!=null) 
            {
                try
                {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
}