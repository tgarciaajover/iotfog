package com.advicetec.language.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.IntStream;

public class SyntaxError extends RecognitionException 
{

    public SyntaxError(String message, RecognitionException e) {
        super(message, e.getRecognizer(), e.getInputStream(), (ParserRuleContext) e.getCtx());
        this.setOffendingToken(e.getOffendingToken());
        this.initCause(e);
    }
    
    public SyntaxError(String message, Token t, Recognizer<?,?> recognizer, IntStream inputStream, ParserRuleContext ctx){
    	super(message, recognizer, inputStream, ctx);
    	this.setOffendingToken(t);
    }
}