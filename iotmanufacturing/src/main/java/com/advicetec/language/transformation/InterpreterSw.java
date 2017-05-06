package com.advicetec.language.transformation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.language.TransformationGrammarParser;
import com.advicetec.language.TransformationGrammarLexer;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.MemorySpace;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.TransformationSymbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.language.ast.VariableSymbol;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

public class InterpreterSw 
{	
	
	static Logger logger = LogManager.getLogger(InterpreterSw.class.getName());
	private DefPhase defPhase;
	private Interpreter interpreter;
	
    public static Symbol.Type getType(int tokenType) {

        switch ( tokenType ) 
        {
            case TransformationGrammarParser.K_VOID :  return Symbol.Type.tVOID;
            case TransformationGrammarParser.K_INT :   return Symbol.Type.tINT;
            case TransformationGrammarParser.K_FLOAT : return Symbol.Type.tFLOAT;
            case TransformationGrammarParser.K_STR : return Symbol.Type.tSTRING;
            case TransformationGrammarParser.K_BOOL : return Symbol.Type.tBOOL;
            case TransformationGrammarParser.K_DATETIME : return Symbol.Type.tDATETIME;
            case TransformationGrammarParser.K_DATE : return Symbol.Type.tDATE;
            case TransformationGrammarParser.K_TIME : return Symbol.Type.tTIME;
        }

        return Symbol.Type.tINVALID;

    }

    public static void error(Token t, String msg) 
    {
        System.err.printf("line %d:%d %s\n", t.getLine(), t.getCharPositionInLine(),msg);
    }
    
    /**
     * 
     * @param program
     * @param parameters
     * @throws Exception
     */
    public void process(String program, Integer entityId, List<InterpretedSignal> parameters) throws Exception 
    {

    	CharStream  stream = (CharStream) new ANTLRInputStream(program);
		TransformationGrammarLexer lexer = new TransformationGrammarLexer(stream);
		
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        TransformationGrammarParser parser = new TransformationGrammarParser(tokens);

        parser.setBuildParseTree(true);
        
        ParseTree tree = parser.program();

	    String mainProgramStr = (parser.getTokenNames())[TransformationGrammarLexer.PROGRAM];
	    
	    // Token names come with a ' at the begin and end. We remove them. 
	    mainProgramStr = mainProgramStr.replace("'","");
        
        ParseTreeWalker walker = new ParseTreeWalker();

        defPhase = new DefPhase(parser);

        walker.walk(defPhase, tree);
        
        logger.debug("Defphase finished globals: " + defPhase.getGlobalScope().toString());
        
        // create next phase and feed symbol table info from def to ref phase
        MemorySpace globals = new MemorySpace("globals");  
        
        String programStr = lexer.getRuleNames()[TransformationGrammarLexer.PROGRAM];
                                
        Symbol symbol = defPhase.getGlobalScope().resolve(mainProgramStr);
                
        if ( symbol == null ) {
        	logger.error("no program defined " + programStr);
        	throw new RuntimeException("no program defined " + programStr);
        }

        if ( symbol instanceof VariableSymbol ) {
        	logger.error(programStr + " is not a function");
        	throw new RuntimeException(programStr + " is not a function");
        }

        if ( symbol instanceof AttributeSymbol ) {
        	logger.error(programStr + " is not a function");
        	throw new RuntimeException(programStr + " is not a function");
        }

        if ( symbol instanceof UnitMeasureSymbol ) {
        	logger.error(programStr + " is not a function");
        	throw new RuntimeException(programStr + " is not a function");
        }

        TransformationSymbol ts = (TransformationSymbol) symbol;
        Map<String, Symbol> parametersDef =  ts.getMembers();

        // The following code verifies the number of parameters given.
        int argCount = parameters.size();
        if ( argCount==0 )
        {
        	if (
        		 (((TransformationSymbol)ts).getMembers() !=null) && 
        		 (((TransformationSymbol)ts).getMembers().size() !=0) )
        	{
        		logger.error("program " + ts.getName() + " parameters required but not given");
        		throw new RuntimeException("program " + ts.getName() + " parameters required but not given");
        	}
        }
        
        if (argCount > 0)
        {
        	if (((TransformationSymbol)ts).getMembers()==null ){
        		logger.error("program " + ts.getName() + " parameters not required and provided");
        		throw new RuntimeException("program " + ts.getName() + " parameters not required and provided");
        	}
        	else if (((TransformationSymbol)ts).getMembers().size()!=argCount){
        		logger.error("program " + ts.getName() + " wrong number of parameters" + "tranfor defin num:" + ((TransformationSymbol)ts).getMembers().size() + "number:" + argCount);
        		throw new RuntimeException("program " + ts.getName() + " wrong number of parameters" + "tranfor defin num:" + ((TransformationSymbol)ts).getMembers().size() + "number:" + argCount);
        	}
        }
                
        // pass the parameters to the program. 
        int i = 0;
        for (Symbol argS : ((TransformationSymbol)ts).getMembers().values()) {
            VariableSymbol arg = (VariableSymbol)argS;
            ASTNode argValue = new ASTNode(parameters.get(i).getValue()); 
            globals.put(arg.getName(), argValue);
            i++;
        }

        MeasuredEntityManager manager = MeasuredEntityManager.getInstance();
        MeasuredEntityFacade facade = manager.getFacadeOfEntityById(entityId);

        logger.debug("before creating a new instance of interpreter");
        interpreter = new Interpreter(defPhase.getGlobalScope(), globals, defPhase.getScopes(), facade);
                
        if (tree instanceof TransformationGrammarParser.ProgramContext){
        	logger.debug("ok it is the correct programa context");
        }
        interpreter.visit(tree);

        Map<String, ASTNode> glob = interpreter.getGlobalSpace().getSymbolMap();
        for (String node : glob.keySet()){
        	logger.debug("Symbol:" + node + " value:" + glob.get(node));
        }
    } 
    
    public GlobalScope getGlobalScope(){
    	return defPhase.getGlobalScope();
    }
    
    public MemorySpace getGlobalSpace(){
    	return interpreter.getGlobalSpace();
    }
}
