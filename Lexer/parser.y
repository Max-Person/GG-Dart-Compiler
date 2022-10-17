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
%token DOUBLE_DOT
%token VOPROS_DOT

%token COMMENT

%left INTERPOLATION_CONCAT

%precedence INNER_IF

%%
    //-------------- ВЕРХНИЙ УРОВЕНЬ --------------

    partDeclaration: topLevelDeclaration
        | partDeclaration topLevelDeclaration
    ;

    //Дописать
    topLevelDeclaration: classDeclaration
        | enumType
        | LATE FINAL type initializedIdentifier ';'
        | LATE FINAL type initializedIdentifierList ';'
        | LATE FINAL initializedIdentifier ';'
        | LATE FINAL initializedIdentifierList ';'
    ;

    //-------------- БАЗОВЫЕ ПОНЯТИЯ --------------

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

    /* otherIdentifier: ASYNC                 {}
        | HIDE                             {}
        | OF                               {}
        | ON                               {}
        | SHOW                             {}
        | SYNC                             {}
        | AWAIT                            {} 
        | YIELD                            {}
    ; */

    identifier: IDENTIFIER                 {}
        | builtInIdentifier                {}
        /* | otherIdentifier                  {} */
    ;

    identifierList: identifier ',' identifier
        | identifierList ',' identifier
    ;

    string: STRING_LITERAL INTERPOLATION_CONCAT expr INTERPOLATION_CONCAT STRING_LITERAL {}
        | string INTERPOLATION_CONCAT expr INTERPOLATION_CONCAT STRING_LITERAL {}
        | STRING_LITERAL                    {}
    ;

    //-------------- ВЫРАЖЕНИЯ --------------

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
        | expr '.' identifier                                      {}       //LOOK
        | expr INC %prec POSTFIX_INC                    {}
        | expr DEC %prec POSTFIX_DEC                    {}
        | expr '[' expr ']'                             {}
    ;

    //список из одного
    exprList: expr 
        | exprList ',' expr
    ;

    exprStatement: ';'                                  {}
        | expr ';'                                      {}
    ;

    //-------------- ТИПИЗАЦИЯ --------------
    // --- обычная типизация

    /* typeIdentifier: IDENTIFIER                          {}
        | otherIdentifier                               {}
        | DYNAMIC                                       {}
    ; */

    //список из одного
    //используется IDENTIFIER, потому что кажется built-in идентификаторы не могут быть названием класса
    qualifiedName: IDENTIFIER
        | IDENTIFIER '.' IDENTIFIER

    typeName: qualifiedName
        | DYNAMIC
    ;

    type: functionType
        | typeNotFunction
    ;

    typeList: type ',' type
        | typeList ',' type
    ;

    varOrType: VAR
        | type
    ;

    //finalConstVarOrType
    declarator: LATE FINAL type                         {}
        | LATE FINAL                                    {}
        | FINAL type                                    {}
        | FINAL                                         {}
        | CONST type                                    {}
        | CONST                                         {}
        | LATE varOrType                                      {}
        | varOrType                                           {}
    ;

    typeNotFunction: VOID
        | typeName
        | typeName '?'
        | FUNCTION
        | FUNCTION '?'
    ;

    typeNotVoid: functionType
        | functionType '?'
        | typeName
        | typeName '?'
        | FUNCTION
        | FUNCTION '?'
    ; 

    typeNotVoidList: typeNotVoid ',' typeNotVoid
        | typeNotVoidList ',' typeNotVoid
    ;

    // --- функ. типизация

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

    functionTypeTail: FUNCTION parameterTypeList
    ;

    //список из одного
    functionTypeTails: functionTypeTails '?' functionTypeTail
        | functionTypeTails functionTypeTail
        | functionTypeTail
    ;

    functionType: functionTypeTails
        | typeNotFunction functionTypeTails
    ;

    //-------------- ПЕРЕМЕННЫЕ И ИНИЦИАЛИЗАЦИЯ --------------

    declaredIdentifier: COVARIANT declarator identifier     {}
        | declarator identifier                             {}
    ;

    initializedIdentifier: staticFinalDeclaration
        | identifier
    ;

    initializedIdentifierList: initializedIdentifier ',' initializedIdentifier
        | initializedIdentifierList ',' initializedIdentifier
    ;

    variableDeclaration: declaredIdentifier                 {}
        | declaredIdentifier '=' expr                       {}
        | variableDeclaration ',' initializedIdentifier
    ;

    variableDeclarationStatement: variableDeclaration ';'   {}
    ;

    //-------------- РАЗВИЛКИ --------------

    ifStatement: IF '(' expr ')' statement
        | IF '(' expr ')' statement ELSE statement %prec INNER_IF
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

    //-------------- ЦИКЛЫ --------------

    //LOOK почему здесь не используются expr statement?
    forStatement: FOR '(' forInitializerStatement expr ';' exprList ')' statement
        | FOR '(' forInitializerStatement ';' ')' statement
        | FOR '(' forInitializerStatement expr ';' ')' statement
        | FOR '(' forInitializerStatement ';' exprList ')' statement
        | FOR '(' declaredIdentifier IN expr ')' statement
        | FOR '(' identifier IN expr ')' statement
    ;

    forInitializerStatement: variableDeclarationStatement
        | exprStatement
    ;

    whileStatement: WHILE '(' expr ')' statement
    ;

    doStatement: DO statement WHILE '(' expr ')' ';'
    ;

    //Здесь убрал идентификаторы, потому что это относится к лейблам
    breakStatement: BREAK ';'
    ;

    continueStatement: CONTINUE ';'
    ;

    returnStatement: RETURN expr ';'
        | RETURN ';'
    ;

    //-------------- СТЕЙТМЕНТЫ ОБЩЕЕ --------------
    
    statement: exprStatement
        | variableDeclarationStatement
        | forStatement
        | whileStatement
        | doStatement
        | switchStatement
        | ifStatement
        | breakStatement
        | continueStatement
        | returnStatement
        | exprStatement
        | localFunctionDeclaration
        | statementBlock
    ;

    statements: statement statement
        | statements statement
    ;

    statementBlock: '{' statements '}'               {}
    ;

    //-------------- ФУНКЦИИ --------------

    formalParameterList: '(' ')'
        | '(' normalFormalParameter ',' ')'
        | '(' normalFormalParameter ')'
        | '(' normalFormalParameterList ',' ')'
        | '(' normalFormalParameterList ')'
    ;

    //LOOK здесь formalParameterList относится к функциональной типизации. Не уверен выпиливаем ли мы ее
    normalFormalParameter: type identifier formalParameterList '?'
        | identifier formalParameterList '?'
        | type identifier formalParameterList
        | identifier formalParameterList
        | declaredIdentifier
        | identifier
        | declarator THIS '.' identifier formalParameterList '?'
        | declarator THIS '.' identifier formalParameterList
        | declarator THIS '.' identifier
        | THIS '.' identifier formalParameterList '?'
        | THIS '.' identifier formalParameterList
        | THIS '.' identifier
    ;

    normalFormalParameterList: normalFormalParameter ',' normalFormalParameter
        | normalFormalParameterList ',' normalFormalParameter
    ;

    functionSignature: type identifier formalParameterList
        | identifier formalParameterList
    ;
    
    functionBody: FUNC_ARROW expr ';'
        | statementBlock
    ;

    localFunctionDeclaration: functionSignature functionBody
    ;

    //-------------- ЕНАМ --------------

    enumType: ENUM identifier '{' identifier '}'            {}       //подумать о метаданных (что-то было написано в документации)
            | ENUM identifier '{' identifierList '}'         {}
    ;

    //------- КЛАССЫ --------------

    mixins: WITH typeNotVoid
        | WITH typeNotVoidList
    ;

    superclassOpt: %empty
        | EXTENDS typeNotVoid
        | EXTENDS typeNotVoid mixins
        | mixins
    ;

    interfacesOpt: %empty
        | IMPLEMENTS typeNotVoid
        | IMPLEMENTS typeNotVoidList
    ;

    classDeclaration: CLASS IDENTIFIER superclassOpt interfacesOpt '{' classMemberDeclarations '}'
        | ABSTRACT CLASS IDENTIFIER superclassOpt interfacesOpt '{' classMemberDeclarations '}'
        | CLASS identifier '=' typeNotVoid mixins interfacesOpt
        | ABSTRACT CLASS identifier '=' typeNotVoid mixins interfacesOpt
    ;

    staticFinalDeclaration: identifier '=' expr
    ;

    staticFinalDeclarationList: staticFinalDeclaration
        | staticFinalDeclarationList ',' staticFinalDeclaration
    ;

    classMemberDeclarations: declaration ';'
        | methodSignature functionBody
    ;

    //Дописать
    declaration: STATIC CONST type staticFinalDeclarationList
        | STATIC CONST staticFinalDeclarationList
        | STATIC FINAL type staticFinalDeclarationList
        | STATIC FINAL staticFinalDeclarationList
        | STATIC LATE FINAL type initializedIdentifierList
        | STATIC LATE FINAL initializedIdentifierList
        | STATIC LATE varOrType initializedIdentifierList
        | STATIC varOrType initializedIdentifierList
        | LATE FINAL type initializedIdentifierList
        | LATE FINAL initializedIdentifierList
        | FINAL type initializedIdentifierList
        | FINAL initializedIdentifierList
        | LATE varOrType initializedIdentifierList
        | varOrType initializedIdentifierList
    ;

    // Дописать
    methodSignature: functionSignature
        | STATIC functionSignature
        | constructorSignature
        | constructorSignature initializers
    ;


    //------- КОНСТРУКТОРЫ --------------

    constructorName: IDENTIFIER '.' identifier
        | IDENTIFIER
    ;

    constructorSignature: constructorName formalParameterList
    ;

    constantConstructorSignature: CONST constructorName formalParameterList
    ;

    // вызвать именованный конструктор или другой конструктор 
    // Пример: Car.withoutABS(this.make, this.model, this.yearMade): this(make, model, yearMade, false);
    redirection: ':' THIS '.' identifier arguments
        | ':' THIS arguments
    ; 

    arguments: '(' exprList ',' ')'
        | '(' exprList ')'
        | '(' ')'
    ;

    initializerListEntry: SUPER arguments 
        | SUPER '.' identifier arguments
        | fieldInitializer
    ;

    //LOOK на месте expr любой expr кроме assign
    fieldInitializer: THIS '.' identifier '=' expr
        | THIS '.' identifier '=' // THIS '.' identifier '=' cascade 
    ;

    initializers: ':' initializerListEntry
        | initializers ',' initializerListEntry
    ;




    
    /* cascade: cascade DOUBLE_DOT ;

    cascadeSection: '[' expr ']' cascadeSectionTail
        | identifier cascadeSectionTail
    ;

    cascadeSectionTail : '=' 
        | 

    assignableSelector: '[' expr ']'
        | '.' identifier
        | VOPROS_DOT identifier
        | '?' '[' expr ']'
    ;

    exprWithoutCascade:  */

    

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