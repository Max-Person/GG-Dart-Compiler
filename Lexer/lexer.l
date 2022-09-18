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
#include <algorithm>
#include <iostream>
#define YY_DECL int yylex()

void _print(const std::string & message)         {
    std::cout << message << std::endl;
}

std::string yytext_str() {
    return std::string(yytext);
}

%}

LETTER[A-Za-z]
DIGIT[0-9]
HEX_DIGIT[0-9A-Fa-f]
EXPONENT[eE][+-]?[0-9]+


%%

%{
std::string stringLiteral;
std::string cuurentComment;
int currentInteger;
double floatingPoint;
%}


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