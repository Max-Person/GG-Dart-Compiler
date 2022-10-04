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
%left SHIFTL SHIFTR TSHIFTR
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
    builtInIdentifier: ABSTRACT            {}
        | AS                               {}
        | COVARIANT                        {}
        | DEFERRED                         {}
        | DYNAMIC                          {}
        | EXPORT                           {}
        | EXTERNAL                         {}
        | EXTENSION                        {}
        | FACTORY                          {}
        | FUNCTION                         {}
        | GET                              {}
        | IMPLEMENTS                       {}
        | IMPORT                           {}
        | INTERFACE                        {}
        | LATE                             {}
        | LIBRARY                          {}
        | MIXIN                            {}
        | OPERATOR                         {}
        | PART                             {}
        | REQUIRED                         {}
        | SET                              {}
        | STATIC                           {}
        | TYPEDEF                          {}
    ;

    otherIdentifier: ASYNC                 {}
        | HIDE                             {}
        | OF                               {}
        | ON                               {}
        | SHOW                             {}
        | SYNC                             {}
        | AWAIT                            {} 
        | YIELD                            {}
    ;

    identifier: IDENTIFIER                 {}
        | builtInIdentifier                {}
        | otherIdentifier                  {}
    ;

    primary: THIS                          {}
        | INTEGER_LITERAL                  {}
        | DOUBLE_LITERAL                   {}
        | STRING_LITERAL                   {}
        | BOOLEAN_LITERAL                  {}
        | identifier                       {}
    ;
    expr: primary                          {}
        | expr '=' expr                    {}
        | expr AND_ASSIGN expr             {} 
        | expr OR_ASSIGN expr              {}
        | expr XOR_ASSIGN expr             {}
        | expr SHIFTL_ASSIGN expr          {}
        | expr SHIFTR_ASSIGN expr          {}
        | expr TSHIFTR_ASSIGN expr         {}
        | expr MUL_ASSIGN expr             {}
        | expr DIV_ASSIGN expr             {}
        | expr TRUNC_DIV_ASSIGN expr       {}
        | expr MOD_ASSIGN expr             {}
        | expr ADD_ASSIGN expr             {}
        | expr SUB_ASSIGN expr             {}
        | expr IFNULL_ASSIGN expr          {}
        | expr '?' expr ':' expr           {}
        | expr IFNULL expr                 {}
        | expr OR expr                     {}
        | expr AND expr                    {}
        | expr EQ expr                     {}
        | expr NEQ expr                    {}
        | expr '>' expr                    {}
        | expr '<' expr                    {}
        | expr GREATER_EQ expr             {}
        | expr LESS_EQ expr                {}
        | expr '|' expr                    {}
        | expr '^' expr                    {}
        | expr '&' expr                    {}
        | expr '<' '<' expr  %prec SHIFTL               {} //!ШИФТЫ И ГЕНЕРИКИ - НЕ БРАТЬЯ НАВЕК??
        | expr '>' '>' expr  %prec SHIFTR               {}
        | expr '>' '>' '>' expr  %prec TSHIFTR          {}
        | expr '+' expr                                 {}
        | expr '-' expr                                 {}
        | expr '*' expr                                 {}
        | expr '/' expr                                 {}
        | expr '%' expr                                 {}
        | expr TRUNCDIV  expr                           {}
        | '-'  expr %prec UMINUS                        {}
        | '!'  expr                                     {}
        | '~'  expr                                     {}
        | INC expr %prec PREFIX_INC                     {}
        | DEC expr %prec PREFIX_DEC                     {}
        | AWAIT  expr                                   {}
        | expr '.'                                      {}
        | expr INC %prec POSTFIX_INC                    {}
        | expr DEC %prec POSTFIX_DEC                    {}
        | expr '[' expr ']'                             {}
        | STRING_LITERAL INTERPOLATION_CONCAT expr INTERPOLATION_CONCAT STRING_LITERAL {}
    ;

    statementList: statement statement                  {}
        | statementList statement                       {}
    ;

    statementBlock: '{' statementList '}'               {}
    ;

    exprStatement: ';'                                  {}
        | expr ';'                                      {}
    ;

    

    typeIdentifier: IDENTIFIER                          {}
        | otherIdentifier                               {}
        | DYNAMIC                                       {}
    ;

    typeName: typeIdentifier                            {}
        | typeName '.' typeIdentifier                   {}
    ;

    typeList: type                                      {}
        | typeList ',' type                             {}
    ;

    type: VOID                                          {}
        | typeName                                      {}
        | typeName '<' typeList '>'                     {}
    ;

    //finalConstVarOrType
    declarator: LATE FINAL type                         {}
        | LATE FINAL                                    {}
        | FINAL type                                    {}
        | FINAL                                         {}
        | CONST type                                    {}
        | CONST                                         {}
        | LATE VAR                                      {}
        | LATE type                                     {}
        | VAR                                           {}
        | type                                          {}
    ;

    declaredIdentifier: COVARIANT declarator identifier     {}
        | declarator identifier                             {}
    ;

    variableDeclaration: declaredIdentifier                 {}
        | declaredIdentifier '=' expr                       {}
        | variableDeclaration ',' identifier                {}
        | variableDeclaration ',' identifier '=' expr       {}
    ;

    variableDeclarationStatement: variableDeclaration ';'   {}
    ;


    statement: exprStatement    {}
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