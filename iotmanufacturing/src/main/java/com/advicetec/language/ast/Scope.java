package com.advicetec.language.ast;

import java.util.Set;

/**
 * Interface specifying which methods should implement any scope.
 * 
 * @author Andres Marentes
 *
 */
public interface Scope 
{

	/**
	 * Every scope have a name
	 * 
	 * @return the scope name
	 */
	public String getScopeName(); 		 

    /** 
     * Where to look next for symbols; superclass or enclosing scope 
     * */
    public Scope getParentScope();
    
    /** 
     * Scope in which this scope is defined. For global scope, it's null 
     * */
    public Scope getEnclosingScope();

    /** 
     * Define a symbol in the current scope 
     * */
    public void define(Symbol sym);

    /** 
     * Look up name in this scope or in parent scope if not here 
     * */
    public Symbol resolve(String name);
	
    /** 
     * Return the name of the symbols included 
     * */
    public Set<String> getkeys();
    
}
