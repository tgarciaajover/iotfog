package com.advicetec.language.behavior;

import java.util.Set;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;

import com.advicetec.language.BehaviorGrammarLexer;
import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.ast.ArraySymbol;
import com.advicetec.language.ast.MemorySpace;
import com.advicetec.language.ast.Symbol;

public class InterpreterSw 
{

	private static final String EXTENSION = "properties";
	
    public static Symbol.Type getType(int tokenType) {

        switch ( tokenType ) {

            case BehaviorGrammarParser.K_VOID :  return Symbol.Type.tVOID;
            case BehaviorGrammarParser.K_INT :   return Symbol.Type.tINT;
            case BehaviorGrammarParser.K_FLOAT : return Symbol.Type.tFLOAT;
            case BehaviorGrammarParser.K_STR : return Symbol.Type.tSTRING;
            case BehaviorGrammarParser.K_BOOL : return Symbol.Type.tBOOL;
            case BehaviorGrammarParser.K_DATETIME : return Symbol.Type.tDATETIME;

        }

        return Symbol.Type.tINVALID;

    }

    public static void error(Token t, String msg) 
    {

        System.err.printf("line %d:%d %s\n", t.getLine(), t.getCharPositionInLine(),msg);

    }
    
    public void process(String program) throws Exception 
    {

		BehaviorGrammarLexer lexer = new BehaviorGrammarLexer(new ANTLRFileStream(program));

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        BehaviorGrammarParser parser = new BehaviorGrammarParser(tokens);

        parser.setBuildParseTree(true);

        ParseTree tree = parser.program();

        // show tree in text form
        // System.out.println(tree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();

        DefPhase def = new DefPhase();

        walker.walk(def, tree);
        
        System.out.println("Defphase finished globals: " + def.globals.toString());

        // create next phase and feed symbol table info from def to ref phase
        MemorySpace globals = new MemorySpace("globals");  
        
        Interpreter ref = new Interpreter(def.globals, globals, def.scopes);
        ref.visit(tree);
        
        System.out.println("Interpreter phase finished");
        int numElements;
        
        Set<String> ids = ref.globals.getkeys();
        for (String id : ids ) 
        {
        	Symbol sym = def.globals.resolve(id);
        	 
        	System.out.println("Global variable:" + id);
        	
        	if (sym instanceof ArraySymbol){
        		ArraySymbol ar  = (ArraySymbol) sym; 
        		numElements = ar.getNumElements();
        		
        		System.out.println("Num Elements:" + numElements);
        		
        		for (int i = 0; i < numElements; i++)
        		{
        			if (sym.getType() == Symbol.Type.tFLOAT){
        				Double val = (ref.globals.get(id).asDoubleVector())[i];
        				System.out.println("Vector " + id + "position : " + i + "value" + val);
        			}
        			
        			if (sym.getType() == Symbol.Type.tINT){
        				Integer val = (ref.globals.get(id).asIntegerVector())[i];
        				System.out.println("Vector " + id + "position : " + i + "value" + val.toString());
        			}
        		}
        	}
        	else
        	{
        		System.out.println("global varId: " + id + " value:" +  ref.globals.get(id).toString());
        	}
        }
    }    
}
