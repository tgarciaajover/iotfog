package com.advicetec.measuredentitity;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.Container;
import com.advicetec.configuration.ReasonCode;
import com.advicetec.utils.PredefinedPeriod;
import com.advicetec.utils.PredefinedPeriodType;

public class OEEAggregationContainer extends Container 
{

	static Logger logger = LogManager.getLogger(OEEAggregationContainer.class.getName());

	public OEEAggregationContainer(String driverStr, String server, String user, String password) {
		super(driverStr, server, user, password);
	}

	public OverallEquipmentEffectiveness calculateDateRangeOEE(Integer owner, MeasuredEntityType ownerType, List<StateInterval> listStateIntervals, LocalDateTime from, LocalDateTime to)
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


	public List<OverallEquipmentEffectiveness> getPeriodOEESubtotals(Integer owner, MeasuredEntityType ownerType, List<PredefinedPeriod> periods)
	{
		logger.info("in getPeriodOEESubtotals:");

		List<OverallEquipmentEffectiveness> ret = new ArrayList<OverallEquipmentEffectiveness>();

		for (int i = 0; i < periods.size(); i++) {
			PredefinedPeriod period = periods.get(i);

			if ((period.getType() == PredefinedPeriodType.HOUR ) || (period.getType() == PredefinedPeriodType.DAY) 
					|| (period.getType() == PredefinedPeriodType.MONTH) || (period.getType() == PredefinedPeriodType.YEAR))   
			{								

				try 
				{

					super.connect_prepared(OverallEquipmentEffectiveness.SQL_Select);
					OverallEquipmentEffectiveness eff = OverallEquipmentEffectiveness.getPeriodOEE((PreparedStatement) super.pst, owner, ownerType, period);
					if (eff != null) {
						ret.add(eff);
					}
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

		}

		return ret;

	}

}
