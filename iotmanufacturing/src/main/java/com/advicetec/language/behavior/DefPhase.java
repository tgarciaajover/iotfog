package com.advicetec.language.behavior;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.advicetec.language.BehaviorGrammarBaseListener;
import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.TransformationGrammarParser;
import com.advicetec.language.ast.ArrayAttributeSymbol;
import com.advicetec.language.ast.ArraySymbol;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.ImportSymbol;
import com.advicetec.language.ast.LocalScope;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.language.ast.VariableSymbol;
import com.advicetec.language.ast.BehaviorSymbol;

import org.antlr.v4.runtime.Token;
import org.codehaus.plexus.compiler.CompilerException;

public class DefPhase extends BehaviorGrammarBaseListener 
{

	private BehaviorGrammarParser parser = null;
	private ParseTreeProperty<Scope> scopes;
	private GlobalScope globals;
	
	// Define symbols in this scope
	private Scope currentScope;
	
	// Define an array for storing syntax errors.
	private ArrayList<SyntaxError> compilationErrors;

	DefPhase(BehaviorGrammarParser parser)
	{
		this.scopes = new ParseTreeProperty<Scope>();
		this.parser = parser;
		this.compilationErrors = new ArrayList<SyntaxError>();
	}	
	
	public void enterProgram(BehaviorGrammarParser.ProgramContext ctx)
	{
		// System.out.println("Enter program");
		globals = new GlobalScope();
		currentScope = globals;
	}		

	public void error(Token t, ParserRuleContext ctx, String msg) 
    {
    	String error = new String("line" + t.getLine() + "." + t.getCharPositionInLine() + msg + "\n");
    	SyntaxError e= new SyntaxError(error, t, this.parser, this.parser.getInputStream(), ctx); 
    	this.compilationErrors.add(e);
    }
	
	public void enterMain(BehaviorGrammarParser.MainContext ctx)
	{
		
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

	public void enterDotted_name(BehaviorGrammarParser.Dotted_nameContext ctx)
	{ 
		// System.out.println("enterDotted_name");
		List<TerminalNode> ids = ctx.ID();
		String id;
				
		if (ctx.nickname == null){
			id = ctx.getText();
		} else {
			id = ctx.nickname.getText();
		}
				
		ImportSymbol symbol = new ImportSymbol(id); 
		
		for (int i=0; i < ids.size() ; i++ )
		{
			String idStr = ids.get(i).getText();
			symbol.addId(idStr);
		}
		
		currentScope.define(symbol);
	}

	
	public void exitProgramparameter(BehaviorGrammarParser.ProgramparameterContext ctx) 
	{ 
		// The parameter does not have a unit of measure defined in the language.
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
		
		// System.out.println("Function Name: " + name + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
		
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
		// System.out.println(currentScope);
		
		// pop scope
		currentScope = currentScope.getEnclosingScope();
		
		// System.out.println("exitFunction_dec" + currentScope);
	}

	public void enterTimer(TransformationGrammarParser.TimerContext ctx) 
	{ 
		// by default seconds
		TimeUnit unitTimer = TimeUnit.SECONDS;
		if (ctx.TIMEUNIT().getText().compareTo("SECOND") == 0){
			unitTimer = TimeUnit.SECONDS;
		}
		if (ctx.TIMEUNIT().getText().compareTo("MINUTE") == 0){
			unitTimer = TimeUnit.MINUTES;
		}
		if (ctx.TIMEUNIT().getText().compareTo("HOUR") == 0){
			unitTimer = TimeUnit.HOURS;
		}
		
		int tunit = Integer.valueOf(ctx.INT().getText());
		String behaviorName = ctx.pack.getText();
		
		TimerSymbol tSymbol = new TimerSymbol(behaviorName,unitTimer,tunit, false);

		// Define the symbol in the current scope
		currentScope.define(tSymbol);
		
	}

	public void enterRepeat(TransformationGrammarParser.RepeatContext ctx) 
	{ 
		// by default seconds
		TimeUnit unitTimer = TimeUnit.SECONDS;
		if (ctx.TIMEUNIT().getText().compareTo("SECOND") == 0){
			unitTimer = TimeUnit.SECONDS;
		}
		if (ctx.TIMEUNIT().getText().compareTo("MINUTE") == 0){
			unitTimer = TimeUnit.MINUTES;
		}
		if (ctx.TIMEUNIT().getText().compareTo("HOUR") == 0){
			unitTimer = TimeUnit.HOURS;
		}
		
		int tunit = Integer.valueOf(ctx.INT().getText());
		String behaviorName = ctx.pack.getText();
		
		TimerSymbol tSymbol = new TimerSymbol(behaviorName,unitTimer,tunit, true);

		// Define the symbol in the current scope
		currentScope.define(tSymbol);
		
	}
	
	public void enterBlock(BehaviorGrammarParser.BlockContext ctx) 
	{ 
		
		// push new scope by making new one that points to enclosing scope
		LocalScope local = new LocalScope(currentScope);
				
		// Push: set function's parent to current
		saveScope(ctx, local); 
		
		// Current Scope is now function scope
		currentScope = local;
		
	}
	
	public void exitBlock(BehaviorGrammarParser.BlockContext ctx) 
	{ 
		// System.out.println("exitBlock" + currentScope);
		
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
		defineUnit( ctx.getParent(), ctx.id1, ctx.ID().getText(),ctx.STRING().getText());	
	}

	public void exitAtrib_dec(BehaviorGrammarParser.Atrib_decContext ctx) 
	{ 
		if (ctx.TREND() == null){
			defineAttribute(ctx.type(), ctx.id1, ctx.id2, false);
		}
		else{
			defineAttribute(ctx.type(), ctx.id1, ctx.id2, true);
		}
	}
	
	public void exitVect_attrib_dec(BehaviorGrammarParser.Vect_attrib_decContext ctx) 
	{ 

		// System.out.println("exit in enterVect attrib dec");
		
		if ((ctx.numElements.getStopIndex() >= 0) || (ctx.numElements.getStartIndex() >= 0) )
		{
			int numElem = Integer.valueOf(ctx.numElements.getText());
			defineAttributeArray(ctx.type(), ctx.id1, numElem, ctx.id2);
		}
	}
	
	public void exitVect_var_dec(BehaviorGrammarParser.Vect_var_decContext ctx) 
	{ 
		// System.out.println("enter in exitVect var dec- stop:" + ctx.numElements.getStopIndex() + "Start: " + ctx.numElements.getStartIndex());
		
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
		
		if (globals.resolve(nameToken.getText()) != null){
			// Define the symbol in the global scope
			this.error(nameToken, typeCtx, "Attribute has been defined before: " + nameToken.getText());
		} else {
			globals.define(atr);
		}

		// System.out.println("Define attribute array: " + atr.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	
	}

	public void defineVarArray(BehaviorGrammarParser.TypeContext typeCtx, Token nameToken, int numElem)
	{
		int typeTokenType = typeCtx.start.getType();
		Symbol.Type type = SyntaxChecking.getType(typeTokenType);
		
		ArraySymbol atr = new ArraySymbol(nameToken.getText(), type, numElem);

		// Define the symbol in the current scope
		currentScope.define(atr);

		// System.out.println("Define var array: " + atr.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	
	}

	public void defineAttribute(BehaviorGrammarParser.TypeContext typeCtx, Token nameToken, Token unit, boolean trend)
	{
		int typeTokenType = typeCtx.start.getType();
		
		Symbol.Type type = SyntaxChecking.getType(typeTokenType);
		
		AttributeSymbol atr = new AttributeSymbol(nameToken.getText(), type, trend);
		
		if (unit != null)
			atr.setUnitOfMeasure(unit.getText());
		
		if (globals.resolve(nameToken.getText()) != null){
			// Define the symbol in the global scope
			this.error(nameToken, typeCtx, "Attribute has been defined before: " + nameToken.getText());
		} else {
			globals.define(atr);
		}
		
		// System.out.println("Define attribute: " + atr.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	}
	
	public void defineUnit(ParserRuleContext ctx, Token nameToken, String unitId, String descr )
	{
		
		UnitMeasureSymbol unt = new UnitMeasureSymbol(unitId, descr);

		// Define the symbol in the current scope
		if (globals.resolve(unitId) != null){
			this.error(nameToken, ctx, "Unit of Measure has been defined before: " + unitId);
		} else {
			globals.define(unt);
		}

		// System.out.println("Define unit: " + unt.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	}
	
	public void defineVar(BehaviorGrammarParser.TypeContext typeCtx, Token nameToken)
	{
		int typeTokenType = typeCtx.start.getType();
		
		Symbol.Type type = SyntaxChecking.getType(typeTokenType);
		
		VariableSymbol var = new VariableSymbol(nameToken.getText(), type);
		
		// Define the symbol in the current scope
		currentScope.define(var);
		
		// System.out.println("Define var: " + var.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	}

	public GlobalScope getGlobalScope(){
		return globals;
	}
	
	public ParseTreeProperty<Scope> getScopes(){
		return scopes;
	}
	
}
