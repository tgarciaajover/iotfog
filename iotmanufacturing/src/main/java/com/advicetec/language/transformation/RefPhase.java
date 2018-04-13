package com.advicetec.language.transformation;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.DisplayDevice;
import com.advicetec.configuration.DisplayDeviceContainer;
import com.advicetec.language.TransformationGrammarParser;
import com.advicetec.language.TransformationGrammarBaseListener;
import com.advicetec.language.ast.AggregateSymbol;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.ImportSymbol;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.measuredentitity.MeasuredEntity;
import com.advicetec.measuredentitity.MeasuredEntityFacade;

public class RefPhase extends TransformationGrammarBaseListener 
{

	static Logger logger = LogManager.getLogger(RefPhase.class.getName());
	
	/**
	 * Parser for the transformation grammar.
	 */
	TransformationGrammarParser parser = null;
	
	/**
	 * Global scope, which is filled by the parser
	 */
	GlobalScope globals;

	/**
	 * The definition for the rest of scopes
	 */	
	ParseTreeProperty<Scope> scopes;
	
	/**
	 * Reference to the current scope.
	 */
	Scope currentScope;

	/**
	 * List of errors compiled during syntax checking
	 */
	private ArrayList<SyntaxError> compilationErrors;

	/**
	 * Measure entity facade where the behavior is being checked.
	 */
	MeasuredEntityFacade entityFacade;
	
	
	/**
	 * Constructor for the class 
	 * 
	 * @param parser   		transformation grammar parser.
	 * @param globals		Global scope previously calculated in the definition phase
	 * @param scopes		Reference to the rest of scopes previously calculated in the definition phase
	 * @param entityFacade	Measure entity facade where the behavior is being checked.
	 */
	RefPhase(TransformationGrammarParser parser, GlobalScope globals , ParseTreeProperty<Scope> scopes, MeasuredEntityFacade entityFacade)
	{
		this.scopes = scopes;
		this.globals = globals;
		this.parser = parser; 
		compilationErrors = new ArrayList<SyntaxError>();
		this.entityFacade = entityFacade;
	}
    
	/**
	 * Register an error in the list of errors 
	 * 
	 * @param t		token where the error occurs
	 * @param ctx	context where the error occurs
	 * @param msg	message error
	 */
	public void error(Token t, ParserRuleContext ctx, String msg) 
    {
    	String error;
		if (t == null){
    		error = new String(msg + "\n");
    	}
    	else {
    		error = new String("line" + t.getLine() + "." + t.getCharPositionInLine() + msg + "\n");
    	}
    	SyntaxError e= new SyntaxError(error, t, this.parser, this.parser.getInputStream(), ctx); 
    	this.compilationErrors.add(e);
    }
	
	/**
	 * Gets the list of errors
	 *  
	 * @return	error list
	 */
	public List<SyntaxError> getErrors()
	{
		return this.compilationErrors;
	}
	
	/**
	 * Establishes the current scope as the global scope
	 */
	public void enterProgram(TransformationGrammarParser.ProgramContext ctx)
	{
		logger.debug("refPhase enter program: ");
		currentScope = globals;
	}	
	
	/**
	 * Verifies that the behavior imported is defined in the facade.
	 */
	public void exitImport_name(TransformationGrammarParser.Import_nameContext ctx)
	{
		
		int dottedNameCount = ctx.dotted_names().getChildCount();
		
		List<String> dottedNames = new ArrayList<String>();
		
		for (int i=0; i < dottedNameCount; i++) {
			TransformationGrammarParser.Dotted_nameContext name = ctx.dotted_names().dotted_name(i);
			if (name != null) {
				dottedNames.add(name.getText());
			}
		}		
		
		// Verify that there exists the behaviorName in the measured entity.
		String namePackage = String.join(".", dottedNames);
		if (entityFacade ==null) {
			this.error(ctx.start, ctx, "Facade was not provided for package:" + namePackage);
			
		} else {
			String behaviorString = ( (MeasuredEntity) entityFacade.getEntity()).getBehaviorText(namePackage);
			if (behaviorString == null) {
				this.error(ctx.start, ctx, " No behavior with name:" + namePackage + " in the measuredEntity" + ((MeasuredEntity) entityFacade.getEntity()).getDescr() );
			}
		}
	}
	
	/**
	 * Verifies that the attribute is registered as a symbol
	 */
	public void exitAtrib_dec(TransformationGrammarParser.Atrib_decContext ctx)
	{ 

		logger.debug("in exit atrib dec : ");
		
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
		
	/**
	 * Verifies that an import symbol is defined for the behavior being referenced by the timer 
	 */
	public void enterTimer(TransformationGrammarParser.TimerContext ctx) 
	{ 
		if (ctx.pack == null){
			this.error(ctx.start, ctx, "no Import Symbol was given");
		} else {

			String packageStr = ctx.pack.getText();		
			Symbol var = currentScope.resolve(packageStr);

			if (var instanceof TimerSymbol) {

				Symbol impSymbol = currentScope.getParentScope().resolve(packageStr);
				if (impSymbol instanceof ImportSymbol)
				{
					ArrayList<String> names = ((ImportSymbol) impSymbol).getLongName();
					for (String name : names){
						((TimerSymbol) var).addId(name);
					}

				} else {
					this.error(ctx.pack, ctx, "no such Import Symbol: " + packageStr);
				}
			} else {
				this.error(ctx.pack, ctx, "no such Time Symbol: " + packageStr);
			}
		}
	}
	
	
	/**
	 * Verifies that an import symbol is defined for the behavior being referenced by the timer 
	 */
	public void enterSchedAggregate(TransformationGrammarParser.Sched_aggregateContext ctx) 
	{ 
		if (ctx.pack == null){
			this.error(ctx.start, ctx, "no Import Symbol was given");
		} else {

			String packageStr = ctx.pack.getText();		
			Symbol var = currentScope.resolve(packageStr);

			if (var instanceof AggregateSymbol) {

				Symbol impSymbol = currentScope.getParentScope().resolve(packageStr);
				if (impSymbol instanceof ImportSymbol)
				{
					ArrayList<String> names = ((ImportSymbol) impSymbol).getLongName();
					for (String name : names){
						((AggregateSymbol) var).addId(name);
					}

				} else {
					this.error(ctx.pack, ctx, "no such Import Symbol: " + packageStr);
				}
			} else {
				this.error(ctx.pack, ctx, "no such Time Symbol: " + packageStr);
			}
		}
	}

	
	/**
	 * Verifies that an import symbol is defined for the behavior being referenced by the repeat 
	 */
	public void enterRepeat(TransformationGrammarParser.RepeatContext ctx) 
	{ 
		if (ctx.pack == null){
			this.error(ctx.start, ctx, "no Import Symbol was given");
		} else {
		
			String packageStr = ctx.pack.getText();
			Symbol var = currentScope.resolve(packageStr);

			if (var instanceof TimerSymbol) {

				Symbol impSymbol = currentScope.getParentScope().resolve(packageStr);
				if (impSymbol instanceof ImportSymbol)
				{
					ArrayList<String> names = ((ImportSymbol) impSymbol).getLongName();
					for (String name : names){
						((TimerSymbol) var).addId(name);
					}

				} else {
					this.error(ctx.pack, ctx, "no such Import Symbol: " + packageStr);
				}
			} else {
				this.error(ctx.pack, ctx, "no such Time Symbol: " + packageStr);
			}
		}
	}
	
	/**
	 * Verifies that a display symbol is defined for the display being referenced 
	 */
	public void enterDisplay(TransformationGrammarParser.DisplayContext ctx) 
	{
        ConfigurationManager manager = ConfigurationManager.getInstance();
        DisplayDeviceContainer displayDeviceCon = manager.getDisplayDeviceContainer();
		
        String name = ctx.deviceId.getText(); 
        
        DisplayDevice displayDevice = displayDeviceCon.getDisplayDevice(name);
        
        if (displayDevice == null){
        	this.error(ctx.deviceId, ctx, "no such DisplayDevice: " + name);
        }
	}
	
	/**
	 * Sets the current scope as the scope of the block. 
	 */
	public void enterBlock(TransformationGrammarParser.BlockContext ctx)
	{
		logger.debug("refPhase enter Block: ");
		
		currentScope = scopes.get(ctx); 
	}
	
	/**
	 * Sets the current scope as the parent scope of the block. 
	 */
	public void exitBlock(TransformationGrammarParser.BlockContext ctx) 
	{
		logger.debug("refPhase exist Block: ");
		
        currentScope = currentScope.getEnclosingScope();

    }
	
	/**
	 * Verifies that the variable has been defined 
	 */
	public void exitVar(TransformationGrammarParser.VarContext ctx) 
	{ 
				
		String name = ctx.ID().getSymbol().getText();
		
		logger.debug("refPhase exist Variable : " + name);

		Symbol var = currentScope.resolve(name);
		
		if ( var == null)
		{
			this.error(ctx.ID().getSymbol(), ctx, "no such Symbol: " + name);
		}
		
		if (var instanceof FunctionSymbol) 
		{
			this.error(ctx.ID().getSymbol(), ctx, name + " is a Function Symbol" );
		}
	}
	
	/**
	 * Verifies that the referenced symbol on the assignment has been defined 
	 */
	public void exitAssign(TransformationGrammarParser.AssignContext ctx) 
	{ 
		String id = ctx.ID().getText();
		Symbol idSymbol = currentScope.resolve(id);
		
		if (idSymbol == null){
			this.error(ctx.ID().getSymbol(), ctx, "no such symbol defined: " + id);
		}

		if (idSymbol instanceof FunctionSymbol) 
		{
			this.error(ctx.ID().getSymbol(), ctx, id + " is not a attribute or variable" );
		}
		
	}
	
}
