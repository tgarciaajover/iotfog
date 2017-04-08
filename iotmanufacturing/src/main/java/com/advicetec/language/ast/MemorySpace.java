package com.advicetec.language.ast;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/** A scope of variable:value pairs */
public class MemorySpace 
{
    String name; // mainly for debugging purposes
    Map<String, ASTNode> members = new HashMap<String, ASTNode>();

    public MemorySpace(String name) { this.name = name; }

    public ASTNode get(String id) { return members.get(id); }

    public void put(String id, ASTNode value) { members.put(id, value); }

    public String toString() { return name+":"+members; }
    
    public Set<String> getkeys() { return members.keySet(); }

	public Map<String, ASTNode> getSymbolMap(){
		return members;
	}
}
