package com.advicetec.language.ast;

/** 
 * A function invocation scope; stores function arguments and locals 
 *
 * @author Andres Marentes
 */
public class FunctionSpace extends MemorySpace {
	
    /**
     * This is the function that we are executing
     */
    FunctionSymbol def; 
    
    /**
     * Constructor for the class. it receives the function symbol being called.  
     * 
     * @param func function being called.
     */
    public FunctionSpace(FunctionSymbol func) 
    {
		super(func.name+" invocation");
        this.def = func;
	}
}
