grammar TransformationGrammar;

program : (import_name)* main EOF
	; 

main : PROGRAM ID PR_OPN programparameters? PR_CLS block
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

type : K_FLOAT | K_INT  | K_BOOL | K_STR | K_VOID | K_DATETIME | K_TIME | K_DATE;


sentence : block									# ref_block	 
			| atrib_dec								# ref_attribute_def
			| unit_dec								# ref_unit_def
			| var_dec							  	# ref_var_def
			| if_stat 								# ref_if_start
			| display								# ref_display
			| save									# ref_save
			| timer									# ref_event
			| sched_aggregate						# ref_sched_aggregate
			| repeat								# ref_repeat
			| RETURN expression SEMICOLON		   	# ref_return
			| state_assign                          # ref_state_assign
			| assign								# ref_assign
			| log 									# ref_log
			| OTHER {System.err.println("unknown char: " + $OTHER.text);} #ref_other
			;

var_dec		: VARIABLE type ID (ASG expression)? SEMICOLON
	;

atrib_dec 	: ATTRIBUTE (TREND)? type id1=ID  (ASG expression)? (UNIT id2=ID)? SEMICOLON
	;

unit_dec	: UNIT id1=ID STRING SEMICOLON
	; 

assign 		: ID ASG expression SEMICOLON
	; 

state_assign : STATE ASG POSSIBLE_STATES SEMICOLON
    ;

display  	: DISPLAY PR_OPN deviceId=ID ',' toShow=expression PR_CLS SEMICOLON
	;

save		: SAVE PR_OPN expressionList? PR_CLS SEMICOLON
	;

timer		: TIMER PR_OPN TIMEUNIT COMMA time=(INT | INT1 | DIGIT | INT4 ) COMMA pack=ID PR_CLS SEMICOLON
	;
	
sched_aggregate		: SCHED_AGGREGATE PR_OPN TIMEUNIT COMMA time=(INT | INT1 | DIGIT | INT4 ) COMMA pack=ID PR_CLS SEMICOLON
	;

repeat		: REPEAT PR_OPN TIMEUNIT COMMA time=(INT | INT1 | DIGIT | INT4 ) COMMA pack=ID PR_CLS SEMICOLON
	;
	
block :  BR_OPN (sentence)* BR_CLS  // Possibly Empty Block of Sentences.
	;

if_stat : IF condition_block (ELSE IF condition_block)* (ELSE block)?
 	;

condition_block
 : expression block
 	;

log : LOG expression SEMICOLON
 ;

expression : expression EXPO expression  							# Expon
 			| round													# ref_round 			
			| MINUS expression                     					# unaryMinusExpr
 			| NOT expression                        				# notExpr
			| token													# ref_split
			| substring                                             # ref_substring
			| startwith                                             # ref_startwith 
			| status												# ref_status
			| state                                                 # ref_state
		    | expression op=( MULT | DIVI | MOD ) expression  		# Mult
			| expression op=(PLUS | MINUS) expression  				# AddSub
 			| expression op=(LTEQ | GTEQ | LT | GT) expression		# relationalExpr
 			| expression op=(EQ | NEQ) expression		            # equalityExpr
 			| expression AND expression		                        # andExpr
 			| expression OR expression		                        # orExpr
 			| valid_states											# ref_valid_states
			| atom													# ref_atom
			;

token 		: TOKEN PR_OPN ex1=expression ',' ex2=expression PR_CLS
	;

substring   : TOKEN PR_OPN ex1=expression ',' ex2=expression ',' ex3=expression PR_CLS
    ;

round : ROUND PR_OPN expression COMMA INT1 PR_CLS
	; 

startwith   : STARTWITH  PR_OPN ex1=expression ',' ex2=expression PR_CLS
    ;

status		: STATUS DOT ID
	;

state       : STATE
    ;

valid_states : SYSTEM_DOWN | POSSIBLE_STATES
			;

atom :		ID								# Var
			| TEXT_DATE						# Date
			| TEXT_TIME						# Time
			| TEXT_DATETIME					# Datetime
			| INT1							# digit
			| INT4							# Year
			| INT  							# Integer
			| FLOAT							# Float
			| BOOLEAN					    # Boolean
			| STRING						# Str
			| PR_OPN expression PR_CLS    	# Parens
			;

expressionList : expression (COMMA expression)*   // argument list.
			;

TEXT_DATE : K_DATE PR_OPN SINGLEDATE_QUOTED_TEXT PR_CLS;

TEXT_TIME:  K_TIME PR_OPN SINGLETIME_QUOTED_TEXT PR_CLS;

TEXT_DATETIME : K_DATETIME PR_OPN SINGLEDATETIME_QUOTED_TEXT PR_CLS
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

PROGRAM 	: 'transform';
ATTRIBUTE 	: 'attr';
VARIABLE 	: 'var';
UNIT 		: 'unit';
DISPLAY 	: 'display';
SAVE		: 'save';
TOKEN 		: 'token';
STATUS 		: 'STATUS';
STATE       : 'STATE';
TREND		: 'trend';
ROUND 		: 'round'; 
IMPORT 		: 'import'; 
AS 			: 'as';
TIMER		: 'timer';
SCHED_AGGREGATE	: 'sched_aggregate';
REPEAT		: 'repeat';
STARTWITH   : 'start_with';

POSSIBLE_STATES : OPERATIVE | SCHED_DOWN | UNSCHED_DOWN | INITIALIZING; 

OR 		: 	'OR';
AND 	: 	'AND';
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

OPERATIVE 		: 'operative';
SCHED_DOWN  	: 'sched_down';
UNSCHED_DOWN 	: 'unsched_down';
SYSTEM_DOWN 	: 'system_down';
INITIALIZING	: 'initializing';


K_FLOAT 		: 'float';
K_INT   		: 'int';
K_BOOL  		: 'boolean';
K_STR   		: 'string';
K_VOID  		: 'void';
K_DATE			: 'date';
K_TIME			: 'time';
K_DATETIME 		: 'datetime';

SEMICOLON 		: ';';
COMMA 	  		: ',';
PR_OPN			: '(';
PR_CLS 			: ')';
BR_OPN			: '{';
BR_CLS 			: '}';
COLON			: ':';


BOOLEAN 	: 'true' | 'false';

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

STRING : '"' .*?  '"';

SECOND 	: 'SECOND';
MINUTE	: 'MINUTE';
HOUR   	: 'HOUR';

ID		: [a-zA-Z_][a-zA-Z0-9_]*;
NAME 	: [a-zA-Z_][a-zA-Z0-9_]*;
INT1 	: DIGIT;
INT4 	: DIGIT DIGIT DIGIT DIGIT;
INT		: DIGIT+;
FLOAT 	: DIGIT+ '.' DIGIT+;
DIGIT 	: [0-9];

SINGLE_QUOTE	: '\'';
SEPARATOR 		: [\-] ;

SL_COMMENT :   '//' .*? '\n' -> skip;

WS	:	[ \t\n\r]+ -> skip;

OTHER
 : . 
 ;