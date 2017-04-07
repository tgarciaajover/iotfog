package com.advicetec.language.behavior;


import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import com.advicetec.language.BehaviorGrammarBaseListener;
import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.ast.ArrayAttributeSymbol;
import com.advicetec.language.ast.ArraySymbol;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.LocalScope;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.language.ast.VariableSymbol;
import com.advicetec.language.ast.BehaviorSymbol;


import org.antlr.v4.runtime.Token;
import org.codehaus.plexus.compiler.CompilerException;

public class DefPhase extends BehaviorGrammarBaseListener 
{

	private ParseTreeProperty<Scope> scopes = new ParseTreeProperty<Scope>();
	private GlobalScope globals;
	
	// Define symbols in this scope
	private Scope currentScope;
	
	public void enterProgram(BehaviorGrammarParser.ProgramContext ctx)
	{
		globals = new GlobalScope();
		currentScope = globals;
		String name = ctx.PROGRAM().getText();

		// Transformation program does not have a return value.
		Symbol.Type type = SyntaxChecking.getType(BehaviorGrammarParser.K_VOID);

		// push new scope by making new one that points to enclosing scope
		BehaviorSymbol program = new BehaviorSymbol(name, type, ctx.block(), currentScope);

		// Defines the function in the current scope.
		currentScope.define(program);
	
		
		// Push: set function's parent to current
		saveScope(ctx, program); 
		
		// Current Scope is now function scope
		currentScope = program;
	}
	
	public void exitProgram(BehaviorGrammarParser.ProgramContext ctx)
	{
		System.out.println("Exit program: " );
		System.out.println(globals);
	}
	
	public void exitProgramparameter(BehaviorGrammarParser.ProgramparameterContext ctx) 
	{ 
		defineVar(ctx.type(), ctx.ID().getSymbol());
	}

	
	public void enterFunction_dec(BehaviorGrammarParser.Function_decContext ctx)
	{
		String name = ctx.ID().getText();
		int typeTokenType = ctx.type().start.getType();
		
		Symbol.Type type = SyntaxChecking.getType(typeTokenType);
		
		// push new scope by making new one that points to enclosing scope
		FunctionSymbol function = new FunctionSymbol(name, type, ctx.block(), currentScope);
		
		// Defines the function in the current scope.
		currentScope.define(function);
		
		System.out.println("Function Name: " + name + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
		
		// Push: set function's parent to current
		saveScope(ctx, function); 
		
		// Current Scope is now function scope
		currentScope = function;
	}
	
	public void saveScope(ParserRuleContext ctx, Scope s)
	{
		scopes.put(ctx, s);
	}
	
	public void exitFunction_dec(BehaviorGrammarParser.Function_decContext ctx) 
	{ 
		System.out.println(currentScope);
		
		// pop scope
		currentScope = currentScope.getEnclosingScope();
		
		System.out.println("exitFunction_dec" + currentScope);
	}
	
	public void enterBlock(BehaviorGrammarParser.BlockContext ctx) 
	{ 
		if (currentScope instanceof FunctionSymbol){   
			System.out.println( "enterBlock" + currentScope + (((FunctionSymbol)currentScope).getMembers()).size() );
		} 
		else
		{
			System.out.println("enterBlock" + currentScope);
		}
		
		// push new scope by making new one that points to enclosing scope
		LocalScope local = new LocalScope(currentScope);
				
		// Push: set function's parent to current
		saveScope(ctx, local); 
		
		// Current Scope is now function scope
		currentScope = local;
		
	}
	
	public void exitBlock(BehaviorGrammarParser.BlockContext ctx) 
	{ 
		System.out.println("exitBlock" + currentScope);
		
		// pop scope
		currentScope = currentScope.getEnclosingScope();
		
	}
	
	public void exitFormalparameter(BehaviorGrammarParser.FormalparameterContext ctx) 
	{ 
		defineVar(ctx.type(), ctx.ID().getSymbol());
	}
	
	public void exitVar_dec(BehaviorGrammarParser.Var_decContext ctx) 
	{ 
		defineVar(ctx.type(), ctx.ID().getSymbol());
	}

	public void exitUnit_dec(BehaviorGrammarParser.Unit_decContext ctx) 
	{ 
		defineUnit(ctx.ID().getText(), ctx.STRING().getText());	
	}

	public void exitAtrib_dec(BehaviorGrammarParser.Atrib_decContext ctx) 
	{ 
		defineAttribute(ctx.type(), ctx.id1, ctx.id2);
	}
	
	public void exitVect_attrib_dec(BehaviorGrammarParser.Vect_attrib_decContext ctx) 
	{ 

		System.out.println("exit in enterVect attrib dec");
		
		if ((ctx.numElements.getStopIndex() >= 0) || (ctx.numElements.getStartIndex() >= 0) )
		{
			int numElem = Integer.valueOf(ctx.numElements.getText());
			defineAttributeArray(ctx.type(), ctx.id1, numElem, ctx.id2);
		}
	}
	
	public void exitVect_var_dec(BehaviorGrammarParser.Vect_var_decContext ctx) 
	{ 
		System.out.println("enter in exitVect var dec- stop:" + ctx.numElements.getStopIndex() + "Start: " + ctx.numElements.getStartIndex());
		
		if ((ctx.numElements.getStopIndex() >= 0) || (ctx.numElements.getStartIndex() >= 0) )
		{
			int numElem = Integer.valueOf(ctx.numElements.getText());				
			defineVarArray(ctx.type(), ctx.id1, numElem);
		}
	}

	public void defineAttributeArray(BehaviorGrammarParser.TypeContext typeCtx, Token nameToken, int numElem, Token unit)
	{
		int typeTokenType = typeCtx.start.getType();
		Symbol.Type type = SyntaxChecking.getType(typeTokenType);
		
		ArrayAttributeSymbol atr = new ArrayAttributeSymbol(nameToken.getText(), type, numElem);

		if (unit != null)
			atr.setUnitOfMeasure(unit.getText());
		
		// Define the symbol in the current scope
		currentScope.define(atr);

		System.out.println("Define attribute array: " + atr.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	
	}

	public void defineVarArray(BehaviorGrammarParser.TypeContext typeCtx, Token nameToken, int numElem)
	{
		int typeTokenType = typeCtx.start.getType();
		Symbol.Type type = SyntaxChecking.getType(typeTokenType);
		
		ArraySymbol atr = new ArraySymbol(nameToken.getText(), type, numElem);

		// Define the symbol in the current scope
		currentScope.define(atr);

		System.out.println("Define var array: " + atr.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	
	}

	public void defineAttribute(BehaviorGrammarParser.TypeContext typeCtx, Token nameToken, Token unit)
	{
		int typeTokenType = typeCtx.start.getType();
		
		Symbol.Type type = SyntaxChecking.getType(typeTokenType);
		
		AttributeSymbol atr = new AttributeSymbol(nameToken.getText(), type);
		
		if (unit != null)
			atr.setUnitOfMeasure(unit.getText());
		
		// Define the symbol in the current scope
		currentScope.define(atr);
		
		System.out.println("Define attribute: " + atr.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	}
	
	public void defineUnit(String unitId, String descr )
	{
		
		UnitMeasureSymbol unt = new UnitMeasureSymbol(unitId, descr);
		
		// Define the symbol in the current scope
		currentScope.define(unt);
		
		System.out.println("Define unit: " + unt.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	}
	
	public void defineVar(BehaviorGrammarParser.TypeContext typeCtx, Token nameToken)
	{
		int typeTokenType = typeCtx.start.getType();
		
		Symbol.Type type = SyntaxChecking.getType(typeTokenType);
		
		VariableSymbol var = new VariableSymbol(nameToken.getText(), type);
		
		// Define the symbol in the current scope
		currentScope.define(var);
		
		System.out.println("Define var: " + var.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	}

	public GlobalScope getGlobalScope(){
		return globals;
	}
	
	public ParseTreeProperty<Scope> getScopes(){
		return scopes;
	}
	
}
