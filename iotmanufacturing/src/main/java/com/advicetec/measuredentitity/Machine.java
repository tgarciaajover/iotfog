package com.advicetec.measuredentitity;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class Machine extends ProductionEntity {
	
	String cannonicalMachineId;
	String cannonicalCompany;
	String cannonicalLocation;
	String cannonicalPlant;
	
	@JsonCreator
	public Machine(@JsonProperty("id") Integer id){
		super(id, MeasuredEntityType.MACHINE);
	}

	public String getCannonicalMachineId() {
		return cannonicalMachineId;
	}

	public void setCannonicalMachineId(String cannonicalMachineId) {
		this.cannonicalMachineId = cannonicalMachineId;
	}

	public String getCannonicalCompany() {
		return cannonicalCompany;
	}

	public void setCannonicalCompany(String cannonicalCompany) {
		this.cannonicalCompany = cannonicalCompany;
	}

	public String getCannonicalLocation() {
		return cannonicalLocation;
	}

	public void setCannonicalLocation(String cannonicalLocation) {
		this.cannonicalLocation = cannonicalLocation;
	}

	public String getCannonicalPlant() {
		return cannonicalPlant;
	}

	public void setCannonicalPlant(String cannonicalPlant) {
		this.cannonicalPlant = cannonicalPlant;
	}
	
}
