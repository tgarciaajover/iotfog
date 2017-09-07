package com.advicetec.language.ast;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/** 
 * A memory space is a table that relates the name of the symbol and the value 
 * 	 that it takes during the execution of the language. 
 */
public class MemorySpace 
{
	/**
	 * Name of the memory space, it is mainly used for debugging purposes 
	 */
	String name;

	/**
	 * Map that relates the name of the symbol and its current value.
	 */
	Map<String, ASTNode> members = new HashMap<String, ASTNode>();

	/**
	 * Constructor for the class 
	 * @param name  name of the memory space.
	 */
	public MemorySpace(String name) { 
		this.name = name; 
	}

	/**
	 * Gets the current value of a symbol name
	 * 
	 * @param id 	Name of the symbol that we want to get its value
	 * 
	 * @return value assigned to the symbol.
	 */
	public ASTNode get(String id) { 
		return members.get(id); 
	}

	/**
	 * Puts a new symbol with its value in the memory space.
	 * 
	 * @param id symbol's name
	 * @param value	value to be assigned.
	 */
	public void put(String id, ASTNode value) { 
		members.put(id, value); 
	}

	/**
	 * serialize the memory space to string
	 */
	public String toString() { 
		return name+":"+members; 
	}

	/**
	 * Gets the name of the symbols registered in the memory space
	 * 
	 * @return set of symbol's name
	 */
	public Set<String> getkeys() { 
		return members.keySet(); 
	}

	/**
	 * Gets a reference to the whole memory space 
	 * 
	 * @returnMap maintaining the relationship (symbol name, value) 
	 */
	public Map<String, ASTNode> getSymbolMap() {
		return members;
	}
}
