package com.advicetec.language.ast;

import java.util.LinkedHashMap;
import java.util.Map;

import com.advicetec.language.TransformationGrammarParser;


public class TransformationSymbol extends ScopedSymbol
{

	Map<String, Symbol> orderedArgs = new LinkedHashMap<String, Symbol>();
	public TransformationGrammarParser.BlockContext block;

	public TransformationSymbol(String name, Type retType, TransformationGrammarParser.BlockContext block, Scope parent) 
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
