package com.advicetec.language.transformation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.SystemConstants;
import com.advicetec.core.AttributeValue;
import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.TransformationGrammarParser;
import com.advicetec.language.TransformationGrammarBaseVisitor;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.AggregateSymbol;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.DisplaySymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.ImportSymbol;
import com.advicetec.language.ast.MemorySpace;
import com.advicetec.language.ast.ReturnValue;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.StateSymbol;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.ast.TransformationSpace;
import com.advicetec.language.ast.TransformationSymbol;
import com.advicetec.language.ast.VariableSymbol;
import com.advicetec.measuredentitity.MeasuredEntityFacade;
import com.advicetec.measuredentitity.MeasuringState;

/**
 * This class corresponds to the interpreter for the transformation language. 
 * 
 * The interpreter first makes a definition phase. In this phase all the symbols are defined in their corresponding scopes. 
 * Afterwards, it performs the interpretation phase where the values for the symbols are calculated.  
 *   
 * @author Andres Marentes
 *
 */
public class Interpreter extends TransformationGrammarBaseVisitor<ASTNode>
{
 	
	static Logger logger = LogManager.getLogger(Interpreter.class.getName()); 
	
	public InterpreterListener listener = // default response to messages
	        new InterpreterListener() {
	            public void info(String msg) { logger.info("info:" +  msg); }
	            public void error(String msg) { logger.info("error:" + msg); }
	            public void error(String msg, Exception e) 
	            {
	                error(msg); e.printStackTrace(System.err);
	            }
	            public void error(String msg, Token t) 
	            {
	                error("line "+t.getLine()+": "+msg);
	            }
	        };
	
	/**
	* Global scope is filled by the parser
	*/
	GlobalScope globalScope;
	
	/**
	 * The definition for the rest of scopes
	 */
	ParseTreeProperty<Scope> scopes;
	
	/**
	 * Reference to the current scope.
	 */
	Scope currentScope;

	/**
	 * Measured entity facade defining the context for behavior execution.   
	 */
	MeasuredEntityFacade facade;
	
	/**
	 * Global memory space 
	 */
	MemorySpace globals;

	/**
	 * Current memory space
	 */
	MemorySpace currentSpace;

	/**
	 * Stack of functions that have been called at any time. 
	 */
	Stack<TransformationSpace> stack; // call stack
	
	/**
	 * Used to compare floating point numbers
	 */
    public static final double SMALL_VALUE = 0.00000000001;

	/**
	 * Constructor for the transformation interpreter
	 * 
	 * @param _globalScope  Global scope where the symbols are defined
	 * @param _globals    	Global memory space where values are going to be stored.
	 * @param scopes		Scopes defined through the whole execution
	 * @param facade		Measure entity facade where the transformation is being executed.
	 */
	public Interpreter(GlobalScope _globalScope, MemorySpace _globals, ParseTreeProperty<Scope> scopes, MeasuredEntityFacade facade)
	{
		// Variable for symbol definition.
		this.globalScope = _globalScope;
		this.globals = _globals;
		this.scopes = scopes;
		this.currentScope = _globalScope;
		this.facade = facade;
		
		// For memory evaluation
		stack = new Stack<TransformationSpace>(); // call stack
		
		logger.debug("Interpreter Constructor");
	}

	/**
	 * Interpret and execute the program.
	 * 
	 * It does:
	 * 		Loop and interpret the main context. 
	 */
	@Override
	public ASTNode visitProgram(TransformationGrammarParser.ProgramContext ctx) 
	{ 
		logger.debug("visitProgram:" );
		return this.visit(ctx.main()); 
		
	}
	
	/**
	 * Interpret and execute the main function.
	 * 
	 * It does:
	 * 		Loop and interpret all lines of code
	 */
	public ASTNode visitMain(TransformationGrammarParser.MainContext ctx) 
	{ 
		logger.debug("visitMain:" );
		
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

        TransformationSpace fspace = new TransformationSpace((TransformationSymbol) fs);
        MemorySpace saveSpace = currentSpace;
        Scope saveScope = currentScope; 
        currentSpace = fspace;

        // update the current scope to the one defined in the function. 
        currentScope = (TransformationSymbol) fs;

        // Counts the number of parameters included.
        int argCount = 0;
        TransformationGrammarParser.ProgramparametersContext listParams = ctx.programparameters();        
        if (listParams != null)
        {
        	for ( TransformationGrammarParser.ProgramparameterContext expres : listParams.programparameter())
        		argCount++;
        }
        
        logger.debug("num params:" + argCount);

        // check for argument compatibility
        if ( argCount==0 )
        {
        	if (
        		 (((TransformationSymbol)fs).getMembers() !=null) && 
        		 (((TransformationSymbol)fs).getMembers().size() !=0) )
        	{
        		throw new RuntimeException("function " + fs.getName() + " arguments required but not given");
        	}
        }
        
        if (argCount > 0)
        {
        	if ( ((TransformationSymbol)fs).getMembers()==null ){
        		throw new RuntimeException("function " + fs.getName() + " arguments not required and provided");
        	}
        	else if (((TransformationSymbol)fs).getMembers().size()!=argCount){
        		throw new RuntimeException("function " + fs.getName() + " wrong number of parameters");
        	}
        }
        
        if (listParams != null)
        {
	        int i = 0; // define args according to order in formalArgs
	        for (Symbol argS : ((TransformationSymbol)fs).getMembers().values()) {
	            VariableSymbol arg = (VariableSymbol)argS;
	            ASTNode argValue = this.visit(listParams.programparameter(i)); 
	            fspace.put(arg.getName(), argValue);
	            i++;
	        }
        }
        
        ASTNode result = null;
        stack.push(fspace);        // PUSH new arg, local scope
        this.visit( ((TransformationSymbol)fs).block ); 

        logger.debug("ending program:" + currentSpace.getkeys());
                
        stack.pop();               // POP arg, locals
        currentSpace = saveSpace;
                
        // goes up in the current scope.
        currentScope = saveScope;        
        return result;

	}
	
	/**
	 * Interpret a variable declaration.
	 * 
	 * It does:
	 * 		Look for the variable in the symbol table
	 * 		Assign a value to the variable and store the value in the current memory space
	 */
	@Override 
	public ASTNode visitVar_dec(TransformationGrammarParser.Var_decContext ctx) 
	{ 
		String id = ctx.ID().getText();
		Symbol s = currentScope.resolve(id);
		logger.debug("visitVar_dec" + id);
		
		// the declaration includes an assignment
		if (ctx.ASG() != null)
		{
	        ASTNode value = this.visit(ctx.expression());

			if (value == null)
				value = initializeSymbol(s);

	        VerifyAssign(s, value);
	        currentSpace.put(id, castAssign(s, value));         // store
	        return value;
		}
		else
		{
	        currentSpace.put(id, new ASTNode(new Object()));         // store
			return ASTNode.VOID;	
		}
	}
	
	/**
	 * Interpret a unit of measure declaration.
	 * 
	 * It does:
	 * 		Look for the unit of measure in the symbol table
	 * 		Assign a void value in the global space. 
	 */
	public ASTNode visitUnit_dec(TransformationGrammarParser.Unit_decContext ctx) 
	{ 
		logger.debug("visit Unit dec");
		
		String id = ctx.ID().getText();
		getGlobalSpace().put(id, new ASTNode(new Object()));         // store
		return ASTNode.VOID;	
	}

	/**
	 * Interpret an attribute declaration.
	 * 
	 * It does:
	 * 		Look for the attribute symbol in the symbol table
	 * 		Assign a value in the global space. It checks that the type is consistent. 
	 */
	public ASTNode visitAtrib_dec(TransformationGrammarParser.Atrib_decContext ctx) 
	{ 
		String id = ctx.id1.getText();
		
		logger.debug("visitAtrib_dec:" + id );
		
        getGlobalSpace().put(id, new ASTNode(new Object()));         // store with an initial value
        Symbol symbol = currentScope.resolve(id);
        if (symbol instanceof AttributeSymbol){
	        AttributeSymbol toAssign = (AttributeSymbol) currentScope.resolve(id);
			// the declaration includes an assignment
			if (ctx.ASG() != null){
				return AssignAttribute(toAssign, ctx);
			}
			else {
				ASTNode node = initializeSymbol(toAssign);
				getGlobalSpace().put(id, node);
				return node;
			}
		} else {
			String error = "The symbol to assign:" + id + " is of type:" + symbol.getClass().getName() + " we are expecting an AttributeSymbol";  
			logger.error( error );
			throw new RuntimeException( error );
		}			
	}	
	
	/**
	 * Interpret an assign attribute declaration.
	 * 
	 * It does:
	 * 		Look for the attribute symbol in the symbol table
	 * 		Assign a value in the global space. It checks that the type is consistent. 
	 */
	public ASTNode AssignAttribute(AttributeSymbol toAssign, TransformationGrammarParser.Atrib_decContext ctx)
	{
		
		// It verifies whether the expression correspond to another attribute. In such 
		// case, it checks whether or not both attributes manage the same units.
		
		
		String id2 = ctx.expression().getText();
		
		logger.debug("AssignAttribute:" + id2 );
		
		Symbol s = currentScope.resolve(id2);
		if (s instanceof AttributeSymbol )
		{
			AttributeSymbol s1 = (AttributeSymbol) s;
			
			logger.debug("s1 unit:" + s1.getUnitOfMeasure() + "sysattr : " + toAssign.getUnitOfMeasure());
			
			if (((s1.getUnitOfMeasure() == null) && (toAssign.getUnitOfMeasure() == null))
					 || (s1.getUnitOfMeasure().equals(toAssign.getUnitOfMeasure())) )
			{

		        ASTNode value = this.visit(ctx.expression());

		        if (value == null)
		        	value = initializeSymbol(toAssign);
		        
		        MemorySpace space = null;
		        
			    space = getSpaceWithSymbol(toAssign.getName());
			    if ( space==null ){ 
			    	space = getGlobalSpace(); // create in global space
			    }
		        
		        VerifyAssign(toAssign, value);
		        space.put(toAssign.getName(), castAssign(toAssign, value));         // store
		        			        
		        return value;						
			
			}
			else 
			{
				String error = "cannot assign to attribute:" + toAssign.getName() + 
						 " which is in unit:" + toAssign.getUnitOfMeasure() + 
						  " from attribute:" + s1.getName() + " in unit of measure: " + s1.getUnitOfMeasure();
				
				logger.error(error);
				// TODO: implement conversion rates.
				throw new RuntimeException( error );
			}
		}
		else
		{
		    ASTNode value = this.visit(ctx.expression());
		    
		    if (value == null)
		    	value = initializeSymbol(toAssign);
		    
	        MemorySpace space = null;
	        
	        space = getSpaceWithSymbol(toAssign.getName());
	        if ( space==null ){ 
	        	space = getGlobalSpace(); // create in current space
	        }
	        
	        VerifyAssign(toAssign, value);
	        space.put(toAssign.getName(), castAssign(toAssign, value));         // store

		    return value;					
		}
		
	}

	/**
	 * Interpret the status.
	 * 
	 * It does:
	 * 		Look for the status symbol in the symbol table
	 * 		Get the current value in the status from the facade.
	 * 		Returns the value as an ASTNode 
	 */
	public ASTNode visitStatus(TransformationGrammarParser.StatusContext ctx) 
	{ 
		String attributeId = ctx.ID().getText(); 

		AttributeValue value = null;
		
		// A possibility is that the attribute value comes from those assigned to the measure entity
		value = (AttributeValue) facade.getNewestByAttributeName(attributeId);
		
		// If the attribute is not defined as an attribute in the measured entity, then it looks for the attribute 
		// in the executed object being processed.
		if (value == null) {
			value = (AttributeValue) ((MeasuredEntityFacade) facade).getExecutedObjectAttribute(attributeId);
		}
		
		if (value == null) {
			return null;
		} else { 
			return new ASTNode(value.getValue());
		}
				
	}
	
	/**
	 * Interpret the measured entity state.
	 * 
	 * It does:
	 * 		Look for the state symbol in the symbol table
	 * 		Get the current state from the facade
	 * 		According with the current state returns the corresponding ASTNode 
	 */
	public ASTNode visitState(TransformationGrammarParser.StateContext ctx) 
	{ 
		String id = "state"; 
		
		MemorySpace space = null;
		ASTNode node = null;

		space = getSpaceWithSymbol(id);
		if ( space==null ){ 
			MeasuringState state = facade.getCurrentState();
			node = new ASTNode(state);
			
		} else {
			node = space.get(id);
		}

		return node;
		 
	}

	/**
	 * Interpret the state assignment, In this statement the measured entity takes another state. 
	 * 
	 * It does:
	 * 		Look for the state symbol in the symbol table
	 * 		Get the state from the memory space
	 * 		Assign the new state 
	 */
	public ASTNode visitState_assign(TransformationGrammarParser.State_assignContext ctx) 
	{ 
		String id = "state";
		// Bring the symbol from the global scope 
		Symbol symbol = currentScope.resolve(id);
		
		Integer value = null;
		ASTNode node = null;

		MemorySpace space = null;

		space = getSpaceWithSymbol(symbol.getName());
		if ( space==null ){ 
			space = getGlobalSpace(); // create in current space
		}
		
		// Verify the symbol as of type StateSymbol
		if (symbol instanceof StateSymbol ){
			
			String newState = ctx.POSSIBLE_STATES().getText();
			
			if ( newState.compareTo("operative") == 0 ){
				node = new ASTNode(MeasuringState.OPERATING); 
			} else if ( newState.compareTo("sched_down") == 0 ){
				node = new ASTNode(MeasuringState.SCHEDULEDOWN); 
			} else if ( newState.compareTo("unsched_down") == 0 ){
				node = new ASTNode(MeasuringState.UNSCHEDULEDOWN); 
			} else if ( newState.compareTo("initializing") == 0 ) {
				node = new ASTNode(MeasuringState.INITIALIZING);
			} else {
				node = new ASTNode(MeasuringState.UNDEFINED); 
			}

			space.put(symbol.getName(), node);         // store
			return node;
			
		} else {
			String error = "the state is not registered in the status as type state symbol";
			logger.error(error);
			throw new RuntimeException( error );			
		}
				 
	}
	
	/**
	 * auxiliary Method to initialize a symbol according with its type 
	 * 
	 * @param Symbol to be initialized
	 * @return The ASTNode to initialize the symbol.
	 */
	public ASTNode initializeSymbol(Symbol symbol)
	{
		
		logger.debug("initializeSymbol");
		
		if (symbol == null){
			String error = "the Symbol given to be initialized is null";
			logger.error(error);			
			throw new RuntimeException( error );
		}
		
		switch (symbol.getType())
		{
		
		case tINT:
			return new ASTNode(new Integer(0));
			
		case tDATETIME:
			return new ASTNode(LocalDateTime.MIN); 
			
		case tFLOAT:
			return new ASTNode(new Double(0.0));
			
		case tSTRING:
			return new ASTNode(new String(""));
			
		case tBOOL:
			return new ASTNode(new Boolean(false));
			
		case tDATE:
			return new ASTNode(LocalDate.MIN);
			
		case tTIME:
			return new ASTNode(LocalTime.MIN);
		
		default:
			String error = "The attribute: " + symbol.getName() + " has a type not supported";
			logger.error(error);
			throw new RuntimeException(error);
		}

	}
	
	/**
	 * Interpret the assign, we can assign a variable or an attribute. 
	 * Other symbols should generate an error.
	 *  
	 * This method verifies the type of the value being assigned 
	 * and the type of the variable or attribute.
	 */
	@Override 
	public ASTNode visitAssign(TransformationGrammarParser.AssignContext ctx) 
	{ 
		
		String id = ctx.ID().getText();
		
		logger.debug("Visit Assign:" + id );
		
        ASTNode value = this.visit(ctx.expression());
                
        Symbol symbol = currentScope.resolve(id) ;
        
        if (value == null)
        	value = initializeSymbol(symbol);

        
        MemorySpace space = null;
        
        if (symbol instanceof VariableSymbol) 
        {
	        space = getSpaceWithSymbol(id);
	        if ( space==null ){ 
	        	space = currentSpace; // create in current space
	        }
        } else if (symbol instanceof AttributeSymbol) {
	        space = getSpaceWithSymbol(id);
	        if ( space==null ){ 
	        	space = getGlobalSpace(); // create in global space
	        }
		} else {
			logger.debug("It is being assigned to a non variable or attribute - symbol:" + symbol.getName() );
			throw new RuntimeException("It is being assigned to a non variable or attribute - symbol:" + symbol.getName());
        }
        
        VerifyAssign(symbol, value);
        space.put(id, value);         // store
        
        return value;
	}
	
	/**  
	 * This method verifies the type of the value being assigned 
	 * and the type of the variable or attribute.
	 * 
	 * If discrepancies exist then it triggers a RunTimeException.
	 */
	public void VerifyAssign(Symbol symbol, ASTNode value)
	{
					
		switch (symbol.getType())
		{
		case tINT:
			if (!(value.isInteger())){
				throw new RuntimeException("The value given is not an integer type");
			}
			break;
		case tDATETIME:
			if (!(value.isDateTime())){
				throw new RuntimeException("the value given is not a datetime type");
			}
			break;
		case tFLOAT:
			if (!(value.isDouble() || value.isInteger())) {
				throw new RuntimeException("The value given is not a float type");
			}
			break;
		case tSTRING:
			if (!(value.isString())){
				throw new RuntimeException("The value given is not a string type");
			}
			break;
		case tBOOL:
			if (!(value.isBoolean())){
				throw new RuntimeException("The value given is not a boolean type");
			}
			break;
		case tDATE:
			if (!(value.isDate())){
				throw new RuntimeException("The value given is not a date type");
			}
			break;
		case tTIME:
			if (!(value.isTime())){
				throw new RuntimeException("The value given is not a time type");
			}
			break;
		case tVOID:
			throw new RuntimeException("The value given is of type void");

		case tINVALID:
			throw new RuntimeException("The value given is of type invalid");
		}	
	}

	/**  
	 * This method converts from a symbol's type to another type.
	 * 
	 * If the conversion cannot be performed, then it triggers a RunTimeException.
	 */
	public ASTNode castAssign(Symbol symbol, ASTNode value)
	{

		switch (symbol.getType())
		{
		case tINT:
            if (value.isInteger()) {
            	return value;
            } else if (value.isString()) { 
            	try {
            		return new ASTNode(Integer.valueOf(value.asString()));
            	} catch (NumberFormatException e){
            		logger.error("Number exception:" + e.getMessage());
            		throw new RuntimeException(e.getMessage());
            	}
            } else{
            	throw new RuntimeException("The value given is of invalid type");
            }
		
		case tDATETIME:
		case tBOOL:
		case tDATE:
		case tTIME:
			return value;

		case tSTRING:
            if (value.isBoolean()) {
            	return new ASTNode(String.valueOf(value.asBoolean()));
            } else if (value.isDate()) {
            	
            	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SystemConstants.DATE_FORMAT);
            	String formatDate = value.asDate().format(formatter);
            	return new ASTNode(formatDate);
            	
            } else if (value.isDateTime()) {
            	
            	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SystemConstants.DATETIME_FORMAT);
            	String formatDateTime = value.asDateTime().format(formatter);
            	return new ASTNode(formatDateTime);
            	
            } else if (value.isTime()) {

            	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SystemConstants.TIME_FORMAT);
            	String formatTime = value.asTime().format(formatter);
            	return new ASTNode(formatTime);

            } else if (value.isDouble()) {
            	return new ASTNode(String.valueOf(value.asDouble()));
            } else if (value.isInteger()) {
            	return new ASTNode(String.valueOf(value.asInterger()));
            } else if (value.isString()) {
            	return value;
            } else {
            	throw new RuntimeException("The value given is of invalid type");
            }
			
		case tFLOAT:
			if (value.isDouble()) {
				return value;
			}
			else if (value.isInteger()) {
				return new ASTNode(new Double(value.asInterger()));
			} else if (value.isString()) {
				try {
					return new ASTNode(Double.valueOf(value.asString()));
				} catch (NumberFormatException e){
					logger.error("Number exception:" + e.getMessage());
					throw new RuntimeException(e.getMessage());
				}
			}
		case tVOID:
			throw new RuntimeException("The value given is of type void");

		case tINVALID:
		default:
			throw new RuntimeException("The value given is of type invalid");
		}	
	}
		
	/**
	 * Visits a variable. It looks for the value of a variable registered in the current memory space 
	 */
	@Override 
	public ASTNode visitVar(TransformationGrammarParser.VarContext ctx) 
	{ 
				
		String id = ctx.getText();
		logger.debug("visitVar:" + id);
				
		MemorySpace space = getSpaceWithSymbol(id); 
        ASTNode value = space.get(id);
        if(value == null) {
            throw new RuntimeException("no such variable: " + id);
        }
        
        return value;
	}

	/**
	 * Visits a round symbol. Rounds the value according to the rules specified.  
	 */
	public ASTNode visitRound(TransformationGrammarParser.RoundContext ctx) 
	{ 
		ASTNode value = this.visit(ctx.expression());
		
		if (value == null){
			throw new RuntimeException("The operator is null");
		}

		long numdecimals = Integer.valueOf(ctx.INT1().getText());
		
		if (value.isDouble() || value.isInteger()){
			if (value.isDouble()){
				
				Double valueD = new Double(value.asDouble());
				valueD = valueD * Math.pow(10, numdecimals);
				long tmp = Math.round(valueD);
				valueD = ((double)tmp / Math.pow(10, numdecimals));
				return new ASTNode(valueD);
				
			} else {
				Integer ret = new Integer(value.asInterger());
				return new ASTNode(ret);
			}
		} else {
			throw new RuntimeException("Only numbers are posible to round");
		}
	}
		
	/**
	 * Visits a return symbol. To return a value from a function we throws it value, so the caller
	 * can catch it as an exception.  
	 */
	@Override 
	public ASTNode visitRef_return(TransformationGrammarParser.Ref_returnContext ctx) 
	{ 
    	ReturnValue sharedReturnValue = new ReturnValue();
    	sharedReturnValue.value = this.visit(ctx.expression());
        throw sharedReturnValue;
	}
	
	@Override 
	public ASTNode visitRef_startwith(TransformationGrammarParser.Ref_startwithContext ctx) 
	{ 
		logger.debug("visit ref start with");
		return this.visit(ctx.startwith());
	}
	
	/**
	 * Visit a text representing an integer
	 * 
	 * @return the ASTNode representing the integer.
	 */
	@Override 
	public ASTNode visitInteger(TransformationGrammarParser.IntegerContext ctx) 
	{ 
		logger.debug("visitInteger");
		return new ASTNode(Integer.valueOf(ctx.getText()));
	}

	/**
	 * Visit a text representing an year
	 * 
	 * @return the ASTNode representing the year.
	 */
	@Override
	public ASTNode visitYear(TransformationGrammarParser.YearContext ctx) 
	{ 
		logger.debug("visitYear");
		return new ASTNode(Integer.valueOf(ctx.getText()));
	}
	
	/**
	 * Visit a text representing a digit
	 * 
	 * @return the ASTNode representing the digit.
	 */
	public ASTNode visitDigit(TransformationGrammarParser.DigitContext ctx) 
	{ 
		logger.debug("visitDigit");
		return new ASTNode(Integer.valueOf(ctx.getText()));
	}
	
	/**
	 * Visit a text representing a float
	 * 
	 * @return the ASTNode representing the float.
	 */
	public ASTNode visitFloat(TransformationGrammarParser.FloatContext ctx) 
	{ 
		logger.debug("visitFloat");
		return new ASTNode(Double.valueOf(ctx.getText()));
	}
	
	/**
	 * Visit a text representing a boolean
	 * 
	 * @return the ASTNode representing the boolean.
	 */
	@Override 
	public ASTNode visitBoolean(TransformationGrammarParser.BooleanContext ctx) 
	{ 
		logger.debug("visitBoolean");
		
		return new ASTNode(Boolean.valueOf(ctx.getText()));
	}

	/**
	 * Visit a text representing a string 
	 * 
	 * @return the ASTNode representing the string.
	 */
	@Override 
	public ASTNode visitStr(TransformationGrammarParser.StrContext ctx) 
	{ 
		logger.debug("visitStr");
		
		String str = ctx.getText();
	     
		// strip quotes
	    str = str.substring(1, str.length() - 1).replace("\"\"", "\"");
	    
	    logger.debug("visitStr" + "end value:" + str);
	    return new ASTNode(str);
	}
	
	/**
	 * Visit a parentheses expression
	 * 
	 * @return the ASTNode representing the evaluation of the expression inside the parentheses.
	 */
	@Override 
	public ASTNode visitParens(TransformationGrammarParser.ParensContext ctx) 
	{ 
		logger.debug("visitParens text:" + ctx.expression().getText());
		
		return this.visit(ctx.expression()); 
	}
	
	/**
	 * Visits a multiplication, division, or module expression
	 * 
	 * @return the ASTNode representing the evaluation of the expression
	 */
	@Override 
	public ASTNode visitMult(TransformationGrammarParser.MultContext ctx) 
	{ 
		logger.debug("visitMult");
		
		ASTNode left = this.visit(ctx.expression(0));
        ASTNode right = this.visit(ctx.expression(1));
		if (left == null){
			throw new RuntimeException("The left operator is null");
		}

		if (right == null){
			throw new RuntimeException("The right operator is null");
		}

        switch (ctx.op.getType()) {
            case TransformationGrammarParser.MULT:
            	if (left.isInteger() && right.isInteger()){
            		return new ASTNode((Integer)(left.asInterger() * right.asInterger()));
            	} else if (left.isDouble() && right.isInteger()){
            		return new ASTNode((Double)(left.asDouble() * right.asInterger()));
            	} else if (left.isInteger() && right.isDouble()){
            		return new ASTNode((Double)(left.asInterger() * right.asDouble()));
            	} else if (left.isDouble() && right.isDouble()){
            		return new ASTNode((Double)(left.asDouble() * right.asDouble()));
            	} else {
            		throw new RuntimeException("operators are not numbers - left:" + left.getObjectClassName() + " right:" + right.getObjectClassName() );
            	}
            case TransformationGrammarParser.DIVI:
            	if (left.isInteger() && right.isInteger()){
            		return new ASTNode((Double)((double)left.asInterger() / (double) right.asInterger()));
            	} else if (left.isDouble() && right.isInteger()){
            		return new ASTNode((Double)(left.asDouble() / (double) right.asInterger()));
            	} else if (left.isInteger() && right.isDouble()){
            		return new ASTNode((Double)(left.asInterger() / (double) right.asDouble()));
            	} else if (left.isDouble() && right.isDouble()){
            		return new ASTNode((Double)(left.asDouble() / right.asDouble()));
            	} else {
            		throw new RuntimeException("operators are not numbers - left:" + left.getObjectClassName() + " right:" + right.getObjectClassName());
            	}
            	
            case TransformationGrammarParser.MOD:
            	if (left.isInteger() && right.isInteger()){
            		return new ASTNode((Double)((double)left.asInterger() % (double) right.asInterger()));
            	} else if (left.isDouble() && right.isInteger()){
            		return new ASTNode((Double)(left.asDouble() % (double) right.asInterger()));
            	} else if (left.isInteger() && right.isDouble()){
            		return new ASTNode((Double)(left.asInterger() % (double) right.asDouble()));
            	} else if (left.isDouble() && right.isDouble()){
            		return new ASTNode((Double)(left.asDouble() % right.asDouble()));
            	} else {
            		throw new RuntimeException("operators are not numbers - left:" + left.getObjectClassName() + " right:" + right.getObjectClassName());
            	}
            default:
                throw new RuntimeException("unknown operator: " + TransformationGrammarParser.tokenNames[ctx.op.getType()]);
        }
	}

	/**
	 * Visits an add or subtraction expression
	 * 
	 * @return the ASTNode representing the evaluation of the expression
	 */
	@Override 
	public ASTNode visitAddSub(TransformationGrammarParser.AddSubContext ctx) 
	{ 
		logger.debug("visitAddSub");
				
		ASTNode left = this.visit(ctx.expression(0));
        ASTNode right = this.visit(ctx.expression(1));
		if (left == null){
			throw new RuntimeException("The left operator is null");
		}

		if (right == null){
			throw new RuntimeException("The right operator is null");
		}

        switch (ctx.op.getType()) {
            case TransformationGrammarParser.PLUS:

            	if (left.isInteger() && right.isInteger()){
            		return new ASTNode(left.asInterger() + right.asInterger());
            	} else if (left.isDouble() && right.isInteger()){
            		return new ASTNode((Double)left.asDouble() +  right.asInterger());
            	} else if (left.isInteger() && right.isDouble()){
            		return new ASTNode((Double) (left.asInterger() + right.asDouble()));
            	} else if (left.isDouble() && right.isDouble()){
            		return new ASTNode(left.asDouble() + right.asDouble());
            	} else if (left.isString() && right.isString()){
            		new ASTNode(left.asString() + right.asString());
            		
		    	} else if (left.isString()) {
		    		
		    		if (right.isBoolean()) {
		    			return new ASTNode(left.asString() + right.asBoolean().toString());
		    		} else if (right.isDate()) {
		    			return new ASTNode(left.asString() + right.asDate().toString());
		    		} else if (right.isDouble()) {
		    			return new ASTNode(left.asString() + right.asDouble().toString());
		    		} else if (right.isInteger()) {
		    			return new ASTNode(left.asString() + right.asInterger().toString());
		    		} else if (right.isTime()) {
		    			return new ASTNode(left.asString() + right.asTime().toString());
		    		} else if (right.isVOID()) {
		    			return new ASTNode(left.asString());
		    		}
		    			 
		    	} else if (right.isString()) {

		    		if (left.isBoolean()) {
		    			return new ASTNode( left.asBoolean().toString() + right.asString() );
		    		} else if (left.isDate()) {
		    			return new ASTNode( left.asDate().toString() + right.asString());
		    		} else if (left.isDouble()) {
		    			return new ASTNode( left.asDouble().toString() + right.asString() );
		    		} else if (left.isInteger()) {
		    			return new ASTNode( left.asInterger().toString() + right.asString());
		    		} else if (left.isTime()) {
		    			return new ASTNode( left.asTime().toString() + right.asString());
		    		} else if (left.isVOID()) {
		    			return new ASTNode( left.asString());
		    		}

		    	}
            	else {
		    		throw new RuntimeException("operators are not numbers or strings");
		    	}
                        
            case TransformationGrammarParser.MINUS:
            	if (left.isInteger() && right.isInteger()){
            		return new ASTNode(left.asInterger() - right.asInterger());
            	} else if (left.isDouble() && right.isInteger()){
            		return new ASTNode((Double)left.asDouble() -  right.asInterger());
            	} else if (left.isInteger() && right.isDouble()){
            		return new ASTNode((Double) (left.asInterger() - right.asDouble()));
            	} else if (left.isDouble() && right.isDouble()){
            		return new ASTNode(left.asDouble() - right.asDouble());
            	} else {
		    		throw new RuntimeException("operators are not numbers");
		    	}
            default:
                throw new RuntimeException("unknown operator: " + TransformationGrammarParser.tokenNames[ctx.op.getType()]);
        }

	}

	/**
	 * Visits an exponent expression
	 * 
	 * @return the ASTNode representing the evaluation of the expression
	 */
	@Override 
	public ASTNode visitExpon(TransformationGrammarParser.ExponContext ctx) 
	{ 
		
		logger.debug("visitExpon");
		
		ASTNode left = this.visit(ctx.expression(0));
        ASTNode right = this.visit(ctx.expression(1));
		if (left == null){
			throw new RuntimeException("The base operator is null");
		}

		if (right == null){
			throw new RuntimeException("The exponent operator is null");
		}

    	if (left.isInteger() && right.isInteger()){
    		return new ASTNode((Double) Math.pow(left.asInterger(), right.asInterger()));
    	} else if (left.isDouble() && right.isInteger()){
    		return new ASTNode((Double) Math.pow(left.asDouble(), right.asInterger()));
    	} else if (left.isInteger() && right.isDouble()){
    		return new ASTNode((Double) Math.pow(left.asInterger(), right.asDouble()));
    	} else if (left.isDouble() && right.isDouble()){
    		return new ASTNode((Double) Math.pow(left.asDouble(), right.asDouble()));
    	} else {
    		throw new RuntimeException("operators are not numbers");
    	}        
	}
	
	/**
	 * Visits a relational operation (less than, greater than, less than equal, greater than equal ) expression
	 * 
	 * @return the ASTNode representing the evaluation of the expression
	 */
	@Override 
	public ASTNode visitRelationalExpr(TransformationGrammarParser.RelationalExprContext ctx) 
	{ 
		
		logger.debug("visitRelationalExpr");
		
		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));
		if ((left == null) && (right == null)){
			switch (ctx.op.getType()) {
			case TransformationGrammarParser.LT:
			case TransformationGrammarParser.GT:
				return new ASTNode(new Boolean(false));
			
			case TransformationGrammarParser.GTEQ:
			case TransformationGrammarParser.LTEQ:
				return new ASTNode(new Boolean(true));

			default:
				throw new RuntimeException("unknown operator: " + TransformationGrammarParser.tokenNames[ctx.op.getType()]);
			}
		}
		
		if ((left != null) && (right == null)){
			switch (ctx.op.getType()) {
			case TransformationGrammarParser.LT:
				return new ASTNode(new Boolean(false));
				
			case TransformationGrammarParser.GT:
				return new ASTNode(new Boolean(true));
			
			case TransformationGrammarParser.GTEQ:
				return new ASTNode(new Boolean(true));
				
			case TransformationGrammarParser.LTEQ:
				return new ASTNode(new Boolean(false));

			default:
				throw new RuntimeException("unknown operator: " + TransformationGrammarParser.tokenNames[ctx.op.getType()]);
			}
		}
			
		if ((left == null) && (right != null)){
			switch (ctx.op.getType()) {
			case TransformationGrammarParser.LT:
				return new ASTNode(new Boolean(true));
				
			case TransformationGrammarParser.GT:
				return new ASTNode(new Boolean(false));
			
			case TransformationGrammarParser.GTEQ:
				return new ASTNode(new Boolean(false));
				
			case TransformationGrammarParser.LTEQ:
				return new ASTNode(new Boolean(true));

			default:
				throw new RuntimeException("unknown operator: " + TransformationGrammarParser.tokenNames[ctx.op.getType()]);
			}
		}
		// Both values are different than null. 

        switch (ctx.op.getType()) {
            case TransformationGrammarParser.LT:
            	if (left.isInteger() && right.isInteger()){
            		return new ASTNode((Boolean) (left.asInterger() < right.asInterger()));
            	} else if (left.isDouble() && right.isInteger()){
            		return new ASTNode((Boolean) (left.asDouble() < right.asInterger()));
            	} else if (left.isInteger() && right.isDouble()){
            		return new ASTNode((Boolean) (left.asInterger() < right.asDouble()));
            	} else if (left.isDouble() && right.isDouble()){
            		return new ASTNode((Boolean) (left.asDouble() < right.asDouble()));
            	} else {
            		throw new RuntimeException("operators are not numbers");
            	}        

            case TransformationGrammarParser.LTEQ:
            	if (left.isInteger() && right.isInteger()){
            		return new ASTNode((Boolean) (left.asInterger() <= right.asInterger()));
            	} else if (left.isDouble() && right.isInteger()){
            		return new ASTNode((Boolean) (left.asDouble() <= right.asInterger()));
            	} else if (left.isInteger() && right.isDouble()){
            		return new ASTNode((Boolean) (left.asInterger() <= right.asDouble()));
            	} else if (left.isDouble() && right.isDouble()){
            		return new ASTNode((Boolean) (left.asDouble() <= right.asDouble()));
            	} else {
            		throw new RuntimeException("operators are not numbers");
            	}        
            case TransformationGrammarParser.GT:
            	if (left.isInteger() && right.isInteger()){
            		return new ASTNode((Boolean) (left.asInterger() > right.asInterger()));
            	} else if (left.isDouble() && right.isInteger()){
            		return new ASTNode((Boolean) (left.asDouble() > right.asInterger()));
            	} else if (left.isInteger() && right.isDouble()){
            		return new ASTNode((Boolean) (left.asInterger() > right.asDouble()));
            	} else if (left.isDouble() && right.isDouble()){
            		return new ASTNode((Boolean) (left.asDouble() > right.asDouble()));
            	} else {
            		throw new RuntimeException("operators are not numbers");
            	}        

            case TransformationGrammarParser.GTEQ:
            	if (left.isInteger() && right.isInteger()){
            		return new ASTNode((Boolean) (left.asInterger() >= right.asInterger()));
            	} else if (left.isDouble() && right.isInteger()){
            		return new ASTNode((Boolean) (left.asDouble() >= right.asInterger()));
            	} else if (left.isInteger() && right.isDouble()){
            		return new ASTNode((Boolean) (left.asInterger() >= right.asDouble()));
            	} else if (left.isDouble() && right.isDouble()){
            		return new ASTNode((Boolean) (left.asDouble() >= right.asDouble()));
            	} else {
            		throw new RuntimeException("operators are not numbers");
            	}        

            default:
                throw new RuntimeException("unknown operator: " + TransformationGrammarParser.tokenNames[ctx.op.getType()]);
        }
	}
	
	/**
	 * Visits an equals expression
	 * 
	 * @return the ASTNode representing the evaluation of the expression
	 */
	@Override 
	public ASTNode visitEqualityExpr(TransformationGrammarParser.EqualityExprContext ctx) 
	{ 
		
		logger.debug("visitEqualityExpr");
		
		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));
		if ((left == null) && (right == null)){
			return new ASTNode(new Boolean(true));
		}
		
		if ((left != null) && (right == null)){
			return new ASTNode(new Boolean(false));
		}

		if ((left == null) && (right != null)){
			return new ASTNode(new Boolean(false));
		}

        switch (ctx.op.getType()) {
            case TransformationGrammarParser.EQ:
            	if (left.isInteger() && right.isInteger()){
            		return new ASTNode((Boolean) (left.asInterger().equals(right.asInterger())));
            	} else if (left.isDouble() && right.isInteger()){
            		return new ASTNode((Boolean) (Math.abs( left.asDouble() - right.asInterger()) < SMALL_VALUE) );
            	} else if (left.isInteger() && right.isDouble()){
            		return new ASTNode((Boolean) (Math.abs( left.asInterger() - right.asDouble()) < SMALL_VALUE) );
            	} else if (left.isDouble() && right.isDouble()){
            		return new ASTNode((Boolean) (Math.abs( left.asDouble() - right.asDouble()) < SMALL_VALUE) );
            	} else if (left.isString() && right.isString()){
            		return new ASTNode((Boolean) (left.asString()).equals(right.asString()) );
		    	} else if (left.isBoolean() && right.isBoolean()){
		    		return new ASTNode((Boolean) (left.asBoolean()).equals(right.asBoolean()) );
		    	} else if (left.isDate() && right.isDate()){
		    		return new ASTNode((Boolean) (left.asDate()).equals(right.asDate()) );
		    	} else if (left.isDateTime() && right.isDateTime()){
		    		return new ASTNode((Boolean) (left.asDateTime()).equals(right.asDateTime()) );
		    	} else if (left.isTime() && right.isTime()){
		    		return new ASTNode((Boolean) (left.asTime()).equals(right.asTime()) );
		    	} else if (left.isMeasuringState() && right.isMeasuringState()) {
		    		return new ASTNode((Boolean) (left.asMeasuringState() == right.asMeasuringState()) ); 
		    	} else {
            		throw new RuntimeException("operators are not of the same type");
            	}        

            case TransformationGrammarParser.NEQ:
            	if (left.isInteger() && right.isInteger()){
            		return new ASTNode((Boolean) (left.asInterger() != right.asInterger()));
            	} else if (left.isDouble() && right.isInteger()){
            		return new ASTNode((Boolean) (Math.abs( left.asDouble() - right.asInterger()) > SMALL_VALUE) );
            	} else if (left.isInteger() && right.isDouble()){
            		return new ASTNode((Boolean) (Math.abs( left.asInterger() - right.asDouble()) > SMALL_VALUE) );
            	} else if (left.isDouble() && right.isDouble()){
            		return new ASTNode((Boolean) (Math.abs( left.asDouble() - right.asDouble()) > SMALL_VALUE) );
            	} else if (left.isString() && right.isString()){
            		return new ASTNode((Boolean) !(left.asString()).equals(right.asString()) );
		    	} else if (left.isBoolean() && right.isBoolean()){
		    		return new ASTNode((Boolean) !(left.asBoolean()).equals(right.asBoolean()) );
		    	} else if (left.isDate() && right.isDate()){
		    		return new ASTNode((Boolean) !(left.asDate()).equals(right.asDate()) );
		    	} else if (left.isDateTime() && right.isDateTime()){
		    		return new ASTNode((Boolean) !(left.asDateTime()).equals(right.asDateTime()) );
		    	} else if (left.isTime() && right.isTime()){
		    		return new ASTNode((Boolean) !(left.asTime()).equals(right.asTime()) );
		    	} else if (left.isMeasuringState() && right.isMeasuringState()) {
		    		return new ASTNode((Boolean) (left.asMeasuringState() != right.asMeasuringState()) );
		    	} else {
            		throw new RuntimeException("operators are not of the same type");
            	}        

            default:
                throw new RuntimeException("unknown operator: " + TransformationGrammarParser.tokenNames[ctx.op.getType()]);
        }
	}
	
	/**
	 * Visits an AND expression
	 * 
	 * @return the ASTNode representing the evaluation of the expression
	 */
	@Override 
	public ASTNode visitAndExpr(TransformationGrammarParser.AndExprContext ctx) 
	{ 
		logger.debug("visitAndExpr");
		
		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));

		if ((left == null) && (right == null)){
			throw new RuntimeException("operators are not of boolean type");
		}

		if ((left != null) && (right == null)){
			throw new RuntimeException("operators are not of boolean type");
		}

		if ((left == null) && (right != null)){
			throw new RuntimeException("operators are not of boolean type");
		}

    	if (left.isBoolean() && right.isBoolean()){
    		
    		Boolean ret = left.asBoolean() && right.asBoolean();
    		logger.debug("visitAndExpr ret value:" + ret.toString());
    		return new ASTNode(ret);
    	} else {
    		throw new RuntimeException("operators are not of boolean type");
    	}

	}
	
	/**
	 * Visits an OR expression
	 * 
	 * @return the ASTNode representing the evaluation of the expression
	 */
	@Override 
	public ASTNode visitOrExpr(TransformationGrammarParser.OrExprContext ctx) 
	{ 
		logger.debug("visitOrExpr");
		
		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));

		if ((left == null) && (right == null)){
			throw new RuntimeException("operators are not of boolean type");
		}

		if ((left != null) && (right == null)){
			throw new RuntimeException("operators are not of boolean type");
		}

		if ((left == null) && (right != null)){
			throw new RuntimeException("operators are not of boolean type");
		}

    	if (left.isBoolean() && right.isBoolean()){
    		return new ASTNode(left.asBoolean() || right.asBoolean());
    	} else {
    		throw new RuntimeException("operators are not of boolean type");
    	}
	}
	
	/**
	 * Visits a Token expression
	 * 
	 * @return the ASTNode representing the evaluation of the expression
	 */
	public ASTNode visitToken(TransformationGrammarParser.TokenContext ctx) 
	{ 
				
		TransformationGrammarParser.ExpressionContext stringEq1 =  ctx.ex1;
		TransformationGrammarParser.ExpressionContext numToken = ctx.ex2;
		
		ASTNode inputNode = this.visit(stringEq1);
		
		if (inputNode == null){
			throw new RuntimeException("The string given is null");
		}

		String input = inputNode.asString();

		logger.debug("visitToken:" + input );
		
		ASTNode numberToken = this.visit(numToken); 
		if (numberToken.isInteger() == false) 
		{
			throw new RuntimeException("param number_from is not valid: " + numberToken.toString());
		}
				
		Integer token = (numberToken).asInterger();

		StringTokenizer defaultTokenizer = new StringTokenizer(input, SystemConstants.TOKEN_SEP);
		int countTokens = defaultTokenizer.countTokens();
		
		if (countTokens >= token){
			int i = 0;
			while (defaultTokenizer.hasMoreTokens())
			{
			    if (i == token){
			    	return new ASTNode( defaultTokenizer.nextToken() );
			    }
			    else { 
			    	i++;
			    }
			}
		} else {
			throw new RuntimeException("The number of token requested:" + token + " is greater than the token count " + countTokens);
		}
		
		return ASTNode.VOID;
    }
	
	/**
	 * Visits a substring expression 
	 * 
	 * @return the ASTNode representing the the substring result
	 */
	public ASTNode visitSubstring(TransformationGrammarParser.SubstringContext ctx) 
	{ 
		TransformationGrammarParser.ExpressionContext stringEq1 =  ctx.ex1;
		TransformationGrammarParser.ExpressionContext numberFrom = ctx.ex2;
		TransformationGrammarParser.ExpressionContext numberTo = ctx.ex3;
		ASTNode stringFrom = this.visit(stringEq1);

		if (stringFrom == null){
			throw new RuntimeException("param String is null which is not valid");
		}
		String ret = stringFrom.asString();
		ASTNode numberFr = this.visit(numberFrom); 
		if (numberFr == null){
			throw new RuntimeException("param number_from is null which is not valid");
		}

		if (numberFr.isInteger() == false) 
		{
			throw new RuntimeException("param number_from is not valid: " + numberFr.toString());
		}
		ASTNode numberT = this.visit(numberTo);
		if (numberT == null){
			throw new RuntimeException("param number_to is null which is not valid");
		}
		
		if ( numberT.isInteger() == false) 
		{
			throw new RuntimeException("param number_to is not valid: " + numberT.toString());
		}
		
		Integer beginIndex = (numberFr).asInterger();
		Integer endIndex = (numberT).asInterger();
		ret = ret.substring(beginIndex.intValue(), endIndex.intValue());
		
		return new ASTNode(ret);
	}

	/**
	 * Visits an expression verifying if a text start with another text
	 * 
	 * @return the ASTNode representing the evaluation
	 */
	@Override
	public ASTNode visitStartwith(TransformationGrammarParser.StartwithContext ctx)
	{ 
		logger.debug("visitStartWith context");		
		
		TransformationGrammarParser.ExpressionContext stringParamCtx1 =  ctx.ex1;
		TransformationGrammarParser.ExpressionContext StringParamCtx2 = ctx.ex2;
		
		ASTNode strNode1 = this.visit(stringParamCtx1);
		ASTNode strNode2 = this.visit(StringParamCtx2);		
		if (strNode1 == null){
			throw new RuntimeException("param string from is null which is not valid");
		}

		if (strNode2 == null){
			throw new RuntimeException("param the string to compare is null which is not valid");			
		}
		String stringParam1 =  strNode1.asString();
		String stringParam2 =  strNode2.asString();
		
		logger.debug("visitStartWith - Param1:"+ stringParam1 + " Param2:" + stringParam2 );
						
		return new ASTNode( new Boolean(stringParam1.startsWith(stringParam2)));
		
    } 	
	
	/**
	 * Visits a repeat expression
	 * 
	 * 		Verifies that the import is defined in the symbol table
	 * 		Defines a timer in the global symbol table
	 * 
	 * @return the ASTNode VOID
	 */
	public ASTNode visitRepeat(TransformationGrammarParser.RepeatContext ctx) 
	{ 
		String name = ctx.ID().getText();
		Symbol symbol =  currentScope.resolve(name);

				
		logger.debug("Symbol name:" + name );
		
		if (symbol instanceof TimerSymbol){
			TimerSymbol timer = (TimerSymbol) symbol;
			GlobalScope global = getGlobalScope();

			Symbol impSymbol = currentScope.getParentScope().resolve(name);
			if (impSymbol instanceof ImportSymbol)
			{
				ArrayList<String> names = ((ImportSymbol) impSymbol).getLongName();
				for (String n : names){
					((TimerSymbol) timer).addId(n);
				}
				
			} else {
				String error = "no such Import Symbol: " + name;
				logger.error(error);
				throw new RuntimeException(error);
			}
			
			logger.debug("number of dotted names in the import:" + timer.getCompleteName().size());
			
			global.define(timer);
			
			logger.debug("Symbol name:" + name + " defined as timer in the global scope");
		} else {
			
			logger.error("Symbol name:" + name + " is not a Timer symbol");
			throw new RuntimeException("The symbol is not a Timer symbol");
		}
		
		return ASTNode.VOID;
	}

	/**
	 * Visits a timer expression
	 * 
	 * 		Verifies that the import is defined in the symbol table
	 * 		Defines a timer in the global symbol table
	 * 
	 * @return the ASTNode VOID
	 */
	public ASTNode visitTimer(TransformationGrammarParser.TimerContext ctx) 
	{ 
		String name = ctx.ID().getText();
		Symbol symbol =  currentScope.resolve(name);
		
		if (symbol instanceof TimerSymbol){
			TimerSymbol timer = (TimerSymbol) symbol;
			GlobalScope global = getGlobalScope();

			Symbol impSymbol = currentScope.getParentScope().resolve(name);
			if (impSymbol instanceof ImportSymbol)
			{
				ArrayList<String> names = ((ImportSymbol) impSymbol).getLongName();
				for (String n : names){
					((TimerSymbol) timer).addId(n);
				}
				
			} else {
				String error = "no such Import Symbol: " + name;
				logger.error(error);
				throw new RuntimeException(error);
			}
			
			logger.debug("number of dotted names in the import:" + timer.getCompleteName().size());
			
			global.define(timer);
			
		} else {
			throw new RuntimeException("The symbol is not a Timer symbol");
		}
		
		return ASTNode.VOID;
	}
	
	/**
	 * Visits a aggregate expression
	 * 
	 * 		Verifies that the import is defined in the symbol table
	 * 		Defines a aggregate in the global symbol table
	 * 
	 * @return the ASTNode VOID
	 */
	public ASTNode visitSched_aggregate(TransformationGrammarParser.Sched_aggregateContext ctx) 
	{ 
		String name = ctx.ID().getText();
		Symbol symbol =  currentScope.resolve(name);
		
		if (symbol instanceof AggregateSymbol){
			AggregateSymbol aggregate = (AggregateSymbol) symbol;
			GlobalScope global = getGlobalScope();
			((AggregateSymbol) aggregate).addId(name);
			
			logger.debug("number of dotted names in the import:" + aggregate.getCompleteName().size());
			
			global.define(aggregate);
		} else {
			throw new RuntimeException("The symbol is not a Timer symbol");
		}
		
		return ASTNode.VOID;
	}
	
	/**
	 * Visits a display expression
	 * 
	 * 		Defines a display in the global symbol table
	 * 
	 * @return the ASTNode VOID
	 */
	public ASTNode visitDisplay(TransformationGrammarParser.DisplayContext ctx) 
	{ 

		String name = ctx.deviceId.getText();
		Symbol symbol =  currentScope.resolve(name);
		
		if (symbol instanceof DisplaySymbol){
			
			DisplaySymbol display = (DisplaySymbol) symbol;
			
			ASTNode dataToShow = this.visit(ctx.toShow);
			
			display.setDisplayText(dataToShow.asString());
			
			GlobalScope global = getGlobalScope();
						
			global.define(display);
			
		} else {
			throw new RuntimeException("The symbol is not a Display symbol");
		}
		
		return ASTNode.VOID;
	
	}
	
	/**
	 * Visits a set of sentences
	 * 
	 * 		executes one by one all the sentences included.
	 * 
	 * @return the ASTNode VOID
	 */
	@Override 
	public ASTNode visitBlock(TransformationGrammarParser.BlockContext ctx) 
	{ 
		logger.debug("VisitBlock");
		
		currentScope = scopes.get(ctx);
		logger.debug(currentScope);
		
		for (TransformationGrammarParser.SentenceContext sentence : ctx.sentence() )
		{
			logger.debug("it is going to run sentence");
			this.visit(sentence);
		}
		
		currentScope = currentScope.getEnclosingScope(); 
		
		return ASTNode.VOID;

	}

	/**
	 * Visits an IF sentence
	 * 
	 * 		Evaluates the expression inside the if and based on that evaluation 
	 * 		executes the corresponding block of instructions.
	 * 
	 * @return the ASTNode VOID
	 */
    @Override
    public ASTNode visitIf_stat(TransformationGrammarParser.If_statContext ctx) 
    {
    	logger.debug("Visit if");
        List<TransformationGrammarParser.Condition_blockContext> conditions =  ctx.condition_block();

        boolean evaluatedBlock = false;

        for(TransformationGrammarParser.Condition_blockContext condition : conditions) {

            
        	logger.debug(condition.getText());
        	ASTNode evaluated = this.visit(condition.expression());
            
        	logger.debug("evaluated:" + evaluated.toString());
            
            if(evaluated.asBoolean()) {
                evaluatedBlock = true;
                logger.debug("entering if");
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

	/**
	 * Visits a log sentence
	 * 
	 * 		Register a log 
	 * 
	 * @return the ASTNode VOID
	 */
    @Override
    public ASTNode visitLog(TransformationGrammarParser.LogContext  ctx) {
        ASTNode value = this.visit(ctx.expression());
        logger.info("Log" + value);
        return value;
    }    
    
	/**
	 * Visits an unary minus expression
	 * 
	 * 		multiplies by -1 the expression
	 * 
	 * @return the ASTNode representing the finla result.
	 */
    @Override 
    public ASTNode visitUnaryMinusExpr(TransformationGrammarParser.UnaryMinusExprContext ctx) 
    { 
    	ASTNode value = this.visit(ctx.expression());
    	if (value.isInteger()){
    		return new ASTNode(value.asInterger() * -1);
    	} else if (value.isDouble()) {
    		return new ASTNode(value.asDouble() * -1);
    	} else {
    		throw new RuntimeException("operator is not a number");
    	}
    }
    
	/**
	 * Visits a NOT expression
	 * 
	 * 		makes a not of the expression
	 * 
	 * @return the ASTNode representing the final result.
	 */
    @Override 
    public ASTNode visitNotExpr(TransformationGrammarParser.NotExprContext ctx) 
    { 
    	 ASTNode value = this.visit(ctx.expression());

 		// The expression corresponds to an attribute in the status, which was not found.
 		// we assume false
 		if (value == null)
 			value = new ASTNode(new Boolean(false));

    	 if (value.isBoolean()){
    		 return new ASTNode(!value.asBoolean());
    	 } else {
    		 throw new RuntimeException("Negation can only be performed against boolean values");
    	 }
    }

	/**
	 * Visits a date expression
	 * 
	 * 		takes a string and convert it in localdate.
	 * 
	 * @return the ASTNode representing the final result.
	 */
	@Override 
	public ASTNode visitDate(TransformationGrammarParser.DateContext ctx) 
	{ 
		String dateStr = (ctx.TEXT_DATE()).getText();
		try {
			
			dateStr = dateStr.substring(dateStr.indexOf("'") + 1, dateStr.lastIndexOf("'"));
			DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("yyyy-MMM-dd").toFormatter();			
			LocalDate dateObject = LocalDate.parse(dateStr, format);
			return new ASTNode(dateObject);
			
	     } catch (DateTimeParseException e) {
	    	 throw new RuntimeException("incorrect date: " + dateStr);
	     } 
	}

	/**
	 * Visits a time expression
	 * 
	 * 		takes a string and convert it in localtime.
	 * 
	 * @return the ASTNode representing the final result.
	 */
	@Override 
	public ASTNode visitTime(TransformationGrammarParser.TimeContext ctx) 
	{ 
		String timeStr = (ctx.TEXT_TIME()).getText();
		try {
			timeStr = timeStr.substring(timeStr.indexOf("'") + 1, timeStr.lastIndexOf("'"));
			DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("HH:mm:ss.SSS").toFormatter();
			LocalTime timeObject = LocalTime.parse(timeStr, format);
			return new ASTNode(timeObject);
			
	     } catch (DateTimeParseException e) {
	    	 throw new RuntimeException("incorrect time: " + timeStr);
	     } 
	}

	/**
	 * Visits a datetime expression
	 * 
	 * 		takes a string and convert it in localdatetime.
	 * 
	 * @return the ASTNode representing the final result.
	 */
	@Override 
	public ASTNode visitDatetime(TransformationGrammarParser.DatetimeContext ctx) 
	{ 
		
		String datetimeStr = (ctx.TEXT_DATETIME()).getText();

		logger.debug("given datetime:" + datetimeStr);

		try 
		{	
			datetimeStr = datetimeStr.substring(datetimeStr.indexOf("'") + 1, datetimeStr.lastIndexOf("'"));
			DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("yyyy-MMM-dd HH:mm:ss.SSS").toFormatter();
			LocalDateTime datetimeObject = LocalDateTime.parse(datetimeStr,format);
			return new ASTNode(datetimeObject);
						
	    } catch (DateTimeParseException e) 
		{
	    	throw new RuntimeException("incorrect datetime: " + datetimeStr + e.toString());
	    } 
	}
    
	/**
	 * Visits a valid state expression
	 * 	  
	 * @return the ASTNode representing the final result.
	 */
	@Override public ASTNode visitRef_valid_states(TransformationGrammarParser.Ref_valid_statesContext ctx) 
	{ 
		String strToken = ctx.getText();
		
		ASTNode node;
		
		if (strToken.equals("operative") ) {
			
			node = new ASTNode(MeasuringState.OPERATING);
			
		} else if (strToken.equals("sched_down") ) {
			
			node = new ASTNode(MeasuringState.SCHEDULEDOWN);
			
		} else if (strToken.equals("unsched_down") ) {
			
			node = new ASTNode(MeasuringState.UNSCHEDULEDOWN);
			
		} else if ( strToken.equals("system_down") ) {
			
			node = new ASTNode(MeasuringState.SYSTEMDOWN);
			
		} else if ( strToken.equals("initializing") ) {
			
			node = new ASTNode(MeasuringState.INITIALIZING);
			
		} else {
			
			node = new ASTNode(MeasuringState.UNDEFINED);
		}

		return node;
		
	}
	
	/**
	 * Visits a program parameter
	 * 
	 * 		takes the value given and put it in the global memory space
	 * 
	 * @return the ASTNode of the value given
	 */
	public ASTNode visitFormalparameters(TransformationGrammarParser.ProgramparameterContext ctx) 
	{ 
		String id = ctx.getText();
		logger.debug("Visit Program Parameter:" + id);
				
        ASTNode value = globals.get(id);
        if(value == null) {
            throw new RuntimeException("no such parameter: " + id);
        }
        
        return value;
	}
	
	/** 
	 * Return scope holding id's value; current func space or global. 
	 */
    public MemorySpace getSpaceWithSymbol(String id) 
    {
        if (stack.size()>0 && stack.peek().get(id)!=null) { // in top stack?
            return stack.peek();
        }
        if ( globals.get(id)!=null ) return globals;        // in globals?
        return null;                                        // nowhere
    }

	/**
	 * Gets the reference to the global space
	 * 
	 * @return Global space
	 */
	public MemorySpace getGlobalSpace(){
		return globals;
	}
	
	/**
	 * Gets the reference to the global scope
	 * 
	 * @return Global scope
	 */
	public GlobalScope getGlobalScope(){
		return globalScope;
	}
	
}
