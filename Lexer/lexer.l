%option noyywrap
%option never-interactive
%option yylineno
%option stack

%{
#include <string>
#include <stdio.h>
#include <stdint.h>
#include <inttypes.h>
#include <stdlib.h>
#include <errno.h>
#include <math.h>
#include <algorithm>
#include <iostream>
#include "parser.tab.h"
#define YY_DECL int yylex()

void _print(const std::string & message)         {
    //std::cout << "lex:" << message << std::endl;
}

std::string yytext_str() {
    return std::string(yytext);
}

char* stdToCstr(std::string str) {
    char* cstr = (char*) malloc((str.length()+1)*(sizeof(char)));
    strcpy(cstr, str.c_str());
    return cstr;
}

char* copyCstr(char* str) {
    char* cstr = (char*) malloc((strlen(str)+1)*(sizeof(char)));
    strcpy(cstr, str);
    return cstr;
}

int delayedReturn = -100;

%}

%x MULTI_LINE_COMMENT
%x MULTI_LINE_RAW_STR_SQ
%x MULTI_LINE_RAW_STR_DQ
LETTER[A-Za-z]
DIGIT[0-9]
HEX_DIGIT[0-9A-Fa-f]
EXPONENT[eE][+-]?[0-9]+

%x SINGLE_LINE_STR_SQ
%x SINGLE_LINE_STR_DQ
%x MULTI_LINE_STR_SQ
%x MULTI_LINE_STR_DQ
%s STR_INTERPOLATION
%x SIMPLE_INTERPOLATION

%%

%{
if(delayedReturn != -100){
    int tmp = delayedReturn; 
    delayedReturn = -100;
    return tmp;
}
std::string stringLiteral;
std::string currentComment;
int multiLineCommentNesting;
%}


\/\/[^\n\r]*[\n\r]?         { _print("SINGLE LINE COMMENT: " + yytext_str());}

\/\*                                {multiLineCommentNesting = 1; currentComment[0] = 0; BEGIN(MULTI_LINE_COMMENT); currentComment+=yytext;}
<MULTI_LINE_COMMENT>\/+\*           {multiLineCommentNesting++; currentComment+=yytext;}
<MULTI_LINE_COMMENT>[^*/]*          {currentComment+=yytext;}
<MULTI_LINE_COMMENT>\/+[^*/]*       {currentComment+=yytext;}
<MULTI_LINE_COMMENT>\*+[^*/]*       {currentComment+=yytext;}
<MULTI_LINE_COMMENT>\*+\/           {
    multiLineCommentNesting--;
    currentComment+=yytext;
    if(multiLineCommentNesting == 0)
    {
        _print("MULTI LINE COMMENT: " + currentComment); BEGIN(INITIAL);
    }
}
<MULTI_LINE_COMMENT><<EOF>>                                                      {
    _print("ERR: Comment isn't properly enclosed: " + currentComment);
    return YYerror;
}

\'                                                                                                  {stringLiteral.clear(); yy_push_state(SINGLE_LINE_STR_SQ);}
\"                                                                                                  {stringLiteral.clear(); yy_push_state(SINGLE_LINE_STR_DQ);}
\'\'\'                                                                                              {stringLiteral.clear(); yy_push_state(MULTI_LINE_STR_SQ);}
\'\'\'(\\?[\t ])+\\?(\n|\r)                                                                         {stringLiteral.clear(); yy_push_state(MULTI_LINE_STR_SQ);}
\"\"\"                                                                                              {stringLiteral.clear(); yy_push_state(MULTI_LINE_STR_DQ);}
\"\"\"(\\?[\t ])+\\?(\n|\r)                                                                         {stringLiteral.clear(); yy_push_state(MULTI_LINE_STR_DQ);}
<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ,MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ>[^$"'\n\r\\]*            {stringLiteral += yytext;}
<MULTI_LINE_STR_DQ,SINGLE_LINE_STR_DQ>\'*                                                           {stringLiteral += yytext;}
<MULTI_LINE_STR_SQ,SINGLE_LINE_STR_SQ>\"*                                                           {stringLiteral += yytext;}
<MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ>[\n\r]*                                                        {stringLiteral += yytext;}
<MULTI_LINE_STR_SQ>\'{1,2}                                                                          {stringLiteral += yytext;}
<MULTI_LINE_STR_DQ>\"{1,2}                                                                          {stringLiteral += yytext;}
<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ,MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ>\\n                      {stringLiteral += '\n';}
<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ,MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ>\\r                      {stringLiteral += '\r';}
<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ,MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ>\\f                      {stringLiteral += '\f';}
<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ,MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ>\\b                      {stringLiteral += '\b';}
<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ,MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ>\\t                      {stringLiteral += '\t';}
<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ,MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ>\\v                      {stringLiteral += '\v';}
<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ,MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ>\\x{HEX_DIGIT}{2}        {
    char hex[3];
    hex[0] = yytext[2];
    hex[1] = yytext[3];
    hex[2] = 0;
    stringLiteral += strtol(hex, NULL, 16);
}

<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ,MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ>\\x                      {
    _print("ERR: \\x escape sequence should be followed by two hexadecimal digits; Just the 'x' char will be used.");
    stringLiteral += yytext[1];
    return YYerror; //Или нет?
}

<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ,MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ>\\.                      {stringLiteral += yytext[1];}


<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ,MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ>\$\{                     {
    _print("STRING LITERAL: " + stringLiteral);
    _print("STR INTERPOLATION CONCAT (START)");
    yy_push_state(STR_INTERPOLATION);

    yylval.stringval = stdToCstr(stringLiteral);
    delayedReturn = INTERPOLATION_CONCAT;    
    return STRING_LITERAL;
}

<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ,MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ>\$                       {
    _print("ERR: Interpolation expected at: " + stringLiteral);
    return YYerror;
}

<STR_INTERPOLATION>\}                                                                               {
    stringLiteral.clear(); 
    _print("STR INTERPOLATION CONCAT (END)");
    yy_pop_state();
    
    return INTERPOLATION_CONCAT;
}

<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ,MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ>\$/((_|{LETTER})([_$]|{LETTER}|{DIGIT})*)                       {
    _print("STRING LITERAL: " + stringLiteral);
    _print("STR INTERPOLATION CONCAT (START)");
    yy_push_state(SIMPLE_INTERPOLATION);
    
    yylval.stringval = stdToCstr(stringLiteral);
    delayedReturn = INTERPOLATION_CONCAT;    
    return STRING_LITERAL;
}

<SIMPLE_INTERPOLATION>((_|{LETTER})([_$]|{LETTER}|{DIGIT})*)                                                                               {
    stringLiteral.clear(); 
    _print("IDENTIFIER: " + yytext_str());
    _print("STR INTERPOLATION CONCAT (END)");
    yy_pop_state();

    yylval._identifier_node=create_identifier_node(false, copyCstr(yytext));
    delayedReturn = INTERPOLATION_CONCAT;    
    return IDENTIFIER; // мб надо вернуть IDENTIFIER ??? Конечно !!! Невероятно!!!!
}

<SINGLE_LINE_STR_SQ>\'                                                         {
    _print("STRING LITERAL: " + stringLiteral);
    yy_pop_state();

    yylval.stringval = stdToCstr(stringLiteral);    
    return STRING_LITERAL;
}
<SINGLE_LINE_STR_DQ>\"                                                         {
    _print("STRING LITERAL: " + stringLiteral);
    yy_pop_state();

    yylval.stringval = stdToCstr(stringLiteral);    
    return STRING_LITERAL;
}
<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ>[\n\r]                                  {
    _print("ERR: String isn't properly enclosed: " + stringLiteral);
    yy_pop_state();
    return YYerror;
}
<MULTI_LINE_STR_SQ>\'\'\'                                                      {
    _print("STRING LITERAL: " + stringLiteral);
    yy_pop_state();

    yylval.stringval = stdToCstr(stringLiteral);    
    return STRING_LITERAL;
}
<MULTI_LINE_STR_DQ>\"\"\"                                                      {
    _print("STRING LITERAL: " + stringLiteral);
    yy_pop_state();

    yylval.stringval = stdToCstr(stringLiteral);    
    return STRING_LITERAL;
}
<SINGLE_LINE_STR_SQ,SINGLE_LINE_STR_DQ,MULTI_LINE_STR_SQ,MULTI_LINE_STR_DQ,MULTI_LINE_RAW_STR_SQ,MULTI_LINE_RAW_STR_DQ><<EOF>>                                                      {
    _print("ERR: String isn't properly enclosed: " + stringLiteral);
    yy_pop_state();
    return YYerror;
}

r\'\'\'                               {stringLiteral.clear(); BEGIN(MULTI_LINE_RAW_STR_SQ);}
<MULTI_LINE_RAW_STR_SQ>\'\'\'         {
    _print("STRING LITERAL: " + stringLiteral); BEGIN(INITIAL);
    
    yylval.stringval = stdToCstr(stringLiteral);
    return STRING_LITERAL;
}

<MULTI_LINE_RAW_STR_SQ>(.|\n)         {stringLiteral += yytext;}

r\"\"\"                               {stringLiteral.clear(); BEGIN(MULTI_LINE_RAW_STR_DQ);}

<MULTI_LINE_RAW_STR_DQ>\"\"\"         {
    _print("STRING LITERAL: " + stringLiteral); BEGIN(INITIAL);

    yylval.stringval = stdToCstr(stringLiteral);
    return STRING_LITERAL;
}

<MULTI_LINE_RAW_STR_DQ>(.|\n)         {stringLiteral += yytext;}

r\'[^\'\r\n]*\'|r\"[^\"\r\r\n]*\"     {
    std::string tmp = yytext_str();
    tmp.erase(0, 2);
    tmp.erase(tmp.size() - 1, 1);
    _print("RAW SINGLE LINE STRING: " + tmp);

    yylval.stringval = stdToCstr(tmp);
    return STRING_LITERAL;
}

r\'[^\'\r\n]*|r\"[^\"\r\r\n]*         {
    std::string tmp = yytext_str(); 
    tmp.erase(0, 2); 
    _print("ERR: String isn't properly enclosed: " + tmp);
    return YYerror;
}


assert          {_print("Keyword ASSERT: " + yytext_str()); return ASSERT;}
break           {_print("Keyword BREAK: " + yytext_str()); return BREAK;}
case            {_print("Keyword CASE: " + yytext_str()); return CASE;} 
catch           {_print("Keyword CATCH: " + yytext_str()); return CATCH;}
class           {_print("Keyword CLASS: " + yytext_str()); return CLASS;}
const           {_print("Keyword CONST: " + yytext_str()); return CONST;} 
continue        {_print("Keyword CONTINUE: " + yytext_str()); return CONTINUE;}
default         {_print("Keyword DEFAULT: " + yytext_str()); return DEFAULT;}
do              {_print("Keyword DO: " + yytext_str()); return DO;}
else            {_print("Keyword ELSE: " + yytext_str()); return ELSE;}
enum            {_print("Keyword ENUM: " + yytext_str()); return ENUM;}
extends         {_print("Keyword EXTENDS: " + yytext_str()); return EXTENDS;}
false           {
    _print("Keyword FALSE: " + yytext_str());
    yylval.boolval=false;
    return BOOLEAN_LITERAL;
}
final           {_print("Keyword FINAL: " + yytext_str()); return FINAL;}
finally         {_print("Keyword FINALLY: " + yytext_str()); return FINALLY;}

for             {_print("Keyword FOR: " + yytext_str()); return FOR;}
if              {_print("Keyword IF: " + yytext_str()); return IF;}
in              {_print("Keyword IN: " + yytext_str()); return IN;}
is              {_print("Keyword IS: " + yytext_str()); return IS;}
new             {_print("Keyword NEW: " + yytext_str()); return NEW;}
null            {_print("Keyword NULL: " + yytext_str()); return NULL_;}
rethrow         {_print("Keyword RETHROW: " + yytext_str()); return RETHROW;}
return          {_print("Keyword RETURN: " + yytext_str()); return RETURN;}
super           {_print("Keyword SUPER: " + yytext_str()); return SUPER;}
switch          {_print("Keyword SWITCH: " + yytext_str()); return SWITCH;}
this            {_print("Keyword THIS: " + yytext_str()); return THIS;}
throw           {_print("Keyword THROW: " + yytext_str()); return THROW;}
true            {
    _print("Keyword TRUE: " + yytext_str()); 
    yylval.boolval=true;
    return BOOLEAN_LITERAL;
}
try             {_print("Keyword TRY: " + yytext_str()); return TRY;}
var             {_print("Keyword VAR: " + yytext_str()); return VAR;}
void            {_print("Keyword VOID: " + yytext_str()); return VOID;}
while           {_print("Keyword WHILE: " + yytext_str()); return WHILE;}
with            {_print("Keyword WITH: " + yytext_str()); return WITH;}

abstract        {_print("Built-in identifier ABSTRACT: " + yytext_str());   yylval._identifier_node=create_identifier_node(true, (char*)"abstract"); return ABSTRACT;}
as              {_print("Built-in identifier AS: " + yytext_str());         yylval._identifier_node=create_identifier_node(true, (char*)"as"); return AS;}
covariant       {_print("Built-in identifier COVARIANT: " + yytext_str());  yylval._identifier_node=create_identifier_node(true, (char*)"covariant"); return COVARIANT;}
deferred        {_print("Built-in identifier DEFERRED: " + yytext_str());   yylval._identifier_node=create_identifier_node(true, (char*)"deferred"); return DEFERRED;}
dynamic         {_print("Built-in identifier DYNAMIC: " + yytext_str());    yylval._identifier_node=create_identifier_node(true, (char*)"dynamic"); return DYNAMIC;}
export          {_print("Built-in identifier EXPORT: " + yytext_str());     yylval._identifier_node=create_identifier_node(true, (char*)"export"); return EXPORT;}
external        {_print("Built-in identifier EXTERNAL: " + yytext_str());   yylval._identifier_node=create_identifier_node(true, (char*)"external"); return EXTERNAL;}
extension       {_print("Built-in identifier EXTENSION: " + yytext_str());  yylval._identifier_node=create_identifier_node(true, (char*)"extension"); return EXTENSION;}
factory         {_print("Built-in identifier FACTORY: " + yytext_str());    yylval._identifier_node=create_identifier_node(true, (char*)"factory"); return FACTORY;}
Function        {_print("Built-in identifier FUNCTION: " + yytext_str());   yylval._identifier_node=create_identifier_node(true, (char*)"Function"); return FUNCTION;}
get             {_print("Built-in identifier GET: " + yytext_str());        yylval._identifier_node=create_identifier_node(true, (char*)"get"); return GET;}
implements      {_print("Built-in identifier IMPLEMENTS: " + yytext_str()); yylval._identifier_node=create_identifier_node(true, (char*)"implements"); return IMPLEMENTS;}
import          {_print("Built-in identifier IMPORT: " + yytext_str());     yylval._identifier_node=create_identifier_node(true, (char*)"import"); return IMPORT;}
interface       {_print("Built-in identifier INTERFACE: " + yytext_str());  yylval._identifier_node=create_identifier_node(true, (char*)"interface"); return INTERFACE;}
late            {_print("Built-in identifier LATE: " + yytext_str());       yylval._identifier_node=create_identifier_node(true, (char*)"late"); return LATE;}
library         {_print("Built-in identifier LIBRARY: " + yytext_str());    yylval._identifier_node=create_identifier_node(true, (char*)"library"); return LIBRARY;}
mixin           {_print("Built-in identifier MIXIN: " + yytext_str());      yylval._identifier_node=create_identifier_node(true, (char*)"mixin"); return MIXIN;}
operator        {_print("Built-in identifier OPERATOR: " + yytext_str());   yylval._identifier_node=create_identifier_node(true, (char*)"operator"); return OPERATOR;}
part            {_print("Built-in identifier PART: " + yytext_str());       yylval._identifier_node=create_identifier_node(true, (char*)"part"); return PART;}
required        {_print("Built-in identifier REQUIRED: " + yytext_str());   yylval._identifier_node=create_identifier_node(true, (char*)"required"); return REQUIRED;}
set             {_print("Built-in identifier SET: " + yytext_str());        yylval._identifier_node=create_identifier_node(true, (char*)"set"); return SET;}
static          {_print("Built-in identifier STATIC: " + yytext_str());     yylval._identifier_node=create_identifier_node(true, (char*)"static"); return STATIC;}
typedef         {_print("Built-in identifier TYPEDEF: " + yytext_str());    yylval._identifier_node=create_identifier_node(true, (char*)"typedef"); return TYPEDEF;}

async           {_print("Contextual keyword ASYNC: " + yytext_str()); return ASYNC;}
hide            {_print("Contextual keyword HIDE: " + yytext_str()); return HIDE;}
of              {_print("Contextual keyword OF: " + yytext_str()); return OF;}
on              {_print("Contextual keyword ON: " + yytext_str()); return ON;}
show            {_print("Contextual keyword SHOW: " + yytext_str()); return SHOW;}
sync            {_print("Contextual keyword SYNC: " + yytext_str()); return SYNC;}
await           {_print("Contextual keyword AWAIT: " + yytext_str()); return AWAIT;}
yield           {_print("Contextual keyword YIELD: " + yytext_str()); return YIELD;}

[\t \n\r]+      {_print("WHITESPACE");}


([_$]|{LETTER})([_$]|{LETTER}|{DIGIT})*     {
    _print("IDENTIFIER: " + yytext_str());
    yylval._identifier_node=create_identifier_node(false, copyCstr(yytext));
    return IDENTIFIER;
}    


{DIGIT}+        {
    uint64_t value = strtoull(yytext, NULL, 10);;
    if(errno == ERANGE)
    {
        _print("DECIMAL INTEGER LITERAL: " + yytext_str() + "(ERR: The integer literal " + yytext_str() + " can't be represented in 64 bits.)");
        return YYerror;
    }
    else
    {
        char buf[30];
        _print("DECIMAL INTEGER LITERAL: " + yytext_str() + " (Value = " + _ui64toa(value, buf, 10) + ")");
        yylval.intval = value; //не уверена в этом
        return INTEGER_LITERAL;
    }
}

(0x|0X){HEX_DIGIT}+     {
    uint64_t value = strtoull(yytext, NULL, 16);
    if(errno == ERANGE)
    {
        _print("HEX INTEGER LITERAL: " + yytext_str() + "(ERR: The integer literal " + yytext_str() + " can't be represented in 64 bits.)");
        return YYerror;
    }
    else
    {
        char buf[30];
        _print("HEX INTEGER LITERAL: " + yytext_str() + " (Value = " + _ui64toa(value, buf, 10) + ")");
        yylval.intval=value; // не уверена в этом
        return INTEGER_LITERAL;
    }
}

({DIGIT}*\.{DIGIT}+{EXPONENT}?|{DIGIT}+{EXPONENT})  {
    double value = strtod(yytext, NULL);
    if(errno == ERANGE)
    {
        //Не уверен насчет всех этих условий
        if(isinf(value)){
            _print("DECIMAL DOUBLE LITERAL: " + yytext_str() + " (Value = Infinity)");
            return YYerror;
        }
        else if(isnan(value)){
            _print("DECIMAL DOUBLE LITERAL: " + yytext_str() + " (Value = NaN)");
            return YYerror;
        }
        else if(!isnormal(value)){
            _print("DECIMAL DOUBLE LITERAL: " + yytext_str() + " (Value = de-/subnormal)");
            return YYerror;
        }
        else{
            _print("DECIMAL DOUBLE LITERAL: " + yytext_str() + "(ERR: The double literal " + yytext_str() + " can't be represented in 64 bits.)");
            return YYerror;
        }
    }
    else
    {   
        char buf[1000];
        sprintf(buf, "%f", value);
        _print("DECIMAL DOUBLE LITERAL: " + yytext_str() + " (Value = " + buf + ")");
        yylval.doubleval = value; //тоже не уверена
        return DOUBLE_LITERAL;
    }
}

\=\>        {_print("FUNCARROW LEXEM =>: " + yytext_str()); return FUNC_ARROW;}
\*\=        {_print("MULASSIGN LEXEM *=: " + yytext_str()); return MUL_ASSIGN;}
\/\=        {_print("DIVASSIGN LEXEM /=: " + yytext_str()); return DIV_ASSIGN;}
\~\/\=      {_print("TRUNCDIVASSIGN LEXEM ~/=: " + yytext_str()); return TRUNC_DIV_ASSIGN;}
\%\=        {_print("MODASSIGN LEXEM %=: " + yytext_str()); return MOD_ASSIGN;}
\+\=        {_print("ADDASSIGN LEXEM +=: " + yytext_str()); return ADD_ASSIGN;}
\-\=        {_print("SUBASSIGN LEXEM -=: " + yytext_str()); return SUB_ASSIGN;}
\<\<\=      {_print("SHIFTLASSIGN LEXEM <<=: " + yytext_str()); return SHIFTL_ASSIGN;}
\>\>\>\=    {_print("TSHIFTRASSIGN LEXEM >>>=: " + yytext_str()); return TSHIFTR_ASSIGN;}
\>\>\=      {_print("SHIFTRASSIGN LEXEM >>=: " + yytext_str()); return SHIFTR_ASSIGN;}
\&\=        {_print("ANDASSIGN LEXEM &=: " + yytext_str()); return AND_ASSIGN;}
\^\=        {_print("XORASSIGN LEXEM ^=: " + yytext_str()); return XOR_ASSIGN;}
\|\=        {_print("ORASSIGN LEXEM |=: " + yytext_str()); return OR_ASSIGN;}
\?\?\=      {_print("IFNULLASSIGN LEXEM ??=: " + yytext_str()); return IFNULL_ASSIGN;}

\=\=        {_print("EQUALITY LEXEM ==: " + yytext_str()); return EQ;}
\!\=        {_print("INEQUALITY LEXEM !=: " + yytext_str()); return NEQ;}

\&\&        {_print("BOOLAND LEXEM &&: " + yytext_str()); return AND;}
\|\|        {_print("BOOLOR LEXEM ||: " + yytext_str()); return OR;}

\>\=         {_print("GREATEREQ LEXEM >=: " + yytext_str()); return GREATER_EQ;}
\>           {_print("ARROWRIGHT LEXEM >: " + yytext_str()); return '>';}
\<\=         {_print("LESSEQ LEXEM <=: " + yytext_str()); return LESS_EQ;}
\<           {_print("ARROWLEFT LEXEM <: " + yytext_str()); return '<';}
\?\?         {_print("IFNULL LEXEM ??: " + yytext_str()); return IFNULL;}
\+\+         {_print("UNARY INCREMENT LEXEM ++: " + yytext_str()); return INC;}    
\-\-         {_print("UNARY DENCREMENT LEXEM --: " + yytext_str()); return DEC;}
\+           {_print("PLUS LEXEM +: " + yytext_str()); return '+';}
\-           {_print("MINUS LEXEM -: " + yytext_str()); return '-';}
\*           {_print("MUL LEXEM *: " + yytext_str()); return '*';}
\/           {_print("DIV LEXEM /: " + yytext_str()); return '/';}
\%           {_print("MOD LEXEM %: " + yytext_str()); return '%';}
\~\/         {_print("TRUNCDIV LEXEM ~/: " + yytext_str()); return TRUNCDIV;}
\=           {_print("EQ LEXEM =: " + yytext_str()); return '=';}
\&           {_print("AMPERSAND LEXEM &: " + yytext_str()); return '&';}
\^           {_print("CIRCUMFLEX LEXEM ^: " + yytext_str()); return '^';}
\|           {_print("BAR LEXEM |: " + yytext_str()); return '|';}
\!           {_print("EXCLAMATION LEXEM !: " + yytext_str()); return '!';}
\?           {_print("QUESTION LEXEM ?: " + yytext_str()); return '?';}
\~           {_print("TILDE LEXEM ~: " + yytext_str()); return '~';}

\(           {_print("PARL LEXEM (: " + yytext_str()); return '(';}
\)           {_print("PARR LEXEM ): " + yytext_str()); return ')';}

\{           {yy_push_state(INITIAL); _print("BRACEL LEXEM {: " + yytext_str()); return '{';}
\}           {yy_pop_state(); _print("BRACER LEXEM }: " + yytext_str()); return '}';}

\[           {_print("BRACKETL LEXEM [: " + yytext_str()); return '[';}
\]           {_print("BRACKETR LEXEM ]: " + yytext_str()); return ']';}

\.           {_print("DOT LEXEM .: " + yytext_str()); return '.';}
\,           {_print("COMMA LEXEM .: " + yytext_str()); return ',';}
\;           {_print("SEMICOLON LEXEM ;: " + yytext_str());  return ';';}
\:           {_print("COLON LEXEM :: " + yytext_str()); return ':';}

.           {_print("UNKNOWN SYMBOL: " + yytext_str());}




%%

/* int main(int argc, char** argv)         {
    if (argc > 1)           {
        fopen_s(&yyin, argv[1], "r");
        yylex();
    }
    else            {
        _print("File not found");
    }
} */