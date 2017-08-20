package com.advicetec.measuredentitity;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

@JsonTypeName("P")
public class Plant extends ProductionEntity {
	
	@JsonProperty("id_compania")
	private String canonicalCompany;
	
	@JsonProperty("id_sede")
	private String canonicalLocation;
	
	@JsonProperty("id_planta")
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
