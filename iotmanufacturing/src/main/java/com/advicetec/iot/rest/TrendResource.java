package com.advicetec.iot.rest;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

public class TrendResource extends ServerResource{
	static final Logger logger = LogManager.getLogger(TrendResource.class.getName());

	@Get("json")
	public Representation getTrendVariable(Representation rep){
		Representation result = null;

		// get the parameters
		String canMachineId = getQueryValue("machineId");
		String canCompany = getQueryValue("company");
		String canLocation = getQueryValue("location");
		String canPlant = getQueryValue("plant");
		String reqStartDateTime = getQueryValue("startDttm");
		String reqEndDateTime = getQueryValue("endDttm");
		String trendVar = getQueryValue("variable");

		try {
			Integer uniqueID = MeasuredEntityManager.getInstance()
					.getMeasuredEntityId(canCompany,canLocation,canPlant,canMachineId);
			// Look for it in the database.
			MeasuredEntityFacade facade = MeasuredEntityManager.getInstance()
					.getFacadeOfEntityById(uniqueID);

			if(facade == null){
				result = new JsonRepresentation("");
				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
				logger.error("Facade does not found:"+uniqueID);
			}else{
				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MMM-dd H:m:s.n");
				LocalDateTime dttmFrom = LocalDateTime.parse(reqStartDateTime,format); 
				LocalDateTime dttmTo = LocalDateTime.parse(reqEndDateTime,format);

				// get the array from the facade.
				JSONArray jsonArray = facade.getJsonTrend(trendVar,dttmFrom, dttmTo);
				result = new JsonRepresentation(jsonArray);
			}
		} catch (SQLException e) {
			logger.error("SQL failure.");
			e.printStackTrace();
		}
		return result;
	}
}
