package com.advicetec.language.transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
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
import com.advicetec.language.ast.AggregateSymbol;
import com.advicetec.language.ast.TransformationSymbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.language.ast.VariableSymbol;
import com.advicetec.language.transformation.SyntaxChecking;
import org.antlr.v4.runtime.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class used to do the first phase of the transformation interpreter. In this phase the interpreter
 * loops through the program to define the symbols used. 
 *  
 * It also verifies that the program code satisfies the language grammar.
 *  
 * @author Andres Marentes
 * 
 * JP 001 2017-11-01 Create Aggregate Symbol
 *
 */public class DefPhase extends TransformationGrammarBaseListener 
{

	static Logger logger = LogManager.getLogger(DefPhase.class.getName());
	
	/**
	 * Parser for the behavior language 
	 */
	private TransformationGrammarParser parser = null;
	
	/**
	 * Other scopes defined during the parse execution 
	 */
	private ParseTreeProperty<Scope> scopes; 
	
	/**
	 * Globals defined during the behavior language parsing 
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
	 * Constructor for the class, it takes as parameter the language grammar parser.
	 * 
	 * @param parser language grammar parser
	 */
	DefPhase(TransformationGrammarParser parser)
	{
		this.scopes = new ParseTreeProperty<Scope>();
		this.parser = parser;
		this.compilationErrors = new ArrayList<SyntaxError>();
	}

	/**
	 * Actions to perform when entering in the program code, in this case 
	 * we set the initial scope which is the global scope.
	 * 
	 *  @param program context 
	 */
	public void enterProgram(TransformationGrammarParser.ProgramContext ctx)
	{
		logger.debug("Enter program");
		globals = new GlobalScope();
		currentScope = globals;
	}

	/**
	 * Method to captures an error during the definition phase
	 * 
	 * @param t   	Token generating the error
	 * @param ctx	Context where the error happens
	 * @param msg	message error
	 */
	public void error(Token t, ParserRuleContext ctx, String msg) 
    {
    	String error = new String("line" + t.getLine() + "." + t.getCharPositionInLine() + msg + "\n");
    	SyntaxError e= new SyntaxError(error, t, this.parser, this.parser.getInputStream(), ctx); 
    	this.compilationErrors.add(e);
    }
	
	/**
	 * Actions to perform when entering in the main program, in this case 
	 * we create the transformation symbol and define it on the current scope.
	 * 
	 * @param context for the main program.
	 */
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
		
	/**
	 * Actions to perform at the end of import statement
	 * 
	 * used to create an import symbol in the language
	 * 
	 * Creates the import symbol and define it on the current scope. 
	 */
	public void exitImport_name(TransformationGrammarParser.Import_nameContext ctx) 
	{ 
		
		logger.debug("exit import name");
		
		int dottedNameCount = ctx.dotted_names().getChildCount();
		
		List<String> dottedNames = new ArrayList<String>();
		String nickname = null;
		
		for (int i=0; i < dottedNameCount; i++) {
			TransformationGrammarParser.Dotted_nameContext name = ctx.dotted_names().dotted_name(i);
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
		
		for (int i=0; i < dottedNames.size() ; i++ )
		{
			String idStr = dottedNames.get(i);
			symbol.addId(idStr);
		}
		
		currentScope.define(symbol);
		
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
	 * Actions to perform when parsing a timer symbol
	 * 
	 * In this case the parser has to:
	 * 		define the timer symbol verifying its correct definition
	 * 		include the timer symbol in the current scope.
	 */
	public void enterTimer(TransformationGrammarParser.TimerContext ctx) 
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
	 * Actions to perform when parsing a aggregate symbol
	 * 
	 * In this case the parser has to:
	 * 		define the aggregate symbol verifying its correct definition
	 * 		include the aggregate symbol in the current scope.
	 */
	public void enterSched_aggregate(TransformationGrammarParser.Sched_aggregateContext ctx) 
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
				String aggregateMethod = ctx.pack.getText();

				AggregateSymbol tSymbol = new AggregateSymbol(aggregateMethod,unitTimer,tunit, true);

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
	public void enterRepeat(TransformationGrammarParser.RepeatContext ctx) 
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
	public void enterDisplay(@NotNull TransformationGrammarParser.DisplayContext ctx) 
	{ 
		
		logger.debug("enterDisplay");
		
		String name = ctx.deviceId.getText(); 
		Symbol s = currentScope.resolve(name);
		
		if (s == null){
			
			DisplaySymbol displaySymbol = new DisplaySymbol(name);
			
			// Define the symbol in the current scope
			globals.define(displaySymbol);
			
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
	
	/**
	 * Actions to perform when finishing parsing a block of code.
	 *  
	 * In this case the parser has to:
	 *		return the current scope context to the parent scope.
	 * 
	 */
	public void exitBlock(TransformationGrammarParser.BlockContext ctx) 
	{ 
		logger.debug("exitBlock" + currentScope);

		// pop scope
		currentScope = currentScope.getEnclosingScope();
	}

	/** 
	 * Actions to perform at the end of the program parameter parser
	 * 
	 * @param get the program parameter context
	 * 
	 * Defines a variables in the symbol table.
	 */
	public void exitProgramparameter(TransformationGrammarParser.ProgramparameterContext ctx) 
	{ 
		logger.debug("parameter:" + ctx.ID().getText());
		defineVar(ctx.type(), ctx.ID().getSymbol());
	}

	/**
	 * Actions to perform when finishing the parse of a variable
	 *
	 * In this case the parser has to:
	 * 		create a variable with the name and type of the parameter
     * 		store the symbol in the current scope
	 */
	public void exitVar_dec(TransformationGrammarParser.Var_decContext ctx) 
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
	public void exitUnit_dec(TransformationGrammarParser.Unit_decContext ctx) 
	{ 
		defineUnit( ctx.getParent(), ctx.id1, ctx.ID().getText(),ctx.STRING().getText());
	}

	
	/**
	 * Actions to perform when assigning the measured entity state.
	 *
	 * In this case the parser has to:
	 * 		define the state symbol 
	 * 		store the symbol in the current scope
	 */
	public void exitState_assign(TransformationGrammarParser.State_assignContext ctx) 
	{ 
		logger.debug("entering entering state assign");
		defineState();
	}
	
	/**
	 * Actions to perform when finishing the parse of an attribute declaration
	 *
	 * In this case the parser has to:
	 * 		create a attribute with the name and type of the parameter
     * 		store the symbol in the global scope
	 */
	public void exitAtrib_dec(TransformationGrammarParser.Atrib_decContext ctx) 
	{ 
		
		
		if (ctx.TREND() ==null){
			defineAttribute(ctx.type(), ctx.id1, ctx.id2, false);
		}
		else{
			defineAttribute(ctx.type(), ctx.id1, ctx.id2, true);
		}
	}

	/**
	 * Defines an attribute in the symbol table.
	 * 
	 * @param typeCtx		context attribute type 	
	 * @param nameToken		Name of the attribute 
	 * @param unit			Unit of measure to assign to the attribute
	 * @param trend			if the trend attribute is trend or not
	 */
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

	/**
	 * Defines a unit of measure in the symbol table.
	 * 
	 * @param ctx			unit of measure context
	 * @param nameToken		Name of the unit of measure
	 * @param unitId		unit of measure		
	 * @param descr			description
	 */
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

	/**
	 * Defines the state in the symbol table.
	 */
	public void defineState()
	{
		logger.debug("Define state:");
		
		// Define the symbol in the global scope
		if (globals.resolve("state") == null){
			StateSymbol state = new StateSymbol();
			globals.define(state);
		}
	}
	
	/**
	 * Defines a variable in the symbol table.
	 * 
	 * @param typeCtx		context variable
	 * @param nameToken		name of the variable
	 */
	public void defineVar(TransformationGrammarParser.TypeContext typeCtx, Token nameToken)
	{

		int typeTokenType = typeCtx.start.getType();

		Symbol.Type type = SyntaxChecking.getType(typeTokenType);
		
		VariableSymbol var = new VariableSymbol(nameToken.getText(), type);

		// Define the symbol in the current scope
		currentScope.define(var);

		logger.debug("Define var: " + var.getName() + " scopeName:" + currentScope.getScopeName() + "tokenType:" + typeTokenType + " type:" + type.name());
	}

	/**
	 * Gets a reference to the global scope 
	 * 
	 * @return Global scope
	 */
	public GlobalScope getGlobalScope()
	{
		return globals;
	}
	
	/**
	 * gets a reference to all scopes apart from the global
	 * 
	 * @return scopes
	 */
	public ParseTreeProperty<Scope> getScopes()
	{
		return scopes;
	}
}
