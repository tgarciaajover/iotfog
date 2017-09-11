package com.advicetec.language.behavior;

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
import com.advicetec.language.BehaviorGrammarBaseListener;
import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.ImportSymbol;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.ast.VariableSymbol;
import com.advicetec.measuredentitity.MeasuredEntityFacade;

public class BehaviorRefPhase extends BehaviorGrammarBaseListener 
{

	static Logger logger = LogManager.getLogger(BehaviorRefPhase.class.getName());

	BehaviorGrammarParser parser = null;
	ParseTreeProperty<Scope> scopes;
	GlobalScope globals;
	Scope currentScope;
	private ArrayList<SyntaxError> compilationErrors;
	MeasuredEntityFacade entityFacade;


	BehaviorRefPhase(BehaviorGrammarParser parser, GlobalScope globals , ParseTreeProperty<Scope> scopes, MeasuredEntityFacade entityFacade)
	{
		this.scopes = scopes;
		this.globals = globals;
		this.parser = parser;
		compilationErrors = new ArrayList<SyntaxError>();
		this.entityFacade = entityFacade;
	}


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

	public List<SyntaxError> getErrors()
	{
		return this.compilationErrors;
	}

	public void enterProgram(BehaviorGrammarParser.ProgramContext ctx)
	{
		currentScope = globals;
	}	

	public void exitImport_name(BehaviorGrammarParser.Import_nameContext ctx)
	{
		
		int dottedNameCount = ctx.dotted_names().getChildCount();
		
		List<String> dottedNames = new ArrayList<String>();
		
		for (int i=0; i < dottedNameCount; i++) {
			BehaviorGrammarParser.Dotted_nameContext name = ctx.dotted_names().dotted_name(i);
			if (name != null) {
				dottedNames.add(name.getText());
			}
		}		
		
		// Verify that there exists the behaviorName in the measured entity.
		String namePackage = String.join(".", dottedNames);
		if (entityFacade ==null) {
			this.error(ctx.start, ctx, " Facade was not provided for package:" + namePackage);
			
		} else {
			String behaviorString = entityFacade.getEntity().getBehaviorText(namePackage);
			if (behaviorString == null) {
				this.error(ctx.start, ctx, " No behavior with name:" + namePackage + " in the measuredEntity" + entityFacade.getEntity().getDescr() );
			}
		}
	}

	
	public void enterFunction_dec(BehaviorGrammarParser.Function_decContext ctx)
	{
		currentScope = scopes.get(ctx);		
	}

	public void exitAtrib_dec(BehaviorGrammarParser.Atrib_decContext ctx)
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

	public void enterTimer(BehaviorGrammarParser.TimerContext ctx) 
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

	public void enterRepeat(BehaviorGrammarParser.RepeatContext ctx) 
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

	public void enterDisplay(BehaviorGrammarParser.DisplayContext ctx) 
	{
		ConfigurationManager manager = ConfigurationManager.getInstance();
		DisplayDeviceContainer displayDeviceCon = manager.getDisplayDeviceContainer();

		String name = ctx.deviceId.getText(); 

		DisplayDevice displayDevice = displayDeviceCon.getDisplayDevice(name);

		if (displayDevice == null){
			this.error(ctx.deviceId, ctx, "no such DisplayDevice: " + name);
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
	
	public void exitAssign(BehaviorGrammarParser.AssignContext ctx) 
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
