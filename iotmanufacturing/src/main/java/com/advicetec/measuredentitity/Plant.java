package com.advicetec.measuredentitity;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class Plant extends ProductionEntity {
	
	private String canonicalCompany;
	private String canonicalLocation;
	private String canonicalPlantId;
	
	public String getCannonicalCompany() {
		return canonicalCompany;
	}

	public String getCannonicalLocation() {
		return canonicalLocation;
	}

	public String getCannonicalPlantId() {
		return canonicalPlantId;
	}

	@JsonCreator
	public Plant(@JsonProperty("id") Integer id){
		super(id, MeasuredEntityType.PLANT);
	}

	public void setCannonicalCompany(String company) {
		this.canonicalCompany = company;
	}
	
	public void setCannonicalLocation(String location){
		this.canonicalLocation = location;
	}

	public void setCannonicalPlant(String plant){
		this.canonicalPlantId = plant;
	}
	
	public String getCanonicalIdentifier() {
		return this.canonicalCompany + "-" + this.canonicalLocation + "-" + this.canonicalPlantId;
	}
}
