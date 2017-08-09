package com.advicetec.aggregation.oee;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.Container;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.configuration.ReasonCodeContainer;
import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.MeasuringState;
import com.advicetec.utils.PredefinedPeriod;
import com.advicetec.utils.PredefinedPeriodType;

/**
 * Container for the OEE aggregation data.
 * @author iot
 *
 */
public class OEEAggregationContainer extends Container 
{

	static Logger logger = LogManager.getLogger(OEEAggregationContainer.class.getName());

	public OEEAggregationContainer(String driverStr, String server, String user, String password) {
		super(driverStr, server, user, password);
	}

	/**
	 * Inserts into database the list of OEE values.
	 * @param list
	 */
	public void dbInsert(List<OverallEquipmentEffectiveness> list)
	{
		try{
			super.connect_prepared(OverallEquipmentEffectiveness.SQL_Insert);
			
			for (OverallEquipmentEffectiveness oee : list){
				oee.dbInsert((PreparedStatement) this.pst);
			}
	
	    	int ret[] = pst.executeBatch();
	    	super.commit();
	    	logger.info("Number of commands executed by insert: " + ret.length);
			super.disconnect();

		} catch (ClassNotFoundException e){
			String error = "Could not find the driver class - Error: " + e.getMessage(); 
			logger.error(error);
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void dbDelete(List<OverallEquipmentEffectiveness> list){
		try{
			super.connect_prepared(OverallEquipmentEffectiveness.SQL_Delete);
			for (OverallEquipmentEffectiveness oee : list){
				oee.dbDelete((PreparedStatement) this.pst);
			}
			int ret[] = pst.executeBatch();
	    	super.commit();
	    	logger.info("Number of commands executed by Delete: " + ret.length);
			super.disconnect();
		} catch (ClassNotFoundException e){
			String error = "Could not find the driver class - Error: " + e.getMessage(); 
			logger.error(error);
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public synchronized List<OverallEquipmentEffectiveness> intervalsByHour(
			Integer owner,MeasuredEntityType ownerType, 
			String periodKey, String parQueryFrom, String parQueryTo) {
		
		List<OverallEquipmentEffectiveness> list = new ArrayList<OverallEquipmentEffectiveness>();
        Calendar cal = Calendar.getInstance();
        TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
        cal.setTimeZone(utcTimeZone);

		
		logger.info("MeasuredEntity:" + owner + "Measured Entity Type:" + ownerType + "intervalsByHour periodKey:" 
						+ periodKey + " parQueryFrom:" + parQueryFrom + " parQueryTo:" + parQueryTo );
		
		double productiveTime = 0;
		double qtySchedToProduce = 0;
		double qtyProduced = 0;
		double qtyDefective = 0;
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				
		try 
		{
			
			super.connect_prepared(OverallEquipmentEffectiveness.SQL_LT_HOUR);

			int lineIndx = parQueryFrom.lastIndexOf("-");
			parQueryFrom = parQueryFrom.substring(0, lineIndx)+" "+parQueryFrom.substring(lineIndx + 1);
			
			lineIndx = parQueryTo.lastIndexOf("-");
			parQueryTo = parQueryTo.substring(0, lineIndx)+" "+parQueryTo.substring(lineIndx + 1);
			
			logger.info(" from:" + parQueryFrom + " to:" + parQueryTo);
			
			((PreparedStatement)super.pst).setInt(1, owner);
			((PreparedStatement)super.pst).setInt(2, ownerType.getValue());      // owner_type
			((PreparedStatement)super.pst).setTimestamp(3, Timestamp.valueOf(parQueryFrom));   		// period key
			
			((PreparedStatement)super.pst).setTimestamp(4, Timestamp.valueOf(parQueryTo));
			((PreparedStatement)super.pst).setInt(5, owner);
			((PreparedStatement)super.pst).setInt(6, ownerType.getValue());
			((PreparedStatement)super.pst).setTimestamp(7, Timestamp.valueOf(parQueryFrom));   		
			((PreparedStatement)super.pst).setTimestamp(8, Timestamp.valueOf(parQueryTo));
			((PreparedStatement)super.pst).setTimestamp(9, Timestamp.valueOf(parQueryTo));
			((PreparedStatement)super.pst).setInt(10, owner);
			((PreparedStatement)super.pst).setInt(11, ownerType.getValue());
			((PreparedStatement)super.pst).setTimestamp(12, Timestamp.valueOf(parQueryFrom));   		
			((PreparedStatement)super.pst).setTimestamp(13, Timestamp.valueOf(parQueryFrom));
			((PreparedStatement)super.pst).setTimestamp(14, Timestamp.valueOf(parQueryTo));
			
			ConfigurationManager manager = ConfigurationManager.getInstance();
			ReasonCodeContainer reasonCont =  manager.getReasonCodeContainer();

			ResultSet rs = ((PreparedStatement)super.pst).executeQuery();

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
					logger.info("from date:" + fromDatetime.format(formatter) + " to date:" + toDatetime.format(formatter) + " act_prod_time:" + actualProductiveTime);
				
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
				
				logger.info("PeriodProductiveTime:" + periodProductiveTime + " actualproductivetime:" + 
						actualProductiveTime +" productiveTime:" + productiveTime + " reducedTime:" + reducedTime);

				
				productiveTime += actualProductiveTime; 
				
				qtySchedToProduce += rowConversion1 * rowProductionRate * (actualProductiveTime / 60);
				
				qtyProduced += rowConversion1 * rowActualProductionRate * (actualProductiveTime / 60);
				
				if (reducedTime == 0) {
					qtyDefective += rowQtyDefective;
				}
				else {
					double effectiveTime = (double) periodProductiveTime - reducedTime;
					double percentage = effectiveTime / periodProductiveTime;
					qtyDefective += (rowQtyDefective * percentage);
				}
			}

			super.disconnect();
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
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
		logger.info("The oee calculated is: " +  eff.toString());
		return list;
	}

	
	/**
	 * 
	 * @param pstmt
	 * @param owner
	 * @param ownerType
	 * @param parQueryFrom
	 * @return
	 */
	public List<OverallEquipmentEffectiveness> intervalsByPredefinedPeriod(
			Integer owner,MeasuredEntityType ownerType, String parQueryFrom) {

		logger.info("in intervalsByPredefinedPeriod Parameters:" + "Owner:" + owner + " OwnerType:" + ownerType.getName() + " parQueryFrom:" + parQueryFrom);
		
		List<OverallEquipmentEffectiveness> list = new ArrayList<OverallEquipmentEffectiveness>();

		try 
		{
			if (getDriver().compareTo("org.postgresql.Driver") == 0){
				super.connect_prepared(OverallEquipmentEffectiveness.SQL_LIKE_POSTGRES);
				
			} else if (getDriver().compareTo("com.microsoft.sqlserver.jdbc.SQLServerDriver") == 0){
				super.connect_prepared(OverallEquipmentEffectiveness.SQL_LIKE_SQLSERVER);
				
			} else {
				logger.error("Unrecognized driver, we cannot decide the appropiate SQL Statment for driver given");
				return list;
			}
			
			
			((PreparedStatement)super.pst).setInt(1, owner);
			((PreparedStatement)super.pst).setInt(2, ownerType.getValue());      // owner_type
			((PreparedStatement)super.pst).setString(3, parQueryFrom );   		// period key

			ResultSet rs = ((PreparedStatement)super.pst).executeQuery();

			while (rs.next()) {

				String periodKey = rs.getString("period_key"); 
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

			super.disconnect();
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}			

		return list;
	}
	
	public synchronized boolean existPeriodOEE(Integer owner, MeasuredEntityType ownerType, PredefinedPeriod predefinedPeriod) 
	{

		boolean ret = false;
		try 
		{

	    	super.connect_prepared(OverallEquipmentEffectiveness.SQL_EXISTS);
	    	
			((PreparedStatement)super.pst).setInt(1, owner);
			((PreparedStatement)super.pst).setInt(2, ownerType.getValue());          		// owner_type
			((PreparedStatement)super.pst).setString(3, predefinedPeriod.getKey() );   			// period key

			ResultSet rs = ((PreparedStatement)super.pst).executeQuery();

			while (rs.next()) {
				ret = true;
			}
			super.disconnect();

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}			
		return ret;
	}
	
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
	
	
	
	public synchronized OverallEquipmentEffectiveness getPeriodOEE(Integer owner, 
			MeasuredEntityType ownerType, PredefinedPeriod predefinedPeriod) {
		OverallEquipmentEffectiveness eff = null;
		try 
		{
			
			super.connect_prepared(OverallEquipmentEffectiveness.SQL_Select);
			
			((PreparedStatement)super.pst).setInt(1, owner);
			((PreparedStatement)super.pst).setInt(2, ownerType.getValue());          		// owner_type
			((PreparedStatement)super.pst).setString(3, predefinedPeriod.getKey() );   		// period key


			ResultSet rs = ((PreparedStatement)super.pst).executeQuery();

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
			super.disconnect();
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return eff;
	}
}
