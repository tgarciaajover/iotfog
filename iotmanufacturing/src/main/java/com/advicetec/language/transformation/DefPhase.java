package com.advicetec.language.transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.TransformationGrammarBaseListener;
import com.advicetec.language.TransformationGrammarParser;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.DisplaySymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.ImportSymbol;
import com.advicetec.language.ast.LocalScope;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.StateSymbol;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.ast.TransformationSymbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.language.ast.VariableSymbol;
import com.advicetec.language.behavior.BehaviorDefPhase;
import com.advicetec.language.transformation.SyntaxChecking;

import org.antlr.v4.runtime.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefPhase extends TransformationGrammarBaseListener 
{

	static Logger logger = LogManager.getLogger(DefPhase.class.getName());
	
	private TransformationGrammarParser parser = null;
	private ParseTreeProperty<Scope> scopes; 
	private GlobalScope globals;
	// Define symbols in this scope
	private Scope currentScope;
	private ArrayList<SyntaxError> compilationErrors;

	DefPhase(TransformationGrammarParser parser)
	{
		this.scopes = new ParseTreeProperty<Scope>();
		this.parser = parser;
		this.compilationErrors = new ArrayList<SyntaxError>();
	}

	public void enterProgram(TransformationGrammarParser.ProgramContext ctx)
	{
		logger.debug("Enter program");
		globals = new GlobalScope();
		currentScope = globals;
	}

	public void error(Token t, ParserRuleContext ctx, String msg) 
    {
    	String error = new String("line" + t.getLine() + "." + t.getCharPositionInLine() + msg + "\n");
    	SyntaxError e= new SyntaxError(error, t, this.parser, this.parser.getInputStream(), ctx); 
    	this.compilationErrors.add(e);
    }
	
	public void enterMain(TransformationGrammarParser.MainContext ctx) 
	{
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
		
	public void enterDotted_name(TransformationGrammarParser.Dotted_nameContext ctx)
	{ 
		logger.debug("enterDotted_name");
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
	
	public void saveScope(ParserRuleContext ctx, Scope s)
	{
		scopes.put(ctx, s);
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
		
		int tunit = Integer.valueOf(ctx.time.getText());
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
		
		int tunit = Integer.valueOf(ctx.time.getText());
		String behaviorName = ctx.pack.getText();
		
		TimerSymbol tSymbol = new TimerSymbol(behaviorName,unitTimer,tunit, true);

		// Define the symbol in the current scope
		currentScope.define(tSymbol);
		
	}
	
	public void enterDisplay(@NotNull TransformationGrammarParser.DisplayContext ctx) 
	{ 
		
		logger.debug("enterDisplay");
		
		String name = ctx.deviceId.getText(); 
		Symbol s = currentScope.resolve(name);
		
		if (s == null){
			DisplaySymbol displaySymbol = new DisplaySymbol(name);

			// Define the symbol in the current scope
			currentScope.define(displaySymbol);
			
		} 
			
	}
	
	public void enterBlock(TransformationGrammarParser.BlockContext ctx) 
	{ 

		logger.debug("Entering block" + currentScope);
		
		// push new scope by making new one that points to enclosing scope
		LocalScope local = new LocalScope(currentScope);

		// Push: set function's parent to current
		saveScope(ctx, local); 

		// Current Scope is now function scope
		currentScope = local;
	}
	
	public void exitBlock(TransformationGrammarParser.BlockContext ctx) 
	{ 
		logger.debug("exitBlock" + currentScope);

		// pop scope
		currentScope = currentScope.getEnclosingScope();
	}

	public void exitProgramparameter(TransformationGrammarParser.ProgramparameterContext ctx) 
	{ 
		logger.debug("parameter:" + ctx.ID().getText());
		defineVar(ctx.type(), ctx.ID().getSymbol());
	}

	public void exitVar_dec(TransformationGrammarParser.Var_decContext ctx) 
	{ 
		defineVar(ctx.type(), ctx.ID().getSymbol());
	}

	public void exitUnit_dec(TransformationGrammarParser.Unit_decContext ctx) 
	{ 
		defineUnit( ctx.getParent(), ctx.id1, ctx.ID().getText(),ctx.STRING().getText());
	}

	
	public void exitState_assign(TransformationGrammarParser.State_assignContext ctx) 
	{ 
		logger.debug("entering entering state assign");
		defineState();
	}
	
	public void exitAtrib_dec(TransformationGrammarParser.Atrib_decContext ctx) 
	{ 
		
		
		if (ctx.TREND() ==null){
			defineAttribute(ctx.type(), ctx.id1, ctx.id2, false);
		}
		else{
			defineAttribute(ctx.type(), ctx.id1, ctx.id2, true);
		}
	}

	public void defineAttribute(TransformationGrammarParser.TypeContext typeCtx, Token nameToken, Token unit, boolean trend)
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
		
		logger.debug("Define attribute: " + atr.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
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

		logger.debug("Define unit: " + unt.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	}

	public void defineState()
	{
		logger.debug("Define state:");
		
		// Define the symbol in the global scope
		if (globals.resolve("state") == null){
			StateSymbol state = new StateSymbol();
			globals.define(state);
		}
	}
	
	public void defineVar(TransformationGrammarParser.TypeContext typeCtx, Token nameToken)
	{

		int typeTokenType = typeCtx.start.getType();

		Symbol.Type type = SyntaxChecking.getType(typeTokenType);
		
		VariableSymbol var = new VariableSymbol(nameToken.getText(), type);

		// Define the symbol in the current scope
		currentScope.define(var);

		logger.debug("Define var: " + var.getName() + " scopeName:" + currentScope.getScopeName() + "tokenType:" + typeTokenType + " type:" + type.name());
	}

	public GlobalScope getGlobalScope()
	{
		return globals;
	}
	
	public ParseTreeProperty<Scope> getScopes()
	{
		return scopes;
	}
}
