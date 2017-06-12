package com.advicetec.measuredentitity;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class Plant extends ProductionEntity {
	
	private String cannonicalCompany;
	private String cannonicalLocation;
	private String cannonicalPlantId;
	
	public String getCannonicalCompany() {
		return cannonicalCompany;
	}

	public String getCannonicalLocation() {
		return cannonicalLocation;
	}

	public String getCannonicalPlantId() {
		return cannonicalPlantId;
	}

	@JsonCreator
	public Plant(@JsonProperty("id") Integer id){
		super(id, MeasuredEntityType.PLANT);
	}

	public void setCannonicalCompany(String company) {
		this.cannonicalCompany = company;
	}
	
	public void setCannonicalLocation(String location){
		this.cannonicalLocation = location;
	}

	public void setCannonicalPlant(String plant){
		this.cannonicalPlantId = plant;
	}
}
