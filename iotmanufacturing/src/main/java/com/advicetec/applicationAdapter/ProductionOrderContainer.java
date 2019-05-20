package com.advicetec.applicationAdapter;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.advicetec.configuration.ConfigurationObject;
import com.advicetec.configuration.Container;
import com.advicetec.core.Attribute;
import com.advicetec.core.AttributeOrigin;
import com.advicetec.core.AttributeType;
import com.advicetec.core.AttributeValue;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.persistence.StateIntervalCache;

public class ProductionOrderContainer extends Container
{

	static Logger logger = LogManager.getLogger(ProductionOrderContainer.class.getName());
	
	/**
	 * SQL statement used to bring the production order information from the database. 
	 */
	static String sqlSelect = "SELECT * FROM canonical_ordenproduccionplaneada where id = ?";
	
	/**
	 * SQL Statement used to bring the id of a production order giving as parameters the canonical production order data
	 */
	static String sqlProductionOrderSelect = "SELECT id FROM canonical_ordenproduccionplaneada where id_compania = ? and id_sede = ? and id_planta = ? and (replace(id_grupo_maquina, ' ', '') = ? or id_grupo_maquina = ?) and id_maquina =? and ano = ? and mes = ? and id_produccion = ? ";
	
	/**
	 * SQL Statement used to bring the canonical production order data from its id registered in the production order.
	 */
	static String sqlCanonicalSelect = "SELECT id_compania, id_sede, id_planta, id_grupo_maquina, id_maquina, ano, mes, id_produccion FROM canonical_ordenproduccionplaneada WHERE id = ?";
	
	/**
	 * SQL Statement used to bring the canonical production order data from its id registered in the production order.
	 */
	static String sqlProductionOrderStartDateSelect = "SELECT MIN(datetime_from) AS datetime_from FROM measuringentitystatusinterval WHERE id_owner = ? and related_object = ?";
	
	/**
	 * SQL Statement used to bring the canonical production order data from its id registered in the production order.
	 */
	static String sqlProductionOrderEndDateSelect = "SELECT MAX(datetime_to) AS datetime_to FROM measuringentitystatusinterval WHERE id_owner = ? and related_object = ?";
	
	static String sqlProductionOrderItemDetail = "SELECT id_articulo, descr_articulo FROM canonical_ordenproduccionplaneada where id_compania = ? and id_sede = ? and id_planta = ? and id_grupo_maquina = ? and ano = ? and mes = ? and id_produccion = ?";
	
	/**
	 * Constructor for the production order 
	 * @param driver : driver string used to connect to the database. In this case this is the canonical database.
	 * @param server : Ip address of the database server 
	 * @param user : database user
	 * @param password : password of the user's database.
	 */
	public ProductionOrderContainer(String driver, String server, String user, String password) {
		super(driver, server, user, password);
		
	}
	
	/**
	 * Get the production order from the database 
	 * 
	 * @param id  Production order identifier
	 */
	private ProductionOrder getProductionOrderFromDB(Integer id)
	{
		try 
		{
			super.connect_prepared(sqlSelect);
			(( PreparedStatement) super.pst).setInt(1, id);
			
			ProductionOrder prdOrderTmp = null;

			ResultSet rs1 = (( PreparedStatement) super.pst).executeQuery();
			ResultSetMetaData rsmd = rs1.getMetaData();
			
			String[] fixedFieldNames = {"id", "create_date", "last_updttm"};
			
			logger.debug("it was executed the query for bringing the order production");
			logger.debug(id);
			while (rs1.next())
			{
				logger.info(id.toString());
				prdOrderTmp =  new ProductionOrder(id);
				

				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					int type = rsmd.getColumnType(i);
					String name = rsmd.getColumnName(i);
					if (!(Arrays.asList(fixedFieldNames).contains(name))) {

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
		        }
			    				
			}

			rs1.close();			

			// set the canonical key for the object.
			String canonicalKey = getCanonicalInformation(prdOrderTmp);
			prdOrderTmp.setCanonicalKey(canonicalKey);
			
			super.configuationObjects.put(id, prdOrderTmp);
									
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
	
	/** 
	 * Gets a Production order from the container 
	 * @param id : id of the production order to retry.
	 */
	public synchronized ConfigurationObject getObject(Integer id) 
	{
		logger.info(id.toString());
		// Load the object previously held on the container 
		ConfigurationObject obj = super.configuationObjects.get(id);

		if (obj != null){
			return obj;
		} else {    		        		        		        
			return getProductionOrderFromDB(id);
		}
		
	}
	
	/**
	 * Returns the identifier of a production order from its canonical data
	 * 
	 * @param company : company to which the production order belongs to.
	 * @param location: location to which the production order belongs to.
	 * @param plant: plant to which the production order belongs to.
	 * @param machineGroup: : machine group to which the production order belongs to.
	 * @param machineId: : machine the production order is going to be executed.
	 * @param year: year when the production order is going to be executed.
	 * @param month: month when the production order is going to be executed.
	 * @param productionOrder: canonical production order code.
	 * 
	 * @return Returns the identifier of a production order from its canonical data
	 */
	public synchronized Integer getCanonicalObject(String company, String location, String plant, String machineGroup, String machineId,
													int year, int month, String productionOrder) 
	{
	
		Integer id  = null;
		
		try 
		{
			super.connect_prepared(sqlProductionOrderSelect);
			(( PreparedStatement) super.pst).setString(1, company);
			(( PreparedStatement) super.pst).setString(2, location);
			(( PreparedStatement) super.pst).setString(3, plant);
			(( PreparedStatement) super.pst).setString(4, machineGroup);
			(( PreparedStatement) super.pst).setString(5, machineGroup);
			(( PreparedStatement) super.pst).setString(6, machineId);
			(( PreparedStatement) super.pst).setInt(7, year);
			(( PreparedStatement) super.pst).setInt(8, month);
			(( PreparedStatement) super.pst).setString(9, productionOrder);

			
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

	/**
	 * Returns an string representing the production order canonical information 
	 * 
	 * @param prodOrder production order object
	 * @return string concatenating all canonical information of the production order.
	 */
	private String getCanonicalInformation(ProductionOrder prodOrder) {
		
		String canonicalKey = "";
		
		try 
		{
			super.prepare_statement(sqlCanonicalSelect);
			logger.debug(prodOrder.getId());
			(( PreparedStatement) super.pst).setInt(1, prodOrder.getId());
			ResultSet rs = (( PreparedStatement) super.pst).executeQuery();

			while (rs.next()) 
			{
				
				String company 		 	= rs.getString("id_compania");
				String location      	= rs.getString("id_sede");
				String plan 	 	 	= rs.getString("id_planta");
				String machineGroup  	= rs.getString("id_grupo_maquina");
				Integer ano 		 	= rs.getInt("ano");
				Integer month        	= rs.getInt("mes");
				String productionOrder = rs.getString("id_produccion");
				
				canonicalKey = company + "-" + location + "-" + plan + "-" + machineGroup + "-" + 
								Integer.toString(ano) + "-" + Integer.toString(month) + "-" + productionOrder;  
				
			}

			rs.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return canonicalKey;
	}
	
	/**
	 * Returns the start LocalDateTime production order into measured entity  
	 * 
	 * @param measuredEntity measured entity id
	 * @param prodOrder production order id
	 * 
	 * @return LocalDateTime of start a production order
	 */
	public JSONObject getProductionOrderStartDate(Integer measuredEntity, Integer prodOrder) {
		
		JSONObject jsob = null;
		LocalDateTime startDate = null;
		LocalDateTime endDate = null;
		
		Calendar cal = Calendar.getInstance();
        TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
        cal.setTimeZone(utcTimeZone);
        
        Connection connDB  = null; 
		PreparedStatement pstDB = null;
		ResultSet rs = null;
		
		try 
		{
			jsob = new JSONObject();
			
			connDB = StateIntervalCache.getConnection();
			connDB.setAutoCommit(false);
			pstDB = connDB.prepareStatement(sqlProductionOrderStartDateSelect);
			pstDB.setInt(1, measuredEntity);
			pstDB.setInt(2, prodOrder);
			rs =  pstDB.executeQuery();
			while (rs.next()) 
			{				
				Timestamp dsTimeFrom = rs.getTimestamp("datetime_from", cal);
				long timestampTimeFrom = dsTimeFrom.getTime();
				cal.setTimeInMillis(timestampTimeFrom);
				startDate = LocalDateTime.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 
																cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
																 cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), 
																  cal.get(Calendar.MILLISECOND)*1000000);
				jsob.append("datetime_from", startDate);
			}
			rs.close();
			
			connDB = StateIntervalCache.getConnection();
			connDB.setAutoCommit(false);
			pstDB = connDB.prepareStatement(sqlProductionOrderEndDateSelect);
			pstDB.setInt(1, measuredEntity);
			pstDB.setInt(2, prodOrder);
			rs =  pstDB.executeQuery();
			while (rs.next()) 
			{				
				Timestamp dsTimeTo = rs.getTimestamp("datetime_to", cal);
				long timestampTimeTo = dsTimeTo.getTime();
				cal.setTimeInMillis(timestampTimeTo);
				endDate = LocalDateTime.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 
																cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
																 cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), 
																  cal.get(Calendar.MILLISECOND)*1000000);
				jsob.append("datetime_to", endDate);
			}
			rs.close();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return jsob;
	}
	
	public ArrayList<String> getItemDetail(String company, String location, String plant, String machineGroup, 
			int year, int month, String productionOrder){
		ArrayList<String> detail = new ArrayList<String>();
		
		try 
		{
			super.connect_prepared(sqlProductionOrderItemDetail);
			(( PreparedStatement) super.pst).setString(1, company);
			(( PreparedStatement) super.pst).setString(2, location);
			(( PreparedStatement) super.pst).setString(3, plant);
			(( PreparedStatement) super.pst).setString(4, machineGroup);			
			(( PreparedStatement) super.pst).setInt(5, year);
			(( PreparedStatement) super.pst).setInt(6, month);
			(( PreparedStatement) super.pst).setString(7, productionOrder);

			
			ResultSet rs1 = (( PreparedStatement) super.pst).executeQuery();
			
			while (rs1.next())
			{
				 detail.add(rs1.getString("id_articulo"));
				 detail.add(rs1.getString("descr_articulo"));
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
		
		return detail;
	}
}
