package com.advicetec.language.behavior;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import com.advicetec.language.BehaviorGrammarBaseListener;
import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.TransformationGrammarParser;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.VariableSymbol;

public class RefPhase extends BehaviorGrammarBaseListener 
{

	BehaviorGrammarParser parser = null;
	ParseTreeProperty<Scope> scopes;
	GlobalScope globals;
	Scope currentScope;
	private ArrayList<SyntaxError> compilationErrors;
	
	
	RefPhase(BehaviorGrammarParser parser, GlobalScope globals , ParseTreeProperty<Scope> scopes)
	{
		this.scopes = scopes;
		this.globals = globals;
		this.parser = parser;
		compilationErrors = new ArrayList<SyntaxError>();
	}
	
	
	public void error(Token t, ParserRuleContext ctx, String msg) 
    {
    	String error = new String("line" + t.getLine() + "." + t.getCharPositionInLine() + msg + "\n");
    	SyntaxError e= new SyntaxError(error, t, this.parser, this.parser.getInputStream(), ctx); 
    	this.compilationErrors.add(e);

    }	

	public List<SyntaxError> getErrors()
	{
		return this.compilationErrors;
	}
	
	public void enterProgram(BehaviorGrammarParser.ProgramContext ctx)
	{
		currentScope = globals;
	}	
	
	public void enterFunction_dec(BehaviorGrammarParser.Function_decContext ctx)
	{
		currentScope = scopes.get(ctx);		
	}
	
	public void exitAtrib_dec(BehaviorGrammarParser.Atrib_decContext ctx)
	{ 

		System.out.println("in exit atrib dec : ");
		
		// It verifies the unit of measure given as parameter.
		if (ctx.id2 != null){
			String name = ctx.id2.getText();
				
			Symbol var = currentScope.resolve(name);
			
			if ( var == null)
			{
				this.error(ctx.id2, ctx, "no such Symbol: " + name);
			}
		}
	}
	
	public void exitFunction_dec(BehaviorGrammarParser.Function_decContext ctx) 
	{
		currentScope = scopes.get(ctx);
	}
	
	public void enterBlock(BehaviorGrammarParser.BlockContext ctx)
	{
		currentScope = scopes.get(ctx); 
	}
	
	public void exitBlock(BehaviorGrammarParser.BlockContext ctx) {

        currentScope = currentScope.getEnclosingScope();

    }
	
	public void exitVar(BehaviorGrammarParser.VarContext ctx) 
	{ 
		String name = ctx.ID().getSymbol().getText();
		Symbol var = currentScope.resolve(name);
		
		if ( var == null)
		{
			this.error(ctx.ID().getSymbol(), ctx, "no such variable: " + name);
		}
		
		if (var instanceof FunctionSymbol) 
		{
			this.error(ctx.ID().getSymbol(), ctx, name + " is not a variable" );
		}
	}
	
	public void exitCall(BehaviorGrammarParser.CallContext ctx) {

        // can only handle f(...) not expr(...)

        String funcName = ctx.ID().getText();

        Symbol meth = currentScope.resolve(funcName);

        if ( meth==null ) {

            this.error(ctx.ID().getSymbol(), ctx, "no such function: "+funcName);

        }

        if ( meth instanceof VariableSymbol ) {

        	this.error(ctx.ID().getSymbol(), ctx, funcName+" is not a function");

        }

    }
}
