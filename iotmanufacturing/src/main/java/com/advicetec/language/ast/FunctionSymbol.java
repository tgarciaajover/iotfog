package com.advicetec.language.ast;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.advicetec.language.BehaviorGrammarParser;

/**
 * The Function symbol represents any function defined in the language.
 * 
 * @author Andres Marentes
 *
 */
public class FunctionSymbol extends ScopedSymbol
{

	/**
	 * This is the ordered set of parameters given to the function.  
	 */
	Map<String, Symbol> orderedArgs = new LinkedHashMap<String, Symbol>();
	
	/**
	 * Set of instructions to execute in the function.
	 */
	public BehaviorGrammarParser.BlockContext block;

	/**
	 * Constructor for the class.
	 * 
	 * @param name		function's name
	 * @param retType	function return type.
	 * @param block		set of instructions to execute as part of the function.
	 * @param parent  	parent scope.
	 */
	public FunctionSymbol(String name, Type retType, BehaviorGrammarParser.BlockContext block, Scope parent) 
	{
        super(name, retType, parent);
        this.block = block; 
    }

    /**
     * Gets the set of arguments.
     * 
     *  @param ordered set of arguments.
     */
    public Map<String, Symbol> getMembers() 
    { 
    	return orderedArgs; 
    }

    /** 
     * Gets the function's name
     * 
     *  @return function's name
     */
    public String getName() 
    {
        return name+"("+stripBrackets(orderedArgs.keySet().toString())+")";
    }

    /** 
     * Gets the name of the variables given as parameters
     * 
     * @param set of variable names.
     */
    public Set<String> getkeys()
    {
    	return orderedArgs.keySet();
    }
}
