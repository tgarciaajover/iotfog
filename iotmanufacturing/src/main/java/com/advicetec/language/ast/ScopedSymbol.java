package com.advicetec.language.ast;

import java.util.Map;

/**
 * This class represents a symbols that produces also a scope; such as, functions.
 * 
 * @author Andres Marentes
 */
public abstract class ScopedSymbol extends Symbol implements Scope 
{
    /**
     *  Parent scope
     */
    Scope enclosingScope;

    /**
     * Constructor for the class
     * 
     * @param name				name of the symbol
     * @param type				type of the symbol
     * @param enclosingScope	parent scope
     */
    public ScopedSymbol(String name, Type type, Scope enclosingScope) {
        super(name, type);
        this.enclosingScope = enclosingScope;
    }
    
    /**
     * Constructor for the class
     * 
     * @param name				name of the symbol
     * @param enclosingScope	parent scope
     */
    public ScopedSymbol(String name, Scope enclosingScope) {
        super(name);
        this.enclosingScope = enclosingScope;
    }

    /**
     * Gets the a symbol by name
     * 
     * @return the symbol that has the name given as parameter
     */
    public Symbol resolve(String name) {
        Symbol s = getMembers().get(name);
        if ( s!=null ) return s;
        // if not here, check any parent scope
        if ( getParentScope() != null ) {
            return getParentScope().resolve(name);
        }
        return null; // not found
    }

    /**
     * Defines a symbol in this scope
     * 
     * @return symbol to include.
     */
    public void define(Symbol sym) {
        getMembers().put(sym.name, sym);
        sym.scope = this; // track the scope in each symbol
    }

    /**
     * Gets the parent scope 
     * 
     * @return parent scope
     */
    public Scope getParentScope() 
    { 
    	return getEnclosingScope(); 
    }
    
    /**
     * Gets the enclosing scope
     * 
     *  @return enclosing scope
     */
    public Scope getEnclosingScope() 
    { 
    	return enclosingScope; 
    }    

    /**
     * Gets the name of the scope
     * 
     * @return scope name
     */
    public String getScopeName() 
    { 
    	return name; 
    }

    /** Indicate how subclasses store scope members. Allows us to
     *  factor out common code in this class.
     */
    public abstract Map<String, Symbol> getMembers();
}
