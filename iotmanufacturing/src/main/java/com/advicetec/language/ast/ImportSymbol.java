package com.advicetec.language.ast;

import java.util.ArrayList;


public class ImportSymbol extends Symbol  
{
	ArrayList<String> longName;
	
	public ImportSymbol(String name) 
	{ 
		super(name,Type.tVOID); 
	}

	public ArrayList<String> getLongName() {
		return longName;
	}

	public void addId(String id) {
		this.longName.add(id);
	}
	
}
