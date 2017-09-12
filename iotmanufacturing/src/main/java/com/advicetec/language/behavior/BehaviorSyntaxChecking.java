package com.advicetec.language.behavior;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.advicetec.language.BehaviorGrammarLexer;
import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.ast.CollectionErrorListener;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.SyntaxError;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;

/**
 * This class corresponds to the behavior language checker. 
 * 
 * The checker first makes a definition phase. In this phase all the symbols are defined in their corresponding scopes. 
 * Afterwards, it performs the validation phase where symbol references are verified.  
 *   
 * @author Andres Marentes
 *
 */
public class BehaviorSyntaxChecking 
{
	/**  XML Tag given*/
	private static final String PROGRAM = "program";

	static Logger logger = LogManager.getLogger(BehaviorSyntaxChecking.class.getName());
	
    /**
     * Symbols' type set 
     * 
     * @param tokenType Token type
     * @return			type of symbol
     */
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
    
    /**
     * Checks the behavior text given as parameter in program 
     *  
     *   It checks the grammar.  
     *   
     * @param program		behavior text to interpret
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
    	BehaviorGrammarLexer lexer = new BehaviorGrammarLexer(stream);
    	
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        BehaviorGrammarParser parser = new BehaviorGrammarParser(tokens);

        parser.setBuildParseTree(true);
        parser.removeErrorListeners();
        CollectionErrorListener collector = new CollectionErrorListener();
        parser.addErrorListener(collector);
        ParseTree tree = parser.program();

        ParseTreeWalker walker = new ParseTreeWalker();

        BehaviorDefPhase def = new BehaviorDefPhase(parser);

        walker.walk(def, tree);
        
        logger.debug("Defphase finished - numErrors:" + collector.getErrors().size() );
                
        // create the next phase and feed the symbol table info from the definition phase to reference phase
        MeasuredEntityManager entityManager = MeasuredEntityManager.getInstance();
        MeasuredEntityFacade facade = entityManager.getFacadeOfEntityById(measuredEntity); 
                
        BehaviorRefPhase ref = new BehaviorRefPhase(parser, def.getGlobalScope(), def.getScopes(), facade);
        walker.walk(ref, tree);
        
        listErrors = collector.getErrors();
        
        // Add the custom errors created during the Ref phase. 
        for (SyntaxError e : ref.getErrors())  { 
        	listErrors.add(e);
        }
        
        logger.debug("num errors:" + listErrors.size());
        
        return listErrors;    

    }    


}
