package com.advicetec.language.transformation;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import com.advicetec.language.TransformationGrammarParser;
import com.advicetec.language.TransformationGrammarLexer;
import com.advicetec.language.ast.CollectionErrorListener;
import com.advicetec.language.ast.ImportSymbol;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

/**
 * This class corresponds to the transformation language checker. 
 * 
 * The checker first makes a definition phase. In this phase all symbols are defined in their corresponding scopes. 
 * Afterwards, it performs the validation phase where symbol references are verified.  
 *   
 * @author Andres Marentes
 *
 */public class SyntaxChecking 
{
	
	static Logger logger = LogManager.getLogger(SyntaxChecking.class.getName());
	
	/**  XML Tag given*/
	private static final String PROGRAM = "program";
	
    /**
     * Symbols' type set 
     * 
     * @param tokenType Token type
     * @return			type of symbol
     */
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
    	
    /**
     * Checks the transformation text given as parameter in program 
     *  
     *   It checks the grammar.  
     *   
     * @param program		transformation text to interpret
     * @param entityId		measure entity id for which the behavior is going to be run. 				
	 *
     * @throws Exception	Run time exceptions generated during the behavior checking.
     */
	public List<SyntaxError> process(String program, Integer measuredEntity ) throws Exception 
    {

    	List<SyntaxError> listErrors;
    	
    	if ((program == null) || program.isEmpty())
    	{
    		listErrors = new ArrayList<SyntaxError>();
    		logger.info("Defphase finished - The program given is empty");
    		return listErrors;
    	}
    	    	
    	CharStream  stream = (CharStream) new ANTLRInputStream(program);
		TransformationGrammarLexer lexer = new TransformationGrammarLexer(stream);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        TransformationGrammarParser parser = new TransformationGrammarParser(tokens);
        parser.setBuildParseTree(true);
        parser.removeErrorListeners();
        CollectionErrorListener collector = new CollectionErrorListener();
        parser.addErrorListener(collector);
        ParseTree tree = parser.program();

        // show tree in text form
        logger.debug(tree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();

        DefPhase def = new DefPhase(parser);

        walker.walk(def, tree);
        
        logger.debug("Defphase finished - numErrors:" + collector.getErrors().size() );

        // create next phase and feed symbol table info from def to ref phase
        
        MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
        MeasuredEntityFacade facade = entityManager.getFacadeOfEntityById(measuredEntity); 
        
        RefPhase ref = new RefPhase(parser, def.getGlobalScope(), def.getScopes(), facade);

        walker.walk(ref, tree);
        
        listErrors = collector.getErrors();
        
        // Add the custom errors created during the Ref phase. 
        for (SyntaxError e : ref.getErrors())  { 
        	listErrors.add(e);
        }
                
        logger.debug("num errors:" + listErrors.size());
        
        return listErrors;
       
    }    

	public boolean referencesImport(String program, Integer measuredEntity, String behaviorName ) {

    	
    	if ((program == null) || program.isEmpty())
    	{
    		return false;
    	}

    	List<SyntaxError> listErrors;

    	CharStream  stream = (CharStream) new ANTLRInputStream(program);
		TransformationGrammarLexer lexer = new TransformationGrammarLexer(stream);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        TransformationGrammarParser parser = new TransformationGrammarParser(tokens);
        parser.setBuildParseTree(true);
        parser.removeErrorListeners();
        CollectionErrorListener collector = new CollectionErrorListener();
        parser.addErrorListener(collector);
        ParseTree tree = parser.program();

        // show tree in text form
        logger.debug(tree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();

        DefPhase def = new DefPhase(parser);

        walker.walk(def, tree);
        
        Symbol symbol = def.getGlobalScope().resolve(behaviorName);
        
        if (symbol instanceof ImportSymbol) {
        	return true;
        } else {
        	return false;
        }

	}
	
}
