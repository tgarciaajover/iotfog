grammar BehaviorGrammar;

program : (import_name)* main
	;

main : PROGRAM ID PR_OPN programparameters? PR_CLS block 
		   (function_dec)*
	;  

import_name
 : IMPORT dotted_names
 ;

dotted_names
 : dotted_name ( ',' dotted_name )* SEMICOLON
 ;

dotted_name
 : ID ( '.' ID )* ( AS nickname=ID )?
 ;

programparameters : programparameter (',' programparameter)*;

programparameter : type ID;

function_dec : type ID PR_OPN formalparameters? PR_CLS block // example: int function(int param1, int param2, ..) { ... } 
			;   

type : K_FLOAT | K_INT  | K_BOOL | K_STR | K_VOID | K_DATETIME | K_TIME | K_DATE;

formalparameters : formalparameter (',' formalparameter)*;

formalparameter : type ID;

sentence : block									# ref_block	  
			| atrib_dec								# ref_attribute_def
			| vect_attrib_dec						# ref_vector_attrib_def
			| unit_dec								# ref_unit_def
			| var_dec							  	# ref_var_def
			| vect_var_dec							# ref_vector_var_def
			| function_dec SEMICOLON			  	# ref_function_dec
			| if_stat 								# ref_if_start
			| display								# ref_display
			| save									# ref_save	
			| count_over_time						# ref_count_over_time
			| max_over_time							# ref_max_over_time	
			| timer									# ref_event
			| repeat								# ref_repeat				
			| RETURN expression SEMICOLON		   	# ref_return
			| assign								# ref_assign
			| assign_vec							# ref_assign_vec
			| expression SEMICOLON				  	# ref_call
			| round									# ref_round 	
			| log 									# ref_log
			| OTHER {System.err.println("unknown char: " + $OTHER.text);} #ref_other
			;

assign 		: ID ASG expression SEMICOLON
	; 

assign_vec 	: ID '[' numElement=INT ']' ASG expression SEMICOLON
	; 

var_dec 	: VARIABLE type ID (ASG expression)? SEMICOLON
	;

atrib_dec 	: ATTRIBUTE type id1=ID  (ASG expression)? (UNIT id2=ID)?  (TREND)? SEMICOLON
	;

unit_dec	: UNIT id1=ID STRING SEMICOLON
	; 

vect_attrib_dec : ATTRIBUTE type id1=ID '[' numElements=INT ']' (UNIT id2=ID)? SEMICOLON
	;

vect_var_dec : VARIABLE type id1=ID '[' numElements=INT ']' SEMICOLON
	;

display  	: DISPLAY PR_OPN expression PR_CLS SEMICOLON
	;

save		: SAVE PR_OPN expressionList? PR_CLS SEMICOLON
	;

count_over_time : COUNT_OVER_TIME PR_OPN ID COMMA TIMEUNIT COMMA range=INT PR_CLS 
	;
max_over_time : MAX_OVER_TIME PR_OPN ID COMMA TIMEUNIT COMMA range=INT PR_CLS
	;

timer		: TIMER PR_OPN TIMEUNIT COMMA INT COMMA pack=ID PR_CLS SEMICOLON
	;

repeat		: REPEAT PR_OPN TIMEUNIT COMMA INT COMMA pack=ID PR_CLS SEMICOLON
	;


block 		:  BR_OPN (sentence)* BR_CLS  // Possibly Empty Block of Sentences.
	;

if_stat 	: IF condition_block (ELSE IF condition_block)* (ELSE block)?
 ;

condition_block
 : expression block
 ;

round : ROUND PR_OPN expression COMMA INT1 PR_CLS
	; 

log 		: LOG expression SEMICOLON
 ;

expression : ID  PR_OPN expressionList?  PR_CLS  					# Call // func call like f(), f(x), f(x1,x2)
            | ID ASG count_over_time								# Call_CountOverTime 
            | ID ASG max_over_time									# Call_MaxOverTime 
			| expression EXPO expression  							# Expon
			| MINUS expression                     					# unaryMinusExpr
 			| NOT expression                        				# notExpr
 			| token													# ref_split
 			| status												# ref_status
 			| ID '[' expression ']'                     		    # ExprArrayIndex // arrayId[i]
		    | expression op=( MULT | DIVI | MOD ) expression  		# Mult
			| expression op=(PLUS | MINUS) expression  				# AddSub
 			| expression op=(LTEQ | GTEQ | LT | GT) expression		# relationalExpr
 			| expression op=(EQ | NEQ) expression		            # equalityExpr
 			| expression AND expression		                        # andExpr
 			| expression OR expression		                        # orExpr
			| atom													# ref_atom
			;

token 		: TOKEN PR_OPN ex1=expression ',' ex2=expression ',' ex3=expression PR_CLS
	;

status		: STATUS DOT ID
	;
						
atom 		:	ID								# Var
		| DATE							# Date
		| TIME							# Time
		| DATETIME						# Datetime
		| INT4 							# Year
		| INT1							# digit
		| INT							# Integer
		| FLOAT 						# Float
		| BOOLEAN					    # Boolean
		| STRING						# Str
		| DATE							# Date
		| TIME							# Time
		| DATETIME						# Datetime
		| PR_OPN expression PR_CLS    	# Parens
		;

expressionList : expression (COMMA expression)*   // argument list.
			;

temporal_literal: DATE | TIME | DATETIME; 

DATE : K_DATE PR_OPN SINGLEDATE_QUOTED_TEXT PR_CLS;

TIME:  K_TIME PR_OPN SINGLETIME_QUOTED_TEXT PR_CLS;

DATETIME : K_DATETIME PR_OPN SINGLEDATETIME_QUOTED_TEXT PR_CLS
			| K_DATETIME PR_OPN SINGLEDATE_QUOTED_TEXT PR_CLS  // the time part 00:00:00.000
;

SINGLEDATE_QUOTED_TEXT : SINGLE_QUOTE DATE_TEXT SINGLE_QUOTE
;

SINGLETIME_QUOTED_TEXT: SINGLE_QUOTE TIME_TEXT SINGLE_QUOTE
;

SINGLEDATETIME_QUOTED_TEXT: SINGLE_QUOTE DATETIME_TEXT SINGLE_QUOTE
;

DATETIME_TEXT :
	(
		DATE_TEXT ' ' TIME_TEXT
	)
;

DATE_TEXT :
    (
	INT4 SEPARATOR MONTH SEPARATOR INT
 	)
;

TIME_TEXT :
	(
		INT COLON INT COLON INT (DOT INT)?
	)
;

MONTH : JAN | FEB | MAR | APR | MAY | JUN | JUL | AUG | SEP | OCT | NOV | DEC;

TIMEUNIT : SECOND | MINUTE | HOUR;

PROGRAM : 	'program';
ATTRIBUTE 	: 'attr';
VARIABLE 	: 'var';
UNIT 		: 'unit';
TOKEN 		: 'token'; 
DISPLAY 	: 'display';
SAVE		: 'save';
STATUS 		: 'STATUS';
COUNT_OVER_TIME : 'count_over_time';
MAX_OVER_TIME	: 'max_over_time';
TREND		: 'trend';
ROUND 		: 'round';
IMPORT 		: 'import'; 
AS 			: 'as';
TIMER		: 'timer';
REPEAT		: 'repeat';

STRING : '"' .*?  '"';

OR 		: 	'||';
AND 	: 	'&&';
EQ 		: 	'==';
NEQ 	: 	'!=';
GT 		: 	'>';
LT 		: 	'<';
GTEQ 	: 	'>=';
LTEQ 	: 	'<=';

PLUS	:	'+';
MINUS	: 	'-';
MULT	:	'*';
DIVI	: 	'/';
EXPO	:   '^';
MOD 	: 	'%';
NOT 	: 	'!';
DOT 	:	'.';

ASG		:	'=';
RETURN 	: 	'return';

SEMICOLON 		: ';';
COMMA 	  		: ',';
PR_OPN			: '(';
PR_CLS 			: ')';
BR_OPN			: '{';
BR_CLS 			: '}';
COLON			: ':';


BOOLEAN 	: 'true' | 'false';

K_FLOAT 	: 'float';
K_INT   	: 'int';
K_BOOL  	: 'boolean';
K_STR   	: 'string';
K_DATETIME 	: 'datetime';
K_VOID  	: 'void';
K_DATE		: 'date';
K_TIME		: 'time';


IF 		: 'if';
ELSE 	: 'else';
LOG 	: 'log';

JAN : [Jj][Aa][Nn] ;
FEB : [Ff][Ee][Bb] ;
MAR : [Mm][Aa][Rr] ;
APR : [Aa][Pp][Rr] ;
MAY : [Mm][Aa][Yy] ; 
JUN : [Jj][Uu][Nn] ;
JUL : [Jj][Uu][Ll] ;
AUG : [Aa][Uu][Gg] ;
SEP : [Ss][Ee][Pp] ; 
OCT : [Oo][Cc][Tt] ; 
NOV : [Nn][Oo][Vv] ;
DEC : [Dd][Ee][Cc] ;

SECOND 	: 'SECOND';
MINUTE	: 'MINUTE';
HOUR   	: 'HOUR';

ID	: 	[a-zA-Z_][a-zA-Z0-9_]*;
INT1 	: DIGIT;
INT4 : DIGIT DIGIT DIGIT DIGIT;
INT	:	DIGIT+;
FLOAT : DIGIT+ '.' DIGIT+;
DIGIT : [0-9];

SINGLE_QUOTE	: '\'';
SEPARATOR 		: [\-] ;

SL_COMMENT :   '//' .*? '\n' -> skip;

WS	:	[ \t\n\r]+ -> skip;

OTHER
 : . 
 ;