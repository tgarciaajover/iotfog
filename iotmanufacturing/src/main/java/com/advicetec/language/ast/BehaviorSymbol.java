package com.advicetec.language.ast;

import com.advicetec.language.BehaviorGrammarParser;

/**
 * The behavior symbol represents the main program which is executed. 
 * 
 * @author Andres Marentes
 */
public class BehaviorSymbol extends FunctionSymbol
{

	/**
	 * Constructor for the class. 
	 * 
	 * @param name		Name given to the program
	 * @param retType	Return type (it can be any of the primitive types)
	 * @param block		The block of instructions part of the program 
	 * @param parent	parent scope.
	 */
	public BehaviorSymbol(String name, Type retType, 
							BehaviorGrammarParser.BlockContext block, Scope parent) 
	{
        super(name, retType, block, parent);
    }

}
