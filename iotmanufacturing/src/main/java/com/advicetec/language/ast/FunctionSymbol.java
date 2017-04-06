package com.advicetec.language.ast;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.advicetec.language.BehaviorGrammarParser;


public class FunctionSymbol extends ScopedSymbol
{

	Map<String, Symbol> orderedArgs = new LinkedHashMap<String, Symbol>();
	public BehaviorGrammarParser.BlockContext block;

	public FunctionSymbol(String name, Type retType, BehaviorGrammarParser.BlockContext block, Scope parent) 
	{
        super(name, retType, parent);
        this.block = block; 
    }

    public Map<String, Symbol> getMembers() 
    { 
    	return orderedArgs; 
    }

    public String getName() 
    {
        return name+"("+stripBrackets(orderedArgs.keySet().toString())+")";
    }

}
