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

%code requires{ #include "structures.h" }


%union {
    long long intval;
    double doubleval;
    char *stringval;
    bool boolval;
    identifier_node* _identifier_node;
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
%token <_identifier_node>IDENTIFIER
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

%token<_identifier_node> ABSTRACT
%token<_identifier_node> AS
%token<_identifier_node> COVARIANT
%token<_identifier_node> DEFERRED
%token<_identifier_node> DYNAMIC
%token<_identifier_node> EXPORT
%token<_identifier_node> EXTERNAL
%token<_identifier_node> EXTENSION
%token<_identifier_node> FACTORY
%token<_identifier_node> FUNCTION
%token<_identifier_node> GET
%token<_identifier_node> IMPLEMENTS
%token<_identifier_node> IMPORT
%token<_identifier_node> INTERFACE
%token<_identifier_node> LATE
%token<_identifier_node> LIBRARY
%token<_identifier_node> MIXIN
%token<_identifier_node> OPERATOR
%token<_identifier_node> PART
%token<_identifier_node> REQUIRED
%token<_identifier_node> SET
%token<_identifier_node> STATIC
%token<_identifier_node> TYPEDEF

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

%precedence PRIMARY

%left INTERPOLATION_CONCAT

%nterm<_identifier_node>builtInIdentifier
%nterm<_identifier_node>identifier

%%
    //-------------- ВЕРХНИЙ УРОВЕНЬ --------------

    partDeclaration: topLevelDeclaration
        | partDeclaration topLevelDeclaration
    ;

    //Дописать
    topLevelDeclaration: classDeclaration
        | enumType
        | LATE FINAL type initializedIdentifierList ';'
        | LATE FINAL initializedIdentifierList ';'
        | functionSignature functionBody
        | FINAL type staticFinalDeclarationList ';'
        | FINAL staticFinalDeclarationList ';'
        | CONST type staticFinalDeclarationList ';'
        | CONST staticFinalDeclarationList ';'
        | LATE VAR initializedIdentifierList ';'
        | LATE type initializedIdentifierList ';'
        | VAR initializedIdentifierList ';'
        | type initializedIdentifierList ';'
    ;

    //-------------- БАЗОВЫЕ ПОНЯТИЯ --------------

    builtInIdentifier: ABSTRACT            {$$ = $1;}
        | AS                               {$$ = $1;}
        | COVARIANT                        {$$ = $1;}
        | DEFERRED                         {$$ = $1;}
        | DYNAMIC                          {$$ = $1;}
        | EXPORT                           {$$ = $1;}
        | EXTERNAL                         {$$ = $1;}
        | EXTENSION                        {$$ = $1;}
        | FACTORY                          {$$ = $1;}
        | FUNCTION                         {$$ = $1;}
        | GET                              {$$ = $1;}
        | IMPLEMENTS                       {$$ = $1;}
        | IMPORT                           {$$ = $1;}
        | INTERFACE                        {$$ = $1;}
        | LATE                             {$$ = $1;}
        | LIBRARY                          {$$ = $1;}
        | MIXIN                            {$$ = $1;}
        | OPERATOR                         {$$ = $1;}
        | PART                             {$$ = $1;}
        | REQUIRED                         {$$ = $1;}
        | SET                              {$$ = $1;}
        | STATIC                           {$$ = $1;}
        | TYPEDEF                          {$$ = $1;}
    ;

    identifier: IDENTIFIER                 {$$ = $1;}
        | builtInIdentifier                {$$ = $1;}
    ;

    identifierList: identifier
        | identifierList ',' identifier
    ;

    string: string INTERPOLATION_CONCAT expr INTERPOLATION_CONCAT STRING_LITERAL %prec INTERPOLATION_CONCAT {}
        | STRING_LITERAL                    {}
    ;

    //-------------- ВЫРАЖЕНИЯ --------------

    primary: THIS                          {}
        | SUPER
        | NULL_
        | INTEGER_LITERAL                  {}
        | DOUBLE_LITERAL                   {}
        | BOOLEAN_LITERAL                  {}
        | string                       %prec PRIMARY{}
        | '(' expr ')'
    ;

    assignableSelector: '[' expr ']'
        | '.' idDotList
        | '.' IDDotList
    ;

    selector: '.' identifier arguments 
        | '.' identifier ambiguousArgumentsOrParameterList
        | assignableSelector
    ;

    selectorExpr: idDotList arguments
        | idDotList ambiguousArgumentsOrParameterList
        | IDDotList arguments
        | IDDotList ambiguousArgumentsOrParameterList
        | identifier arguments
        | identifier ambiguousArgumentsOrParameterList
        | NEW IDDotList arguments
        | NEW IDDotList ambiguousArgumentsOrParameterList
        | NEW IDENTIFIER arguments
        | NEW IDENTIFIER ambiguousArgumentsOrParameterList
        | CONST IDDotList arguments
        | CONST IDDotList ambiguousArgumentsOrParameterList
        | CONST IDENTIFIER arguments
        | CONST IDENTIFIER ambiguousArgumentsOrParameterList
        | primary selector
        | selectorExpr selector
    ;

    //Можно вставить и в exprNotAssign, но по какой-то причине это вызывает конфликты c POSTFIX_INC/DEC, хотя приоритеты определены.
    postfixExpr: primary
        | idDotList      
        | IDDotList  
        | identifier              
        | selectorExpr
        | postfixExpr INC %prec POSTFIX_INC                    {}
        | postfixExpr DEC %prec POSTFIX_DEC                    {}
    ;

    //Можно бы указать exprNotAssign вместо expr, но это не имеет значения из-за приоритетов
    exprNotAssign: postfixExpr
        /* | expr '?' expr ':' expr           {} */
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
    ;

    expr: exprNotAssign                          {}
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
    ;

    exprList: expr 
        | exprList ',' expr
    ;

    exprStatement: ';'                                  {}
        | expr ';'                                      {}
    ;

    //-------------- ТИПИЗАЦИЯ --------------
    
    IDDotList: IDENTIFIER '.' IDENTIFIER
        | IDDotList '.' IDENTIFIER
    ;

    idDotList: builtInIdentifier '.' IDENTIFIER
        | builtInIdentifier '.' builtInIdentifier
        | IDDotList '.' builtInIdentifier
        | idDotList '.' IDENTIFIER
        | idDotList '.' builtInIdentifier
    ;

    typeName: IDENTIFIER
        | IDDotList
        | DYNAMIC
    ;

    typeNotVoid: typeName
        | typeName '?'
    ; 

    typeNotVoidList: typeNotVoid
        | typeNotVoidList ',' typeNotVoid
    ;

    type: typeNotVoid
        | VOID
    ;

    //finalConstVarOrType
    declarator: LATE FINAL type                         {}
        | LATE FINAL                                    {}
        | FINAL type                                    {}
        | FINAL                                         {}
        | CONST type                                    {}
        | CONST                                         {}
        | LATE VAR                                      {}
        | LATE type                                      {}
        | VAR                                           {}
        | type                                           {}
    ;

    //-------------- ПЕРЕМЕННЫЕ И ИНИЦИАЛИЗАЦИЯ --------------

    declaredIdentifier: LATE FINAL type identifier                       {}
        | LATE FINAL identifier                                   {}
        | FINAL type identifier                                   {}
        | FINAL identifier                                        {}
        | CONST type identifier                                   {}
        | CONST identifier                                        {}
        | LATE VAR identifier                                      {}
        | LATE type identifier                                      {}
        | VAR identifier                             {}
        | type identifier                             {}
    ;

    initializedIdentifier: staticFinalDeclaration
        | identifier
    ;

    initializedIdentifierList: initializedIdentifier
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
        | IF '(' expr ')' statement ELSE statement
    ;

    switchCase: CASE expr ':' statements
    ;

    switchCases: switchCase
        | switchCases switchCase
    ;

    switchStatement: SWITCH '(' expr ')' '{' switchCases '}'
        | SWITCH '(' expr ')' '{' switchCases  DEFAULT ':' statements '}'
    ;

    //-------------- ЦИКЛЫ --------------

    forStatement: FOR '(' forInitializerStatement exprStatement exprList ')' statement
        | FOR '(' forInitializerStatement exprStatement ')' statement
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
        | localFunctionDeclaration
        | statementBlock
    ;

    statements: statement
        | statements statement
    ;

    statementBlock: '{' statements '}'               {}
    ;

    //-------------- ФУНКЦИИ --------------

    formalParameterList: '(' normalFormalParameterList ',' ')'
        | '(' normalFormalParameterList ')'
        /* | '(' ')' */
    ;

    normalFormalParameter: declaredIdentifier
        /* | identifier */      // покрывается ambiguousArgumentsOrParameterList
        /* | declarator THIS '.' identifier
        | THIS '.' identifier */       // см fieldFormalParameter
    ;

    normalFormalParameterList: normalFormalParameter
        | normalFormalParameterList ',' normalFormalParameter
    ;

    ambiguousArgumentsOrParameterList: '(' identifierList ')'
        | '(' identifierList ',' ')'
        | '(' ')'
    ;

    arguments: '(' exprList ',' ')'
        | '(' exprList ')'
        /* | '(' ')' */
    ;

    functionSignature: type identifier formalParameterList
        | identifier formalParameterList
        | type identifier ambiguousArgumentsOrParameterList
        | identifier ambiguousArgumentsOrParameterList
    ;
    
    functionBody: FUNC_ARROW expr ';'
        | statementBlock
    ;

    localFunctionDeclaration: functionSignature functionBody
    ;

    //-------------- ЕНАМ --------------

    enumType: ENUM identifier '{' identifierList '}'         {}
    ;

    //------- КЛАССЫ --------------

    mixins: WITH typeNotVoidList
    ;

    superclassOpt: %empty
        | EXTENDS typeNotVoid
        | EXTENDS typeNotVoid mixins
        | mixins
    ;

    interfacesOpt: %empty
        | IMPLEMENTS typeNotVoidList
    ;

    classDeclaration: CLASS IDENTIFIER superclassOpt interfacesOpt '{' classMemberDeclarations '}'
        | ABSTRACT CLASS IDENTIFIER superclassOpt interfacesOpt '{' classMemberDeclarations '}'
        | CLASS identifier '=' typeNotVoid mixins interfacesOpt ';'
        | ABSTRACT CLASS identifier '=' typeNotVoid mixins interfacesOpt ';'
    ;

    staticFinalDeclaration: identifier '=' expr
    ;

    staticFinalDeclarationList: staticFinalDeclaration
        | staticFinalDeclarationList ',' staticFinalDeclaration
    ;

    classMemberDeclaration: declaration ';'
        | methodSignature functionBody
    ;

    classMemberDeclarations: classMemberDeclaration
        | classMemberDeclarations classMemberDeclaration
    ;

    //Дописать
    declaration: STATIC CONST type staticFinalDeclarationList
        | STATIC CONST staticFinalDeclarationList
        | STATIC FINAL type staticFinalDeclarationList
        | STATIC FINAL staticFinalDeclarationList
        | STATIC LATE FINAL type initializedIdentifierList
        | STATIC LATE FINAL initializedIdentifierList
        | STATIC LATE VAR initializedIdentifierList
        | STATIC LATE type initializedIdentifierList
        | STATIC VAR initializedIdentifierList
        | STATIC type initializedIdentifierList
        | LATE FINAL type initializedIdentifierList
        | LATE FINAL initializedIdentifierList
        | FINAL type initializedIdentifierList
        | FINAL initializedIdentifierList
        | LATE VAR initializedIdentifierList
        | LATE type initializedIdentifierList
        | VAR initializedIdentifierList
        | type initializedIdentifierList
        | constantConstructorSignature
        | constantConstructorSignature redirection
        | constantConstructorSignature initializers
        | namedConstructorSignature
        | namedConstructorSignature redirection
        | namedConstructorSignature initializers
        | functionSignature
        | functionSignature redirection
        | functionSignature initializers
    ;

    // Дописать
    methodSignature: functionSignature
        | STATIC functionSignature
        | namedConstructorSignature
        | namedConstructorSignature initializers
    ;


    //------- КОНСТРУКТОРЫ --------------

    fieldFormalParameter: declarator THIS '.' identifier
        | THIS '.' identifier
    ;

    constructorFormalParameterList: fieldFormalParameter
        | normalFormalParameterList ',' fieldFormalParameter
        | constructorFormalParameterList ',' normalFormalParameter
        | constructorFormalParameterList ',' fieldFormalParameter
    ;

    constructorFormalParameters: '(' constructorFormalParameterList ',' ')'
        | '(' constructorFormalParameterList ')'
    ;

    namedConstructorSignature: IDDotList formalParameterList
        | IDDotList constructorFormalParameters
        | IDDotList ambiguousArgumentsOrParameterList
    ;

    constantConstructorSignature: CONST IDDotList formalParameterList
        | CONST IDDotList constructorFormalParameters
        | CONST IDDotList ambiguousArgumentsOrParameterList
        | CONST IDENTIFIER formalParameterList
        | CONST IDENTIFIER constructorFormalParameters
        | CONST IDENTIFIER ambiguousArgumentsOrParameterList
    ;

    // вызвать именованный конструктор или другой конструктор 
    // Пример: Car.withoutABS(this.make, this.model, this.yearMade): this(make, model, yearMade, false);
    redirection: ':' THIS '.' identifier arguments
        | ':' THIS arguments
        | ':' THIS '.' identifier ambiguousArgumentsOrParameterList
        | ':' THIS ambiguousArgumentsOrParameterList
    ;

    initializerListEntry: SUPER arguments 
        | SUPER '.' identifier arguments
        | SUPER ambiguousArgumentsOrParameterList 
        | SUPER '.' identifier ambiguousArgumentsOrParameterList
        | THIS '.' identifier '=' exprNotAssign
    ;

    initializers: ':' initializerListEntry
        | initializers ',' initializerListEntry
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