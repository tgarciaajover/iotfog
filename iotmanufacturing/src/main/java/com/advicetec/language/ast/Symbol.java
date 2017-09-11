package com.advicetec.language.ast;

/**
 * 
 * @author Andres Marentes
 *
 */
public abstract class Symbol
{

	/**
	 * Enumeration with type of symbols.
	 * 
	 * @author Andres Marentes
	 */
	public static enum Type {tINVALID, tVOID, tINT, tFLOAT, tSTRING, tBOOL, tDATETIME, tDATE, tTIME}
	
	/**
	 * All symbols have a name
	 */
	String name; 
	
	/**
	 * Symbols have a type
	 */
	Type type;
	
	/**
	 * All symbols know what scope contains them.
	 */
	public Scope scope;      

	/**
	 * Constructor for the class 
	 * 
	 * @param name name of the symbol.
	 */
	public Symbol(String name) 
	{ 
		this.name = name;
	} 
	
	/**
	 * Alternative constructor for the class by using type and name
	 * @param name 	name of the symbol
	 * @param type	symbol's type
	 */
	public Symbol(String name, Type type) 
	{
		this(name); 
		this.type = type;
	} 
	
	/**
	 * Gets the name of the symbol.
	 *
	 * @return symbol's name
	 */
	public String getName() 
	{ 
		return name; 
	} 
	
	/**
	 * Gets the type of the symbol.
	 *
	 * @return symbol's type
	 */
	public Type getType()
	{
		return this.type;
	}
	
	/**
	 *Serialize the symbol into string
	 *
	 * @return string representing the symbol. 
	 */
	public String toString() 
	{ 
		if ( type!=null ) return '<' +getName()+ ":" + type + '>' ; 
		return getName(); 
	}

    /**
     * Auxiliary method to remove the first and the last characters from the string
     *   
     * @param s string to remove those two characters.
     * @return substring. 
     */
    public static String stripBrackets(String s) {
        return s.substring(1,s.length()-1);
    }
	
}
