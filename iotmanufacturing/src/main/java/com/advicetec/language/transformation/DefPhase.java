package com.advicetec.language.transformation;


import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import com.advicetec.language.TransformationGrammarBaseListener;
import com.advicetec.language.TransformationGrammarParser;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.LocalScope;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.TransformationSymbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.language.ast.VariableSymbol;
import com.advicetec.language.behavior.SyntaxChecking;

import org.antlr.v4.runtime.Token;

public class DefPhase extends TransformationGrammarBaseListener 
{

	private ParseTreeProperty<Scope> scopes = new ParseTreeProperty<Scope>();
	private GlobalScope globals;
	// Define symbols in this scope
	private Scope currentScope;


	public void enterProgram(TransformationGrammarParser.ProgramContext ctx)
	{
		globals = new GlobalScope();
		currentScope = globals;

		String name = ctx.PROGRAM().getText();

		// Transformation program does not have a return value.
		Symbol.Type type = SyntaxChecking.getType(TransformationGrammarParser.K_VOID);

		// push new scope by making new one that points to enclosing scope
		TransformationSymbol program = new TransformationSymbol(name, type, ctx.block(), currentScope);

		// Defines the function in the current scope.
		currentScope.define(program);
	
		
		// Push: set function's parent to current
		saveScope(ctx, program); 
		
		// Current Scope is now function scope
		currentScope = program;
	}

	public void exitProgram(TransformationGrammarParser.ProgramContext ctx)
	{
		System.out.println("Exit program: " );
		System.out.println(globals);
	}

	public void saveScope(ParserRuleContext ctx, Scope s)
	{
		scopes.put(ctx, s);
	}

	public void enterBlock(TransformationGrammarParser.BlockContext ctx) 
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

	public void exitBlock(TransformationGrammarParser.BlockContext ctx) 
	{ 
		System.out.println("exitBlock" + currentScope);

		// pop scope
		currentScope = currentScope.getEnclosingScope();
	}

	public void exitFormalparameter(TransformationGrammarParser.ProgramparameterContext ctx) 
	{ 
		defineVar(ctx.type(), ctx.ID().getSymbol());
	}

	public void exitVar_dec(TransformationGrammarParser.Var_decContext ctx) 
	{ 
		defineVar(ctx.type(), ctx.ID().getSymbol());
	}

	public void exitUnit_dec(TransformationGrammarParser.Unit_decContext ctx) 
	{ 
		defineUnit(ctx.ID().getText(), ctx.STRING().getText());
	}

	public void exitAtrib_dec(TransformationGrammarParser.Atrib_decContext ctx) 
	{ 
		defineAttribute(ctx.type(), ctx.id1, ctx.id2);
	}

	public void defineAttribute(TransformationGrammarParser.TypeContext typeCtx, Token nameToken, Token unit)
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



	public void defineVar(TransformationGrammarParser.TypeContext typeCtx, Token nameToken)
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
