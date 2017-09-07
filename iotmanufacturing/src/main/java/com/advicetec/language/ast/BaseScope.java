package com.advicetec.language.ast;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class for scopes. 
 * 
 * Scopes represent memory spaces where symbols are defined. Scope examples are the global scope, 
 * function scopes, etc...
 * 
 * Scopes forms a heap. For example, During a function call in the language, the interpreter
 * creates a child scope which is added to the heap. The last scope in the heap is referenced 
 * in the class attribute enclosingScope. When the function ends, the scope is removed 
 * from the heap and interpreter sets the current scope as the one in enclosing scope.  
 * 
 * @author Andres Marentes
 */
public abstract class BaseScope implements Scope
{

	/**
	 * A reference to the parent scope, null if global (outermost) scope 
	 */
	Scope enclosingScope; 
	
	/**
	 * This map maintains the symbols registered or defined in this scope. 
	 * The String key corresponds to the Symbol name, the Symbol corresponds 
	 * to the symbol definition. 
	 * 
	 * We can not have two symbols with the name in the same scope.   
	 */
	Map<String, Symbol> symbols = new LinkedHashMap<String, Symbol>();

    /**
     * Constructor for the class. It receives the parent scope  
     * @param parent parent scope.
     */
    public BaseScope(Scope parent) 
    { 
    	this.enclosingScope = parent;	
    }

    /**
     * Gets the symbol by its name. 
     * This method is recursive and search the symbol in parent scopes. 
     * 
     * If the symbol cannot be found in any of the scopes on the heap, then the system
     * returns null.
     * 
     * @param name of the symbol to be returned
     */
    public Symbol resolve(String name) 
    {
		Symbol s = symbols.get(name);
        if ( s!=null ) return s;
		// if not here, check any enclosing scope
		if ( getParentScope() != null ) return getParentScope().resolve(name);
		return null; // not found
	}

	/**
	 * Defines a symbol in this scope.
	 * 
	 * @param symbol to be defined in the current scope.
	 */
	public void define(Symbol sym) 
	{
		symbols.put(sym.name, sym);
		sym.scope = this; // track the scope in each symbol
	}

    /**
     * Gets the parent scope from this scope.
     * 
     * @return the parent scope
     */
    public Scope getParentScope() 
    { 
    	return getEnclosingScope(); 
    }
    
    /**
     * Gets the enclosing scope
     * 
     * @return enclosing scope
     */
    public Scope getEnclosingScope() 
    { 
    	return enclosingScope; 
    }

	/**
	 * string serialization
	 * 
	 * @return get the name of the symbols registered in the scope
	 */
	public String toString() 
	{ 
		return symbols.keySet().toString(); 
	}
	
	/**
	 * Gets the symbol map  
	 * 
	 * @return symbol map
	 */
	public Map<String, Symbol> getSymbolMap(){
		return symbols;
	}

	/**
	 * Gets the name of the symbols variables 
	 * 
	 * @return get the names of the symbols registered in the scope
	 */
	public Set<String> getkeys() 
	{ 
		return symbols.keySet(); 
	}
}
