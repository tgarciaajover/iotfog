package com.advicetec.language.ast;

/**
 * This class represents the local scope within a function.
 * 
 * @author Andres Marentes
 */
public class LocalScope extends BaseScope
{
    /**
     * Constructor for the class. Receives as parameter the parent scope.
     * 
     * @param parent The parent scope
     */
    public LocalScope(Scope parent) 
    { 
    	super(parent); 
    }
    
    /**
     * Gets the scope name
     * 
     *  @return scope name which in this case is the string local.
     */
    public String getScopeName() 
    { 
    	return "local"; 
    }
}
