package com.advicetec.aggregation.oee;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonProperty;

import com.advicetec.measuredentitity.MeasuredEntityType;
import com.advicetec.measuredentitity.StateInterval;
import com.advicetec.persistence.Storable;
import com.advicetec.utils.PredefinedPeriod;
import com.advicetec.utils.PredefinedPeriodType;

public class OverallEquipmentEffectiveness implements Storable
{

	static Logger logger = LogManager.getLogger(OverallEquipmentEffectiveness.class.getName());
	static int SECONDS_HOUR = 3600;
	static int SECONDS_DAY = 86400;
	static int DAYS_LEAP_YEAR = 366;
	static int DAYS_NON_LEAP_YEAR = 365;
    	

	// information about the parent.
	private Integer parent;
	private MeasuredEntityType parentType;

	// Calculated Predefined Period
	private PredefinedPeriod predefinedPeriod;

	// The productive time is measured in seconds. 
	double productiveTime;

	// Total quantity possible to produce.
	double qtySchedToProduce; 

	// Total quantity produced
	double qtyProduced;

	// Total reproduced or unusefull parts produced.
	double qtyDefective;

	public static final String SQL_Insert = "insert into measuringentityoee (id_owner, owner_type, period_key, productive_time, qty_sched_to_produce, qty_produced, qty_defective) values (?, ?, ?, ?, ?, ?, ? )";
	public static final String SQL_Delete =	"delete from measuringentityoee where id_owner = ? and owner_type = ? and period_key = ?";
	public static final String SQL_Select = "select productive_time, qty_sched_to_produce, qty_produced, qty_defective from measuringentityoee where id_owner = ? and owner_type = ? and period_key = ?";
	public static final String SQL_EXISTS = "select 'x' as found from measuringentityoee where id_owner = ? and owner_type = ? and period_key = ?";
	
	public static final String SQL_LT_HOUR = "SELECT datetime_from,datetime_to,"
			+ "status,reason_code,production_rate, conversion1, conversion2, actual_production_rate, qty_defective FROM measuringentitystatusinterval "
			+ "WHERE id_owner = ? AND owner_type = ? AND datetime_from >= ? AND datetime_to <= ? " 
			+ "UNION " 
			+ "SELECT datetime_from,datetime_to,status,reason_code,production_rate, "
			+ "conversion1, conversion2, actual_production_rate,qty_defective FROM measuringentitystatusinterval "
			+ "WHERE id_owner = ? AND owner_type = ? AND datetime_from >= ? AND datetime_from <= ? AND datetime_to >= ? "   
			+ "UNION " 
			+ "SELECT datetime_from,datetime_to,status,reason_code,production_rate, "
			+ "conversion1, conversion2, actual_production_rate,qty_defective FROM measuringentitystatusinterval "
			+ "WHERE id_owner = ? AND owner_type = ? AND datetime_from <= ? AND datetime_to >= ? AND datetime_to <= ? "
			+ "ORDER BY datetime_from ";

	public static final String SQL_LIKE_POSTGRES = "SELECT id_owner, owner_type,period_key,productive_time,qty_sched_to_produce,qty_produced,qty_defective  FROM measuringentityoee " 
			+ " WHERE id_owner = ? AND owner_type = ? AND TRIM(period_key) like ?";

	public static final String SQL_LIKE_SQLSERVER = "SELECT id_owner, owner_type,period_key,productive_time,qty_sched_to_produce,qty_produced,qty_defective  FROM measuringentityoee " 
			+ " WHERE id_owner = ? AND owner_type = ? AND RTRIM(period_key) like ?";


	public OverallEquipmentEffectiveness(@JsonProperty("predefinedPeriod") PredefinedPeriod predefinedPeriod, 
			@JsonProperty("origin")Integer parent, 
			@JsonProperty("originType")MeasuredEntityType parentType) {
		super();
		this.predefinedPeriod = predefinedPeriod;
		this.parent = parent;
		this.parentType = parentType;
		this.productiveTime = 0;
		this.qtySchedToProduce = 0;
		this.qtyProduced = 0;
		this.qtyDefective = 0;
	}

	public Integer getParent() {
		return parent;
	}

	public MeasuredEntityType getParentType() {
		return parentType;
	}

	public double getAvailableTime() {
		
		double ret = 0.0;
		
		logger.info("Predefined period:" + this.predefinedPeriod.getType().getName());
		
		if (this.predefinedPeriod.getType() == PredefinedPeriodType.INT_LT_HOUR) {
			
			Date dateFrom = this.predefinedPeriod.getCalendar().getTime();
			Date dateTo =  this.predefinedPeriod.getCalendarTo().getTime();

			// TODO: Verify this with timezone.
			LocalDateTime lDateTimeFrom = LocalDateTime.ofInstant(dateFrom.toInstant(), ZoneId.systemDefault());
			LocalDateTime lDateTimeTo = LocalDateTime.ofInstant(dateTo.toInstant(), ZoneId.systemDefault());

			return (double) lDateTimeFrom.until(lDateTimeTo,  ChronoUnit.SECONDS);

		}	else if (this.predefinedPeriod.getType() == PredefinedPeriodType.HOUR) {
			return SECONDS_HOUR;

		}	else if (this.predefinedPeriod.getType() == PredefinedPeriodType.DAY) {
			return SECONDS_DAY;

		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.MONTH) {
			int year = this.predefinedPeriod.getCalendar().get(Calendar.YEAR);
			int month = this.predefinedPeriod.getCalendar().get(Calendar.MONTH) + 1;

			// Get the number of days in that month
			YearMonth yearMonthObject = YearMonth.of(year, month);
			int daysInMonth = yearMonthObject.lengthOfMonth();

			return (double)  daysInMonth * SECONDS_DAY;

		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.YEAR) {
			int year = this.predefinedPeriod.getCalendar().get(Calendar.YEAR);

			// Get the number of days in that month
			YearMonth yearMonthObject = YearMonth.of(year, Calendar.FEBRUARY + 1);
			int daysInMonth = yearMonthObject.lengthOfMonth();
			
			if (daysInMonth >= 29)
				return (double) DAYS_LEAP_YEAR * SECONDS_DAY;
			else
				return (double) DAYS_NON_LEAP_YEAR * SECONDS_DAY;
		
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.UNDEFINED) {
			logger.error("Undefined predefined period type");
			return ret; 
		}
		
		return ret;
	}


	public double getProductiveTime() {
		return productiveTime;
	}

	public void setProductiveTime(double productiveTime) {
		this.productiveTime = productiveTime;
	}

	public double getQtySchedToProduce() {
		return qtySchedToProduce;
	}

	public void setQtySchedToProduce(double qtySchedToProduce) {
		this.qtySchedToProduce = qtySchedToProduce;
	}

	public double getQtyProduced() {
		return qtyProduced;
	}

	public void setQtyProduced(double qtyProduced) {
		this.qtyProduced = qtyProduced;
	}

	public double getQtyDefective() {
		return qtyDefective;
	}

	public void setQtyDefective(double qtyDefective) {
		this.qtyDefective = qtyDefective;
	}
	
	public PredefinedPeriod getPredefinedPeriod(){
		return predefinedPeriod;
	}

	@Override
	public String getPreparedInsertText() {
		return SQL_Insert;
	}

	@Override
	public String getPreparedDeleteText() {
		return SQL_Delete;
	}

	@Override
	public void dbInsert(PreparedStatement pstmt) {
		try 
		{
			pstmt.setInt(1, getParent());
			pstmt.setInt(2, getParentType().getValue());          			// owner_type
			pstmt.setString(3, this.predefinedPeriod.getKey() );   			// period key
			pstmt.setDouble(4, this.getProductiveTime());						// productive time
			pstmt.setDouble(5, this.getQtySchedToProduce());				// Quantity scheduled to produce
			pstmt.setDouble(6, this.getQtyProduced());						// Quantity produced
			pstmt.setDouble(7, this.getQtyDefective());						// Quantity with rework or considered defective. 

			pstmt.addBatch();

		} catch (SQLException e) {
			Logger logger = LogManager.getLogger(OverallEquipmentEffectiveness.class.getName());
			logger.error(e.getMessage());
			e.printStackTrace();
		}		

	}

	@Override
	public void dbDelete(PreparedStatement pstmt) {

		try 
		{
			pstmt.setInt(1, getParent());
			pstmt.setInt(2, getParentType().getValue());          			// owner_type
			pstmt.setString(3, this.predefinedPeriod.getKey() );   			// period key

			pstmt.addBatch();

		} catch (SQLException e) {
			Logger logger = LogManager.getLogger(StateInterval.class.getName());
			logger.error(e.getMessage());
			e.printStackTrace();
		}			

	}

	public String getStartDttm() {
		
		if (this.predefinedPeriod.getType() == PredefinedPeriodType.YEAR) {
			return  String.format("%04d",this.predefinedPeriod.getCalendar().get(Calendar.YEAR)) + "-01-01 00:00:00.000"; 
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.MONTH) {
			return  String.format("%04d",this.predefinedPeriod.getCalendar().get(Calendar.YEAR)) + "-" + 
					 String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.MONTH) + 1) + "-01 00:00:00.000";
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.DAY) {
			return  String.format("%04d", this.predefinedPeriod.getCalendar().get(Calendar.YEAR)) + "-" + 
					 String.format("%02d", this.predefinedPeriod.getCalendar().get(Calendar.MONTH) + 1)  + 
					 String.format("%02d", this.predefinedPeriod.getCalendar().get(Calendar.DAY_OF_MONTH)) + " 00:00:00.000";
			
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.HOUR) {
			return  String.format("%04d",this.predefinedPeriod.getCalendar().get(Calendar.YEAR)) + "-" + 
					 String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.MONTH) + 1) + "-" +
					  String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.DAY_OF_MONTH)) + " " + 
					   String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.HOUR_OF_DAY)) + ":00:00.000";
			
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.INT_LT_HOUR) {
			return  String.format("%04d",this.predefinedPeriod.getCalendar().get(Calendar.YEAR)) + "-" + 
					 String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.MONTH) + 1) + "-" + 
					  String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.DAY_OF_MONTH)) + " " + 
					   String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.HOUR_OF_DAY)) + ":" + 
					    String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.MINUTE)) + ":" +
					     String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.SECOND)) + ".000";

		} 
		
		return null;
	}

	public Object endDttm() {
		
		if (this.predefinedPeriod.getType() == PredefinedPeriodType.YEAR) {
			return  String.format("%04d",this.predefinedPeriod.getCalendar().get(Calendar.YEAR)) + "-12-31 59:59:59.999"; 
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.MONTH) {
			
			int year = this.predefinedPeriod.getCalendar().get(Calendar.YEAR);
			int month = this.predefinedPeriod.getCalendar().get(Calendar.MONTH) + 1;
			
			YearMonth yearMonthObject = YearMonth.of(year, month);
			int daysInMonth = yearMonthObject.lengthOfMonth();
			
			return  String.format("%04d", this.predefinedPeriod.getCalendar().get(Calendar.YEAR)) + "-" + 
			 		 String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.MONTH) + 1) + "-" +
			 		 String.format("%02d",daysInMonth) + " " + "23:59:59.999";
			
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.DAY) {
			return  String.format("%04d",this.predefinedPeriod.getCalendar().get(Calendar.YEAR)) + "-" + 
					 String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.MONTH) + 1) +  "-" +
					  String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.DAY_OF_MONTH)) + " "+ "23:59:59.999";
			
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.HOUR) {
			return  String.format("%04d",this.predefinedPeriod.getCalendar().get(Calendar.YEAR)) + "-" + 
					 String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.MONTH) + 1) + "-" + 
					  String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.DAY_OF_MONTH)) + " " + 
					   String.format("%02d",this.predefinedPeriod.getCalendar().get(Calendar.HOUR_OF_DAY)) + ":59:59.999";
			
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.INT_LT_HOUR) {
			return  String.format("%04d",this.predefinedPeriod.getCalendarTo().get(Calendar.YEAR)) + "-" + 
					 String.format("%02d",this.predefinedPeriod.getCalendarTo().get(Calendar.MONTH) + 1) + "-" + 
					  String.format("%02d",this.predefinedPeriod.getCalendarTo().get(Calendar.DAY_OF_MONTH)) + " " +  
					   String.format("%02d",this.predefinedPeriod.getCalendarTo().get(Calendar.HOUR_OF_DAY)) + ":" +
					    String.format("%02d",this.predefinedPeriod.getCalendarTo().get(Calendar.MINUTE)) + ":" + 
					     String.format("%02d",this.predefinedPeriod.getCalendarTo().get(Calendar.SECOND)) + ".000";

		} 
		return null;
	}

	public String toString(){
		return "predefinedPeriod:" + getPredefinedPeriod().getType().getName() + 
				"productiveTime:" + getProductiveTime() + "qtySchedToProduce:" + 
				   getQtySchedToProduce() + "qtyProduced:" + getQtyProduced() 
				     + "qtyDefective:" + getQtyDefective();  
 		
	}
}
