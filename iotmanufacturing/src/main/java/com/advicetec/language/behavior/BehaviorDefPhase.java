package com.advicetec.language.behavior;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.advicetec.language.BehaviorGrammarBaseListener;
import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.TransformationGrammarParser;
import com.advicetec.language.ast.ArrayAttributeSymbol;
import com.advicetec.language.ast.ArraySymbol;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.DisplaySymbol;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.ImportSymbol;
import com.advicetec.language.ast.LocalScope;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.StateSymbol;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.language.ast.VariableSymbol;
import com.advicetec.language.ast.BehaviorSymbol;

import org.antlr.v4.runtime.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.compiler.CompilerException;

/**
 * Class used to do the first phase of the behavior interpreter.In this phase the interpreter
 * loop through the program to define the symbols used, check if they are complete and have 
 * the correct types. 
 *  
 * It also verifies that the program code satisfies the language grammar.
 *  
 * @author Andres Marentes
 *
 */
public class BehaviorDefPhase extends BehaviorGrammarBaseListener 
{

	static Logger logger = LogManager.getLogger(BehaviorDefPhase.class.getName());
	
	/**
	 * Parser for the behavior langauge
	 */
	private BehaviorGrammarParser parser = null;
    /**
	 * Scopes defined during the parse execution 
	 */
	private ParseTreeProperty<Scope> scopes;
	
	/**
	 * Globals defined in the behavior language parsing
	 */
	private GlobalScope globals;
	
	/**
	 * Define symbols in the current scope being executed.
	 */
	private Scope currentScope;
	
	/**
	 * Define an array for storing syntax errors.
	 */
	private ArrayList<SyntaxError> compilationErrors;

	/**
	 * Constructor for the class, it takes as parameter the behavior grammar parser.
	 * 
	 * @param parser behavior grammar parser
	 */
	BehaviorDefPhase(BehaviorGrammarParser parser)
	{
		this.scopes = new ParseTreeProperty<Scope>();
		this.parser = parser;
		this.compilationErrors = new ArrayList<SyntaxError>();
		globals = new GlobalScope();
	}	
	
	/**
	 * Actions to perform when entering in the program code, in this case 
	 * we set the initial scope which is the global scope.
	 * 
	 *  @param program context 
	 */
	public void enterProgram(BehaviorGrammarParser.ProgramContext ctx)
	{
		logger.debug("Enter program");
		currentScope = globals;
	}		

	/**
	 * Method to captures an error during the definition phase
	 * 
	 * @param t   	Token generating the error
	 * @param ctx	Context where the error happens
	 * @param msg	messsage error
	 */
	public void error(Token t, ParserRuleContext ctx, String msg) 
    {
    	String error = new String("line" + t.getLine() + "." + t.getCharPositionInLine() + msg + "\n");
    	SyntaxError e= new SyntaxError(error, t, this.parser, this.parser.getInputStream(), ctx); 
    	this.compilationErrors.add(e);
    }
	
	/**
	 * Actions to perform when entering in the main program, in this case 
	 * we create the behavior symbol and define it on the current scope.
	 * 
	 * @param context for the main program.
	 */
	public void enterMain(BehaviorGrammarParser.MainContext ctx)
	{
		
		String name = ctx.PROGRAM().getText();

		// Transformation program does not have a return value.
		Symbol.Type type = BehaviorSyntaxChecking.getType(BehaviorGrammarParser.K_VOID);

		// push new scope by making new one that points to enclosing scope
		BehaviorSymbol program = new BehaviorSymbol(name, type, ctx.block(), currentScope);

		// Defines the function in the current scope.
		currentScope.define(program);
	
		// Push: set function's parent to current
		saveScope(ctx, program); 
		
		// Current Scope is now function scope
		currentScope = program;
	}
	
	/**
	 * Actions to perform during the loop through a dotted name 
	 * used to create an import in the language
	 * 
	 * Creates the import symbol and define it on the current scope. 
	 */
	public void exitImport_name(BehaviorGrammarParser.Import_nameContext ctx) 
	{ 
		
		logger.debug("exit import name");
		
		int dottedNameCount = ctx.dotted_names().getChildCount();
		
		List<String> dottedNames = new ArrayList<String>();
		String nickname = null;
		
		for (int i=0; i < dottedNameCount; i++) {
			BehaviorGrammarParser.Dotted_nameContext name = ctx.dotted_names().dotted_name(i);
			if (name != null) {
				dottedNames.add(name.getText());
				if (name.AS() != null) {
					logger.info(name.AS().getText());
					nickname = name.AS().getText(); 
				}
			}
		}		
		
		ImportSymbol symbol = null;
		if (nickname != null) {
			symbol =  new ImportSymbol(nickname);
		}
		else {
			symbol =  new ImportSymbol(String.join(".", dottedNames));
		}
		
		currentScope.define(symbol);
		
	}


	
	public void exitProgramparameter(BehaviorGrammarParser.ProgramparameterContext ctx) 
	{ 
		// The parameter does not have a unit of measure defined in the language.
		defineVar(ctx.type(), ctx.ID().getSymbol());
	}

	/**
	 * Actions to perform when starting the parser of a function declaration
	 * 
	 * @param function declaration context.
	 */
	public void enterFunction_dec(BehaviorGrammarParser.Function_decContext ctx)
	{
		String name = ctx.ID().getText();
		int typeTokenType = ctx.type().start.getType();
		
		Symbol.Type type = BehaviorSyntaxChecking.getType(typeTokenType);
		
		// push new scope by making new one that points to enclosing scope
		FunctionSymbol function = new FunctionSymbol(name, type, ctx.block(), currentScope);
		
		// Defines the function in the current scope.
		currentScope.define(function);
		
		logger.debug("Function Name: " + name + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
		
		// Push: set function's parent to current
		saveScope(ctx, function); 
		
		// Current Scope is now function scope
		currentScope = function;
	}
	
	/**
	 * Method to put the current scope in the heap
	 * @param ctx 	parser rule context
	 * @param s		Scope to save.
	 */
	public void saveScope(ParserRuleContext ctx, Scope s)
	{
		scopes.put(ctx, s);
	}
	
	/**
	 * Actions to perform when ending the parser of a function. This happens when the ends the function processing
	 *
	 * In this case we put the current scope as the parent scope and removethe scope from the heap.  
	 */
	public void exitFunction_dec(BehaviorGrammarParser.Function_decContext ctx) 
	{ 
		logger.debug(currentScope);
		
		// pop scope
		currentScope = currentScope.getEnclosingScope();
		
		logger.debug("exitFunction_dec" + currentScope);
	}

	/**
	 * Actions to perform when parsing a timer symbol
	 * 
	 * In this case the parser has to:
	 * 		define the timer symbol verifying its correct definition
	 * 		include the timer symbol in the current scope.
	 */
	public void enterTimer(BehaviorGrammarParser.TimerContext ctx) 
	{ 
		// by default seconds
		TimeUnit unitTimer = TimeUnit.SECONDS;

		if (ctx.TIMEUNIT() == null){
			Token nameToken = ctx.start;			
			this.error(nameToken, ctx, "No time unit was given: " + nameToken.getText());
		} else {

			if (ctx.TIMEUNIT().getText().compareTo("SECOND") == 0){
				unitTimer = TimeUnit.SECONDS;
			}
			if (ctx.TIMEUNIT().getText().compareTo("MINUTE") == 0){
				unitTimer = TimeUnit.MINUTES;
			}
			if (ctx.TIMEUNIT().getText().compareTo("HOUR") == 0){
				unitTimer = TimeUnit.HOURS;
			}
			try
			{
				int tunit = Integer.valueOf(ctx.time.getText());
				String behaviorName = ctx.pack.getText();

				TimerSymbol tSymbol = new TimerSymbol(behaviorName,unitTimer,tunit, false);

				// Define the symbol in the current scope
				currentScope.define(tSymbol);
			} catch (NumberFormatException e){
				Token nameToken = ctx.start;	
				this.error(nameToken, ctx, "The second parameter must be an integer" + nameToken.getText());
			}
			
		}
	}

	/**
	 * Actions to perform when parsing a repeat symbol (the same as a timer, but it should be re-scheduled)
	 * 
	 * In this case the parser has to:
	 * 		define the timer symbol verifying its correct definition
	 * 		include the timer symbol in the current scope.
	 */
	public void enterRepeat(BehaviorGrammarParser.RepeatContext ctx) 
	{ 
		// by default seconds
		TimeUnit unitTimer = TimeUnit.SECONDS;
		if (ctx.TIMEUNIT() == null){
			Token nameToken = ctx.start;			
			this.error(nameToken, ctx, "No time unit was given: " + nameToken.getText());
		} else {

			if (ctx.TIMEUNIT().getText().compareTo("SECOND") == 0){
				unitTimer = TimeUnit.SECONDS;
			}
			if (ctx.TIMEUNIT().getText().compareTo("MINUTE") == 0){
				unitTimer = TimeUnit.MINUTES;
			}
			if (ctx.TIMEUNIT().getText().compareTo("HOUR") == 0){
				unitTimer = TimeUnit.HOURS;
			}

			try{
				int tunit = Integer.valueOf(ctx.time.getText());
				String behaviorName = ctx.pack.getText();

				TimerSymbol tSymbol = new TimerSymbol(behaviorName,unitTimer,tunit, true);

				// Define the symbol in the current scope
				currentScope.define(tSymbol);
			} catch (NumberFormatException e){
				Token nameToken = ctx.start;	
				this.error(nameToken, ctx, "The second parameter must be an integer" + nameToken.getText());
			}
			
		}
	}
	
	/**
	 * Actions to perform when parsing a display symbol
	 * 
	 * In this case the parser has to:
	 * 		Create the display symbol
	 * 		include the symbol in the current scope.
	 */
	public void enterDisplay(@NotNull BehaviorGrammarParser.DisplayContext ctx) 
	{ 
		String name = ctx.deviceId.getText(); 
		Symbol s = currentScope.resolve(name);
		
		if (s == null){
			DisplaySymbol displaySymbol = new DisplaySymbol(name);

			// Define the symbol in the current scope
			currentScope.define(displaySymbol);
			
		} 
			
	}	
	
	/**
	 * Actions to perform when parsing a block of code. A block is a set of instructions 
	 * starting with the symbol "{" and ending with the symbol "}".
	 *
	 * In this case the parser has to:
	 * 		Create a new scope for the block context
	 * 		store the previous context as parent of the new scope
	 * 		Change the current scope context to the new scope 	
	 * 
	 * @param Block context
	 */
	public void enterBlock(BehaviorGrammarParser.BlockContext ctx) 
	{ 
		
		// push new scope by making new one that points to enclosing scope
		LocalScope local = new LocalScope(currentScope);
				
		// Push: set function's parent to current
		saveScope(ctx, local); 
		
		// Current Scope is now function scope
		currentScope = local;
		
	}
	
	/**
	 * Actions to perform when finishing parsing a block of code.
	 *  
	 * In this case the parser has to:
	 *		return the current scope context to the parent scope.
	 * 
	 */
	public void exitBlock(BehaviorGrammarParser.BlockContext ctx) 
	{ 
		logger.debug("exitBlock" + currentScope);
		
		// pop scope
		currentScope = currentScope.getEnclosingScope();
		
	}
	
	/**
	 * Actions to perform when finishing parsing a formal parameter. 
	 * 
	 * These are the parameters given to functions which are not the main program.
	 *
	 * In this case the parser has to:
	 * 		create a variable symbol with the name and type of the parameter
	 *      store the symbol in the current scope 
	 * 
	 */
	public void exitFormalparameter(BehaviorGrammarParser.FormalparameterContext ctx) 
	{ 
		defineVar(ctx.type(), ctx.ID().getSymbol());
	}
	
	/**
	 * Actions to perform when finishing the parse of a variable
	 *
	 * In this case the parser has to:
	 * 		create a variable with the name and type of the parameter
     * 		store the symbol in the current scope
	 */
	public void exitVar_dec(BehaviorGrammarParser.Var_decContext ctx) 
	{ 
		defineVar(ctx.type(), ctx.ID().getSymbol());
	}

	/**
	 * Actions to perform when finishing parsing of a unit of measure
	 * 
	 * In this case the parser has to:
	 * 		define the unit of measure symbol 
	 * 		store the symbol in the current scope
	 */
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
	
	/**
	 * 
	 */
	public void exitVect_attrib_dec(BehaviorGrammarParser.Vect_attrib_decContext ctx) 
	{ 

		logger.debug("exit in enterVect attrib dec");
		
		if ((ctx.numElements.getStopIndex() >= 0) || (ctx.numElements.getStartIndex() >= 0) )
		{
			int numElem = Integer.valueOf(ctx.numElements.getText());
			defineAttributeArray(ctx.type(), ctx.id1, numElem, ctx.id2);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.advicetec.language.BehaviorGrammarBaseListener#exitVect_var_dec(com.advicetec.language.BehaviorGrammarParser.Vect_var_decContext)
	 */
	public void exitVect_var_dec(BehaviorGrammarParser.Vect_var_decContext ctx) 
	{ 
		logger.debug("enter in exitVect var dec- stop:" + ctx.numElements.getStopIndex() + "Start: " + ctx.numElements.getStartIndex());
		
		if ((ctx.numElements.getStopIndex() >= 0) || (ctx.numElements.getStartIndex() >= 0) )
		{
			int numElem = Integer.valueOf(ctx.numElements.getText());				
			defineVarArray(ctx.type(), ctx.id1, numElem);
		}
	}

	/* (non-Javadoc)
	 * @see com.advicetec.language.BehaviorGrammarBaseListener#exitState_assign(com.advicetec.language.BehaviorGrammarParser.State_assignContext)
	 */
	public void exitState_assign(BehaviorGrammarParser.State_assignContext ctx) 
	{ 
		logger.debug("entering entering state assign");
		defineState();
	}

	/**
	 * @param typeCtx
	 * @param nameToken
	 * @param numElem
	 * @param unit
	 */
	public void defineAttributeArray(BehaviorGrammarParser.TypeContext typeCtx, Token nameToken, int numElem, Token unit)
	{
		int typeTokenType = typeCtx.start.getType();
		Symbol.Type type = BehaviorSyntaxChecking.getType(typeTokenType);
		
		ArrayAttributeSymbol atr = new ArrayAttributeSymbol(nameToken.getText(), type, numElem);

		if (unit != null)
			atr.setUnitOfMeasure(unit.getText());
		
		if (globals.resolve(nameToken.getText()) != null){
			// Define the symbol in the global scope
			this.error(nameToken, typeCtx, "Attribute has been defined before: " + nameToken.getText());
		} else {
			globals.define(atr);
		}

		logger.debug("Define attribute array: " + atr.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	
	}

	/**
	 * @param typeCtx
	 * @param nameToken
	 * @param numElem
	 */
	public void defineVarArray(BehaviorGrammarParser.TypeContext typeCtx, Token nameToken, int numElem)
	{
		int typeTokenType = typeCtx.start.getType();
		Symbol.Type type = BehaviorSyntaxChecking.getType(typeTokenType);
		
		ArraySymbol atr = new ArraySymbol(nameToken.getText(), type, numElem);

		// Define the symbol in the current scope
		currentScope.define(atr);

		logger.debug("Define var array: " + atr.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	
	}

	/**
	 * @param typeCtx
	 * @param nameToken
	 * @param unit
	 * @param trend
	 */
	public void defineAttribute(BehaviorGrammarParser.TypeContext typeCtx, Token nameToken, Token unit, boolean trend)
	{
		int typeTokenType = typeCtx.start.getType();
		
		Symbol.Type type = BehaviorSyntaxChecking.getType(typeTokenType);
		
		AttributeSymbol atr = new AttributeSymbol(nameToken.getText(), type, trend);
		
		if (unit != null)
			atr.setUnitOfMeasure(unit.getText());
		
		if (globals.resolve(nameToken.getText()) != null){
			// Define the symbol in the global scope
			String error = "Attribute has been defined before: " + nameToken.getText();
			logger.debug(error);
			this.error(nameToken, typeCtx, error);
		} else {
			globals.define(atr);
		}
		
		logger.debug("Define attribute: " + atr.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	}
	
	/**
	 * @param ctx
	 * @param nameToken
	 * @param unitId
	 * @param descr
	 */
	public void defineUnit(ParserRuleContext ctx, Token nameToken, String unitId, String descr )
	{
		
		UnitMeasureSymbol unt = new UnitMeasureSymbol(unitId, descr);

		// Define the symbol in the current scope
		if (globals.resolve(unitId) != null){
			String error = "Unit of Measure has been defined before: " + unitId;
			logger.debug(error);
			this.error(nameToken, ctx, error);
		} else {
			globals.define(unt);
		}

		logger.debug("Define unit: " + unt.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	}
	
	/**
	 * @param typeCtx
	 * @param nameToken
	 */
	public void defineVar(BehaviorGrammarParser.TypeContext typeCtx, Token nameToken)
	{
		
		logger.debug("it is going to define var:" + nameToken.getText());
		
		if (currentScope.resolve(nameToken.getText())!= null){
			if (currentScope.resolve(nameToken.getText()) instanceof AttributeSymbol){
				String error = "The variable:" + nameToken.getText()+ " has been defined before as AttributeSymbol";
				logger.debug(error);
				this.error(nameToken, typeCtx, error);
				return;
			}
		}
		
		int typeTokenType = typeCtx.start.getType();		
		Symbol.Type type = BehaviorSyntaxChecking.getType(typeTokenType);
		VariableSymbol var = new VariableSymbol(nameToken.getText(), type);

		// Define the symbol in the current scope
		currentScope.define(var);
		
		logger.debug("Define var: " + var.getName() + " scopeName:" + currentScope.getScopeName() + " symbols:" + currentScope);
	}

	/**
	 * 
	 */
	public void defineState()
	{
		// Define the symbol in the global scope
		if (globals.resolve("state") == null){
			StateSymbol state = new StateSymbol();
			globals.define(state);
		}
	}
	
	/**
	 * @return
	 */
	public GlobalScope getGlobalScope(){
		return globals;
	}
	
	/**
	 * @return
	 */
	public ParseTreeProperty<Scope> getScopes(){
		return scopes;
	}
	
}
