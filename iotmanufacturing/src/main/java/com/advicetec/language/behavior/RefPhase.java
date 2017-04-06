package com.advicetec.language.behavior;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import com.advicetec.language.BehaviorGrammarBaseListener;
import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.VariableSymbol;

public class RefPhase extends BehaviorGrammarBaseListener 
{

	ParseTreeProperty<Scope> scopes;
	GlobalScope globals;
	Scope currentScope;
	
	
	RefPhase(GlobalScope globals , ParseTreeProperty<Scope> scopes)
	{
		this.scopes = scopes;
		this.globals = globals;
	}
	
	public void enterProgram(BehaviorGrammarParser.ProgramContext ctx)
	{
		currentScope = globals;
	}	
	public void exitProgram(BehaviorGrammarParser.ProgramContext ctx) 
	{ 
		System.out.println("refPhase exit program: ");
	}	

	public void enterFormalparameters(BehaviorGrammarParser.FormalparametersContext ctx) 
	{ 
		System.out.println("refPhase enter formal parameters: ");
	}
	
	public void exitFormalparameters(BehaviorGrammarParser.FormalparametersContext ctx) 
	{ 
		System.out.println("refPhase exit formal parameters: ");
	}

	public void enterType(BehaviorGrammarParser.TypeContext ctx) 
	{ 
		System.out.println("refPhase enter type: ");
	}
	
	public void enterRef_unit_def(BehaviorGrammarParser.Ref_unit_defContext ctx) 
	{ 
		System.out.println("refPhase enter ref_unit: ");
	}	
	
	public void enterFunction_dec(BehaviorGrammarParser.Function_decContext ctx)
	{
		currentScope = scopes.get(ctx);
		
	}


	public void exitRef_var_def(BehaviorGrammarParser.Ref_var_defContext ctx) 
	{ 
		System.out.println("refvar_def exit: ");
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
				SyntaxChecking.error(ctx.id2, "no such Symbol: " + name);
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
			SyntaxChecking.error(ctx.ID().getSymbol(), "no such variable: " + name);
		}
		
		if (var instanceof FunctionSymbol) 
		{
			SyntaxChecking.error(ctx.ID().getSymbol(), name + " is not a variable" );
		}
	}
	
	public void exitCall(BehaviorGrammarParser.CallContext ctx) {

        // can only handle f(...) not expr(...)

        String funcName = ctx.ID().getText();

        Symbol meth = currentScope.resolve(funcName);

        if ( meth==null ) {

            SyntaxChecking.error(ctx.ID().getSymbol(), "no such function: "+funcName);

        }

        if ( meth instanceof VariableSymbol ) {

        	SyntaxChecking.error(ctx.ID().getSymbol(), funcName+" is not a function");

        }

    }
}
