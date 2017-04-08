package com.advicetec.language.behavior;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;

import com.advicetec.core.AttributeValue;
import com.advicetec.language.BehaviorGrammarLexer;
import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.ArraySymbol;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.BehaviorSymbol;
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
    public void process(String program, String entityId, List<AttributeValue> parameters) throws Exception 
    {

		BehaviorGrammarLexer lexer = new BehaviorGrammarLexer(new ANTLRFileStream(program));

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        BehaviorGrammarParser parser = new BehaviorGrammarParser(tokens);

        parser.setBuildParseTree(true);

        ParseTree tree = parser.program();

	    String mainProgramStr = (parser.getTokenNames())[BehaviorGrammarLexer.PROGRAM];

	    // Token names come with a ' at the begin and end. We remove them. 
	    mainProgramStr = mainProgramStr.replace("'", "");

        ParseTreeWalker walker = new ParseTreeWalker();

        defPhase = new DefPhase();

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
        
        System.out.println("Interpreter phase finished");
        int numElements;
        
        Set<String> ids = interpreter.globals.getkeys();
        for (String id : ids ) 
        {
        	Symbol sym = defPhase.getGlobalScope().resolve(id);
        	 
        	System.out.println("Global variable:" + id);
        	
        	if (sym instanceof ArraySymbol){
        		ArraySymbol ar  = (ArraySymbol) sym; 
        		numElements = ar.getNumElements();
        		
        		System.out.println("Num Elements:" + numElements);
        		
        		for (int j = 0; i < numElements; j++)
        		{
        			if (sym.getType() == Symbol.Type.tFLOAT){
        				Double val = (interpreter.globals.get(id).asDoubleVector())[j];
        				System.out.println("Vector " + id + "position : " + j + "value" + val);
        			}
        			
        			if (sym.getType() == Symbol.Type.tINT){
        				Integer val = (interpreter.globals.get(id).asIntegerVector())[i];
        				System.out.println("Vector " + id + "position : " + i + "value" + val.toString());
        			}
        		}
        	}
        	else
        	{
        		System.out.println("global varId: " + id + " value:" +  interpreter.globals.get(id).toString());
        	}
        }
    }    

    public Map<String, ASTNode> getGlobalValues(){
    	return interpreter.getGlobalSpace().getSymbolMap();
    }
}
