package com.advicetec.measuredentitity;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * This class models a Plant as an Entity.
 * 
 * This object stores canonical information such as company, location, plant_id.
 * 
 * @author advicetec
 *
 */
@JsonTypeName("P")
public class Plant extends ProductionEntity {
	
	// canonical information
	
	/**
	 *  canonical company identifier 
	 */
	@JsonProperty("id_compania")
	private String canonicalCompany;
	
	/**
	 * canonical location identifier
	 */
	@JsonProperty("id_sede")
	private String canonicalLocation;
	
	/**
	 * canonical plant identifier
	 */
	@JsonProperty("id_planta")
	private String canonicalPlantId;
	
	
	/**
	 * Gets the canonical company identifier
	 * 
	 * @return	canonical company identifier
	 */
	public String getCannonicalCompany() {
		return canonicalCompany;
	}

	/**
	 * Gets the canonical location identifier
	 * @return	canonical location identifier
	 */
	public String getCannonicalLocation() {
		return canonicalLocation;
	}

	/**
	 * Gets the canonical plant identifier 
	 * @return	canonical plant identifier
	 */
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
