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
%token FUNC_ARROW

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

    string: STRING_LITERAL INTERPOLATION_CONCAT expr INTERPOLATION_CONCAT STRING_LITERAL {}
        | string INTERPOLATION_CONCAT expr INTERPOLATION_CONCAT STRING_LITERAL {}
        | STRING_LITERAL                    {}
    ;

    primary: THIS                          {}
        | INTEGER_LITERAL                  {}
        | DOUBLE_LITERAL                   {}
        | BOOLEAN_LITERAL                  {}
        | string                       {}
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

    typeName: typeIdentifier '.' typeIdentifier                           {}
        | typeName '.' typeIdentifier                   {}
    ;

    typeList: type ',' type                                    {}
        | typeList ',' type                             {}
    ;

    type: typeIdentifier                                      {}
        | typeName                                      {}
        | typeIdentifier '<' type '>'                     {}
        | typeName '<' type '>'                     {}
        | typeIdentifier '<' typeList '>'                     {}
        | typeNotFunction
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

    identifierList: identifier ',' identifier
        | identifierList ',' identifier
    ;

    enumType: ENUM identifier '{' identifier '}'            {}       //подумать о метаданных (что-то было написано в документации)
            | ENUM identifier '{' identifierList '}'         {}
    ;

    exprList: expr 
        | exprList ',' expr
    ;

    forStatement: AWAIT FOR '(' forInitializerStatement expr ';' exprList ')' statement
        | AWAIT FOR '(' forInitializerStatement ';' ')' statement 
        | AWAIT FOR '(' forInitializerStatement expr ';' ')' statement
        | AWAIT FOR '(' forInitializerStatement ';' exprList ')' statement
        | AWAIT FOR '(' declaredIdentifier IN expr ')' statement
        | AWAIT FOR '(' identifier IN expr ')' statement
        | FOR '(' forInitializerStatement expr ';' exprList ')' statement
        | FOR '(' forInitializerStatement ';' ')' statement
        | FOR '(' forInitializerStatement expr ';' ')' statement
        | FOR '(' forInitializerStatement ';' exprList ')' statement
        | FOR '(' declaredIdentifier IN expr ')' statement
        | FOR '(' identifier IN expr ')' statement
    ;

    forInitializerStatement: variableDeclaration
        | exprStatement
    ;

    whileStatement: WHILE '(' expr ')' statement
    ;

    doStatement: DO statement WHILE '(' expr ')' ';'
    ;

    functionSignature: type identifier typeParameters formalParameterList
        | type identifier formalParameterList
        | identifier typeParameters formalParameterList
        | identifier formalParameterList
    ;

    typeParameter: identifier EXTENDS functionType '?'
        | identifier EXTENDS functionType
        | typeName '<' typeList '>' '?'
        | typeName '?'
        | typeName '<' typeList '>'
        | FUNCTION '?'
        | FUNCTION
        | identifier
    ;

    typeParamList: typeParameter ',' typeParameter
        | typeParamList ',' typeParameter
    ;

    typeParameters: '<' typeParameter '>'
        | '<' typeParamList '>'
    ;

    normalParameterType: type identifier
        | type
    ;

    normalParameterTypes: normalParameterType ',' normalParameterType
        | normalParameterTypes ',' normalParameterType 
    ;

    parameterTypeList: '(' ')'
        | '(' normalParameterType ',' ')'
        | '(' normalParameterType ')'
        | '(' normalParameterTypes ',' ')'
        | '(' normalParameterTypes ')'
    ;

    breakStatement: BREAK identifier ';'
        | BREAK ';'
    ;

    continueStatement: CONTINUE identifier ';'
        | CONTINUE ';'
    ;

    returnStatement: RETURN expr ';'
        | RETURN ';'
    ;

    functionTypeTail: FUNCTION typeParameters parameterTypeList
        | FUNCTION parameterTypeList
    ;

    functionTypeTails: functionTypeTail '?' functionTypeTail
        | functionTypeTail functionTypeTail
        | functionTypeTails '?' functionTypeTail
        | functionTypeTails functionTypeTail
        | functionTypeTail
    ;

    functionType: functionTypeTails
        | typeNotFunction functionTypeTails
    ;

    typeNotVoid: functionType '?'
        | functionType
        | typeName '<' typeList '>' '?'
        | typeName '?'
        | typeName '<' typeList '>'
        | FUNCTION '?'
        | FUNCTION
    ; 

    typeNotFunction: VOID
        | typeName '<' typeList '>' '?'
        | typeName '?'
        | typeName '<' typeList '>'
        | FUNCTION '?'
        | FUNCTION
    ;


    formalParameterList: '(' ')'
        | '(' normalFormalParameterList ',' ')'
        | '(' normalFormalParameterList ')'
    ;

    normalFormalParameterList: normalFormalParameter ',' normalFormalParameter
        | normalFormalParameterList ',' normalFormalParameter
    ;

    normalFormalParameter: type identifier formalParameterPart '?'
        | identifier formalParameterPart '?'
        | type identifier formalParameterPart
        | identifier formalParameterPart
        | declaredIdentifier
        | identifier
        | declarator THIS '.' identifier formalParameterPart '?'
        | declarator THIS '.' identifier formalParameterPart
        | declarator THIS '.' identifier
        | THIS '.' identifier formalParameterPart '?'
        | THIS '.' identifier formalParameterPart
        | THIS '.' identifier
    ;

    formalParameterPart: typeParameters formalParameterList
        | formalParameterList
    ;

    functionBody: FUNC_ARROW expr ';'
        | statementBlock
    ;

    localFunctionDeclaration: functionSignature functionBody;

    ifStatement: IF '(' expr ')' statement
        | IF '(' expr ')' statement ELSE statement
    ;

    switchCase: CASE expr ':' statement
        | CASE expr ':' statements
    ;

    switchCases: switchCase switchCase
        | switchCases switchCase
    ;

    switchStatement: SWITCH '(' expr ')' '{' switchCase '}'
        | SWITCH '(' expr ')' '{' switchCases '}'
        | SWITCH '(' expr ')' '{' switchCase DEFAULT ':' statement '}'
        | SWITCH '(' expr ')' '{' switchCases  DEFAULT ':' statement '}'
        | SWITCH '(' expr ')' '{' switchCase DEFAULT ':' statements '}'
        | SWITCH '(' expr ')' '{' switchCases  DEFAULT ':' statements '}'
    ;
    
    statement: exprStatement    {}
    ;

    statements: statement statement
        | statements statement
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