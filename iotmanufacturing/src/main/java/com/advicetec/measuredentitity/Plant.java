package com.advicetec.measuredentitity;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * This class models a Plant as an Entity.
 * This object stores canonical information such as company, location, plant_id.
 * 
 * @author advicetec
 *
 */
@JsonTypeName("P")
public class Plant extends ProductionEntity {
	
	// canonical information
	
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

	/**
	 * Constructor for this Plant object.
	 * @param id Plant identifier.
	 */
	@JsonCreator
	public Plant(@JsonProperty("id") Integer id){
		super(id, MeasuredEntityType.PLANT);
	}

	/**
	 * Set canonical information company.
	 * @param company 
	 */
	public void setCannonicalCompany(String company) {
		this.canonicalCompany = company;
	}
	/**
	 * Set canonical information location.
	 * @param location 
	 */
	public void setCannonicalLocation(String location){
		this.canonicalLocation = location;
	}

	/**
	 * Set canonical information plant.
	 * @param plant
	 */
	public void setCannonicalPlant(String plant){
		this.canonicalPlantId = plant;
	}
	
	/**
	 * Builds and returns the canonical identifier with the information
	 * of company, location and plant id.
	 */
	public String getCanonicalIdentifier() {
		return this.canonicalCompany + "-" + this.canonicalLocation + "-" + this.canonicalPlantId;
	}
}
