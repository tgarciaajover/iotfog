package com.advicetec.language.ast;

public interface Scope 
{


	public String getScopeName(); 		// do I have a name? 

    /** Where to look next for symbols; superclass or enclosing scope */
    public Scope getParentScope();
    /** Scope in which this scope defined. For global scope, it's null */
    public Scope getEnclosingScope();

    /** Define a symbol in the current scope */
    public void define(Symbol sym);

    /** Look up name in this scope or in parent scope if not here */
    public Symbol resolve(String name);
		
}
