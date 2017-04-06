package com.advicetec.language.transformation;

import org.antlr.v4.runtime.Token;

/** How to response to messages and errors from interpreter */
public interface InterpreterListener 
{
    public void info(String msg);
    
    public void error(String msg);
    
    public void error(String msg, Exception e);
    
    public void error(String msg, Token t);
}
