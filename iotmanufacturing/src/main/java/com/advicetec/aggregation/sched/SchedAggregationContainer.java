package com.advicetec.aggregation.sched;

import java.beans.PropertyVetoException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.aggregation.oee.OverallEquipmentEffectiveness;
import com.advicetec.configuration.AggregateMethod;
import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.Container;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.configuration.ReasonCodeContainer;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.MeasuringState;
import com.advicetec.utils.PredefinedPeriod;
import com.advicetec.utils.PredefinedPeriodType;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Container for the OEE aggregation data.
 * @author Jose Pulgarin
 *
 */
public class SchedAggregationContainer extends Container 
{

	static Logger logger = LogManager.getLogger(SchedAggregationContainer.class.getName());

	/**
	 * Connection pool for managing database connections.
	 */
	private ComboPooledDataSource cpds = null;
	
	
	/**
	 * Class's constructor 
	 * @param driverStr: driver string used to connect to the database.
	 * @param server: Ip address of the database server 
	 * @param user: database user
	 * @param password: password of the user's database.
	 */
	public SchedAggregationContainer(String driverStr, String server, String user, String password, int minDbThreadPool, int maxDbThreadPool) {
		super(driverStr, server, user, password);

		try{
			// Establishes the pool of connection to the database
			cpds = new ComboPooledDataSource();
			cpds.setDriverClass( driverStr );
			cpds.setJdbcUrl( server );
			cpds.setUser(user);                                  
			cpds.setPassword(password); 
			
			// the settings below are optional -- c3p0 can work with defaults
			cpds.setMinPoolSize(minDbThreadPool);                                     
			cpds.setAcquireIncrement(8);
			cpds.setMaxPoolSize(maxDbThreadPool);

			// This part inserts any pending data in the cache to the database in case of shutdown.  
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run()
				{	
					System.out.println("closing the database connection pool");
					if (cpds != null){
						cpds.close();
					}
				}
			}); 
			
			
		} catch (PropertyVetoException e1) {
			logger.error(e1.getMessage());
			e1.printStackTrace();
			System.exit(0);
		}             			
	}
	
	public synchronized Connection getConnection() throws SQLException{
		return cpds.getConnection();
	}
	
	/**
	 * Call stored procedure for execute aggregate code.
	 * @param aggregateMethod Method to Aggregate
	 * @param measuringEntity Entity
	 * @param measuredEntityType Entity type
	 */
	public boolean dbCall_USP(AggregateMethod aggregateMethod,  Integer measuringEntity)
	{
		
		Connection connDB  = null; 
	    CallableStatement cstmt = null;

		try{

		    connDB = getConnection();
		    //connDB.setAutoCommit(false);
		    
	        cstmt = connDB.prepareCall(
	                "{call usp_iot_aggregate_atrtibute(?,?,?,?,?,?)}",
	                ResultSet.TYPE_SCROLL_INSENSITIVE,
	                ResultSet.CONCUR_READ_ONLY);
            //98,'speed_cycles_min','YY',1,'2017-09-28 15:48:00','AVG'
            cstmt.setInt(1,measuringEntity);
            cstmt.setString(2,aggregateMethod.getAttribute());
            cstmt.setString(3,aggregateMethod.getType());
            cstmt.setInt(4,aggregateMethod.getScale());
            cstmt.setString(5,(LocalDateTime.now()).toString());
            cstmt.setString(6,aggregateMethod.getMethod());
            logger.debug("measuringEntity: " + measuringEntity + "     Attribute: " + aggregateMethod.getAttribute() + "     Type: " + aggregateMethod.getType() + "     Scale: " + aggregateMethod.getScale() + "     Method: " + aggregateMethod.getMethod());
            
	        //cstmt.setString("lastNameStartsWith", lastNameStartsWith);
	        boolean results = cstmt.execute();		    			
	    	//connDB.commit();
	    	logger.debug("Aggregate Method results : " + results);
	    	
		} catch (SQLException e) {
	    	logger.error("SQLException");
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if(cstmt!=null){
				try{
					cstmt.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}

			if(connDB!=null){
				try	{
					connDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Delete a list of OEEs from the database
	 * @param list: list of OEE to delete. 
	 */
	/*
	public void dbDelete(List<OverallEquipmentEffectiveness> list){

		Connection connDB  = null; 
		PreparedStatement pstDB = null;

		try {

		    connDB = getConnection();
		    connDB.setAutoCommit(false);
			
		    pstDB = connDB.prepareStatement(OverallEquipmentEffectiveness.SQL_Delete);
			
			for (OverallEquipmentEffectiveness oee : list){
				oee.dbDelete(pstDB);
			}
			int ret[] = pstDB.executeBatch();
			connDB.commit();
	    	logger.debug("Number of OEE deleted: " + ret.length);

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if(pstDB!=null){
				try{
					pstDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}

			if(connDB!=null){
				try	{
					connDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}

	}
	*/
	/**
	 * Return the list of OEE intervals (initially five minutes intervals) previously calculated and stored in the DB.
	 * 
	 * @param owner: measure entity for which the intervals were calculated.
	 * @param ownerType: type of measuring entity for which the intervals were calculated.
	 * @param periodKey: key for the predefined hour that we are quering the intervals
	 * @param parQueryFrom: start Dttm for the intervals
	 * @param parQueryTo: End Dttm for the intervals
	 * @return: List of predefined intervals for the hour.
	 */
	/*
	public List<OverallEquipmentEffectiveness> intervalsByHour(
			Integer owner,MeasuredEntityType ownerType, 
			String periodKey, String parQueryFrom, String parQueryTo) {
		
		List<OverallEquipmentEffectiveness> list = new ArrayList<OverallEquipmentEffectiveness>();
        Calendar cal = Calendar.getInstance();
        TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
        cal.setTimeZone(utcTimeZone);

		
		logger.debug("MeasuredEntity:" + owner + "Measured Entity Type:" + ownerType + "intervalsByHour periodKey:" 
						+ periodKey + " parQueryFrom:" + parQueryFrom + " parQueryTo:" + parQueryTo );
		
		double productiveTime = 0;
		double qtySchedToProduce = 0;
		double qtyProduced = 0;
		double qtyDefective = 0;
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		Connection connDB  = null; 
		PreparedStatement pstDB = null;
		
		try 
		{
		    connDB = getConnection();
		    connDB.setAutoCommit(false);
			
		    pstDB = connDB.prepareStatement(OverallEquipmentEffectiveness.SQL_LT_HOUR);

			int lineIndx = parQueryFrom.lastIndexOf("-");
			parQueryFrom = parQueryFrom.substring(0, lineIndx)+" "+parQueryFrom.substring(lineIndx + 1);
			
			lineIndx = parQueryTo.lastIndexOf("-");
			parQueryTo = parQueryTo.substring(0, lineIndx)+" "+parQueryTo.substring(lineIndx + 1);
			
			logger.debug(" from:" + parQueryFrom + " to:" + parQueryTo);
			
			pstDB.setInt(1, owner);
			pstDB.setInt(2, ownerType.getValue());      // owner_type
			pstDB.setTimestamp(3, Timestamp.valueOf(parQueryFrom));   		// period key
			
			pstDB.setTimestamp(4, Timestamp.valueOf(parQueryTo));
			pstDB.setInt(5, owner);
			pstDB.setInt(6, ownerType.getValue());
			pstDB.setTimestamp(7, Timestamp.valueOf(parQueryFrom));   		
			pstDB.setTimestamp(8, Timestamp.valueOf(parQueryTo));
			pstDB.setTimestamp(9, Timestamp.valueOf(parQueryTo));
			pstDB.setInt(10, owner);
			pstDB.setInt(11, ownerType.getValue());
			pstDB.setTimestamp(12, Timestamp.valueOf(parQueryFrom));   		
			pstDB.setTimestamp(13, Timestamp.valueOf(parQueryFrom));
			pstDB.setTimestamp(14, Timestamp.valueOf(parQueryTo));
			
			ConfigurationManager manager = ConfigurationManager.getInstance();
			ReasonCodeContainer reasonCont =  manager.getReasonCodeContainer();

			ResultSet rs = pstDB.executeQuery();

			while (rs.next()) {
								
				Timestamp dsTimeFrom = rs.getTimestamp("datetime_from", cal);

				long timestampTimeFrom = dsTimeFrom.getTime();
				cal.setTimeInMillis(timestampTimeFrom);
				LocalDateTime fromDatetime = LocalDateTime.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 
																cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
																 cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), 
																  cal.get(Calendar.MILLISECOND));

						
				Timestamp dsTimeTo = rs.getTimestamp("datetime_to", cal);
				
				long timestampTimeTo = dsTimeTo.getTime();
				cal.setTimeInMillis(timestampTimeTo);
				LocalDateTime toDatetime = LocalDateTime.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 
																cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
																 cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), 
																  cal.get(Calendar.MILLISECOND));
				
				
				long reducedTime = 0;
				long periodProductiveTime = fromDatetime.until(toDatetime, ChronoUnit.SECONDS);
				long actualProductiveTime = 0;
				
				if (fromDatetime.isBefore(Timestamp.valueOf(parQueryFrom).toLocalDateTime())) {

					reducedTime = fromDatetime.until(Timestamp.valueOf(parQueryFrom).toLocalDateTime(), ChronoUnit.SECONDS);		
					fromDatetime = Timestamp.valueOf(parQueryFrom).toLocalDateTime();
					
				}
				
				if (Timestamp.valueOf(parQueryTo).toLocalDateTime().isBefore(toDatetime)) {
					reducedTime = reducedTime + Timestamp.valueOf(parQueryTo).toLocalDateTime().until(toDatetime, ChronoUnit.SECONDS);
					toDatetime = Timestamp.valueOf(parQueryTo).toLocalDateTime();
				}
								
				Double rowProductionRate = rs.getDouble("production_rate");
				Double rowConversion1 = rs.getDouble("conversion1");
				Double rowConversion2 = rs.getDouble("conversion2");
				Double rowActualProductionRate = rs.getDouble("actual_production_rate");  
				Double rowQtyDefective = rs.getDouble("qty_defective");
				String rowStatus = rs.getString("status");
				String rowReasonCode = rs.getString("reason_code");
								
				MeasuringState state = MeasuringState.getByName(rowStatus);
				if (state == MeasuringState.OPERATING) {
					
					actualProductiveTime = fromDatetime.until(toDatetime, ChronoUnit.SECONDS);
					logger.debug("from date:" + fromDatetime.format(formatter) + " to date:" + toDatetime.format(formatter) + " act_prod_time:" + actualProductiveTime);
				
				} else if (state == MeasuringState.SCHEDULEDOWN)  {
					
					ReasonCode rCode = null;
					
					logger.info("ReasonCode: " + rowReasonCode);
					if (rowReasonCode != null) {
						rCode = (ReasonCode) reasonCont.getObject(Integer.valueOf(rowReasonCode.trim()));
						
						if (rCode.includeProductiveTime())
							actualProductiveTime = fromDatetime.until(toDatetime, ChronoUnit.SECONDS);
					}

					
				} else if (state == MeasuringState.UNSCHEDULEDOWN) {
					actualProductiveTime = 0;
				}
				else {
					logger.error("Invalid measuring state" + state.getName());
				}
				
				logger.debug("PeriodProductiveTime:" + periodProductiveTime + " actualproductivetime:" + 
						actualProductiveTime +" productiveTime:" + productiveTime + " reducedTime:" + reducedTime);

				
				productiveTime += actualProductiveTime; 
				
				qtySchedToProduce += rowConversion1 * rowProductionRate * ( (float) actualProductiveTime / 60);
				
				qtyProduced += rowConversion1 * rowActualProductionRate * ( (float) actualProductiveTime / 60);
				
				if (reducedTime == 0) {
					qtyDefective += rowQtyDefective;
				}
				else {
					double effectiveTime = (double) periodProductiveTime - reducedTime;
					double percentage = effectiveTime / periodProductiveTime;
					qtyDefective += (rowQtyDefective * percentage);
				}
			}
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally{
			if(pstDB!=null){
				try{
					pstDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}

			if(connDB!=null){
				try	{
					connDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}

		
		LocalDateTime from = Timestamp.valueOf(parQueryFrom).toLocalDateTime();
		LocalDateTime to = Timestamp.valueOf(parQueryTo).toLocalDateTime();
		PredefinedPeriod pPeriod = new PredefinedPeriod (from, to);
		OverallEquipmentEffectiveness eff = new OverallEquipmentEffectiveness(pPeriod, owner, ownerType);
		eff.setProductiveTime(productiveTime);
		eff.setQtySchedToProduce(qtySchedToProduce);
		eff.setQtyProduced(qtyProduced);
		eff.setQtyDefective(qtyDefective);
		list.add(eff);
		logger.debug("The oee calculated is: " +  eff.toString());
		return list;
	}
*/
	
	/**
	 * Return the list of OEE intervals previously calculated for the predefined interval with key parQueryFrom and stored in the DB.
	 * 
	 * @param owner: measure entity for which the intervals were calculated.
	 * @param ownerType: type of measuring entity for which the intervals were calculated.
	 * @param parQueryFrom: predefined period key prefix to find.
	 * @return all OEE aggregations which associated predefined periods start with parQueryFrom.
	 */
	/*
	public List<OverallEquipmentEffectiveness> intervalsByPredefinedPeriod(
			Integer owner,MeasuredEntityType ownerType, String parQueryFrom) {

		logger.debug("in intervalsByPredefinedPeriod Parameters:" + "Owner:" + owner + " OwnerType:" + ownerType.getName() + " parQueryFrom:" + parQueryFrom);
		
		List<OverallEquipmentEffectiveness> list = new ArrayList<OverallEquipmentEffectiveness>();

		Connection connDB  = null; 
		PreparedStatement pstDB = null;

		try 
		{
		    connDB = getConnection();
		    connDB.setAutoCommit(false);
			
			if (getDriver().compareTo("org.postgresql.Driver") == 0){

				pstDB = connDB.prepareStatement(OverallEquipmentEffectiveness.SQL_LIKE_POSTGRES);
				
			} else if (getDriver().compareTo("com.microsoft.sqlserver.jdbc.SQLServerDriver") == 0){

				pstDB = connDB.prepareStatement(OverallEquipmentEffectiveness.SQL_LIKE_SQLSERVER);
				
			} else {
				logger.error("Unrecognized driver, we cannot decide the appropiate SQL Statment for driver given");
				return list;
			}
			
			
			pstDB.setInt(1, owner);
			pstDB.setInt(2, ownerType.getValue());      // owner_type
			pstDB.setString(3, parQueryFrom );   		// period key

			ResultSet rs = pstDB.executeQuery();

			while (rs.next()) {

				String periodKey = rs.getString("period_key").trim(); 
				Integer productiveTime	= rs.getInt("productive_time");
				Double qtySchedToProduce = rs.getDouble("qty_sched_to_produce");
				Double qtyProduced = rs.getDouble("qty_produced");  
				Double qtyDefective = rs.getDouble("qty_defective");

				PredefinedPeriod pPeriod = PredefinedPeriod.getInstanceFrom(periodKey);
				if(pPeriod != null){

					OverallEquipmentEffectiveness eff = new OverallEquipmentEffectiveness(pPeriod, owner, ownerType);
					eff.setProductiveTime(productiveTime);
					eff.setQtySchedToProduce(qtySchedToProduce);
					eff.setQtyProduced(qtyProduced);
					eff.setQtyDefective(qtyDefective);
					list.add(eff);
				} else{
					logger.error("Cannot create the period with key:"+periodKey);
				}
			}
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally{
			if(pstDB!=null){
				try{
					pstDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}

			if(connDB!=null){
				try	{
					connDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}

		return list;
	}
	*/
	/**
	 * Verify if a predefined period is defined in the database. 
	 * 
	 * @param owner : measure entity for which the intervals were calculated.
	 * @param ownerType: type of measuring entity for which the intervals were calculated.
	 * @param predefinedPeriod: predefined period prefix to find.
	 * @return: True if the database contains the predefined period key, false otherwise.  
	 */
	/*
	public boolean existPeriodOEE(Integer owner, MeasuredEntityType ownerType, PredefinedPeriod predefinedPeriod) 
	{

		boolean ret = false;

		Connection connDB  = null; 
		PreparedStatement pstDB = null;
		
		try 
		{
		    connDB = getConnection();
		    connDB.setAutoCommit(false);
		    
		    pstDB = connDB.prepareStatement(OverallEquipmentEffectiveness.SQL_EXISTS);
	    	
		    pstDB.setInt(1, owner);
		    pstDB.setInt(2, ownerType.getValue());          		// owner_type
		    pstDB.setString(3, predefinedPeriod.getKey() );   			// period key

			ResultSet rs = pstDB.executeQuery();

			while (rs.next()) {
				ret = true;
			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if(pstDB!=null){
				try{
					pstDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}

			if(connDB!=null){
				try	{
					connDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}

		return ret;
	}
	*/
	/**
	 * Facade method to call method to get a list of predefined periods
	 * @param owner  : measure entity for which the intervals were calculated.
	 * @param ownerType : type of measuring entity for which the intervals were calculated.
	 * @param predefinedPeriod : predefined period prefix to find.
	 * @return List of those OEE aggregations that fits the predefined period prefix.
	 */
	/*
	public List<OverallEquipmentEffectiveness> getOEEList(Integer owner, 
			MeasuredEntityType ownerType, PredefinedPeriod predefinedPeriod) 
	{
				
		String parQueryFrom = "";
		String parQueryTo = "";
		if(predefinedPeriod.getType() == PredefinedPeriodType.HOUR){
			parQueryFrom = predefinedPeriod.getKey()+"00:00.001"; //2010-01-05-18
			parQueryTo = predefinedPeriod.getKey()+"59:59.999"; //2010-01-05-18

			return intervalsByHour(owner, ownerType,predefinedPeriod.getKey(), 
					parQueryFrom,parQueryTo);

		} else if(predefinedPeriod.getType() == PredefinedPeriodType.DAY || 
				predefinedPeriod.getType() == PredefinedPeriodType.MONTH ||
				predefinedPeriod.getType() == PredefinedPeriodType.YEAR){
			
			if (getDriver().compareTo("org.postgresql.Driver") == 0){
				parQueryFrom = predefinedPeriod.getKey()+"-__"; //2010-01-05
			} else if (getDriver().compareTo("com.microsoft.sqlserver.jdbc.SQLServerDriver") == 0){
				parQueryFrom = predefinedPeriod.getKey()+"-[0-9][0-9]"; //2010-01-05
			}
			return intervalsByPredefinedPeriod(owner, ownerType, parQueryFrom);
		}else{
			logger.error("Invalid predefined time period:" + predefinedPeriod.getType().getName());
		}
		return new ArrayList<OverallEquipmentEffectiveness>();
	}
	*/
	
	/**
	 * Get an OEE aggregation defined in the database. 
	 * 
	 * @param owner : measure entity for which the intervals were calculated.
	 * @param ownerType: type of measuring entity for which the intervals were calculated.
	 * @param predefinedPeriod: : predefined period prefix to find.
	 * @return The OEE object which predefined period was given as parameter
	 */
	/*
	public OverallEquipmentEffectiveness getPeriodOEE(Integer owner, 
			MeasuredEntityType ownerType, PredefinedPeriod predefinedPeriod) {
		OverallEquipmentEffectiveness eff = null;

		Connection connDB  = null; 
		PreparedStatement pstDB = null;

		try 
		{
		    connDB = getConnection();
		    connDB.setAutoCommit(false);
		    
		    pstDB = connDB.prepareStatement(OverallEquipmentEffectiveness.SQL_Select);
			
		    pstDB.setInt(1, owner);
		    pstDB.setInt(2, ownerType.getValue());          		// owner_type
		    pstDB.setString(3, predefinedPeriod.getKey() );   		// period key

			ResultSet rs = pstDB.executeQuery();

			while (rs.next()) {

				Integer productiveTime	= rs.getInt("productive_time");
				Double qtySchedToProduce = rs.getDouble("qty_sched_to_produce");
				Double qtyProduced = rs.getDouble("qty_produced");  
				Double qtyDefective = rs.getDouble("qty_defective");

				eff = new OverallEquipmentEffectiveness(predefinedPeriod, owner, ownerType);
				eff.setProductiveTime(productiveTime);
				eff.setQtySchedToProduce(qtySchedToProduce);
				eff.setQtyProduced(qtyProduced);
				eff.setQtyDefective(qtyDefective);
				break;
			}
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if(pstDB!=null){
				try{
					pstDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}

			if(connDB!=null){
				try	{
					connDB.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		
		return eff;
	}
	*/
}
