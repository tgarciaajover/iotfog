package com.advicetec.measuredentitity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.Container;
import com.advicetec.configuration.ReasonCode;
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
	 * Inserts into database the list of oee values.
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
	
	public synchronized OverallEquipmentEffectiveness calculateDateRangeOEE(Integer owner, MeasuredEntityType ownerType, List<StateInterval> listStateIntervals, LocalDateTime from, LocalDateTime to)
	{

		double availTime = 0.0;
		double downTime = 0.0;
		double productiveTime = 0.0;
		double qtySchedToProduce = 0.0;
		double qtyProduced = 0.0;
		double qtyDefective = 0.0;
		for (int i= 0; i < listStateIntervals.size(); i++ ) 
		{
			StateInterval state = listStateIntervals.get(i);
			Double prodRate = state.getProductionRate();
			Double actualProdRate = state.getActualProductionRate();
			Double duration = state.getDurationMin();
			if (state.getState() != MeasuringState.OPERATING) 
			{
				// The machine is not working 	
				ReasonCode reasonCd = state.getReason();	
				if (reasonCd != null) {
					// The user gave a reason code
					if (!reasonCd.isIdleDown()) {
						// This reason code does not deplete the avail time
						availTime =availTime + duration;
						downTime = downTime + duration;
					}
					else {
						availTime =availTime + duration;
						downTime = downTime + duration;					
					}
				} else {
					// The reason is null
					availTime =availTime + duration;
					downTime = downTime + duration;
				}

			} 
			else {
				// The machine is working 

				qtySchedToProduce = qtySchedToProduce + ( duration * prodRate);
				qtyProduced = qtyProduced + ( duration * actualProdRate ); 
				availTime =availTime + duration;
				productiveTime = productiveTime + duration;
				qtyDefective = qtyDefective + state.getQtyDefective(); 
			}
		}
		
		OverallEquipmentEffectiveness oee = new OverallEquipmentEffectiveness(new PredefinedPeriod(from, to), owner, ownerType);
		oee.setAvailableTime(availTime);
		oee.setProductiveTime(productiveTime);
		oee.setQtySchedToProduce(qtySchedToProduce);
		oee.setQtyProduced(qtyProduced);
		oee.setQtyDefective(qtyDefective);
		
		return oee;

	}


	public synchronized OverallEquipmentEffectiveness getPeriodOEESubtotals(Integer owner, MeasuredEntityType ownerType, PredefinedPeriod period)
	{
		logger.info("in getPeriodOEESubtotals:");

		OverallEquipmentEffectiveness ret = null;
	
		if ((period.getType() == PredefinedPeriodType.HOUR ) || (period.getType() == PredefinedPeriodType.DAY) 
				|| (period.getType() == PredefinedPeriodType.MONTH) || (period.getType() == PredefinedPeriodType.YEAR))   
		{								

			try 
			{

				super.connect_prepared(OverallEquipmentEffectiveness.SQL_Select);
				ret = getPeriodOEE(owner, ownerType, period);
				super.disconnect();
				
			} catch (ClassNotFoundException e){
				String error = "Could not find the driver class - Error" + e.getMessage(); 
				logger.error(error);
				e.printStackTrace();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}

		return ret;

	}


	public synchronized List<OverallEquipmentEffectiveness> intervalsByHour(
			Integer owner,MeasuredEntityType ownerType, 
			String periodKey, String parQueryFrom, String parQueryTo) {
		
		List<OverallEquipmentEffectiveness> list = new ArrayList<OverallEquipmentEffectiveness>();
		
		double productiveTime = 0;
		double qtySchedToProduce = 0;
		double qtyProduced = 0;
		double qtyDefective = 0;
				
		try 
		{
			super.connect_prepared(OverallEquipmentEffectiveness.SQL_LT_HOUR);

			System.out.println("before:"+parQueryFrom);
			int lineIndx = parQueryFrom.lastIndexOf("-");
			parQueryFrom = parQueryFrom.substring(0, lineIndx)+" "+parQueryFrom.substring(lineIndx + 1);
			
			lineIndx = parQueryTo.lastIndexOf("-");
			parQueryTo = parQueryTo.substring(0, lineIndx)+" "+parQueryTo.substring(lineIndx + 1);
			
			System.out.println("timestamp:"+parQueryFrom);
			
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
			((PreparedStatement)super.pst).setTimestamp(13, Timestamp.valueOf(parQueryTo));

			ResultSet rs = ((PreparedStatement)super.pst).executeQuery();

			while (rs.next()) {
				
				LocalDateTime fromDatetime = rs.getTimestamp("datetime_from").toLocalDateTime();
				LocalDateTime toDatetime = rs.getTimestamp("datetime_to").toLocalDateTime();
				Double rowProductionRate = rs.getDouble("production_rate");
				Double rowActualProductionRate = rs.getDouble("actual_production_rate");  
				Double rowQtyDefective = rs.getDouble("qty_defective");

				productiveTime += fromDatetime.until(toDatetime, ChronoUnit.SECONDS);
				qtySchedToProduce += rowProductionRate*
						fromDatetime.until(toDatetime, ChronoUnit.SECONDS) / 60;
				qtyProduced += rowActualProductionRate *
						fromDatetime.until(toDatetime, ChronoUnit.SECONDS) / 60;
				qtyDefective += rowQtyDefective;
			}

			super.disconnect();
			
		} catch (SQLException e) {
			Logger logger = LogManager.getLogger(StateInterval.class.getName());
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Logger logger = LogManager.getLogger(StateInterval.class.getName());
			logger.error(e.getMessage());
			e.printStackTrace();
		}			

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

		List<OverallEquipmentEffectiveness> list = new ArrayList<OverallEquipmentEffectiveness>();

		try 
		{
			super.connect_prepared(OverallEquipmentEffectiveness.SQL_LIKE);
			
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
			Logger logger = LogManager.getLogger(StateInterval.class.getName());
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
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

		}else if(predefinedPeriod.getType() == PredefinedPeriodType.DAY || 
				predefinedPeriod.getType() == PredefinedPeriodType.MONTH ||
				predefinedPeriod.getType() == PredefinedPeriodType.YEAR){
			parQueryFrom = predefinedPeriod.getKey()+"-[0-9][0-9]"; //2010-01-05
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
			Logger logger = LogManager.getLogger(StateInterval.class.getName());
			logger.error(e.getMessage());
			e.printStackTrace();
		}			
		return eff;
	}
}
