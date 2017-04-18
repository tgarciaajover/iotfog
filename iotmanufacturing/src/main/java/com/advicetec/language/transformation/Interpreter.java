package com.advicetec.language.transformation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.Token;

import com.advicetec.language.BehaviorGrammarParser;
import com.advicetec.language.TransformationGrammarParser;
import com.advicetec.language.TransformationGrammarBaseVisitor;
import com.advicetec.language.ast.ASTNode;
import com.advicetec.language.ast.AttributeSymbol;
import com.advicetec.language.ast.FunctionSpace;
import com.advicetec.language.ast.FunctionSymbol;
import com.advicetec.language.ast.GlobalScope;
import com.advicetec.language.ast.MemorySpace;
import com.advicetec.language.ast.ReturnValue;
import com.advicetec.language.ast.Scope;
import com.advicetec.language.ast.Symbol;
import com.advicetec.language.ast.TransformationSpace;
import com.advicetec.language.ast.TransformationSymbol;
import com.advicetec.language.ast.VariableSymbol;

public class Interpreter extends TransformationGrammarBaseVisitor<ASTNode>
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
	
	MemorySpace globals;
	MemorySpace currentSpace;
	Stack<TransformationSpace> stack; // call stack
	
	// used to compare floating point numbers
    public static final double SMALL_VALUE = 0.00000000001;

	Interpreter(GlobalScope _globalScope, MemorySpace _globals, ParseTreeProperty<Scope> scopes)
	{
		// Variable for symbol definition.
		this.globalScope = _globalScope;
		this.globals = _globals;
		this.scopes = scopes;
		this.currentScope = _globalScope;
		
		// For memory evaluation
		stack = new Stack<TransformationSpace>(); // call stack
		
		System.out.println("Interpreter main");
	}
	
	public ASTNode visitProgram(TransformationGrammarParser.ProgramContext ctx) 
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
        
        System.out.println("num params:" + argCount);

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
	public ASTNode visitVar_dec(TransformationGrammarParser.Var_decContext ctx) 
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
	
	public ASTNode visitUnit_dec(TransformationGrammarParser.Unit_decContext ctx) 
	{ 
		System.out.println("visit Unit dec");
		
		String id = ctx.ID().getText();
	    currentSpace.put(id, new ASTNode(new Object()));         // store
		return ASTNode.VOID;	
	}
	
	public ASTNode visitAtrib_dec(TransformationGrammarParser.Atrib_decContext ctx) 
	{ 
		String id = ctx.id1.getText();
		System.out.println("visitAtrib_dec:" + id);
		
		// the declaration includes an assignment
		if (ctx.ASG() != null)
		{
			AttributeSymbol sysAttr = (AttributeSymbol) currentScope.resolve(id);
			
			// It verifies if the expression correspond to another attribute. In such 
			// case check whether or not they manages the same units.
			String id2 = ctx.expression().getText();
			Symbol s = currentScope.resolve(id2) ;
			if (s instanceof AttributeSymbol ){
				AttributeSymbol s1 = (AttributeSymbol) s;
				
				System.out.println("s1 unit:" + s1.getUnitOfMeasure() + "sysattr : " + sysAttr.getUnitOfMeasure());
				
				if (s1.getUnitOfMeasure() != sysAttr.getUnitOfMeasure() )
				{
			        ASTNode value = this.visit(ctx.expression());
			        currentSpace.put(id, value);         // store
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
			    currentSpace.put(id, value);         // store
			    return value;					
			}
		}
		else
		{
	        currentSpace.put(id, new ASTNode(new Object()));         // store
			return ASTNode.VOID;	
		}
	}
	
	@Override 
	public ASTNode visitAssign(TransformationGrammarParser.AssignContext ctx) 
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
	
	@Override 
	public ASTNode visitVar(TransformationGrammarParser.VarContext ctx) 
	{ 
				
		String id = ctx.getText();
		System.out.println("visitVar:" + id);
				
		MemorySpace space = getSpaceWithSymbol(id); 
        ASTNode value = space.get(id);
        if(value == null) {
            throw new RuntimeException("no such variable: " + id);
        }
        
        return value;
	}

	public ASTNode visitRound(TransformationGrammarParser.RoundContext ctx) 
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
	public ASTNode visitRef_return(TransformationGrammarParser.Ref_returnContext ctx) 
	{ 
    	ReturnValue sharedReturnValue = new ReturnValue();
    	sharedReturnValue.value = this.visit(ctx.expression());
        throw sharedReturnValue;
	}
	
	@Override 
	public ASTNode visitInteger(TransformationGrammarParser.IntegerContext ctx) 
	{ 
		System.out.println("visitInteger");
		return new ASTNode(Integer.valueOf(ctx.getText()));
	}

	@Override
	public ASTNode visitYear(TransformationGrammarParser.YearContext ctx) 
	{ 
		System.out.println("visitYear");
		return new ASTNode(Integer.valueOf(ctx.getText()));
	}
	
	public ASTNode visitDigit(TransformationGrammarParser.DigitContext ctx) 
	{ 
		return new ASTNode(Integer.valueOf(ctx.getText()));
	}
	
	public ASTNode visitFloat(TransformationGrammarParser.FloatContext ctx) 
	{ 
		System.out.println("visitFloat");
		return new ASTNode(Double.valueOf(ctx.getText()));
	}
	
	@Override 
	public ASTNode visitBoolean(TransformationGrammarParser.BooleanContext ctx) 
	{ 
		System.out.println("visitBoolean");
		
		return new ASTNode(Boolean.valueOf(ctx.getText()));
	}

	@Override 
	public ASTNode visitStr(TransformationGrammarParser.StrContext ctx) 
	{ 
		System.out.println("visitStr");
		
		String str = ctx.getText();
	     
		// strip quotes
	    str = str.substring(1, str.length() - 1).replace("\"\"", "\"");
	    return new ASTNode(str);
	}
	
	@Override 
	public ASTNode visitParens(TransformationGrammarParser.ParensContext ctx) 
	{ 
		System.out.println("visitParens");
		
		return this.visit(ctx.expression()); 
	}
	
	@Override 
	public ASTNode visitMult(TransformationGrammarParser.MultContext ctx) 
	{ 
		System.out.println("visitMult");
		
		ASTNode left = this.visit(ctx.expression(0));
        ASTNode right = this.visit(ctx.expression(1));

        switch (ctx.op.getType()) {
            case TransformationGrammarParser.MULT:
                return new ASTNode(left.asDouble() * right.asDouble());
            case TransformationGrammarParser.DIVI:
                return new ASTNode(left.asDouble() / right.asDouble());
            case TransformationGrammarParser.MOD:
                return new ASTNode(left.asDouble() % right.asDouble());
            default:
                throw new RuntimeException("unknown operator: " + TransformationGrammarParser.tokenNames[ctx.op.getType()]);
        }
	}

	@Override 
	public ASTNode visitAddSub(TransformationGrammarParser.AddSubContext ctx) 
	{ 
		System.out.println("visitAddSub");
				
		ASTNode left = this.visit(ctx.expression(0));
        ASTNode right = this.visit(ctx.expression(1));

        switch (ctx.op.getType()) {
            case TransformationGrammarParser.PLUS:
                return left.isDouble() && right.isDouble() ?
                        new ASTNode(left.asDouble() + right.asDouble()) :
                        new ASTNode(left.asString() + right.asString());
            case TransformationGrammarParser.MINUS:
                return new ASTNode(left.asDouble() - right.asDouble());
            default:
                throw new RuntimeException("unknown operator: " + TransformationGrammarParser.tokenNames[ctx.op.getType()]);
        }

	}

	@Override 
	public ASTNode visitExpon(TransformationGrammarParser.ExponContext ctx) 
	{ 
		
		System.out.println("visitExpon");
		
		ASTNode left = this.visit(ctx.expression(0));
        ASTNode right = this.visit(ctx.expression(1));
        
        return new ASTNode(Math.pow(left.asDouble(), right.asDouble()));
	}
	
	@Override 
	public ASTNode visitRelationalExpr(TransformationGrammarParser.RelationalExprContext ctx) 
	{ 
		
		System.out.println("visitRelationalExpr");
		
		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));

        switch (ctx.op.getType()) {
            case TransformationGrammarParser.LT:
                return new ASTNode(left.asDouble() < right.asDouble());
            case TransformationGrammarParser.LTEQ:
                return new ASTNode(left.asDouble() <= right.asDouble());
            case TransformationGrammarParser.GT:
                return new ASTNode(left.asDouble() > right.asDouble());
            case TransformationGrammarParser.GTEQ:
                return new ASTNode(left.asDouble() >= right.asDouble());
            default:
                throw new RuntimeException("unknown operator: " + TransformationGrammarParser.tokenNames[ctx.op.getType()]);
        }
	}
	
	@Override 
	public ASTNode visitEqualityExpr(TransformationGrammarParser.EqualityExprContext ctx) 
	{ 
		
		System.out.println("visitEqualityExpr");
		
		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));

        switch (ctx.op.getType()) {
            case TransformationGrammarParser.EQ:
                return left.isDouble() && right.isDouble() ?
                        new ASTNode(Math.abs(left.asDouble() - right.asDouble()) < SMALL_VALUE) :
                        new ASTNode(left.equals(right));
            case TransformationGrammarParser.NEQ:
                return left.isDouble() && right.isDouble() ?
                        new ASTNode(Math.abs(left.asDouble() - right.asDouble()) >= SMALL_VALUE) :
                        new ASTNode(!left.equals(right));
            default:
                throw new RuntimeException("unknown operator: " + TransformationGrammarParser.tokenNames[ctx.op.getType()]);
        }
	}
	
	@Override 
	public ASTNode visitAndExpr(TransformationGrammarParser.AndExprContext ctx) 
	{ 
		System.out.println("visitAndExpr");
		
		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));
        return new ASTNode(left.asBoolean() && right.asBoolean());

	}
	
	@Override 
	public ASTNode visitOrExpr(TransformationGrammarParser.OrExprContext ctx) 
	{ 
		System.out.println("visitOrExpr");
		
		ASTNode left = this.visit(ctx.expression(0));
		ASTNode right = this.visit(ctx.expression(1));
        return new ASTNode(left.asBoolean() || right.asBoolean());
	}
	
	public ASTNode visitToken(TransformationGrammarParser.TokenContext ctx) 
	{ 
		TransformationGrammarParser.ExpressionContext stringEq1 =  ctx.ex1;
		TransformationGrammarParser.ExpressionContext numberFrom = ctx.ex2;
		TransformationGrammarParser.ExpressionContext numberTo = ctx.ex3;
		
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
	public ASTNode visitBlock(TransformationGrammarParser.BlockContext ctx) 
	{ 
		System.out.println("VisitBlock");
		
		currentScope = scopes.get(ctx);
		System.out.println(currentScope);
		
		for (TransformationGrammarParser.SentenceContext sentence : ctx.sentence() )
		{
			System.out.println("it is going to run sentence");
			this.visit(sentence);
		}
		
		currentScope = currentScope.getEnclosingScope(); 
		
		return ASTNode.VOID;

	}

    // if override
    @Override
    public ASTNode visitIf_stat(TransformationGrammarParser.If_statContext ctx) {

        List<TransformationGrammarParser.Condition_blockContext> conditions =  ctx.condition_block();

        boolean evaluatedBlock = false;

        for(TransformationGrammarParser.Condition_blockContext condition : conditions) {

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

    // log override
    @Override
    public ASTNode visitLog(TransformationGrammarParser.LogContext  ctx) {
        ASTNode value = this.visit(ctx.expression());
        System.out.println(value);
        return value;
    }    
    
    @Override 
    public ASTNode visitUnaryMinusExpr(TransformationGrammarParser.UnaryMinusExprContext ctx) 
    { 
    	ASTNode value = this.visit(ctx.expression());
        return new ASTNode(-value.asDouble());
    }
    
    @Override 
    public ASTNode visitNotExpr(TransformationGrammarParser.NotExprContext ctx) 
    { 
    	 ASTNode value = this.visit(ctx.expression());
    	 
    	 if (value.isBoolean()){
    		 return new ASTNode(!value.asBoolean());
    	 } else {
    		 throw new RuntimeException("Negation can only be performed against boolean values");
    	 }
    }

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

	@Override 
	public ASTNode visitDatetime(TransformationGrammarParser.DatetimeContext ctx) 
	{ 
		
		String datetimeStr = (ctx.TEXT_DATETIME()).getText();

		System.out.println("given datetime:" + datetimeStr);

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
    
	public ASTNode visitFormalparameters(TransformationGrammarParser.ProgramparameterContext ctx) 
	{ 
		String id = ctx.getText();
		System.out.println("Visit Program Parameter:" + id);
				
        ASTNode value = globals.get(id);
        if(value == null) {
            throw new RuntimeException("no such parameter: " + id);
        }
        
        return value;
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
