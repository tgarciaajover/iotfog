package com.advicetec.language.behavior;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;

import com.advicetec.core.AttributeValue;
import com.advicetec.language.BehaviorGrammarLexer;
import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.TransformationGrammarLexer;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.ArraySymbol;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.BehaviorSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.MemorySpace;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.UnitMeasureSymbol;
import com.advicetec.language.ast.VariableSymbol;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuredEntityManager;
import com.advicetec.monitorAdapter.protocolconverter.InterpretedSignal;

public class InterpreterSw 
{

	private DefPhase defPhase;
	private Interpreter interpreter; 
	
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
     * 
     * @param program
     * @param parameters
     * @throws Exception
     */
    public void process(String program, Integer entityId, List<InterpretedSignal> parameters) throws Exception 
    {

    	CharStream  stream = (CharStream) new ANTLRInputStream(program);
    	BehaviorGrammarLexer lexer = new BehaviorGrammarLexer(stream);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        BehaviorGrammarParser parser = new BehaviorGrammarParser(tokens);

        parser.setBuildParseTree(true);

        ParseTree tree = parser.program();

	    String mainProgramStr = (parser.getTokenNames())[TransformationGrammarLexer.PROGRAM];

	    // Token names come with a ' at the begin and end. We remove them. 
	    mainProgramStr = mainProgramStr.replace("'","");

        ParseTreeWalker walker = new ParseTreeWalker();

        defPhase = new DefPhase(parser);

        walker.walk(defPhase, tree);
       
        System.out.println("Defphase finished globals: " + defPhase.getGlobalScope().toString());

        
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

        MeasuredEntityManager manager = MeasuredEntityManager.getInstance();
        MeasuredEntityFacade facade = manager.getFacadeOfEntityById(entityId);
        
        interpreter = new Interpreter(defPhase.getGlobalScope(), globals, defPhase.getScopes(), facade);
        interpreter.visit(tree);
        
        System.out.println("Interpreter phase finished globals" + interpreter.globals.toString());
    }    

    public GlobalScope getGlobalScope(){
    	return defPhase.getGlobalScope();
    }
    
    public MemorySpace getGlobalSpace(){
    	return interpreter.getGlobalSpace();
    }

}
