package com.advicetec.language.ast;

/** 
 * A function invocation scope; stores arguments and locals variables given to the main behavior program  
 *
 * @author Andres Marentes
 */
public class BehaviorSpace extends FunctionSpace 
{    
    /**
     * Constructor for the behavior scope. 
     * 
     * It receives the behavior function symbol for which this scope is created.  
     * 
     * @param func  behavior function symbol
     */
    public BehaviorSpace(BehaviorSymbol func) 
    {
		super(func);
	}
}
