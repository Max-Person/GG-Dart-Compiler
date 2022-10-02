%{
#include <stdio.h>
#include <stdlib.h>

extern int yylineno;
extern FILE* yyin;

int yyparse();
int yylex();

void yyerror(char const *s) {
    fprintf(stderr, "Error: %s on line %d\n", s, yylineno);
    exit(1);
}
%}


%union {
    long long intval;
    double floatval;
    char *runeval;
    char *stringval;
    char *identifier;
}

%token <intval> tIntLit
%token <floatval>  tFloatLit
%token <runeval> tRuneLit
%token <stringval> tStringLit
%token <boolval> tBooleanLit
%token <identifier> tIdentifier
%token END

%left tAnd                          // &&
%left tOr                           // ||
%left tIncrement                    // ++
%left tDecrement                    // --

%right tAndAssignment               // &=
%right tBitclearAssignment          // &^=
%right tOrAssignment                // |=
%right tXorAssignment               // ^=
%right tLeftShiftAssignment         // <<=
%right tRightShiftAssignment        // >>=

%left tBitwiseAnd                   // &
%left tBitclear                     // &^
%left tBitwiseOr                    // |
%left tBitwiseXor                   // ^
%left tLeftShift                    // <<
%left tRightShift                   // >>

%right tPlusAssignment              // +=
%right tMinusAssignment             // -=
%right tMulAssignment               // *=
%right tDivAssignment               // /=
%right tModAssignment               // %=

%left tEqual                        // ==
%left tNotEqual                     // !=
%left tGreaterOrEqual               // >=
%left tLessOrEqual                  // <=

%left tUnderlying                   // ~
%right tShortDeclarationOperator    // :=
%right tUnaryPlus                   // +
%right tUnaryMinus                  // -
%token tVariadic                    // ...

%token tBreak
%token tDefault
%token tFunc
%token tInterface
%token tSelect
%token tCase
%token tDefer
%token tGo
%token tMap
%token tStruct
%token tChan
%token tElse
%token tGoto
%token tPackage
%token tSwitch
%token tConst
%token tFallthrough
%token tIf
%token tRange
%token tType
%token tContinue
%token tFor
%token tImport
%token tReturn
%token tVar

%%
    expr: tIdentifier                       {  }
        | tIntLit                           {  }
        | tFloatLit                         {  }
        | tStringLit                        {  }
        | expr '+' expr                     {  }
        | expr '-' expr                     {  }
        | expr '*' expr                     {  }
        | expr '/' expr                     {  }
        | expr '=' expr                     {  }
        | expr '<' expr                     {  }
        | expr '>' expr                     {  }
        | expr tEqual expr                  {  }
        | expr tNotEqual expr               {  }
        | expr tLessOrEqual expr            {  }
        | expr tGreaterOrEqual expr         {  }
        | expr tAnd expr                    {  }
        | expr tOr expr                     {  }
        | '!' expr                          {  }
        | '+' expr %prec tUnaryPlus         {  }
        | '-' expr %prec tUnaryMinus        {  }
    ;
    
%%

int main(int argc, char** argv) {
    if (argc > 1) {
        yyin = fopen(argv[1], "r");
        yyparse();
        fclose(yyin);
    }
    else {
        yyerror("Not found file");
    }
}