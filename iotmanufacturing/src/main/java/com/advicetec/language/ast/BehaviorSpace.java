package com.advicetec.language.ast;

/** A function invocation scope; stores arguments and locals */
public class BehaviorSpace extends FunctionSpace 
{    
    public BehaviorSpace(BehaviorSymbol func) 
    {
		super(func);
	}
}
