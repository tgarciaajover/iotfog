package com.advicetec.language.transformation;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;

import com.advicetec.language.TransformationGrammarParser;
import com.advicetec.language.TransformationGrammarLexer;
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
            case TransformationGrammarParser.K_VOID :  return Symbol.Type.tVOID;
            case TransformationGrammarParser.K_INT :   return Symbol.Type.tINT;
            case TransformationGrammarParser.K_FLOAT : return Symbol.Type.tFLOAT;
            case TransformationGrammarParser.K_STR : return Symbol.Type.tSTRING;
            case TransformationGrammarParser.K_BOOL : return Symbol.Type.tBOOL;
            case TransformationGrammarParser.K_DATE : return Symbol.Type.tDATE;
            case TransformationGrammarParser.K_TIME : return Symbol.Type.tTIME;            
            case TransformationGrammarParser.K_DATETIME : return Symbol.Type.tDATETIME;
        }
        return Symbol.Type.tINVALID;

    }

    public static void error(Token t, String msg) 
    {

        System.err.printf("line %d:%d %s\n", t.getLine(), t.getCharPositionInLine(),msg);

    }
    
    public void process(String program) throws Exception 
    {

		TransformationGrammarLexer lexer = new TransformationGrammarLexer(new ANTLRFileStream(program));

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        TransformationGrammarParser parser = new TransformationGrammarParser(tokens);
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

        RefPhase ref = new RefPhase(def.globals, def.scopes);

        walker.walk(ref, tree);

        for (SyntaxError e : collector.getErrors()) {
            // RecognitionExceptionUtil is my custom class discussed next.
            System.out.println(RecognitionExceptionUtil.formatVerbose(e));
        }

        
        System.out.println("Refphase finished globals" + def.globals.toString());
       
    }    
}