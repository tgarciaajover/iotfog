package com.advicetec.language.ast;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.advicetec.language.TransformationGrammarParser;

/**
 * Symbol representing the main program in the transformation language.
 * 
 * @author Andres Marentes
 */
public class TransformationSymbol extends ScopedSymbol
{

	/**
	 * Ordered set of arguments given to the transformation language
	 */
	Map<String, Symbol> orderedArgs = new LinkedHashMap<String, Symbol>();
	
	/**
	 * Set of instructions that should be executed
	 */
	public TransformationGrammarParser.BlockContext block;

	/**
	 * Constructor for the class, it takes: 
	 * 
	 * @param name     name of the main transformation function. 
	 * @param retType  return type
	 * @param block	   set of instructions to execute
	 * @param parent   Parent scope, this is the global scope.
	 */
	public TransformationSymbol(String name, Type retType, TransformationGrammarParser.BlockContext block, Scope parent) 
	{
        super(name, retType, parent);
        this.block = block; 
    }

    /**
     * Gets the arguments given to the function
     */
    public Map<String, Symbol> getMembers() 
    { 
    	return orderedArgs; 
    }

    /**
     * Get the name of the transformation scope
     */
    public String getName() 
    {
        return name+"("+stripBrackets(orderedArgs.keySet().toString())+")";
    }

    /**
     * Gets the name of the parameters.
     */
    public Set<String> getkeys()
    {
    	return orderedArgs.keySet();
    }

}
