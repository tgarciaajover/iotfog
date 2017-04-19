package com.advicetec.language.behavior;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.Token;

import com.advicetec.core.AttributeType;
import com.advicetec.core.AttributeValue;
import com.advicetec.language.BehaviorGrammarBaseVisitor;
import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.ArrayAttributeSymbol;
import com.advicetec.language.ast.ArraySymbol;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.BehaviorSpace;
import com.advicetec.language.ast.BehaviorSymbol;
import com.advicetec.language.ast.FunctionSpace;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.MemorySpace;
import com.advicetec.language.ast.ReturnValue;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.Type;
import com.advicetec.language.ast.VariableSymbol;
import com.advicetec.measuredentitity.MeasuredEntityFacade;

public class Interpreter extends BehaviorGrammarBaseVisitor<ASTNode>
{
 	
	public InterpreterListener listener = // default response to messages
	        new InterpreterListener() {
	            public void info(String msg) { System.out.println(msg); }
	            public void error(String msg) { System.err.println(msg); }
	            public void error(String msg, Exception e) 
	            {
	                error(msg); e.printStackTrace(System.err);
	            }
	            public void error(String msg, Token t) 
	            {
	                error("line "+t.getLine()+": "+msg);
	            }
	        };
	
	GlobalScope globalScope; // Global scope is filled by the parser
	ParseTreeProperty<Scope> scopes;  // The definition for the rest of scopes
	Scope currentScope;
	MeasuredEntityFacade facade;
	
	MemorySpace globals;
	MemorySpace currentSpace;
	Stack<FunctionSpace> stack; // call stack
	
	// used to compare floating point numbers
    public static final double SMALL_VALUE = 0.00000000001;

	Interpreter(GlobalScope _globalScope, MemorySpace _globals, ParseTreeProperty<Scope> scopes, MeasuredEntityFacade facade)
	{
		// Variable for symbol definition.
		this.globalScope = _globalScope;
		this.globals = _globals;
		this.scopes = scopes;
		this.currentScope = _globalScope;
		this.facade = facade; 
		
		// For memory evaluation
		stack = new Stack<FunctionSpace>(); // call stack
		System.out.println("Interpreter main");
	}
	
	@Override 
	public ASTNode visitProgram(BehaviorGrammarParser.ProgramContext ctx) 
	{ 
		System.out.println("visitProgram");
		
		currentScope = globalScope; 
		currentSpace = globals;
		
        String fname = ctx.PROGRAM().getText();
        Symbol fs = currentScope.resolve(fname);

        if ( fs == null ) {
            listener.error( "no such function: " + fname);
            throw new RuntimeException("no such function: " + fname);
        }

        if ( fs instanceof VariableSymbol ) {
        	listener.error( fname + " is not a function");
        	throw new RuntimeException(fname + " is not a function");
        }

        BehaviorSpace fspace = new BehaviorSpace((BehaviorSymbol) fs);
        MemorySpace saveSpace = currentSpace;
        Scope saveScope = currentScope; 
        currentSpace = fspace;

        // update the current scope to the one defined in the function. 
        currentScope = (BehaviorSymbol) fs;

        // Counts the number of parameters included.
        int argCount = 0;
        BehaviorGrammarParser.ProgramparametersContext listParams = ctx.programparameters();        
        if (listParams != null)
        {
        	for ( BehaviorGrammarParser.ProgramparameterContext expres : listParams.programparameter())
        		argCount++;
        }
        
        System.out.println("num params:" + argCount);

        // check for argument compatibility
        if ( argCount==0 )
        {
        	if (
        		 (((BehaviorSymbol)fs).getMembers() !=null) && 
        		 (((BehaviorSymbol)fs).getMembers().size() !=0) )
        	{
        		throw new RuntimeException("function " + fs.getName() + " arguments required but not given");
        	}
        }
        
        if (argCount > 0)
        {
        	if ( ((BehaviorSymbol)fs).getMembers()==null ){
        		throw new RuntimeException("function " + fs.getName() + " arguments not required and provided");
        	}
        	else if (((BehaviorSymbol)fs).getMembers().size()!=argCount){
        		throw new RuntimeException("function " + fs.getName() + " wrong number of parameters");
        	}
        }
        
        if (listParams != null)
        {
	        int i = 0; // define args according to order in program parameters
	        for (Symbol argS : ((BehaviorSymbol)fs).getMembers().values()) {
	            AttributeSymbol arg = (AttributeSymbol)argS;
	            ASTNode argValue = this.visit(listParams.programparameter(i)); 
	            fspace.put(arg.getName(), argValue);
	            i++;
	        }
        }
        
        ASTNode result = null;
        stack.push(fspace);        // PUSH new arg, local scope
        this.visit( ((BehaviorSymbol)fs).block ); 

        System.out.println("ending program:" + currentSpace.getkeys());
        
        // Copies the program stack to global stack to be returned to the interpreter caller.
        Set<String> keys = currentSpace.getkeys();
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			saveSpace.put(id, currentSpace.get(id));
		} 
        
        stack.pop();               // POP arg, locals
        currentSpace = saveSpace;
        
        // goes up in the current scope.
        currentScope = saveScope;
        System.out.println("Starting function: "+ fname + "Ending currentScope:" + currentScope.getScopeName());        
        
        return result;

	}
	
	@Override 
	public ASTNode visitVar_dec(BehaviorGrammarParser.Var_decContext ctx) 
	{ 
		System.out.println("visitVar_dec");
		
		// the declaration includes an assignment
		if (ctx.ASG() != null)
		{
			String id = ctx.ID().getText();
	        ASTNode value = this.visit(ctx.expression());
	        currentSpace.put(id, value);         // store
	        
	        return value;
			
		}
		else
		{
			String id = ctx.ID().getText();
	        currentSpace.put(id, new ASTNode(new Object()));         // store
			return ASTNode.VOID;	
		}
	}

	public ASTNode visitVect_var_dec(BehaviorGrammarParser.Vect_var_decContext ctx) 
	{ 
		
		String id = ctx.ID().getText();

		System.out.println("Visit vector variable declaration id:" + id);

		int numElements = Integer.valueOf(ctx.numElements.getText());
		Symbol vec = currentScope.resolve(id);

		if (numElements > 0)
		{ 
		    switch (vec.getType())
		    {
		    	case tINT:
		    		Integer [] inarray = new Integer[numElements];
		    		Arrays.fill(inarray, 0);
		    		currentSpace.put(id, new ASTNode(inarray));
		    		break;
		    	
		    	case tFLOAT:
		    		Double [] dbarray = new Double[numElements];
		    		Arrays.fill(dbarray, 0);
		    		currentSpace.put(id, new ASTNode(dbarray));
		    		break;
		    	
		    	case tSTRING:
		    		String [] starray = new String[numElements];
		    		Arrays.fill(starray, "");
		    		currentSpace.put(id, new ASTNode(starray));
		    		break;
		    	
		    	case tBOOL:
		    		Boolean [] boarray = new Boolean[numElements];
		    		Arrays.fill(boarray, false);
		    		currentSpace.put(id, new ASTNode(boarray));
		    		break;
		    	
		    	case tDATETIME:
		    		LocalDateTime [] dtarray = new LocalDateTime[numElements];
		    		Arrays.fill(dtarray, LocalDateTime.MIN);
		    		currentSpace.put(id, new ASTNode(dtarray));
		    		break;
		    	
		    	case tDATE:
		    		LocalDate [] darray = new LocalDate[numElements];
		    		Arrays.fill(darray, LocalDate.MIN);
		    		currentSpace.put(id, new ASTNode(darray));
		    		break;
		    	
		    	case tTIME:
		    		LocalTime [] tarray = new LocalTime[numElements];
		    		Arrays.fill(tarray, LocalDate.MIN);
		    		currentSpace.put(id, new ASTNode(new LocalTime[numElements]));
		    		break;
		    	
		    	default:
		    		throw new RuntimeException("unknown type for vector: " + vec.getType().name());
		    }
		}
		
		return ASTNode.VOID;
	}
	
	public ASTNode visitUnit_dec(BehaviorGrammarParser.Unit_decContext ctx) 
	{ 
		System.out.println("visit Unit dec");
		
		String id = ctx.ID().getText();
	    currentSpace.put(id, new ASTNode(new Object()));         // store
		return ASTNode.VOID;	
	}
	
	public ASTNode visitStatus(BehaviorGrammarParser.StatusContext ctx) 
	{ 
		String attributeId = ctx.ID().getText(); 
		AttributeValue value = (AttributeValue) facade.getNewestByAttributeName(attributeId);
		Symbol symbol = currentScope.resolve(attributeId);
		Object valObj = value.getValue();
				
		switch (value.getAttribute().getType()){
		case INT:
			if (symbol.getType() != Symbol.Type.tINT){
				throw new RuntimeException("the attribute given: " + attributeId + " is not registered in the status as type int" );
			}
			break;
		case DATETIME:
			if (symbol.getType() != Symbol.Type.tDATETIME){
				throw new RuntimeException("the attribute given: " + attributeId + " is not registered in the status as type datetime" );
			}
			break;
		case DOUBLE:
			if (symbol.getType() != Symbol.Type.tFLOAT){
				throw new RuntimeException("the attribute given: " + attributeId + " is not registered in the status as type float" );
			}
			break;
		case STRING:
			if (symbol.getType() != Symbol.Type.tSTRING){
				throw new RuntimeException("the attribute given: " + attributeId + " is not registered in the status as type string" );
			}
			break;
		case BOOLEAN:
			if (symbol.getType() != Symbol.Type.tBOOL){
				throw new RuntimeException("the attribute given: " + attributeId + " is not registered in the status as type boolean" );
			}
			break;
		case DATE:
			if (symbol.getType() != Symbol.Type.tDATE){
				throw new RuntimeException("the attribute given: " + attributeId + " is not registered in the status as type date" );
			}
			break;
		case TIME:
			if (symbol.getType() != Symbol.Type.tTIME){
				throw new RuntimeException("the attribute given: " + attributeId + " is not registered in the status as type time" );
			}
			break;
		case VOID:
			throw new RuntimeException("the attribute given: " + attributeId + " is registered in the status as type void" );
		}
		
		return new ASTNode(valObj); 
				
	}
		
	public ASTNode visitAtrib_dec(BehaviorGrammarParser.Atrib_decContext ctx) 
	{ 
		String id = ctx.id1.getText();
		System.out.println("visitAtrib_dec:" + id);
        currentSpace.put(id, new ASTNode(new Object()));         // store
		
        AttributeSymbol toAssign = (AttributeSymbol) currentScope.resolve(id);
		// the declaration includes an assignment
		if (ctx.ASG() != null)
			return AssignAttribute(toAssign, ctx);
		else
			return ASTNode.VOID;	
	}

	@Override 
	public ASTNode visitVect_attrib_dec(BehaviorGrammarParser.Vect_attrib_decContext ctx) 
	{ 		
		String id = ctx.id1.getText();
		System.out.println("Visit vector attribute declaration id:" + id);

		int numElements = Integer.valueOf(ctx.numElements.getText());
		Symbol vec = currentScope.resolve(id);
		if (numElements > 0)
		{ 
		    switch (vec.getType())
		    {
		    	case tINT:
		    		Integer [] inarray = new Integer[numElements];
		    		Arrays.fill(inarray, 0);
		    		currentSpace.put(id, new ASTNode(inarray));
		    		break;
		    	
		    	case tFLOAT:
		    		Double [] dbarray = new Double[numElements];
		    		Arrays.fill(dbarray, 0.0);
		    		currentSpace.put(id, new ASTNode(dbarray));
		    		break;
		    	
		    	case tSTRING:
		    		String [] starray = new String[numElements];
		    		Arrays.fill(starray, "");
		    		currentSpace.put(id, new ASTNode(starray));
		    		break;
		    	
		    	case tBOOL:
		    		Boolean [] boarray = new Boolean[numElements];
		    		Arrays.fill(boarray, false);
		    		currentSpace.put(id, new ASTNode(boarray));
		    		break;
		    	
		    	case tDATETIME:
		    		LocalDateTime [] dtarray = new LocalDateTime[numElements];
		    		Arrays.fill(dtarray, LocalDateTime.MIN);
		    		currentSpace.put(id, new ASTNode(dtarray));
		    		break;
		    	
		    	case tDATE:
		    		LocalDate [] darray = new LocalDate[numElements];
		    		Arrays.fill(darray, LocalDate.MIN);
		    		currentSpace.put(id, new ASTNode(darray));
		    		break;
		    	
		    	case tTIME:
		    		LocalTime [] tarray = new LocalTime[numElements];
		    		Arrays.fill(tarray, LocalDate.MIN);
		    		currentSpace.put(id, new ASTNode(new LocalTime[numElements]));
		    		break;
		    	
		    	default:
		    		throw new RuntimeException("unknown type for vector: " + vec.getType().name());
		    }
		}
		
		return ASTNode.VOID;
	}
	
	@Override 
	public ASTNode visitAssign(BehaviorGrammarParser.AssignContext ctx) 
	{ 
				
		System.out.println("visitAssign");
		
		String id = ctx.ID().getText();
        ASTNode value = this.visit(ctx.expression());

        // var assign ^('=' a expr)
        MemorySpace space = getSpaceWithSymbol(id);
        if ( space==null ) 
        	space = currentSpace; // create in current space

        space.put(id, value);         // store
        
        return value;

	}

	public ASTNode AssignAttribute(AttributeSymbol toAssign, BehaviorGrammarParser.Atrib_decContext ctx)
	{
		
		// It verifies whether the expression correspond to another attribute. In such 
		// case, it checks whether or not both attributes manage the same units.
		
		String id2 = ctx.expression().getText();
		Symbol s = currentScope.resolve(id2);
		if (s instanceof AttributeSymbol )
		{
			AttributeSymbol s1 = (AttributeSymbol) s;
			
			System.out.println("s1 unit:" + s1.getUnitOfMeasure() + "sysattr : " + toAssign.getUnitOfMeasure());
			
			if (s1.getUnitOfMeasure() != toAssign.getUnitOfMeasure() )
			{
		        ASTNode value = this.visit(ctx.expression());
		        currentSpace.put(toAssign.getName(), value);         // store
		        return value;						
			}
			else 
			{
				// TODO: implement conversion rates.
				throw new RuntimeException("cannot assign attribute with different units of measure:" );
			}
		}
		else
		{
		    ASTNode value = this.visit(ctx.expression());
		    currentSpace.put(toAssign.getName(), value);         // store
		    return value;					
		}
		
	}
	
	@Override 
	public ASTNode visitAssign_vec(BehaviorGrammarParser.Assign_vecContext ctx) 
	{ 
		
		String id = ctx.ID().getText();
		
		Symbol vec = currentScope.resolve(id);
		if (!(vec instanceof ArraySymbol) ){
			throw new RuntimeException("The symbol : " + id + "is not a vector");
		}

		int numElements = ((ArraySymbol) vec).getNumElements();
		int numElement = Integer.valueOf(ctx.numElement.getText());

		// Verifies that the element to assign must be between and numElements -1.
		if ((numElement < 0) || (numElement >= numElements))
		{
			throw new RuntimeException("The element number provided is out of range");
		}

		ASTNode value = this.visit(ctx.expression());		
		ASTNode array = currentSpace.get(id);

		switch (vec.getType())
		{
		case tINT:
			Integer[] prevArray1 = array.asIntegerVector();
			prevArray1[numElement] = value.asInterger();
			currentSpace.put(id, new ASTNode(prevArray1));
			break;

		case tFLOAT:
			Double[] prevArray2 = array.asDoubleVector();
			prevArray2[numElement] = value.asDouble();
			currentSpace.put(id, new ASTNode(prevArray2));
			break;

		case tSTRING:
			String[] prevArray3 = array.asStringVector();
			prevArray3[numElement] = value.asString();
			currentSpace.put(id, new ASTNode(prevArray3));
			break;

		case tBOOL:
			Boolean[] prevArray4 = array.asBooleanVector();
			prevArray4[numElement] = value.asBoolean();
			currentSpace.put(id, new ASTNode(prevArray4));
			break;

		case tDATETIME:
			LocalDateTime[] prevArray5 = array.asDateTimeVector();
			prevArray5[numElement] = value.asDateTime();
			currentSpace.put(id, new ASTNode(prevArray5));
			break;

		case tDATE:
			LocalDate[] prevArray6 = array.asDateVector();
			prevArray6[numElement] = value.asDate();
			currentSpace.put(id, new ASTNode(prevArray6));
			break;

		case tTIME:
			LocalTime[] prevArray7 = array.asTimeVector();
			prevArray7[numElement] = value.asTime();
			currentSpace.put(id, new ASTNode(prevArray7));
			break;

		default:
			throw new RuntimeException("unknown type for vector: " + vec.getType().name());
		}

		return ASTNode.VOID;
	}	
	
	@Override 
	public ASTNode visitVar(BehaviorGrammarParser.VarContext ctx) 
	{ 
		
		System.out.println("visitVar");
		
		String id = ctx.getText();
		
		MemorySpace space = getSpaceWithSymbol(id); 
        ASTNode value = space.get(id);
        if(value == null) {
            throw new RuntimeException("no such variable: " + id);
        }
        
        return value;
	}

	public ASTNode visitRound(BehaviorGrammarParser.RoundContext ctx) 
	{ 
		ASTNode value = this.visit(ctx.expression());
		long numdecimals = Integer.valueOf(ctx.INT1().getText());
		
		if (value.isDouble() || value.isInteger()){
			if (value.isDouble()){
				Double valueD = new Double(value.asDouble());
				valueD = valueD * numdecimals;
				long tmp = Math.round(valueD);
				valueD = ((double)tmp / numdecimals);
				return new ASTNode(valueD);
			} else {
				Integer ret = new Integer(value.asInterger());
				return new ASTNode(ret);
			}
		} else {
			throw new RuntimeException("Only numbers are posible to round");
		}
	}
	
	@Override 
	public ASTNode visitRef_return(BehaviorGrammarParser.Ref_returnContext ctx) 
	{ 
    	ReturnValue sharedReturnValue = new ReturnValue();
    	sharedReturnValue.value = this.visit(ctx.expression());
        throw sharedReturnValue;
	}

	@Override 
	public ASTNode visitInteger(BehaviorGrammarParser.IntegerContext ctx) 
	{ 
		System.out.println("visitInteger");
		return new ASTNode(Integer.valueOf(ctx.getText()));
	}

	@Override
	public ASTNode visitYear(BehaviorGrammarParser.YearContext ctx) 
	{ 
		System.out.println("visitYear");
		return new ASTNode(Integer.valueOf(ctx.getText()));
	}
	
	public ASTNode visitDigit(BehaviorGrammarParser.DigitContext ctx) 
	{ 
		return new ASTNode(Integer.valueOf(ctx.getText()));
	}
	
	@Override 
	public ASTNode visitFloat(BehaviorGrammarParser.FloatContext ctx) 
	{ 
		System.out.println("visit Float");
		
		return new ASTNode(Double.valueOf(ctx.getText()));
	}

	@Override 
	public ASTNode visitBoolean(BehaviorGrammarParser.BooleanContext ctx) 
	{ 
		System.out.println("visitBoolean");
		
		return new ASTNode(Boolean.valueOf(ctx.getText()));
	}

	@Override 
	public ASTNode visitStr(BehaviorGrammarParser.StrContext ctx) 
	{ 
		System.out.println("visitStr");
		
		String str = ctx.getText();
	     
		// strip quotes
	    str = str.substring(1, str.length() - 1).replace("\"\"", "\"");
	    return new ASTNode(str);
	}
	
	@Override 
	public ASTNode visitParens(BehaviorGrammarParser.ParensContext ctx) 
	{ 
		System.out.println("visitParens");
		
		return this.visit(ctx.expression()); 
	}
	
	@Override 
	public ASTNode visitMult(BehaviorGrammarParser.MultContext ctx) 
	{ 
		System.out.println("visitMult");
		
		ASTNode left = this.visit(ctx.expression(0));
        ASTNode right = this.visit(ctx.expression(1));

        switch (ctx.op.getType()) {
            case BehaviorGrammarParser.MULT:
                return new ASTNode(left.asDouble() * right.asDouble());
            case BehaviorGrammarParser.DIVI:
                return new ASTNode(left.asDouble() / right.asDouble());
            case BehaviorGrammarParser.MOD:
                return new ASTNode(left.asDouble() % right.asDouble());
            default:
                throw new RuntimeException("unknown operator: " + BehaviorGrammarParser.tokenNames[ctx.op.getType()]);
        }
	}

	@Override 
	public ASTNode visitAddSub(BehaviorGrammarParser.AddSubContext ctx) 
	{ 
		System.out.println("visitAddSub");
		
		ASTNode left = this.visit(ctx.expression(0));
        ASTNode right = this.visit(ctx.expression(1));

        switch (ctx.op.getType()) {
            case BehaviorGrammarParser.PLUS:
                return left.isDouble() && right.isDouble() ?
                        new ASTNode(left.asDouble() + right.asDouble()) :
                        new ASTNode(left.asString() + right.asString());
            case BehaviorGrammarParser.MINUS:
                return new ASTNode(left.asDouble() - right.asDouble());
            default:
                throw new RuntimeException("unknown operator: " + BehaviorGrammarParser.tokenNames[ctx.op.getType()]);
        }

	}

	@Override 
	public ASTNode visitExpon(BehaviorGrammarParser.ExponContext ctx) 
	{ 
		
		System.out.println("visitExpon");
		
		ASTNode left = this.visit(ctx.expression(0));
        ASTNode right = this.visit(ctx.expression(1));
        
        return new ASTNode(Math.pow(left.asDouble(), right.asDouble()));
	}
	
	@Override 
	public ASTNode visitRelationalExpr(BehaviorGrammarParser.RelationalExprContext ctx) 
	{ 
		
		System.out.println("visitRelationalExpr");
		
		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));

        switch (ctx.op.getType()) {
            case BehaviorGrammarParser.LT:
                return new ASTNode(left.asDouble() < right.asDouble());
            case BehaviorGrammarParser.LTEQ:
                return new ASTNode(left.asDouble() <= right.asDouble());
            case BehaviorGrammarParser.GT:
                return new ASTNode(left.asDouble() > right.asDouble());
            case BehaviorGrammarParser.GTEQ:
                return new ASTNode(left.asDouble() >= right.asDouble());
            default:
                throw new RuntimeException("unknown operator: " + BehaviorGrammarParser.tokenNames[ctx.op.getType()]);
        }
	}
	
	@Override 
	public ASTNode visitEqualityExpr(BehaviorGrammarParser.EqualityExprContext ctx) 
	{ 
		
		System.out.println("visitEqualityExpr");
		
		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));

        switch (ctx.op.getType()) {
            case BehaviorGrammarParser.EQ:
                return left.isDouble() && right.isDouble() ?
                        new ASTNode(Math.abs(left.asDouble() - right.asDouble()) < SMALL_VALUE) :
                        new ASTNode(left.equals(right));
            case BehaviorGrammarParser.NEQ:
                return left.isDouble() && right.isDouble() ?
                        new ASTNode(Math.abs(left.asDouble() - right.asDouble()) >= SMALL_VALUE) :
                        new ASTNode(!left.equals(right));
            default:
                throw new RuntimeException("unknown operator: " + BehaviorGrammarParser.tokenNames[ctx.op.getType()]);
        }
	}
	
	@Override 
	public ASTNode visitAndExpr(BehaviorGrammarParser.AndExprContext ctx) 
	{ 
		System.out.println("visitAndExpr");
		
		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));
        return new ASTNode(left.asBoolean() && right.asBoolean());

	}
	
	@Override 
	public ASTNode visitOrExpr(BehaviorGrammarParser.OrExprContext ctx) 
	{ 
		System.out.println("visitOrExpr");
		
		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));
        return new ASTNode(left.asBoolean() || right.asBoolean());
	}
	
	public ASTNode visitToken(BehaviorGrammarParser.TokenContext ctx) 
	{ 
		BehaviorGrammarParser.ExpressionContext stringEq1 =  ctx.ex1;
		BehaviorGrammarParser.ExpressionContext numberFrom = ctx.ex2;
		BehaviorGrammarParser.ExpressionContext numberTo = ctx.ex3;
		
		String ret =  (this.visit(stringEq1)).asString();
		
		ASTNode numberFr = this.visit(numberFrom); 
		if (numberFr.isInteger() == false) 
		{
			throw new RuntimeException("param number_from is not valid: " + numberFr.toString());
		}
		
		ASTNode numberT = this.visit(numberTo);
		if ( numberT.isInteger() == false) 
		{
			throw new RuntimeException("param number_to is not valid: " + numberT.toString());
		}
		
		Integer beginIndex = (numberFr).asInterger();
		Integer endIndex = (numberT).asInterger();
		ret = ret.substring(beginIndex.intValue(), endIndex.intValue());
		
		return new ASTNode(ret);
	}
	
	@Override 
	public ASTNode visitCall(BehaviorGrammarParser.CallContext ctx) 
	{ 
        
		System.out.println("visitCall");
		
		// Resolve function's name
        String fname = ctx.ID().getText();
        Symbol fs = currentScope.resolve(fname);

        if ( fs == null ) {
            listener.error( "no such function: " + fname);
            throw new RuntimeException("no such function: " + fname);
        }

        if ( fs instanceof VariableSymbol ) {
        	listener.error( fname + " is not a function");
        	throw new RuntimeException(fname + " is not a function");
        }
        
        FunctionSpace fspace = new FunctionSpace((FunctionSymbol) fs);
        MemorySpace saveSpace = currentSpace;
        Scope saveScope = currentScope; 
        currentSpace = fspace;
        
        // update the current scope to the one defined in the function. 
        currentScope = (FunctionSymbol) fs;

        System.out.println("Starting function: "+ fname + " currentScope:" + currentScope.getScopeName());
        
        // Counts the number of parameters included.
        int argCount = 0;
        BehaviorGrammarParser.ExpressionListContext listParams = ctx.expressionList();        
        if (listParams != null)
        {
        	for ( BehaviorGrammarParser.ExpressionContext expres : listParams.expression())
        		argCount++;
        }
        
        System.out.println("num params:" + argCount);
        
        // check for argument compatibility
        if ( argCount==0 )
        {
        	if (
        		 (((FunctionSymbol)fs).getMembers() !=null) && 
        		 (((FunctionSymbol)fs).getMembers().size() !=0) )
        	{
        		throw new RuntimeException("function " + fs.getName() + " arguments required but not given");
        	}
        }
        
        if (argCount > 0)
        {
        	if ( ((FunctionSymbol)fs).getMembers()==null ){
        		throw new RuntimeException("function " + fs.getName() + " arguments not required and provided");
        	}
        	else if (((FunctionSymbol)fs).getMembers().size()!=argCount){
        		throw new RuntimeException("function " + fs.getName() + " wrong number of parameters");
        	}
        }
        
        if (listParams != null)
        {
	        int i = 0; // define args according to order in formalArgs
	        for (Symbol argS : ((FunctionSymbol)fs).getMembers().values()) {
	            VariableSymbol arg = (VariableSymbol)argS;
	            ASTNode argValue = this.visit(listParams.expression(i)); 
	            fspace.put(arg.getName(), argValue);
	            i++;
	        }
        }
        
        // TODO: verify the type of parameter.
        
        ASTNode result = null;
        stack.push(fspace);        // PUSH new arg, local scope
        try 
        { 
        	this.visit( ((FunctionSymbol)fs).block ); 
        } // do the call
        catch (ReturnValue rv) 
        { 
        	result = rv.value; 
        } // trap return value
        
        stack.pop();               // POP arg, locals
        currentSpace = saveSpace;
        
        // goes up in the current scope.
        currentScope = saveScope;
        System.out.println("Starting function: "+ fname + "Ending currentScope:" + currentScope.getScopeName());
        return result;

	}
	
	@Override 
	public ASTNode visitBlock(BehaviorGrammarParser.BlockContext ctx) 
	{ 
		System.out.println("VisitBlock");
		
		currentScope = scopes.get(ctx);
		
		System.out.println(currentScope);
		
		for (BehaviorGrammarParser.SentenceContext sentence : ctx.sentence() )
		{
			System.out.println("it is going to run sentence");
			this.visit(sentence);
		}
		
		currentScope = currentScope.getEnclosingScope(); 
		
		return ASTNode.VOID;

	}

    @Override
    public ASTNode visitIf_stat(BehaviorGrammarParser.If_statContext ctx) {

        List<BehaviorGrammarParser.Condition_blockContext> conditions =  ctx.condition_block();

        boolean evaluatedBlock = false;

        for(BehaviorGrammarParser.Condition_blockContext condition : conditions) {

            ASTNode evaluated = this.visit(condition.expression());

            if(evaluated.asBoolean()) {
                evaluatedBlock = true;
                // evaluate this block whose expr==true
                this.visit(condition.block());
                break;
            }
        }

        if(!evaluatedBlock && ctx.block() != null) {
            // evaluate the else-stat_block (if present == not null)
            this.visit(ctx.block());
        }

        return ASTNode.VOID;
    }

    @Override
    public ASTNode visitLog(BehaviorGrammarParser.LogContext  ctx) {
        ASTNode value = this.visit(ctx.expression());
        System.out.println(value);
        return value;
    }    
    
    @Override 
    public ASTNode visitUnaryMinusExpr(BehaviorGrammarParser.UnaryMinusExprContext ctx) 
    { 
    	ASTNode value = this.visit(ctx.expression());
        return new ASTNode(-value.asDouble());
    }
    
    @Override 
    public ASTNode visitNotExpr(BehaviorGrammarParser.NotExprContext ctx) 
    { 
    	 ASTNode value = this.visit(ctx.expression());
    	 
    	 if (value.isBoolean()){
    		 return new ASTNode(!value.asBoolean());
    	 } else {
    		 throw new RuntimeException("Negation can only be performed against boolean values");
    	 }
    }

	@Override 
	public ASTNode visitDate(BehaviorGrammarParser.DateContext ctx) 
	{ 
		String dateStr = (ctx.DATE()).getText();
		try {
			
			dateStr = dateStr.substring(dateStr.indexOf("'"), dateStr.lastIndexOf("'"));
			DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MMM-dd");
			LocalDate dateObject = LocalDate.parse(dateStr, format);
			return new ASTNode(dateObject);
			
	     } catch (DateTimeParseException e) {
	    	 throw new RuntimeException("incorrect date: " + dateStr);
	     } 
	}

	@Override 
	public ASTNode visitTime(BehaviorGrammarParser.TimeContext ctx) 
	{ 
		String timeStr = (ctx.TIME()).getText();
		try {
			timeStr = timeStr.substring(timeStr.indexOf("'"), timeStr.lastIndexOf("'"));
			DateTimeFormatter format = DateTimeFormatter.ofPattern("H:m:s.n");
			LocalTime timeObject = LocalTime.parse(timeStr, format);
			return new ASTNode(timeObject);
			
	     } catch (DateTimeParseException e) {
	    	 throw new RuntimeException("incorrect time: " + timeStr);
	     } 
	}

	@Override 
	public ASTNode visitDatetime(BehaviorGrammarParser.DatetimeContext ctx) 
	{ 
		String datetimeStr = (ctx.DATETIME()).getText();
		try {
			
			datetimeStr = datetimeStr.substring(datetimeStr.indexOf("'"), datetimeStr.lastIndexOf("'"));
			DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MMM-dd H:m:s.n");
			LocalDateTime datetimeObject = LocalDateTime.parse(datetimeStr,format);
			return new ASTNode(datetimeObject);
			
	     } catch (DateTimeParseException e) {
	    	throw new RuntimeException("incorrect datetime: " + datetimeStr);
	     } 
	}
    
	public ASTNode visitProgramparameters(BehaviorGrammarParser.ProgramparameterContext ctx) 
	{ 
		String id = ctx.getText();
		System.out.println("Visit Program Parameter:" + id);
				
        ASTNode value = globals.get(id);
        if(value == null) {
            throw new RuntimeException("no such parameter: " + id);
        }
        
        return value;
	}
    
	public ASTNode visitCount_over_time(BehaviorGrammarParser.Count_over_timeContext ctx) 
	{ 
		// Obtain parameters given.
		String attributeId  = ctx.ID().getText();
		String timeUnit = ctx.TIMEUNIT().getText();
		int range = Integer.valueOf(ctx.range.getText());
				
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime from = now;
		
		if (timeUnit.compareTo("SECOND") == 0){
			from = now.minusSeconds(range);
		}
			
		else if (timeUnit.compareTo("MINUTE") == 0){
			from = now.minusMinutes(range);
		}
			
		else if (timeUnit.compareTo("HOUR") == 0){
			from = now.minusHours(range);
		}
		
		// Call the facade to get the attribute value during the interval. 
		List<AttributeValue> values = facade.getByIntervalByAttributeName(attributeId, from, now);
		return new ASTNode(new Integer(values.size()));
		
	}
	
	public ASTNode visitMax_over_time(BehaviorGrammarParser.Max_over_timeContext ctx) 
	{ 
		// Obtain parameters given.
		String attributeId  = ctx.ID().getText();
		String timeUnit = ctx.TIMEUNIT().getText();
		int range = Integer.valueOf(ctx.range.getText());
				
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime from = now;
		
		if (timeUnit.compareTo("SECOND") == 0){
			from = now.minusSeconds(range);
		}
			
		else if (timeUnit.compareTo("MINUTE") == 0){
			from = now.minusMinutes(range);
		}
			
		else if (timeUnit.compareTo("HOUR") == 0){
			from = now.minusHours(range);
		}
		
		// Call the facade to get the attribute value during the interval. 
		List<AttributeValue> values = facade.getByIntervalByAttributeName(attributeId, from, now);
		
		
		Object maxValue = null;
		for (int i = 0; i < values.size(); i++) {
			AttributeValue value = values.get(i);
			AttributeType type = value.getAttribute().getType();
			
			switch (type)
			{
			case DOUBLE:
				Double doubleValue = (Double) value.getValue();
				if (maxValue == null){
					maxValue = doubleValue;
				}
				else {
					if ((Double)maxValue < doubleValue){
						maxValue = doubleValue;
					}
				}
				break;

			case INT:
				Integer integerValue = (Integer) value.getValue();
				if (maxValue == null){
					maxValue = integerValue;
				}
				else {
					if ((Integer)maxValue < integerValue){
						maxValue = integerValue;
					}
				}
				break;

			case STRING:
				String stringValue = (String) value.getValue();
				if (maxValue == null){
					maxValue = stringValue;
				}
				else {
					if ( ((String)maxValue).compareTo(stringValue) < 0 ){
						maxValue = stringValue;
					}
				}
				break;

			case DATETIME:
				LocalDateTime dtTimeValue = (LocalDateTime) value.getValue();
				if (maxValue == null){
					maxValue = dtTimeValue;
				}
				else {
					if ( ((LocalDateTime)maxValue).compareTo(dtTimeValue) < 0 ){
						maxValue = dtTimeValue;
					}
				}
				break;

			case DATE:
				LocalDate dtValue = (LocalDate) value.getValue();
				if (maxValue == null){
					maxValue = dtValue;
				}
				else {
					if ( ((LocalDate)maxValue).compareTo(dtValue) < 0 ){
						maxValue = dtValue;
					}
				}
				break;

			case TIME:
				LocalTime timeValue = (LocalTime) value.getValue();
				if (maxValue == null){
					maxValue = timeValue;
				}
				else {
					if ( ((LocalTime)maxValue).compareTo(timeValue) < 0 ){
						maxValue = timeValue;
					}
				}
				break;
			
			case VOID:
				throw new RuntimeException("The Void type is not comparable with the operator maximum");
			
			case BOOLEAN:
				throw new RuntimeException("The Boolean type is not comparable with the operator maximum");
			
			default:
				throw new RuntimeException("unknown type: " + type.name());
			}
			
		}
		
		return new ASTNode(maxValue);
	}
	
	/** Return scope holding id's value; current func space or global. */
    public MemorySpace getSpaceWithSymbol(String id) 
    {
        if (stack.size()>0 && stack.peek().get(id)!=null) { // in top stack?
            return stack.peek();
        }
        if ( globals.get(id)!=null ) return globals;        // in globals?
        return null;                                        // nowhere
    }

	public MemorySpace getGlobalSpace(){
		return globals;
	}

	
}
