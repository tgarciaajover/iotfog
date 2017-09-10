package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

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

	@Test
	public void InsertStringMeasuredAttributeValue()
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
			Attribute atr = new Attribute("velStr", AttributeType.STRING, measure);
			
			MeasuredAttributeValue value = new MeasuredAttributeValue(atr, new String("10.01"), 10, MeasuredEntityType.MACHINE, nowe);
			
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
	public void InsertDateTimeMeasuredAttributeValue()
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
			Attribute atr = new Attribute("velDttm", AttributeType.DATETIME, measure);
			
			MeasuredAttributeValue value = new MeasuredAttributeValue(atr, LocalDateTime.now(), 10, MeasuredEntityType.MACHINE, nowe);
			
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
	public void InsertDateMeasuredAttributeValue()
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
			Attribute atr = new Attribute("velDt", AttributeType.DATE, measure);
			
			MeasuredAttributeValue value = new MeasuredAttributeValue(atr, LocalDate.now(), 10, MeasuredEntityType.MACHINE, nowe);
			
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
	public void InsertTimeMeasuredAttributeValue()
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
			Attribute atr = new Attribute("velTm", AttributeType.TIME, measure);
			
			MeasuredAttributeValue value = new MeasuredAttributeValue(atr, LocalTime.now(), 10, MeasuredEntityType.MACHINE, nowe);
			
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
	public void InsertBooleanMeasuredAttributeValue()
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
			Attribute atr = new Attribute("velBoolean", AttributeType.BOOLEAN, measure);
			
			MeasuredAttributeValue value = new MeasuredAttributeValue(atr, new Boolean(true), 10, MeasuredEntityType.MACHINE, nowe);
			
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
	public void batchInsert() throws InterruptedException
	{

		MeasuringUnit measure = new MeasuringUnit("KG", "Kilogram");  

		

		Attribute atr = new Attribute("velint", AttributeType.INT, measure);
		
		
		MeasureAttributeValueCache attValueCache = MeasureAttributeValueCache.getInstance();
		SortedMap<LocalDateTime,String> statesMap = new TreeMap<LocalDateTime,String>();
		
		int i = 0;
		for (; i <= 4000; i++) {
			MeasuredAttributeValue value = new MeasuredAttributeValue(atr, new Integer(i), 10, MeasuredEntityType.MACHINE, LocalDateTime.now());
			Thread.sleep(1);
			attValueCache.cacheStore(value);
			statesMap.put(value.getTimeStamp(), value.getKey());
		}
		
		Thread.sleep(10000);
		
		for (; i <= 8000; i++) {
			MeasuredAttributeValue value = new MeasuredAttributeValue(atr, new Integer(i), 10, MeasuredEntityType.MACHINE, LocalDateTime.now());
			Thread.sleep(1);
			attValueCache.cacheStore(value);
			statesMap.put(value.getTimeStamp(), value.getKey());
		}

		Thread.sleep(10000);

		for (; i <= 12000; i++) {
			MeasuredAttributeValue value = new MeasuredAttributeValue(atr, new Integer(i), 10, MeasuredEntityType.MACHINE, LocalDateTime.now());
			Thread.sleep(1);
			attValueCache.cacheStore(value);
			statesMap.put(value.getTimeStamp(), value.getKey());
		}

		Thread.sleep(10000);

		for (; i <= 16000; i++) {
			MeasuredAttributeValue value = new MeasuredAttributeValue(atr, new Integer(i), 10, MeasuredEntityType.MACHINE, LocalDateTime.now());
			Thread.sleep(1);
			attValueCache.cacheStore(value);
			statesMap.put(value.getTimeStamp(), value.getKey());
		}

		Thread.sleep(10000);

		for (; i <= 20000; i++) {
			MeasuredAttributeValue value = new MeasuredAttributeValue(atr, new Integer(i), 10, MeasuredEntityType.MACHINE, LocalDateTime.now());
			Thread.sleep(1);
			attValueCache.cacheStore(value);
			statesMap.put(value.getTimeStamp(), value.getKey());
		}
		
		System.out.println("execution finished" + LocalDateTime.now());

		
	}
	
}