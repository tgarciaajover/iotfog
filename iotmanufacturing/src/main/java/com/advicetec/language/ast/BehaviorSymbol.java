package com.advicetec.language.ast;


import com.advicetec.language.BehaviorGrammarParser;


public class BehaviorSymbol extends FunctionSymbol
{

	public BehaviorSymbol(String name, Type retType, BehaviorGrammarParser.BlockContext block, Scope parent) 
	{
        super(name, retType, block, parent);
    }


}
