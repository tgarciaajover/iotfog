package com.advicetec.measuredentitity;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class Machine extends ProductionEntity {
	
	String canonicalMachineId;
	String canonicalCompany;
	String canonicalLocation;
	String canonicalPlant;
	String canonicalGroup;
	
	@JsonCreator
	public Machine(@JsonProperty("id") Integer id){
		super(id, MeasuredEntityType.MACHINE);
	}

	public String getCannonicalMachineId() {
		return canonicalMachineId;
	}

	public void setCannonicalMachineId(String cannonicalMachineId) {
		this.canonicalMachineId = cannonicalMachineId;
	}

	public String getCannonicalCompany() {
		return canonicalCompany;
	}

	public void setCannonicalCompany(String cannonicalCompany) {
		this.canonicalCompany = cannonicalCompany;
	}

	public String getCannonicalLocation() {
		return canonicalLocation;
	}

	public void setCannonicalLocation(String cannonicalLocation) {
		this.canonicalLocation = cannonicalLocation;
	}

	public String getCannonicalPlant() {
		return canonicalPlant;
	}

	public void setCannonicalPlant(String cannonicalPlant) {
		this.canonicalPlant = cannonicalPlant;
	}

	public void setCannonicalGroup(String machineGroup) {
		this.canonicalGroup = machineGroup;
	}
	
	public String getCannonicalGroup(){
		return canonicalGroup;
	}
	
	public String getCanonicalIdentifier() {
		return this.canonicalCompany + "-" + this.canonicalLocation + "-" + this.canonicalPlant + "-" + this.canonicalGroup + "-" + this.canonicalMachineId;  
	}
}
