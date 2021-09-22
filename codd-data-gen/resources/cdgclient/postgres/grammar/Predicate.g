grammar Predicate;

// In the antlr ws cd grammar
//java -jar ../library/antlr-4.5.3-complete.jar Predicate.g ; mv *.java ../src/antlr ; ls ../src/antlr/*.java | xargs sed -i '1s;^;package antlr\;;'

// (dt.icurrentprice = dtt.icurrentpricee)
// (dt.icurrentprice = 0.53)
// (icurrentpr_ice < 550)
// (i_category = 'Jewelry')
// ((i_category = 'Jewe_l-ry2') OR (i_category = '2Jewelry') OR (i_category = 'Jewelry2 #') OR (i_category = '# Jewelry 2# 2A#'))
// (i_category = 'Women'::bpchar)
// (i_current_price >= '22'::numeric)
// (i_current_price >= '22'::integer)
// (d_date >= '2001-01-12'::date)
// (d_date >= '1998-03-09 00:00:00'::timestamp without time zone)
// (d_date >= '1998-03-09 00:00:00'::timestamp without time zone)
// (i_category = ANY ('{Jewelry,Sports,Books}'::bpchar[]))
// (((i_category)::text = ANY ('{Jewelry,Sports,Books}'::text[])) OR (i_brand = ANY ('{\"sch #14\",\"sch #7\"}'::bpchar[])))
// ((i_current_price >= '22'::numeric) AND (i_current_price <= '52'::numeric) AND (i_manufact_id = ANY ('{678,964,918,849}'::integer[])))
// ((i_current_price >= 0.99) AND (i_current_price <= 1.49))
// ((i_current_price >= 0.99) OR (i_current_price <= 1.49))
// ((i_current_price >= 0.99) AND (i_current_price <= 1.49) AND (dt.icurrentprice = 0.53))
// ((i_current_price >= 0.99) AND ((i_current_price <= 1.49) OR (dt.icurrentprice = 0.53)))
// (i_manufact = i1.i_manufact)
// ((i_manufact = i1.i_manufact) AND (((i_category = 'Women'::bpchar) AND ((i_color = 'orchid'::bpchar) OR (i_color = 'papaya'::bpchar)) AND ((i_units = 'Pound'::bpchar) OR (i_units = 'Lb'::bpchar)) AND ((i_size = 'petite'::bpchar) OR (i_size = 'medium'::bpchar))) OR ((i_category = 'Women'::bpchar) AND ((i_color = 'burlywood'::bpchar) OR (i_color = 'navy'::bpchar)) AND ((i_units = 'Bundle'::bpchar) OR (i_units = 'Each'::bpchar)) AND ((i_size = 'NA'::bpchar) OR (i_size = 'extra_large'::bpchar))) OR ((i_category = 'Men'::bpchar) AND ((i_color = 'bisque'::bpchar) OR (i_color = 'azure'::bpchar)) AND ((i_units = 'NA'::bpchar) OR (i_units = 'Tsp'::bpchar)) AND ((i_size = 'small'::bpchar) OR (i_size = 'large'::bpchar))) OR ((i_category = 'Men'::bpchar) AND ((i_color = 'chocolate'::bpchar) OR (i_color = 'cornflower'::bpchar)) AND ((i_units = 'Bunch'::bpchar) OR (i_units = 'Gross'::bpchar)) AND ((i_size = 'petite'::bpchar) OR (i_size = 'medium'::bpchar))) OR ((i_category = 'Women'::bpchar) AND ((i_color = 'salmon'::bpchar) OR (i_color = 'midnight'::bpchar)) AND ((i_units = 'Oz'::bpchar) OR (i_units = 'Box'::bpchar)) AND ((i_size = 'petite'::bpchar) OR (i_size = 'medium'::bpchar))) OR ((i_category = 'Women'::bpchar) AND ((i_color = 'snow'::bpchar) OR (i_color = 'steel'::bpchar)) AND ((i_units = 'Carton'::bpchar) OR (i_units = 'Tbl'::bpchar)) AND ((i_size = 'NA'::bpchar) OR (i_size = 'extra_large'::bpchar))) OR ((i_category = 'Men'::bpchar) AND ((i_color = 'purple'::bpchar) OR (i_color = 'gainsboro'::bpchar)) AND ((i_units = 'Dram'::bpchar) OR (i_units = 'Unknown'::bpchar)) AND ((i_size = 'small'::bpchar) OR (i_size = 'large'::bpchar))) OR ((i_category = 'Men'::bpchar) AND ((i_color = 'metallic'::bpchar) OR (i_color = 'forest'::bpchar)) AND ((i_units = 'Gram'::bpchar) OR (i_units = 'Ounce'::bpchar)) AND ((i_size = 'petite'::bpchar) OR (i_size = 'medium'::bpchar)))))
// ((s_store_name)::text = 'ese'::text)



// Parser Rules

predicate : conditionStr ;
conditionStr : OPEN condition CLOSE ;
condition : andedCondition | oredCondition | joinCondition | simpleCondition;
andedCondition : conditionStr (and conditionStr)+;
oredCondition: conditionStr (or conditionStr)+;
joinCondition: columnname SPACE '=' SPACE columnname;
simpleCondition : simpleNumCondition | simpleStrCondition | simpleDateCondition | simpleINCondition;
simpleNumCondition: columnname SPACE symbol SPACE number;
simpleStrCondition: OPEN columnname CLOSE '::text' SPACE symbol SPACE QUOTE stringliteral QUOTE '::text' | columnname SPACE symbol SPACE (QUOTE stringliteral QUOTE | QUOTE stringliteral QUOTE '::bpchar' | QUOTE INTEGER QUOTE '::numeric' | QUOTE INTEGER QUOTE '::integer');
simpleDateCondition: columnname SPACE symbol SPACE ( QUOTE DATE QUOTE '::date' | QUOTE TIMESTAMP QUOTE '::timestamp without time zone');
simpleINCondition: OPEN columnname CLOSE '::text' SPACE '=' SPACE any OPEN QUOTE '{' stringliteral (','stringliteral)* '}' QUOTE '::text[]' CLOSE | columnname SPACE '=' SPACE any OPEN (QUOTE '{' stringliteral (','stringliteral)* '}' QUOTE '::bpchar[]' | QUOTE '{' INTEGER (','INTEGER)* '}' QUOTE '::numeric[]' | QUOTE '{' INTEGER (','INTEGER)* '}' QUOTE '::integer[]') CLOSE;

and : SPACE 'AND' SPACE;
or : SPACE 'OR' SPACE;
any : 'ANY' SPACE;
columnname : TEXT (DOT TEXT)?;
symbol : '=' | '<' | '>' | '>=' | '<=';
number : INTEGER | DECIMAL;

stringliteral : TEXT | (INTEGER|SPACE|'#'|'\\'|'\"')*TEXT*(SPACE)(TEXT|INTEGER|SPACE|'#'|'\\'|'\"')*;	//TEXT | TEXTWITHSPACES


// Lexer Rules

OPEN : '(' ;
CLOSE : ')' ;
QUOTE : '\'' ;

DOT : '.' ;

TEXT : ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'-')*('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'-')* ;
SPACE : ' ' ;

INTEGER : ('-')?('0'..'9')+ ;
DECIMAL : ('-')?('0'..'9')+('.')('0'..'9')+ ;

DATE : ('0'..'9')('0'..'9')('0'..'9')('0'..'9')'-'('0'..'9')('0'..'9')'-'('0'..'9')('0'..'9') ;
TIMESTAMP : ('0'..'9')('0'..'9')('0'..'9')('0'..'9')'-'('0'..'9')('0'..'9')'-'('0'..'9')('0'..'9')' '('0'..'9')('0'..'9')':'('0'..'9')('0'..'9')':'('0'..'9')('0'..'9') ;

//WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ -> skip ;