package com.advicetec.language.ast;

/** A function invocation scope; stores arguments and locals */
public class TransformationSpace extends MemorySpace 
{
    TransformationSymbol def; // what function are we executing?
    
    public TransformationSpace(TransformationSymbol func) 
    {
		super(func.name+" invocation");
        this.def = func;
	}
}
