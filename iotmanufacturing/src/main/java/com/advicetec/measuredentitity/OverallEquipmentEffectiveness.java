package com.advicetec.measuredentitity;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonProperty;

import com.advicetec.persistence.Storable;
import com.advicetec.utils.PredefinedPeriod;

public class OverallEquipmentEffectiveness implements Storable
{

	static Logger logger = LogManager.getLogger(OverallEquipmentEffectiveness.class.getName());

	// information about the parent.
	private Integer parent;
	private MeasuredEntityType parentType;

	// Calculated Predefined Period
	private PredefinedPeriod predefinedPeriod;

	double availableTime; 

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
			+ "status,reason_code,production_rate, actual_production_rate, qty_defective FROM measuringentitystatusinterval "
			+ "WHERE id_owner = ? AND owner_type = ? AND datetime_from >= ? AND datetime_to <= ? " 
			+ "UNION " 
			+ "SELECT datetime_from,datetime_to,status,reason_code,production_rate, "
			+ "actual_production_rate,qty_defective FROM measuringentitystatusinterval "
			+ "WHERE id_owner = ? AND owner_type = ? AND datetime_from >= ? AND datetime_from <= ? AND datetime_to >= ? "   
			+ "UNION " 
			+ "SELECT datetime_from,datetime_to,status,reason_code,production_rate, "
			+ "actual_production_rate,qty_defective FROM measuringentitystatusinterval "
			+ "WHERE id_owner = ? AND owner_type = ? AND datetime_from <= ? AND datetime_to <= ? ORDER BY datetime_from ";

	public static final String SQL_LIKE = "SELECT id_owner, owner_type,period_key,productive_time,qty_sched_to_produce,qty_produced,qty_defective  FROM measuringentityoee " 
			+ " WHERE id_owner = ? AND owner_type = ? AND period_key like ?";


	public OverallEquipmentEffectiveness(@JsonProperty("predefinedPeriod") PredefinedPeriod predefinedPeriod, 
			@JsonProperty("origin")Integer parent, 
			@JsonProperty("originType")MeasuredEntityType parentType) {
		super();
		this.predefinedPeriod = predefinedPeriod;
		this.parent = parent;
		this.parentType = parentType;
	}

	public Integer getParent() {
		return parent;
	}

	public MeasuredEntityType getParentType() {
		return parentType;
	}


	public double getAvailableTime() {
		return availableTime;
	}

	public void setAvailableTime(double availableTime) {
		this.availableTime = availableTime;
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

	
}
