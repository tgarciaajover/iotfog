package com.advicetec.language.transformation;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.ConfigurationManager;
import com.advicetec.configuration.DisplayDevice;
import com.advicetec.configuration.DisplayDeviceContainer;
import com.advicetec.language.TransformationGrammarParser;
import com.advicetec.language.TransformationGrammarBaseListener;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.ImportSymbol;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.behavior.BehaviorRefPhase;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

public class RefPhase extends TransformationGrammarBaseListener 
{

	static Logger logger = LogManager.getLogger(RefPhase.class.getName());
	
	TransformationGrammarParser parser = null;
	ParseTreeProperty<Scope> scopes;
	GlobalScope globals;
	Scope currentScope;
	private ArrayList<SyntaxError> compilationErrors;
	
	
	RefPhase(TransformationGrammarParser parser, GlobalScope globals , ParseTreeProperty<Scope> scopes)
	{
		this.scopes = scopes;
		this.globals = globals;
		this.parser = parser; 
		compilationErrors = new ArrayList<SyntaxError>();
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
	
	public void enterProgram(TransformationGrammarParser.ProgramContext ctx)
	{
		logger.debug("refPhase enter program: ");
		currentScope = globals;
	}	
	
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
	
	public void enterBlock(TransformationGrammarParser.BlockContext ctx)
	{
		logger.debug("refPhase enter Block: ");
		
		currentScope = scopes.get(ctx); 
	}
	
	public void exitBlock(TransformationGrammarParser.BlockContext ctx) 
	{
		logger.debug("refPhase exist Block: ");
		
        currentScope = currentScope.getEnclosingScope();

    }
	
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
