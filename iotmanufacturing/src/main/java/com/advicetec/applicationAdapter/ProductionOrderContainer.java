package com.advicetec.applicationAdapter;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.sql.PreparedStatement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.Container;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.AttributeType;
import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.Plant;

public class ProductionOrderContainer extends Container
{

	static Logger logger = LogManager.getLogger(ProductionOrderContainer.class.getName());
	
	static String sqlSelect = "SELECT id, id_compania, id_sede, id_planta, id_grupo_maquina, id_maquina, ano, mes, id_produccion, id_articulo, descr_articulo, fechahora_inicial, fechahora_final, num_horas, cantidad_producir, tasa_esperada, velocidad_esperada, create_date, last_updttm FROM canonical_ordenproduccionplaneada where id = ?";
	static String sqlProductionOrderSelect = "SELECT id FROM canonical_ordenproduccionplaneada where id_compania = ? and id_sede = ? and id_planta = ? and id_grupo_maquina = ? and id_maquina =? and ano = ? and mes = ? and id_produccion = ? ";
	
	public ProductionOrderContainer(String driver, String server, String user, String password) {
		super(driver, server, user, password);
		
	}
	
	public synchronized com.advicetec.configuration.ConfigurationObject getObject(Integer id) 
	{
		try 
		{
			super.connect_prepared(sqlSelect);
			(( PreparedStatement) super.pst).setInt(1, id);
			
			ProductionOrder prdOrderTmp = null;

			ResultSet rs1 = (( PreparedStatement) super.pst).executeQuery();
			ResultSetMetaData rsmd = rs1.getMetaData();
			
			logger.info("it was executed the query for bringing the order production");
			while (rs1.next())
			{

				prdOrderTmp =  new ProductionOrder(id);

				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					int type = rsmd.getColumnType(i);
					String name = rsmd.getColumnName(i);

					switch (type) {
	
					    case java.sql.Types.TINYINT:
					    case java.sql.Types.SMALLINT:
					    case java.sql.Types.INTEGER:
					    {	
					    	Integer valueInteger = rs1.getInt(i);
					    	Attribute attrInt = new Attribute(name, AttributeType.INT);
					    	attrInt.setTrend(false);
					    	attrInt.setOrigin(AttributeOrigin.ERP);
					    	prdOrderTmp.registerAttribute(attrInt);
					    	AttributeValue valueAttrInt = new AttributeValue(name, attrInt, valueInteger, i, MeasuredEntityType.JOB );
					    	prdOrderTmp.registerAttributeValue(valueAttrInt);
					    	break;
					    }
					    
					    case java.sql.Types.FLOAT:
					    case java.sql.Types.REAL:
					    {
					    	Double valueDouble = 0.0 + rs1.getFloat(i);
					    	Attribute attrFloat = new Attribute(name, AttributeType.DOUBLE);
					    	attrFloat.setTrend(false);
					    	attrFloat.setOrigin(AttributeOrigin.ERP);
					    	prdOrderTmp.registerAttribute(attrFloat);
					    	AttributeValue valueAttrFloat = new AttributeValue(name, attrFloat, valueDouble, i, MeasuredEntityType.JOB );
					    	prdOrderTmp.registerAttributeValue(valueAttrFloat);
					    	break;
					    }	
					    case java.sql.Types.DOUBLE:
					    case java.sql.Types.NUMERIC:
					    case java.sql.Types.DECIMAL:
					    {
					    	Double valueDouble2 = rs1.getDouble(i);
					    	Attribute attrDouble = new Attribute(name, AttributeType.DOUBLE);
					    	attrDouble.setTrend(false);
					    	attrDouble.setOrigin(AttributeOrigin.ERP);
					    	prdOrderTmp.registerAttribute(attrDouble);
					    	AttributeValue valueAttrDouble = new AttributeValue(name, attrDouble, valueDouble2, i, MeasuredEntityType.JOB );
					    	prdOrderTmp.registerAttributeValue(valueAttrDouble);
					    	break;
					    }
					    						    		
					    case java.sql.Types.CHAR:
					    case java.sql.Types.VARCHAR:
					    case java.sql.Types.NCHAR:
					    case java.sql.Types.NVARCHAR:
					    {
					    	String valueString = rs1.getString(i);
					    	Attribute attrString = new Attribute(name, AttributeType.STRING);
					    	attrString.setTrend(false);
					    	attrString.setOrigin(AttributeOrigin.ERP);
					    	prdOrderTmp.registerAttribute(attrString);
					    	AttributeValue valueAttrString = new AttributeValue(name, attrString, valueString, i, MeasuredEntityType.JOB );
					    	prdOrderTmp.registerAttributeValue(valueAttrString);
					    	break;
					    }
	
					    case java.sql.Types.DATE:
					    {
					    	Date valueDate = rs1.getDate(i);
					    	Attribute attrDate = new Attribute(name, AttributeType.DATE);
					    	attrDate.setTrend(false);
					    	attrDate.setOrigin(AttributeOrigin.ERP);
					    	prdOrderTmp.registerAttribute(attrDate);
					    	AttributeValue valueAttrDate = new AttributeValue(name, attrDate, valueDate.toLocalDate(), i, MeasuredEntityType.JOB );
					    	prdOrderTmp.registerAttributeValue(valueAttrDate);
					    	break;
					    }
					    case java.sql.Types.TIME:
					    {
					    	Time valueTime = rs1.getTime(i);
					    	Attribute attrTime = new Attribute(name, AttributeType.TIME);
					    	attrTime.setTrend(false);
					    	attrTime.setOrigin(AttributeOrigin.ERP);
					    	prdOrderTmp.registerAttribute(attrTime);
					    	AttributeValue valueAttrTime = new AttributeValue(name, attrTime, valueTime.toLocalTime(), i, MeasuredEntityType.JOB );
					    	prdOrderTmp.registerAttributeValue(valueAttrTime);
					    	break;					    	
					    }
					    case java.sql.Types.TIMESTAMP:
					    {
					    	Timestamp valueDateTime = rs1.getTimestamp(i);
					    	Attribute attrDateTime = new Attribute(name, AttributeType.DATETIME);
					    	attrDateTime.setTrend(false);
					    	attrDateTime.setOrigin(AttributeOrigin.ERP);
					    	prdOrderTmp.registerAttribute(attrDateTime);
					    	AttributeValue valueAttrDateTime = new AttributeValue(name, attrDateTime, valueDateTime.toLocalDateTime(), i, MeasuredEntityType.JOB );
					    	prdOrderTmp.registerAttributeValue(valueAttrDateTime);
					    	break;					    						    
					    }
	
					    case java.sql.Types.BINARY:
					    case java.sql.Types.BOOLEAN:
					    {
					    	Boolean valueBool = rs1.getBoolean(i);
					    	Attribute attrBool = new Attribute(name, AttributeType.BOOLEAN);
					    	attrBool.setTrend(false);
					    	attrBool.setOrigin(AttributeOrigin.ERP);
					    	prdOrderTmp.registerAttribute(attrBool);
					    	AttributeValue valueAttrBool = new AttributeValue(name, attrBool, valueBool, i, MeasuredEntityType.JOB );
					    	prdOrderTmp.registerAttributeValue(valueAttrBool);
					    	break;					    						    					    	
					    }
					    
					    case java.sql.Types.ROWID:
					    	// This is the id of the production in the database, which is the id of the object. 
					    	// So we don't have to do anything.
					    	break;
					    	
					    case java.sql.Types.BIT:
					    case java.sql.Types.BIGINT:
					    case java.sql.Types.LONGVARCHAR:
					    case java.sql.Types.VARBINARY:
					    case java.sql.Types.LONGVARBINARY:
					    case java.sql.Types.NULL:
					    case java.sql.Types.OTHER:
					    case java.sql.Types.JAVA_OBJECT:
					    case java.sql.Types.DISTINCT:
					    case java.sql.Types.STRUCT:
					    case java.sql.Types.ARRAY:
					    case java.sql.Types.BLOB:
					    case java.sql.Types.CLOB:
					    case java.sql.Types.REF:
					    case java.sql.Types.DATALINK:
					    case java.sql.Types.LONGNVARCHAR:
					    case java.sql.Types.NCLOB:
					    case java.sql.Types.SQLXML:
					    	logger.error("Type is not translatable to attribute");
					    	break;
					    	
					}
		        }
			    
				// Load the object previously held on the container 
				super.configuationObjects.get(id);

				if (super.configuationObjects.get(id) != null){
					if (!(super.configuationObjects.get(id).equals(prdOrderTmp))){
						// The host system changed the the production, so we need to update it
						super.configuationObjects.put(id, prdOrderTmp);
					}
				} else {    		        		        		        
					super.configuationObjects.put(id, prdOrderTmp);
				}
				
			}
		    
			rs1.close();			
						
			super.disconnect();
			
			return prdOrderTmp; 
			
		} catch (ClassNotFoundException e){
        	String error = "Could not find the driver class - Error" + e.getMessage(); 
        	logger.error(error);
        	e.printStackTrace();
        } catch (SQLException e) {
        	String error = "Container:" + this.getClass().getName() +  "Error connecting to the database - error:" + e.getMessage();
        	logger.error(error);
        	e.printStackTrace();        	
        }

		return null;
	}
	
	public synchronized Integer getCanonicalObject(String company, String location, String plant, String machineGroup, String machineId,
			int year, int month, String productionOrder) {
	
		Integer id  = null;
		
		try 
		{
			super.connect_prepared(sqlProductionOrderSelect);
			(( PreparedStatement) super.pst).setString(1, company);
			(( PreparedStatement) super.pst).setString(2, location);
			(( PreparedStatement) super.pst).setString(3, plant);
			(( PreparedStatement) super.pst).setString(4, machineGroup);
			(( PreparedStatement) super.pst).setString(5, machineId);
			(( PreparedStatement) super.pst).setInt(6, year);
			(( PreparedStatement) super.pst).setInt(7, month);
			(( PreparedStatement) super.pst).setString(8, productionOrder);

			ResultSet rs1 = (( PreparedStatement) super.pst).executeQuery();
			
			while (rs1.next())
			{
				 id = rs1.getInt("id");
			}
			
			rs1.close();			
			
			super.disconnect();
			
			
		} catch (ClassNotFoundException e){
        	String error = "Could not find the driver class - Error" + e.getMessage(); 
        	logger.error(error);
        	e.printStackTrace();
        } catch (SQLException e) {
        	String error = "Container:" + this.getClass().getName() +  "Error connecting to the database - error:" + e.getMessage();
        	logger.error(error);
        	e.printStackTrace();        	
        }

		return id;

		
	}
	
}
