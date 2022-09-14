%option noyywrap
%option never-interactive
%option yylineno

%{
#include <string>
#include <stdio.h>
#include <algorithm>
#include <iostream>
#define YY_DECL int yylex()

void Print(const std::string & message) {
    std::cout << message << std::endl;
}

%}


%%

%{
std::string stringLiteral;
std::string cuurentComment;
int currentInteger;
double floatingPoint;
%}

. {Print(yytext);}




%%

int main(int argc, char** argv) {
    if (argc > 1) {
        fopen_s(&yyin, argv[1], "r");
        yylex();
    }
    else {
        Print("Not found file");
    }
}