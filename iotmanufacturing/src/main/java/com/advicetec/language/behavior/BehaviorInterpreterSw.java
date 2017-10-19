package com.advicetec.language.behavior;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.core.EntityFacade;
import com.advicetec.language.BehaviorGrammarLexer;
import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.ArrayAttributeSymbol;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.BehaviorSymbol;
import com.advicetec.language.ast.DisplaySymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.MemorySpace;
import com.advicetec.language.ast.StateSymbol;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.language.ast.VariableSymbol;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

/**
 * This class corresponds to the interpreter interface for the behavior language. 
 * 
 * The interpreter first makes a definition phase. In this phase all symbols are defined in their corresponding scopes. 
 * Afterwards, it performs the interpretation phase where the values for the symbols are calculated.  
 *   
 * @author Andres Marentes
 *
 */
public class BehaviorInterpreterSw 
{

	static Logger logger = LogManager.getLogger(BehaviorInterpreterSw.class.getName());
	
	/**
	 *  instance to the definition phase object 
	 */
	private BehaviorDefPhase defPhase = null;
	
	/**
	 * instance to the interpreter phase object
	 */
	private BehaviorInterpreter interpreter = null; 
	
    public static Symbol.Type getType(int tokenType) {

        switch ( tokenType ) {

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
    
    /**
     * Interprets the behavior text given as parameter in program 
     *  
     *   It checks the grammar and interprets the code.  
     *   
     * @param program			behavior text to interpret
     * @param entityFacade		entity facade for which the behavior is going to be run. 
     * @param measuredEntityId	measured entity identifier that this behavior belongs to. 	
     * @param parameters		List of parameters required for the behavior execution.
     * @throws Exception		Run time exceptions generated during the behavior execution.
     */
    @SuppressWarnings("deprecation")
	public void process(String program, EntityFacade entityFacade, Integer measuredEntityId, List<InterpretedSignal> parameters) throws Exception 
    {
        if ((program == null) || program.isEmpty()){
        	logger.error("The program given is empty");
        	return;
        }
        
    	CharStream  stream = (CharStream) new ANTLRInputStream(program);
    	BehaviorGrammarLexer lexer = new BehaviorGrammarLexer(stream);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        BehaviorGrammarParser parser = new BehaviorGrammarParser(tokens);

        parser.setBuildParseTree(true);

        ParseTree tree = parser.program();

	    String mainProgramStr = (parser.getTokenNames())[BehaviorGrammarLexer.PROGRAM];

	    logger.debug("mainProgramStr:" + mainProgramStr);
	    
	    // Token names come with a ' at the begin and end. We remove them. 
	    mainProgramStr = mainProgramStr.replace("'","");

        ParseTreeWalker walker = new ParseTreeWalker();

        defPhase = new BehaviorDefPhase(parser);

        walker.walk(defPhase, tree);
       
        logger.debug("Defphase finished globals: " + defPhase.getGlobalScope().toString());
        
        // create next phase and feed symbol table info from def to ref phase
        MemorySpace globals = new MemorySpace("globals");  
        
        String programStr = lexer.getRuleNames()[BehaviorGrammarLexer.PROGRAM];
                                
        Symbol symbol = defPhase.getGlobalScope().resolve(mainProgramStr);
                
        if ( symbol == null ) {
            throw new RuntimeException("no program defined " + programStr);
        }

        if ( symbol instanceof VariableSymbol ) {
        	throw new RuntimeException(programStr + " is not a function");
        }

        if ( symbol instanceof AttributeSymbol ) {
        	throw new RuntimeException(programStr + " is not a function");
        }

        if ( symbol instanceof UnitMeasureSymbol ) {
        	throw new RuntimeException(programStr + " is not a function");
        }

        BehaviorSymbol ts = (BehaviorSymbol) symbol;
        Map<String, Symbol> parametersDef =  ts.getMembers();

        // The following code verifies the number of parameters given.
        int argCount = parameters.size();
        if ( argCount==0 )
        {
        	if (
        		 (((BehaviorSymbol)ts).getMembers() !=null) && 
        		 (((BehaviorSymbol)ts).getMembers().size() !=0) )
        	{
        		throw new RuntimeException("program " + ts.getName() + " parameters required but not given");
        	}
        }
        
        if (argCount > 0)
        {
        	if ( ((BehaviorSymbol)ts).getMembers()==null ){
        		throw new RuntimeException("program " + ts.getName() + " parameters not required and provided");
        	}
        	else if (((BehaviorSymbol)ts).getMembers().size()!=argCount){
        		throw new RuntimeException("program " + ts.getName() + " wrong number of parameters");
        	}
        }
                
        // pass the parameters to the program. 
        int i = 0;
        for (Symbol argS : ((BehaviorSymbol)ts).getMembers().values()) {
            VariableSymbol arg = (VariableSymbol)argS;
            ASTNode argValue = new ASTNode(parameters.get(i).getValue()); 
            globals.put(arg.getName(), argValue);
            i++;
        }
        
        interpreter = new BehaviorInterpreter(defPhase.getGlobalScope(), globals, defPhase.getScopes(), entityFacade, measuredEntityId);
        interpreter.visit(tree);
        
        
        Map<String, ASTNode> glob = interpreter.getGlobalSpace().getSymbolMap();
        for (String node : glob.keySet()){
        	logger.debug("Symbol:" + node + " value:" + glob.get(node));
        }

    }    
    
    /**
     * Returns attributes and the state from the language global space.
     * 
     * @return  Map string (symbol name) value (object)
     */
    public Map<String, ASTNode> getGlobalAttributes()
    {    	
    	Map<String, ASTNode> ret = new HashMap<String, ASTNode>();

    	if (getGlobalSpace() == null){
    		return ret;
    	}
    		
    	Map<String, ASTNode> map = getGlobalSpace().getSymbolMap();

    	for (String symbolId : map.keySet()) {
    		Symbol symbol = interpreter.getGlobalScope().resolve(symbolId);
    		if (symbol instanceof AttributeSymbol) {
    			ret.put(symbolId, map.get(symbolId));
    		} else if (symbol instanceof TimerSymbol) {
    			ret.put(symbolId, map.get(symbolId));
    		} else if (symbol instanceof DisplaySymbol) {
    			ret.put(symbolId, map.get(symbolId));
    		}  else if (symbol instanceof ArrayAttributeSymbol) {
    			ret.put(symbolId, map.get(symbolId));
    		} 
    	}
    	
    	return ret;
    }

    /**
     * Returns attributes and the state from the language global space.
     * 
     * @return  Map string (symbol name) value (object)
     */
    public Map<String, ASTNode> getState()
    {    	
    	Map<String, ASTNode> ret = new HashMap<String, ASTNode>();

    	if (getGlobalSpace() == null){
    		return ret;
    	}
    	
    	Map<String, ASTNode> map = getGlobalSpace().getSymbolMap();

    	for (String symbolId : map.keySet()) {
    		Symbol symbol = interpreter.getGlobalScope().resolve(symbolId);
    		if (symbol instanceof StateSymbol) {
    			ret.put(symbolId, map.get(symbolId));
    		} 
    	}
    	
    	return ret;
    }

    /**
     * Gets the global scope, it returns null if the definition phase was not executed,    
     * 
     * @return global scope
     */
    public GlobalScope getGlobalScope(){
    	if (defPhase == null)
    		return null;
    	
    	return defPhase.getGlobalScope();
    }
    
    /**
     * Gets the global space, it returns null if the interpretation phase was not executed,
     * 
     * @return global space
     */
    public MemorySpace getGlobalSpace(){
    	if (interpreter == null)
    		return null;
    	
    	return interpreter.getGlobalSpace();
    }

}
