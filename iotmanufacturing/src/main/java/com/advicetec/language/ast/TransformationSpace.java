package com.advicetec.language.ast;

/** 
 * A function invocation space for the main program of the transformation language; 
 * it stores value arguments and variable local values.
 * 
 * @author Andres Marentes
 */
public class TransformationSpace extends MemorySpace 
{
    /**
     * what function are we executing?
     */
    TransformationSymbol def; 
    
    /**
     * Constructor for the class. It gets as parameter the transformation symbol.
     * 
     * @param func  Main program function.
     */
    public TransformationSpace(TransformationSymbol func) 
    {
		super(func.name+" invocation");
        this.def = func;
	}
}
