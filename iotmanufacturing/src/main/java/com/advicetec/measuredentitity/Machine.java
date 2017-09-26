package com.advicetec.measuredentitity;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * This class represents a machine being monitored.
 * 
 * @author Advicetec
 *
 */
@JsonTypeName("M")
public class Machine extends ProductionEntity {
	
	/**
	 * Canonical code for the machine.
	 */
	String canonicalMachineId;
	
	/**
	 * Canonical code for the company where this machine is valid.
	 */
	String canonicalCompany;
	
	/**
	 * Canonical code for the location where this machine is valid.
	 */
	String canonicalLocation;
	
	/**
	 * Canonical code for the plant where this machine is valid.
	 */
	String canonicalPlant;
	
	/**
	 * Canonical code for the machine group where this machine is valid.
	 */
	String canonicalGroup;
	
	/**
	 * Constructor for the class, it assigns as measured entity type machine.
	 *  
	 * @param id internal identifier for the machine.
	 */
	@JsonCreator
	public Machine(@JsonProperty("id") Integer id){
		super(id, MeasuredEntityType.MACHINE);
	}

	/**
	 * Gets the canonical machine code
	 * 
	 * @return	 canonical machine code
	 */
	public String getCannonicalMachineId() {
		return canonicalMachineId;
	}

	/**
	 * Sets the canonical machine code
	 * 
	 * @param cannonicalMachineId 	canonical machine code
	 */
	public void setCannonicalMachineId(String cannonicalMachineId) {
		this.canonicalMachineId = cannonicalMachineId;
	}

	/**
	 * Gets the canonical company code
	 * 
	 * @return	canonical company code
	 */
	public String getCannonicalCompany() {
		return canonicalCompany;
	}

	/**
	 * Sets the canonical company code
	 * 
	 * @param cannonicalCompany	canonical company code
	 */
	public void setCannonicalCompany(String cannonicalCompany) {
		this.canonicalCompany = cannonicalCompany;
	}

	/**
	 * Gets the canonical location code
	 * 
	 * @return	canonical location code
	 */
	public String getCannonicalLocation() {
		return canonicalLocation;
	}

	/**
	 * Sets the canonical location code
	 * 
	 * @param cannonicalLocation	canonical location code
	 */
	public void setCannonicalLocation(String cannonicalLocation) {
		this.canonicalLocation = cannonicalLocation;
	}

	/**
	 * Gets the canonical plant code
	 * 
	 * @return	canonical plant code
	 */
	public String getCannonicalPlant() {
		return canonicalPlant;
	}

	/**
	 * Sets the canonical plant code
	 * 
	 * @param cannonicalPlant	canonical plant code
	 */
	public void setCannonicalPlant(String cannonicalPlant) {
		this.canonicalPlant = cannonicalPlant;
	}

	/**
	 * Sets the machine group code
	 * 
	 * @param machineGroup	machine group code
	 */
	public void setCannonicalGroup(String machineGroup) {
		this.canonicalGroup = machineGroup;
	}
	
	/**
	 * Gets the machine group code
	 *  
	 * @return	machine group code	
	 */
	public String getCannonicalGroup(){
		return canonicalGroup;
	}
	
	/**
	 * Gets a canonical identifier for the instance based on the canonical codes.
	 * 
	 * @return canonical key that uniquely identifies the entity in the host system
	 */
	public String getCanonicalIdentifier() {
		return this.canonicalCompany + "-" + this.canonicalLocation + "-" + this.canonicalPlant + "-" + this.canonicalGroup + "-" + this.canonicalMachineId;  
	}
}
