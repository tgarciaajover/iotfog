package com.advicetec.language.transformation;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.advicetec.language.TransformationGrammarParser;
import com.advicetec.language.TransformationGrammarBaseListener;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.ImportSymbol;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.Symbol;

public class RefPhase extends TransformationGrammarBaseListener 
{

	ParseTreeProperty<Scope> scopes;
	GlobalScope globals;
	Scope currentScope;
	
	
	RefPhase(GlobalScope globals , ParseTreeProperty<Scope> scopes)
	{
		this.scopes = scopes;
		this.globals = globals;
	}
	
	public void enterProgram(TransformationGrammarParser.ProgramContext ctx)
	{
		System.out.println("refPhase enter program: ");
		
		currentScope = globals;
	}	

	public void exitProgram(TransformationGrammarParser.ProgramContext ctx) 
	{ 
		System.out.println("refPhase exit program: ");
	}	
	
	public void enterFormalparameters(TransformationGrammarParser.ProgramparametersContext ctx) 
	{ 
		System.out.println("refPhase enter formal parameters: ");
	}
	
	public void exitFormalparameters(TransformationGrammarParser.ProgramparametersContext ctx) 
	{ 
		System.out.println("refPhase exit formal parameters: ");
	}
	
	public void enterType(TransformationGrammarParser.TypeContext ctx) 
	{ 
		System.out.println("refPhase enter type: ");
	}
	
	public void enterRef_unit_def(TransformationGrammarParser.Ref_unit_defContext ctx) 
	{ 
		System.out.println("refPhase enter ref_unit: ");
	}
	
	public void exitRef_var_def(TransformationGrammarParser.Ref_var_defContext ctx) 
	{ 
		System.out.println("refvar_def exit: ");
	}
	
	public void exitAtrib_dec(TransformationGrammarParser.Atrib_decContext ctx)
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
	
	public void exitEventparameters(TransformationGrammarParser.EventparametersContext ctx) 
	{ 
		List<TerminalNode> ids = ctx.ID();
		for (int i = 0; i < ids.size(); i++){
			
			String name = ids.get(i).getText();
			
			Symbol var = currentScope.resolve(name);
			
			if (var == null)
			{
				SyntaxChecking.error(ids.get(i).getSymbol(), "no such symbol: " + name);
			}
			
			if (!(var instanceof AttributeSymbol)) {
				SyntaxChecking.error(ids.get(i).getSymbol(), "no such Attribute Symbol: " + name);
			}		
		}
	}
	
	public void exitEvent(TransformationGrammarParser.EventContext ctx) 
	{ 
		String packageStr = ctx.pack.getText();
		Symbol var = currentScope.resolve(packageStr);
		
		if (!(var instanceof ImportSymbol)) {
			SyntaxChecking.error(ctx.pack, "no such Import Symbol: " + packageStr);
		}
	}
	
	public void enterBlock(TransformationGrammarParser.BlockContext ctx)
	{
		System.out.println("refPhase enter Block: ");
		
		currentScope = scopes.get(ctx); 
	}
	
	public void exitBlock(TransformationGrammarParser.BlockContext ctx) 
	{
		System.out.println("refPhase exist Block: ");
		
        currentScope = currentScope.getEnclosingScope();

    }
	
	public void exitVar(TransformationGrammarParser.VarContext ctx) 
	{ 
				
		String name = ctx.ID().getSymbol().getText();
		
		System.out.println("refPhase exist Variable : " + name);

		Symbol var = currentScope.resolve(name);
		
		if ( var == null)
		{
			SyntaxChecking.error(ctx.ID().getSymbol(), "no such Symbol: " + name);
		}
		
		if (var instanceof FunctionSymbol) 
		{
			SyntaxChecking.error(ctx.ID().getSymbol(), name + " is not a Symbol" );
		}
	}
	
}
