package com.advicetec.language.behavior;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.configuration.SystemConstants;
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
import com.advicetec.language.ast.ImportSymbol;
import com.advicetec.language.ast.MemorySpace;
import com.advicetec.language.ast.ReturnValue;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.TimerSymbol;
import com.advicetec.language.ast.Type;
import com.advicetec.language.ast.VariableSymbol;
import com.advicetec.measuredentitity.MeasuredEntityFacade;

public class BehaviorInterpreter extends BehaviorGrammarBaseVisitor<ASTNode>
{

	static Logger logger = LogManager.getLogger(BehaviorInterpreter.class.getName());


	GlobalScope globalScope; // Global scope is filled by the parser
	ParseTreeProperty<Scope> scopes;  // The definition for the rest of scopes
	Scope currentScope;
	MeasuredEntityFacade facade;

	MemorySpace globals;
	MemorySpace currentSpace;
	Stack<FunctionSpace> stack; // call stack

	// used to compare floating point numbers
	public static final double SMALL_VALUE = 0.00000000001;

	BehaviorInterpreter(GlobalScope _globalScope, MemorySpace _globals, ParseTreeProperty<Scope> scopes, MeasuredEntityFacade facade)
	{
		// Variable for symbol definition.
		this.globalScope = _globalScope;
		this.globals = _globals;
		this.scopes = scopes;
		this.currentScope = _globalScope;
		this.facade = facade; 

		// For memory evaluation
		stack = new Stack<FunctionSpace>(); // call stack
		logger.debug("Interpreter Constructor");
	}

	public ASTNode visitProgram(BehaviorGrammarParser.ProgramContext ctx) 
	{ 
		logger.debug("visitProgram:" );
		return this.visit(ctx.main()); 
	}

	@Override 
	public ASTNode visitMain(BehaviorGrammarParser.MainContext ctx) 
	{ 
		logger.debug("visitMain:" );

		currentScope = globalScope; 
		currentSpace = globals;

		String fname = ctx.PROGRAM().getText();
		Symbol fs = currentScope.resolve(fname);

		if ( fs == null ) {
			logger.error( "no such function: " + fname);
			throw new RuntimeException("no such function: " + fname);
		}

		if ( fs instanceof VariableSymbol ) {
			logger.error( fname + " is not a function");
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
		String id = ctx.ID().getText();
		System.out.println("visitVar_dec");
		Symbol s = currentScope.resolve(id);

		// the declaration includes an assignment
		if (ctx.ASG() != null)
		{
			ASTNode value = this.visit(ctx.expression());	        
			VerifyAssign(s, value);
			currentSpace.put(id, castAssign(s, value));         // store	        
			return value;
		}
		else
		{
			ASTNode node = initializeSymbol(s);
			currentSpace.put(id, node);         // store
			return ASTNode.VOID;	
		}
	}

	public ASTNode visitUnit_dec(BehaviorGrammarParser.Unit_decContext ctx) 
	{ 
		// System.out.println("visit Unit dec");

		String id = ctx.ID().getText();
		getGlobalSpace().put(id, new ASTNode(new Object()));         // store
		return ASTNode.VOID;	
	}

	public ASTNode visitAtrib_dec(BehaviorGrammarParser.Atrib_decContext ctx) 
	{ 
		String id = ctx.id1.getText();

		logger.debug("visitAtrib_dec:" + id );

		getGlobalSpace().put(id, new ASTNode(new Object()));         // store with an initial value		
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
	}	


	public ASTNode AssignAttribute(AttributeSymbol toAssign, BehaviorGrammarParser.Atrib_decContext ctx)
	{

		// It verifies whether the expression correspond to another attribute. In such 
		// case, it checks whether or not both attributes manage the same units.


		String id2 = ctx.expression().getText();

		logger.debug("AssignAttribute:" + id2 );

		Symbol s = currentScope.resolve(id2);
		if (s instanceof AttributeSymbol )
		{
			AttributeSymbol s1 = (AttributeSymbol) s;

			System.out.println("s1 unit:" + s1.getUnitOfMeasure() + "sysattr : " + toAssign.getUnitOfMeasure());

			if (((s1.getUnitOfMeasure() == null) && (toAssign.getUnitOfMeasure() == null))
					|| (s1.getUnitOfMeasure().equals(toAssign.getUnitOfMeasure())) )
			{

				ASTNode value = this.visit(ctx.expression());

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


	public ASTNode visitStatus(BehaviorGrammarParser.StatusContext ctx) 
	{ 
		String attributeId = ctx.ID().getText(); 
		AttributeValue value = (AttributeValue) facade.getNewestByAttributeName(attributeId);
		Symbol symbol = currentScope.resolve(attributeId);
		Object valObj = null;
		if (value == null)
		{
			return initializeSymbol(symbol);
		} else {
			valObj = value.getValue();
		}

		switch (value.getAttr().getType()){
		case INT:
			if (symbol.getType() != Symbol.Type.tINT){
				String error =  "the attribute given: " + attributeId + " is not registered in the status as type int";
				logger.error(error);
				throw new RuntimeException( error );
			}
			break;
		case DATETIME:
			if (symbol.getType() != Symbol.Type.tDATETIME){
				String error = "the attribute given: " + attributeId + " is not registered in the status as type datetime";
				logger.error(error);
				throw new RuntimeException( error );
			}
			break;
		case DOUBLE:
			if (symbol.getType() != Symbol.Type.tFLOAT){
				String error = "the attribute given: " + attributeId + " is not registered in the status as type float"; 
				logger.error(error);
				throw new RuntimeException( error );
			}
			break;
		case STRING:
			if (symbol.getType() != Symbol.Type.tSTRING){
				String error = "the attribute given: " + attributeId + " is not registered in the status as type string";
				logger.error(error);
				throw new RuntimeException( error );
			}
			break;
		case BOOLEAN:
			if (symbol.getType() != Symbol.Type.tBOOL){
				String error = "the attribute given: " + attributeId + " is not registered in the status as type boolean";
				logger.error(error);
				throw new RuntimeException( error );
			}
			break;
		case DATE:
			if (symbol.getType() != Symbol.Type.tDATE){
				String error = "the attribute given: " + attributeId + " is not registered in the status as type date";
				logger.error(error);
				throw new RuntimeException( error );
			}
			break;
		case TIME:
			if (symbol.getType() != Symbol.Type.tTIME){
				String error = "the attribute given: " + attributeId + " is not registered in the status as type time";
				logger.error(error);
				throw new RuntimeException( error );
			}
			break;
		case VOID:
			String error = "the attribute given: " + attributeId + " is registered in the status as type void";
			logger.error(error);			
			throw new RuntimeException( error );
		}

		return new ASTNode(valObj); 

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
				getGlobalSpace().put(id, new ASTNode(inarray));
				break;

			case tFLOAT:
				Double [] dbarray = new Double[numElements];
				Arrays.fill(dbarray, 0.0);
				getGlobalSpace().put(id, new ASTNode(dbarray));
				break;

			case tSTRING:
				String [] starray = new String[numElements];
				Arrays.fill(starray, "");
				getGlobalSpace().put(id, new ASTNode(starray));
				break;

			case tBOOL:
				Boolean [] boarray = new Boolean[numElements];
				Arrays.fill(boarray, false);
				getGlobalSpace().put(id, new ASTNode(boarray));
				break;

			case tDATETIME:
				LocalDateTime [] dtarray = new LocalDateTime[numElements];
				Arrays.fill(dtarray, LocalDateTime.MIN);
				getGlobalSpace().put(id, new ASTNode(dtarray));
				break;

			case tDATE:
				LocalDate [] darray = new LocalDate[numElements];
				Arrays.fill(darray, LocalDate.MIN);
				getGlobalSpace().put(id, new ASTNode(darray));
				break;

			case tTIME:
				LocalTime [] tarray = new LocalTime[numElements];
				Arrays.fill(tarray, LocalDate.MIN);
				getGlobalSpace().put(id, new ASTNode(new LocalTime[numElements]));
				break;

			default:
				throw new RuntimeException("unknown type for vector: " + vec.getType().name());
			}
		}

		return ASTNode.VOID;
	}

	public ASTNode initializeSymbol(Symbol symbol)
	{

		if (symbol == null){
			String error = "the Symbol given to be initialized is null";
			logger.error(error);			
			throw new RuntimeException( error );
		}

		logger.debug("initializeSymbol:" + symbol.getName());
	
		if (!((symbol instanceof VariableSymbol) || (symbol instanceof AttributeSymbol))){
			String error = "Calling inialize for a symbol which is not variable nor attribute";
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


	@Override 
	public ASTNode visitAssign(BehaviorGrammarParser.AssignContext ctx) 
	{ 


		String id = ctx.ID().getText();
		logger.debug("Visit Assign:" + id );
		ASTNode value = this.visit(ctx.expression());

		Symbol symbol = currentScope.resolve(id) ;
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
		space.put(id,castAssign(symbol, value));         // store

		return value;

	}

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


	public ASTNode castAssign(Symbol symbol, ASTNode value)
	{

		switch (symbol.getType())
		{
		case tINT:
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
			}
		case tVOID:
			throw new RuntimeException("The value given is of type void");

		case tINVALID:
		default:
			throw new RuntimeException("The value given is of type invalid");
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
			if (left.isInteger() && right.isInteger()){
				return new ASTNode((Integer)(left.asInterger() * right.asInterger()));
			} else if (left.isDouble() && right.isInteger()){
				return new ASTNode((Double)(left.asDouble() * right.asInterger()));
			} else if (left.isInteger() && right.isDouble()){
				return new ASTNode((Double)(left.asInterger() * right.asDouble()));
			} else if (left.isDouble() && right.isDouble()){
				return new ASTNode((Double)(left.asDouble() * right.asDouble()));
			} else {
				throw new RuntimeException("operators are not numbers");
			}

		case BehaviorGrammarParser.DIVI:
			if (left.isInteger() && right.isInteger()){
				return new ASTNode((Double)((double)left.asInterger() / (double) right.asInterger()));
			} else if (left.isDouble() && right.isInteger()){
				return new ASTNode((Double)(left.asDouble() / (double) right.asInterger()));
			} else if (left.isInteger() && right.isDouble()){
				return new ASTNode((Double)(left.asInterger() / (double) right.asDouble()));
			} else if (left.isDouble() && right.isDouble()){
				return new ASTNode((Double)(left.asDouble() / right.asDouble()));
			} else {
				throw new RuntimeException("operators are not numbers");
			}

		case BehaviorGrammarParser.MOD:
			if (left.isInteger() && right.isInteger()){
				return new ASTNode((Double)((double)left.asInterger() % (double) right.asInterger()));
			} else if (left.isDouble() && right.isInteger()){
				return new ASTNode((Double)(left.asDouble() % (double) right.asInterger()));
			} else if (left.isInteger() && right.isDouble()){
				return new ASTNode((Double)(left.asInterger() % (double) right.asDouble()));
			} else if (left.isDouble() && right.isDouble()){
				return new ASTNode((Double)(left.asDouble() % right.asDouble()));
			} else {
				throw new RuntimeException("operators are not numbers");
			}

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
			} else {
				throw new RuntimeException("operators are not numbers or strings");
			}

		case BehaviorGrammarParser.MINUS:
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
			throw new RuntimeException("unknown operator: " + BehaviorGrammarParser.tokenNames[ctx.op.getType()]);
		}

	}

	@Override 
	public ASTNode visitExpon(BehaviorGrammarParser.ExponContext ctx) 
	{ 

		System.out.println("visitExpon");

		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));

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

	@Override 
	public ASTNode visitRelationalExpr(BehaviorGrammarParser.RelationalExprContext ctx) 
	{ 

		System.out.println("visitRelationalExpr");

		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));

		switch (ctx.op.getType()) {
		case BehaviorGrammarParser.LT:
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

		case BehaviorGrammarParser.LTEQ:
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

		case BehaviorGrammarParser.GT:
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

		case BehaviorGrammarParser.GTEQ:
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
			if (left.isInteger() && right.isInteger()){
				return new ASTNode((Boolean) (left.asInterger() == right.asInterger()));
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
			} else {
				throw new RuntimeException("operators are not of the same type");
			}        

		case BehaviorGrammarParser.NEQ:
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
			} else {
				throw new RuntimeException("operators are not of the same type");
			}        

		default:
			throw new RuntimeException("unknown operator: " + BehaviorGrammarParser.tokenNames[ctx.op.getType()]);
		}
	}

	@Override 
	public ASTNode visitAndExpr(BehaviorGrammarParser.AndExprContext ctx) 
	{ 
		logger.debug("visitAndExpr");

		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));
		if (left.isBoolean() && right.isBoolean()){

			Boolean ret = left.asBoolean() && right.asBoolean();
			logger.debug("visitAndExpr ret value:" + ret.toString());
			return new ASTNode(ret);
		} else {
			throw new RuntimeException("operators are not of boolean type");
		}

	}

	@Override 
	public ASTNode visitOrExpr(BehaviorGrammarParser.OrExprContext ctx) 
	{ 
		System.out.println("visitOrExpr");

		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));
		if (left.isBoolean() && right.isBoolean()){
			return new ASTNode(left.asBoolean() || right.asBoolean());
		} else {
			throw new RuntimeException("operators are not of boolean type");
		}
	}

	public ASTNode visitToken(BehaviorGrammarParser.TokenContext ctx) 
	{ 
				
		BehaviorGrammarParser.ExpressionContext stringEq1 =  ctx.ex1;
		BehaviorGrammarParser.ExpressionContext numToken = ctx.ex2;
		
		String input =  (this.visit(stringEq1)).asString();

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
	
	public ASTNode visitSubstring(BehaviorGrammarParser.SubstringContext ctx) 
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

	public ASTNode visitStartWith(BehaviorGrammarParser.StartwithContext ctx) 
	{ 
				
		BehaviorGrammarParser.ExpressionContext stringParamCtx1 =  ctx.ex1;
		BehaviorGrammarParser.ExpressionContext StringParamCtx2 = ctx.ex2;
		
		String stringParam1 =  (this.visit(stringParamCtx1)).asString();
		String stringParam2 =  (this.visit(StringParamCtx2)).asString();
		
		logger.debug("visitStartWith - Param1:"+ stringParam1 + " Param2:" + stringParam2 );
						
		return new ASTNode( new Boolean(stringParam1.startsWith(stringParam2)));
		
    } 

	public ASTNode visitRepeat(BehaviorGrammarParser.RepeatContext ctx) 
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

	public ASTNode visitTimer(BehaviorGrammarParser.TimerContext ctx) 
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


	@Override 
	public ASTNode visitBlock(BehaviorGrammarParser.BlockContext ctx) 
	{ 
		// System.out.println("VisitBlock");

		currentScope = scopes.get(ctx);
		// System.out.println(currentScope);

		for (BehaviorGrammarParser.SentenceContext sentence : ctx.sentence() )
		{
			// System.out.println("it is going to run sentence");
			this.visit(sentence);
		}

		currentScope = currentScope.getEnclosingScope(); 

		return ASTNode.VOID;

	}

	@Override 
	public ASTNode visitCall(BehaviorGrammarParser.CallContext ctx) 
	{ 

		System.out.println("visitCall");

		// Resolve function's name
		String fname = ctx.ID().getText();
		Symbol fs = currentScope.resolve(fname);

		if ( fs == null ) {
			logger.error( "no such function: " + fname);
			throw new RuntimeException("no such function: " + fname);
		}

		if ( fs instanceof VariableSymbol ) {
			logger.error( fname + " is not a function");
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
		if (value.isInteger()){
			return new ASTNode(value.asInterger() * -1);
		} else if (value.isDouble()) {
			return new ASTNode(value.asDouble() * -1);
		} else {
			throw new RuntimeException("operator is not a number");
		}
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

		
		double valueRet = 0;
		AttributeType type = null;
		for (int i = 0; i < values.size(); i++) {
			AttributeValue value = values.get(i);
			type = value.getAttr().getType();

			switch (type)
			{
			case DOUBLE:
				Double doubleValue = (Double) value.getValue();
				valueRet = doubleValue.doubleValue() + valueRet;
				break;

			case INT:
				Integer integerValue = (Integer) value.getValue();
				valueRet = integerValue.intValue() + valueRet;
				break;

			case BOOLEAN:
				boolean boolValue = ((Boolean) value.getValue()).booleanValue();
				if (boolValue == true){
					valueRet = valueRet + 1;
				}
				break;

			case STRING:
				valueRet = valueRet + 1;
				break;				 

			case DATETIME:
				valueRet = valueRet + 1;
				break;				 				

			case DATE:
				valueRet = valueRet + 1;
				break;				 				

			case TIME:
				valueRet = valueRet + 1;
				break;				 				

			case VOID:
				throw new RuntimeException("The Void type is not comparable with the operator maximum");

			default:
				throw new RuntimeException("unknown type: " + type.name());
			}

		}		
		
		Object objRet = null;
		if (type != null){
			switch (type)
			{
			case DOUBLE:
				objRet = new Double(valueRet);
				break;

			case INT:
			case BOOLEAN:
			case STRING:
			case DATETIME:
			case DATE:
			case TIME:
				objRet = new Integer(new Double(valueRet).intValue() );
				break;				 				
			}
		}
		
		logger.debug("Attribute Type:" + type.getName() + "Output" + objRet + "total samples" + values.size());

		return new ASTNode(objRet);

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
			AttributeType type = value.getAttr().getType();

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

	public GlobalScope getGlobalScope(){
		return globalScope;
	}

}
