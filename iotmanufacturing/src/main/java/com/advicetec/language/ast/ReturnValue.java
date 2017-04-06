package com.advicetec.language.ast;

/** Unchecked exception used to pass Pie return value all the way out
 *  of deeply nested java method call chain.
 */
public class ReturnValue extends Error 
{
    public ASTNode value;
    public ReturnValue() { super(""); }
}
