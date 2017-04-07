package com.advicetec.language.behavior;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;

import com.advicetec.language.BehaviorGrammarLexer;
import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.ast.CollectionErrorListener;
import com.advicetec.language.ast.RecognitionExceptionUtil;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;

public class SyntaxChecking 
{

	private static final String EXTENSION = "properties";
	
    public static Symbol.Type getType(int tokenType) {

        switch ( tokenType ) 
        {

            case BehaviorGrammarParser.K_VOID :  return Symbol.Type.tVOID;
            case BehaviorGrammarParser.K_INT :   return Symbol.Type.tINT;
            case BehaviorGrammarParser.K_FLOAT : return Symbol.Type.tFLOAT;
            case BehaviorGrammarParser.K_STR : return Symbol.Type.tSTRING;
            case BehaviorGrammarParser.K_BOOL : return Symbol.Type.tBOOL;
            case BehaviorGrammarParser.K_DATETIME : return Symbol.Type.tDATETIME;
            case BehaviorGrammarParser.K_DATE : return Symbol.Type.tDATE;
            case BehaviorGrammarParser.K_TIME : return Symbol.Type.tTIME;

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
        parser.removeErrorListeners();
        CollectionErrorListener collector = new CollectionErrorListener();
        parser.addErrorListener(collector);
        ParseTree tree = parser.program();

        // show tree in text form
        // System.out.println(tree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();

        DefPhase def = new DefPhase();

        walker.walk(def, tree);
        
        System.out.println("Defphase finished");

        // create next phase and feed symbol table info from def to ref phase

        RefPhase ref = new RefPhase(def.getGlobalScope(), def.getScopes());
        walker.walk(ref, tree);
        
        for (SyntaxError e : collector.getErrors()) {
            // RecognitionExceptionUtil is my custom class discussed next.
            System.out.println(RecognitionExceptionUtil.formatVerbose(e));
        }
        
        System.out.println("Defphase finished globals" + def.getGlobalScope().toString());
        

    }    

}
