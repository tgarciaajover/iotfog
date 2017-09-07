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
    	

	/**
	 * information about the measured entity for which this OEE was calculated.
	 */
	private Integer parent;
	private MeasuredEntityType parentType;

	/**
	 * Calculated Predefined Period
	 */
	private PredefinedPeriod predefinedPeriod;

	/**
	 *  The productive time which is measured in seconds. 
	 */
	double productiveTime;

	/**
	 * Total quantity possible to produce.
	 */
	double qtySchedToProduce; 

	/**
	 * Total quantity produced
	 */
	double qtyProduced;

	/**
	 * Total reproduced or not useful parts produced.
	 */
	double qtyDefective;

	/**
	 * Sql sentence used to insert an OEE aggregation
	 */
	public static final String SQL_Insert = "insert into measuringentityoee (id_owner, owner_type, period_key, productive_time, qty_sched_to_produce, qty_produced, qty_defective) values (?, ?, ?, ?, ?, ?, ? )";
	
	/**
	 * Sql sentence used to delete an OEE aggregation
	 */
	public static final String SQL_Delete =	"delete from measuringentityoee where id_owner = ? and owner_type = ? and period_key = ?";
	
	/**
	 * Select an specific OEE aggregation from the database. The following fields defines a unique OEE aggregation (id_owner, owner_type, period_key)
	 */
	public static final String SQL_Select = "select productive_time, qty_sched_to_produce, qty_produced, qty_defective from measuringentityoee where id_owner = ? and owner_type = ? and period_key = ?";
	
	/**
	 * SQL statement used to verify if an OEE aggregation exists in the database.
	 */
	public static final String SQL_EXISTS = "select 'x' as found from measuringentityoee where id_owner = ? and owner_type = ? and period_key = ?";
	
	/**
	 * SQL statement used to search for those OEE aggregations less than an hour that are in an interval defined by datetime_from and datetime_from. 
	 * If an OEE aggregation is not completely included in the interval given, then it is still included. However, the user should split the part included from the part not included. 
	 */
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

	/**
	 * Brings all intervals which predefined period key start with a specific prefix. It is specific for Postgres. This sentence helps to bring the following cases:
	 *    - all months within a year.
	 *    - all days within a month
	 *    - all hours within a day
	 */
	public static final String SQL_LIKE_POSTGRES = "SELECT id_owner, owner_type,period_key,productive_time,qty_sched_to_produce,qty_produced,qty_defective  FROM measuringentityoee " 
			+ " WHERE id_owner = ? AND owner_type = ? AND TRIM(period_key) like ?";

	/**
	 * Brings all intervals which predefined period key start with a specific prefix. It is specific for SQL server or SQL Express. This sentence helps to bring the following cases:
	 *    - all months within a year.
	 *    - all days within a month
	 *    - all hours within a day
	 */
	public static final String SQL_LIKE_SQLSERVER = "SELECT id_owner, owner_type,period_key,productive_time,qty_sched_to_produce,qty_produced,qty_defective  FROM measuringentityoee " 
			+ " WHERE id_owner = ? AND owner_type = ? AND RTRIM(period_key) like ?";

	/**
	 * Overall Equipment Effectiveness Aggregation Constructor.
	 * @param predefinedPeriod predefined period for which we are creating a new instance
	 * @param parent: measuring entity or production order
	 * @param parentType: it establishes the type of parent.
	 */
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

	/**
	 * @return Gets the OEE aggregation parent (measured entity, production order)
	 */
	public Integer getParent() {
		return parent;
	}

	/**
	 * @return Gets the OEE aggregation parent type (measured entity, production order)
	 */
	public MeasuredEntityType getParentType() {
		return parentType;
	}

	/**
	 * @return Returns the available time in seconds for the OEE Aggregation. This value is fixed and depends on the king of aggregation 
	 * year, month, day, hour.
	 */
	public double getAvailableTime() {
		
		double ret = 0.0;
		
		logger.debug("Predefined period:" + this.predefinedPeriod.getType().getName());
		
		if (this.predefinedPeriod.getType() == PredefinedPeriodType.INT_LT_HOUR) {
			
			Date dateFrom = this.predefinedPeriod.getCalendarFrom().getTime();
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
			int year = this.predefinedPeriod.getCalendarFrom().get(Calendar.YEAR);
			int month = this.predefinedPeriod.getCalendarFrom().get(Calendar.MONTH) + 1;

			// Get the number of days in that month
			YearMonth yearMonthObject = YearMonth.of(year, month);
			int daysInMonth = yearMonthObject.lengthOfMonth();

			return (double)  daysInMonth * SECONDS_DAY;

		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.YEAR) {
			int year = this.predefinedPeriod.getCalendarFrom().get(Calendar.YEAR);

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


	/**
	 * @return Return the productive time. This value is defined as the available time minus all stops not market to be excluded.
	 */
	public double getProductiveTime() {
		return productiveTime;
	}

	/**
	 * @param productiveTime: productive time to be set.
	 */
	public void setProductiveTime(double productiveTime) {
		this.productiveTime = productiveTime;
	}

	/**
	 * @return Returns the quantity schedule to produce during this OEE aggregation.
	 */
	public double getQtySchedToProduce() {
		return qtySchedToProduce;
	}

	/**
	 * Set the quantity scheduled to procedure during this OEE aggregation.
	 * @param qtySchedToProduce
	 */
	public void setQtySchedToProduce(double qtySchedToProduce) {
		this.qtySchedToProduce = qtySchedToProduce;
	}

	/**
	 * Get the quantity produced during this OEE aggregation.
	 * @return
	 */
	public double getQtyProduced() {
		return qtyProduced;
	}

	/**
	 * Sets the quantity produced during this OEE aggregation.
	 * @param qtyProduced
	 */
	public void setQtyProduced(double qtyProduced) {
		this.qtyProduced = qtyProduced;
	}

	/**
	 * Gets the quantity that has been reported as defective during this OEE aggregation.
	 * @return
	 */
	public double getQtyDefective() {
		return qtyDefective;
	}

	/**
	 * Sets the quantity that has been reported as defective during this OEE aggregation.
	 * @param qtyDefective
	 */
	public void setQtyDefective(double qtyDefective) {
		this.qtyDefective = qtyDefective;
	}
	
	/**
	 * @return Gets the predefined period for which this OEE aggregation is calculated.
	 */
	public PredefinedPeriod getPredefinedPeriod(){
		return predefinedPeriod;
	}

	/** 
	 * Gets the SQL statement used to Insert in the database.
	 */
	@Override
	public String getPreparedInsertText() {
		return SQL_Insert;
	}

	/** 
	 * Gets the SQL statement used to delete in the database.
	 */
	@Override
	public String getPreparedDeleteText() {
		return SQL_Delete;
	}

	/** 
	 * Inserts this object in the database by filling the parameters of a prepared statement. 
	 */
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

	/** 
	 * Deletes this object in the database by filling the parameters of a prepared statement. 
	 */
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

	/** 
	 * Gets an string representing the startDttm of the predefined period for which this OEE was previously calculated.  
	 */
	public String getStartDttm() {
		
		if (this.predefinedPeriod.getType() == PredefinedPeriodType.YEAR) {
			return  String.format("%04d",this.predefinedPeriod.getCalendarFrom().get(Calendar.YEAR)) + "-01-01 00:00:00.000"; 
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.MONTH) {
			return  String.format("%04d",this.predefinedPeriod.getCalendarFrom().get(Calendar.YEAR)) + "-" + 
					 String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.MONTH) + 1) + "-01 00:00:00.000";
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.DAY) {
			return  String.format("%04d", this.predefinedPeriod.getCalendarFrom().get(Calendar.YEAR)) + "-" + 
					 String.format("%02d", this.predefinedPeriod.getCalendarFrom().get(Calendar.MONTH) + 1)  + 
					 String.format("%02d", this.predefinedPeriod.getCalendarFrom().get(Calendar.DAY_OF_MONTH)) + " 00:00:00.000";
			
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.HOUR) {
			return  String.format("%04d",this.predefinedPeriod.getCalendarFrom().get(Calendar.YEAR)) + "-" + 
					 String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.MONTH) + 1) + "-" +
					  String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.DAY_OF_MONTH)) + " " + 
					   String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.HOUR_OF_DAY)) + ":00:00.000";
			
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.INT_LT_HOUR) {
			return  String.format("%04d",this.predefinedPeriod.getCalendarFrom().get(Calendar.YEAR)) + "-" + 
					 String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.MONTH) + 1) + "-" + 
					  String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.DAY_OF_MONTH)) + " " + 
					   String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.HOUR_OF_DAY)) + ":" + 
					    String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.MINUTE)) + ":" +
					     String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.SECOND)) + ".000";

		} 
		
		return null;
	}

	/** 
	 * Gets an string representing the endDttm of the predefined period for which this OEE was previously calculated.  
	 */
	public String endDttm() {
		
		if (this.predefinedPeriod.getType() == PredefinedPeriodType.YEAR) {
			return  String.format("%04d",this.predefinedPeriod.getCalendarFrom().get(Calendar.YEAR)) + "-12-31 59:59:59.999"; 
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.MONTH) {
			
			int year = this.predefinedPeriod.getCalendarFrom().get(Calendar.YEAR);
			int month = this.predefinedPeriod.getCalendarFrom().get(Calendar.MONTH) + 1;
			
			YearMonth yearMonthObject = YearMonth.of(year, month);
			int daysInMonth = yearMonthObject.lengthOfMonth();
			
			return  String.format("%04d", this.predefinedPeriod.getCalendarFrom().get(Calendar.YEAR)) + "-" + 
			 		 String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.MONTH) + 1) + "-" +
			 		 String.format("%02d",daysInMonth) + " " + "23:59:59.999";
			
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.DAY) {
			return  String.format("%04d",this.predefinedPeriod.getCalendarFrom().get(Calendar.YEAR)) + "-" + 
					 String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.MONTH) + 1) +  "-" +
					  String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.DAY_OF_MONTH)) + " "+ "23:59:59.999";
			
		} else if (this.predefinedPeriod.getType() == PredefinedPeriodType.HOUR) {
			return  String.format("%04d",this.predefinedPeriod.getCalendarFrom().get(Calendar.YEAR)) + "-" + 
					 String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.MONTH) + 1) + "-" + 
					  String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.DAY_OF_MONTH)) + " " + 
					   String.format("%02d",this.predefinedPeriod.getCalendarFrom().get(Calendar.HOUR_OF_DAY)) + ":59:59.999";
			
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

	/** 
	 * prints the object in string.
	 */
	public String toString(){
		return "predefinedPeriod:" + getPredefinedPeriod().getType().getName() + 
				"productiveTime:" + getProductiveTime() + "qtySchedToProduce:" + 
				   getQtySchedToProduce() + "qtyProduced:" + getQtyProduced() 
				     + "qtyDefective:" + getQtyDefective();  
 		
	}
}
