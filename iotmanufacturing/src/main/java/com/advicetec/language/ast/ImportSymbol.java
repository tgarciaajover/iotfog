package com.advicetec.language.ast;

import java.util.ArrayList;

/**
 * This class represents an import to behaviors that are coded in the language. 
 * 
 * @author Andres Marentes
 */
public class ImportSymbol extends Symbol  
{
	/**
	 * Set of names used to reference the behavior 
	 */
	ArrayList<String> longName;
	
	/**
	 * Constructor for the class.
	 * 
	 * @param name import name
	 */
	public ImportSymbol(String name) 
	{ 
		super(name,Type.tVOID); 
		longName = new ArrayList<String>();
	}

	/**
	 * Gets the set of names that forms the long name 
	 * 
	 * @return list of names
	 */
	public ArrayList<String> getLongName() {
		return longName;
	}

	/**
	 * Add a new name to the set of parts used to reference an import.
	 * 
	 * @param id part used to reference the import.
	 */
	public void addId(String id) {
		this.longName.add(id);
	}
	
}
