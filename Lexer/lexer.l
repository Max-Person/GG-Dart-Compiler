%option noyywrap
%option never-interactive
%option yylineno

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
#define YY_DECL int yylex()

void _print(const std::string & message)         {
    std::cout << message << std::endl;
}

std::string yytext_str() {
    return std::string(yytext);
}

char* processEsc(const char* str) {
	int len = strlen(str) + 1;
	char* fin = (char*)malloc(sizeof(char) * len);

	for (int i = 0; i < len; ++i) {
		fin[i] = 0;
	}

	const char* end = str + strlen(str);
	const char* start = str;
	while (start != end) {
		char* slash = strchr((char*)start, '\\');
		if (slash == NULL) {
			strcat(fin, start);
			return fin;
		}
		else {
			strncat(fin, start, slash - start);
		}

		start = slash;
		char esc = start[1];
		switch (esc) {
		case 'n':
			fin[strlen(fin)] = '\n';
			start += 2;
			break;
		case 'r':
			fin[strlen(fin)] = '\r';
			start += 2;
			break;
		case 'f':
			fin[strlen(fin)] = '\f';
			start += 2;
			break;
		case 'b':
			fin[strlen(fin)] = '\b';
			start += 2;
			break;
		case 't':
			fin[strlen(fin)] = '\t';
			start += 2;
			break;
		case 'v':
			fin[strlen(fin)] = '\v';
			start += 2;
			break;
		case 'x':
			char hex[3];
			hex[0] = start[2];
			hex[1] = start[3];
			hex[2] = 0;
			fin[strlen(fin)] = strtol(hex, NULL, 16);
			start += 4;
			break;
		default:
			fin[strlen(fin)] = esc;
			start += 2;
		}
	}
	return fin;
}

char* trimAndProcess(int trimStart, int trimEnd, char* str){
    char* content = str+trimStart; //не использовать первый символ (кавычку)
    content[strlen(content)-trimEnd] = 0; //удалить последний символ (закрывающую кавычку)
    char* fin = processEsc(content); //превратить спецсимволы в символы
    return fin;
}

%}

%x MULTI_LINE_COMMENT 
LETTER[A-Za-z]
DIGIT[0-9]
HEX_DIGIT[0-9A-Fa-f]
EXPONENT[eE][+-]?[0-9]+

IDENTIFIER_NO_DOLLAR[A-Za-z]|'_'[A-Za-z]|'_'|[0-9]
BUILT_IN_IDENTIFIER[ abstract | as | covariant | deferred | dynamic | export | external | extension | factory | Function | get | implements | import | interface | late | library | mixin | operator | part | required | set | static | typedef ]
SIMPLE_STRING_INTERPOLATION(\$({IDENTIFIER_NO_DOLLAR}){BUILT_IN_IDENTIFIER}|this)
HEX_DIGIT_SEQUENCE({HEX_DIGIT}{HEX_DIGIT}?{HEX_DIGIT}?{HEX_DIGIT}?{HEX_DIGIT}?{HEX_DIGIT}?)
ESCAPE_SEQUANCE([\n\r\f\b\\\v]|\\x{HEX_DIGITHEX_DIGIT}|\\u{HEX_DIGIT}{4}|\\u{HEX_DIGIT_SEQUENCE})

STRING_CONTENT_COMMON([^$"'\n\r]|\\\$)
STRING_CONTENT_SQ({STRING_CONTENT_COMMON}|\")
SINGLE_LINE_STRING_SQ_BEGIN_END(\'({STRING_CONTENT_SQ})*\')
SINGLE_LINE_STRING_SQ_BEGIN_MID(\'{STRING_CONTENT_SQ}*\$\{)
SINGLE_LINE_STRING_SQ_MID_MID(\}{STRING_CONTENT_SQ}*\$\{)
SINGLE_LINE_STRING_SQ_MID_END(\}{STRING_CONTENT_SQ}*\')

STRING_CONTENT_DQ({STRING_CONTENT_COMMON}|\')
SINGLE_LINE_STRING_DQ_BEGIN_END(\"STRING_CONTENT_DQ\")
SINGLE_LINE_STRING_DQ_BEGIN_MID(\"{STRING_CONTENT_DQ}*\$\{)
SINGLE_LINE_STRING_DQ_MID_MID(\}{STRING_CONTENT_DQ}*\$\{)
SINGLE_LINE_STRING_DQ_MID_END(\}{STRING_CONTENT_SQ}*\")

%%

%{
std::string stringLiteral;
std::string currentComment;
int multiLineCommentNesting;
int currentInteger;
double floatingPoint;
%}


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


{SINGLE_LINE_STRING_SQ_MID_END}      {
    char* fin = trimAndProcess(1, 1, yytext); //превратить спецсимволы в символы
    _print("SINGLE LINE STRING SQ MID END : " + std::string(fin));
    free(fin); //Освободить память
}
{SINGLE_LINE_STRING_SQ_MID_MID}      {
    char* fin = trimAndProcess(1, 2, yytext); //превратить спецсимволы в символы
    _print("SINGLE LINE STRING SQ MID MID : " + std::string(fin));
    free(fin); //Освободить память
}
{SINGLE_LINE_STRING_SQ_BEGIN_MID}    {
    char* fin = trimAndProcess(1, 2, yytext); //превратить спецсимволы в символы
    _print("SINGLE LINE STRING SQ BEGIN MID : " + std::string(fin));
    free(fin); //Освободить память
}
{SINGLE_LINE_STRING_SQ_BEGIN_END}    {
    char* fin = trimAndProcess(1, 1, yytext); //превратить спецсимволы в символы
    _print("SINGLE LINE STRING SQ BEGIN END : " + std::string(fin));
    free(fin); //Освободить память
}

{SINGLE_LINE_STRING_DQ_MID_END}      {
    char* fin = trimAndProcess(1, 1, yytext); //превратить спецсимволы в символы
    _print("SINGLE LINE STRING DQ MID END : " + std::string(fin));
    free(fin); //Освободить память
}
{SINGLE_LINE_STRING_DQ_MID_MID}      {
    char* fin = trimAndProcess(1, 2, yytext); //превратить спецсимволы в символы
    _print("SINGLE LINE STRING DQ MID MID : " + std::string(fin));
    free(fin); //Освободить память
}
{SINGLE_LINE_STRING_DQ_BEGIN_MID}    {
    char* fin = trimAndProcess(1, 2, yytext); //превратить спецсимволы в символы
    _print("SINGLE LINE STRING DQ BEGIN MID : " + std::string(fin));
    free(fin); //Освободить память
}
{SINGLE_LINE_STRING_DQ_BEGIN_END}    {
    char* fin = trimAndProcess(1, 1, yytext); //превратить спецсимволы в символы
    _print("SINGLE LINE STRING DQ BEGIN END : " + std::string(fin));
    free(fin); //Освободить память
}

\/\/[^\n\r]*[\n\r]?         { _print("SINGLE LINE COMMENT: " + yytext_str());}


r\'[^\'\r\n]*\'|r\"[^\"\r\r\n]*\" {std::string tmp = yytext_str(); tmp.erase(0, 2); tmp.erase(tmp.size() - 1, 1); _print("RAW SINGLE LINE STRING: " + tmp);}


assert          {_print("Keyword ASSERT: " + yytext_str());}
break           {_print("Keyword BREAK: " + yytext_str());}
case            {_print("Keyword CASE: " + yytext_str());}
catch           {_print("Keyword CATCH: " + yytext_str());}
class           {_print("Keyword CLASS: " + yytext_str());}
const           {_print("Keyword CONST: " + yytext_str());}
continue        {_print("Keyword CONTINUE: " + yytext_str());}
default         {_print("Keyword DEFAULT: " + yytext_str());}
do              {_print("Keyword DO: " + yytext_str());}
else            {_print("Keyword ELSE: " + yytext_str());}
enum            {_print("Keyword ENUM: " + yytext_str());}
extends         {_print("Keyword EXTENDS: " + yytext_str());}
false           {_print("Keyword FALSE: " + yytext_str());}
final           {_print("Keyword FINAL: " + yytext_str());}
finally         {_print("Keyword FINALLY: " + yytext_str());}
for             {_print("Keyword FOR: " + yytext_str());}
if              {_print("Keyword IF: " + yytext_str());}
in              {_print("Keyword IN: " + yytext_str());}
is              {_print("Keyword IS: " + yytext_str());}
new             {_print("Keyword NEW: " + yytext_str());}
null            {_print("Keyword NULL: " + yytext_str());}
rethrow         {_print("Keyword RETHROW: " + yytext_str());}
return          {_print("Keyword RETURN: " + yytext_str());}
super           {_print("Keyword SUPER: " + yytext_str());}
switch          {_print("Keyword SWITCH: " + yytext_str());}
this            {_print("Keyword THIS: " + yytext_str());}
throw           {_print("Keyword THROW: " + yytext_str());}
true            {_print("Keyword TRUE: " + yytext_str());}
try             {_print("Keyword TRY: " + yytext_str());}
var             {_print("Keyword VAR: " + yytext_str());}
void            {_print("Keyword VOID: " + yytext_str());}
while           {_print("Keyword WHILE: " + yytext_str());}
with            {_print("Keyword WITH: " + yytext_str());}


abstract        {_print("Built-in identifier ABSTRACT: " + yytext_str());}        
as              {_print("Built-in identifier AS: " + yytext_str());}
covariant       {_print("Built-in identifier COVARIANT: " + yytext_str());}        
deferred        {_print("Built-in identifier DEFERRED: " + yytext_str());}        
dynamic         {_print("Built-in identifier DYNAMIC: " + yytext_str());}    
export          {_print("Built-in identifier EXPORT: " + yytext_str());}    
external        {_print("Built-in identifier EXTERNAL: " + yytext_str());}        
extension       {_print("Built-in identifier EXTENSION: " + yytext_str());}        
factory         {_print("Built-in identifier FACTORY: " + yytext_str());}    
Function        {_print("Built-in identifier FUNCTION: " + yytext_str());}        
get             {_print("Built-in identifier GET: " + yytext_str());}
implements      {_print("Built-in identifier IMPLEMENTS: " + yytext_str());}        
import          {_print("Built-in identifier IMPORT: " + yytext_str());}    
interface       {_print("Built-in identifier INTERFACE: " + yytext_str());}        
late            {_print("Built-in identifier LATE: " + yytext_str());}    
library         {_print("Built-in identifier LIBRARY: " + yytext_str());}    
mixin           {_print("Built-in identifier MIXIN: " + yytext_str());}    
operator        {_print("Built-in identifier OPERATOR: " + yytext_str());}        
part            {_print("Built-in identifier PART: " + yytext_str());}    
required        {_print("Built-in identifier REQUIRED: " + yytext_str());}        
set             {_print("Built-in identifier SET: " + yytext_str());}
static          {_print("Built-in identifier STATIC: " + yytext_str());}    
typedef         {_print("Built-in identifier TYPEDEF: " + yytext_str());} 


async           {_print("Contextual keyword ASYNC: " + yytext_str());}    
hide            {_print("Contextual keyword HIDE: " + yytext_str());}    
of              {_print("Contextual keyword OF: " + yytext_str());}
on              {_print("Contextual keyword ON: " + yytext_str());}
show            {_print("Contextual keyword SHOW: " + yytext_str());}    
sync            {_print("Contextual keyword SYNC: " + yytext_str());}    
await           {_print("Contextual keyword AWAIT: " + yytext_str());}    
yield           {_print("Contextual keyword YIELD: " + yytext_str());}    


[\t \n\r]+      {_print("WHITESPACE");}


([_$]|{LETTER})([_$]|{LETTER}|{DIGIT})*     {_print("IDENTIFIER: " + yytext_str());}    


{DIGIT}+        {
    int64_t value = _atoi64(yytext);
    if(errno == ERANGE)
    {
        _print("DECIMAL INTEGER LITERAL: " + yytext_str() + "(ERR: The integer literal " + yytext_str() + " can't be represented in 64 bits.)");
        //ВОПРОС: на практике -9223372036854775808 считается целочисленным литералом (помещается в 64 бита),
        //однако согласно документации "-" это унарный операнд, и его положение относительно числа должно учитываться только на этапе парсинга,
        //т.е. должен обнаружиться литерал 9223372036854775808, не помещающийся в 64 бита
        //Варианты решения: 1) оставить проверку значения численных литералов до этапа парсинга, 2) Допустить невозможность записать -9223372036854775808 в целочисленную константу,
        // 3) Искать "-" на этапе лексинга (скорее всего, наименее корректный вариант)
    }
    else
    {
        char buf[30];
        _print("DECIMAL INTEGER LITERAL: " + yytext_str() + " (Value = " + _i64toa(value, buf, 10) + ")");
    }
}

(0x|0X){HEX_DIGIT}+     {
    int64_t value = strtoull(yytext, NULL, 16);
    if(errno == ERANGE)
    {
        _print("HEX INTEGER LITERAL: " + yytext_str() + "(ERR: The integer literal " + yytext_str() + " can't be represented in 64 bits.)");
    }
    else
    {
        char buf[30];
        _print("HEX INTEGER LITERAL: " + yytext_str() + " (Value = " + _i64toa(value, buf, 10) + ")");
    }
}

({DIGIT}*\.{DIGIT}+{EXPONENT}?|{DIGIT}+{EXPONENT})  {
    double value = strtod(yytext, NULL);
    if(errno == ERANGE)
    {
        //Не уверен насчет всех этих условий
        if(isinf(value)){
            _print("DECIMAL DOUBLE LITERAL: " + yytext_str() + " (Value = Infinity)");
        }
        else if(isnan(value)){
            _print("DECIMAL DOUBLE LITERAL: " + yytext_str() + " (Value = NaN)");
        }
        else if(!isnormal(value)){
            _print("DECIMAL DOUBLE LITERAL: " + yytext_str() + " (Value = de-/subnormal)");
        }
        else{
            _print("DECIMAL DOUBLE LITERAL: " + yytext_str() + "(ERR: The double literal " + yytext_str() + " can't be represented in 64 bits.)");
        }
    }
    else
    {   
        char buf[1000];
        sprintf(buf, "%f", value);
        _print("DECIMAL DOUBLE LITERAL: " + yytext_str() + " (Value = " + buf + ")");
    }
}

\*\=        {_print("MULASSIGN LEXEM *=: " + yytext_str());}
\/\=        {_print("DIVASSIGN LEXEM /=: " + yytext_str());}
\~\/\=      {_print("TRUNCDIVASSIGN LEXEM ~/=: " + yytext_str());}
\%\=        {_print("MODASSIGN LEXEM %=: " + yytext_str());}
\+\=        {_print("ADDASSIGN LEXEM +=: " + yytext_str());}
\-\=        {_print("SUBASSIGN LEXEM -=: " + yytext_str());}
\<\<\=      {_print("SHIFTLASSIGN LEXEM <<=: " + yytext_str());}
\>\>\>\=    {_print("TSHIFTRASSIGN LEXEM >>>=: " + yytext_str());}
\>\>\=      {_print("SHIFTRASSIGN LEXEM >>=: " + yytext_str());}
\&\=        {_print("ANDASSIGN LEXEM &=: " + yytext_str());}
\^\=        {_print("XORASSIGN LEXEM ^=: " + yytext_str());}
\|\=        {_print("ORASSIGN LEXEM |=: " + yytext_str());}
\?\?\=      {_print("IFNULLASSIGN LEXEM ??=: " + yytext_str());}

\=\=        {_print("EQUALITY LEXEM ==: " + yytext_str());}
\!\=        {_print("INEQUALITY LEXEM !=: " + yytext_str());}

\&\&        {_print("BOOLAND LEXEM &&: " + yytext_str());}
\|\|        {_print("BOOLOR LEXEM ||: " + yytext_str());}

\>\=         {_print("GREATEREQ LEXEM >=: " + yytext_str());}
\>           {_print("ARROWRIGHT LEXEM >: " + yytext_str());}
\<\=         {_print("LESSEQ LEXEM <=: " + yytext_str());}
\<           {_print("ARROWLEFT LEXEM <: " + yytext_str());}

\?\?         {_print("IFNULL LEXEM ??: " + yytext_str());}

\+\+         {_print("UNARY INCREMENT LEXEM ++: " + yytext_str());}    
\-\-         {_print("UNARY DENCREMENT LEXEM --: " + yytext_str());}

\+           {_print("PLUS LEXEM +: " + yytext_str());}    
\-           {_print("MINUS LEXEM -: " + yytext_str());}

\*           {_print("MUL LEXEM *: " + yytext_str());}
\/           {_print("DIV LEXEM /: " + yytext_str());}
\%           {_print("MOD LEXEM %: " + yytext_str());}
\~\/         {_print("TRUNCDIV LEXEM ~/: " + yytext_str());}

\=           {_print("EQ LEXEM =: " + yytext_str());}

\&           {_print("AMPERSAND LEXEM &: " + yytext_str());}
\^           {_print("CIRCUMFLEX LEXEM ^: " + yytext_str());}
\|           {_print("BAR LEXEM |: " + yytext_str());}

\!           {_print("EXCLAMATION LEXEM !: " + yytext_str());}
\?           {_print("QUESTION LEXEM ?: " + yytext_str());}
\~           {_print("TILDE LEXEM ~: " + yytext_str());}

.           {_print("UNKNOWN SYMBOL: " + yytext_str());}




%%

int main(int argc, char** argv)         {
    if (argc > 1)           {
        fopen_s(&yyin, argv[1], "r");
        yylex();
    }
    else            {
        _print("File not found");
    }
}