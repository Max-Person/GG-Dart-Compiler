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
    double doubleval;
    char *stringval;
    char *identifier;
    bool boolval;
}

%right '=' AND_ASSIGN OR_ASSIGN XOR_ASSIGN SHIFTL_ASSIGN SHIFTR_ASSIGN TSHIFTR_ASSIGN MUL_ASSIGN DIV_ASSIGN TRUNC_DIV_ASSIGN MOD_ASSIGN ADD_ASSIGN SUB_ASSIGN IFNULL_ASSIGN
%left IFNULL
%left OR
%left AND
%nonassoc EQ NEQ
%nonassoc '>' '<' GREATER_EQ LESS_EQ
%left '|'
%left '^'
%left '&'
// ШИФТЫ
%left '+' '-'
%left '*' '/' '%' TRUNCDIV
%nonassoc UMINUS '!' '~' PREFIX_INC PREFIX_DEC AWAIT
%nonassoc '.' '(' ')' '[' ']' POSTFIX_INC POSTFIX_DEC


%token <intval>INTEGER_LITERAL
%token <doubleval>DOUBLE_LITERAL
%token <stringval>STRING_LITERAL
%token <boolval> BOOLEAN_LITERAL
%token <identifier>IDENTIFIER
%token END

%token INC                          // ++
%token DEC                          // --

%right '?'
%right ';'
%right ':'

%token ASSERT
%token BREAK
%token CASE
%token CATCH
%token CLASS
%token CONST
%token CONTINUE
%token DEFAULT
%token DO
%token ELSE
%token ENUM
%token EXTENDS
%token FALSE
%token FINAL
%token FINALLY
%token FOR
%token IF
%token IN
%token IS
%token NEW
%token NULL_
%token RETHROW
%token RETURN
%token SUPER
%token SWITCH
%token THIS
%token THROW
%token TRUE
%token TRY
%token VAR
%token VOID
%token WHILE
%token WITH

%token ABSTRACT
%token AS
%token COVARIANT
%token DEFERRED
%token DYNAMIC
%token EXPORT
%token EXTERNAL
%token EXTENSION
%token FACTORY
%token FUNCTION
%token GET
%token IMPLEMENTS
%token IMPORT
%token INTERFACE
%token LATE
%token LIBRARY
%token MIXIN
%token OPERATOR
%token PART
%token REQUIRED
%token SET
%token STATIC
%token TYPEDEF

%token ASYNC
%token HIDE
%token OF
%token ON
%token SHOW
%token SYNC
// AWAIT жив
%token YIELD

%token COMMENT

%left INTERPOLATION_CONCAT

%%
    expr: IDENTIFIER    { }
        | expr '+' expr                     {  }
        | expr '-' expr                     {  }
        | expr '*' expr                     {  }
        | expr '/' expr                     {  }
        | expr '=' expr                     {  }
        | expr '<' expr                     {  }
        | expr '>' expr                     {  }
        | '!' expr                          {  }
    ;
    
%%

/* int main(int argc, char** argv) {
    if (argc > 1) {
        yyin = fopen(argv[1], "r");
        yyparse();
        fclose(yyin);
    }
    else {
        yyerror("Not found file");
    }
} */