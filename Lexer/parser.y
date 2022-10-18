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


%precedence PRIMARY

%left INTERPOLATION_CONCAT

%precedence OUTER_IF
%precedence INNER_IF

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
        | identifier                       {}
        | identifier arguments
        | identifier ambiguousArgumentsOrParameterList
        | NEW qualifiedName arguments
        | NEW qualifiedName ambiguousArgumentsOrParameterList
        | CONST qualifiedName arguments
        | CONST qualifiedName ambiguousArgumentsOrParameterList
        | '(' expr ')'
    ;

    assignableSelector: '[' expr ']'
        | '.' identifier
    ;

    selector: '.' identifier arguments 
        | '.' identifier ambiguousArgumentsOrParameterList
        | assignableSelector
    ;

    //--- Не используется (?) Проверять можно ли присваивать на этапе семантики.
    selectors: selector
        | selectors selector
    ;

    assignableExpr: selectorExpr assignableSelector
        | identifier
    ;
    //---

    selectorExpr: primary
        | selectorExpr selector
    ;

    //Можно вставить и в exprNotAssign, но по какой-то причине это вызывает конфликты c POSTFIX_INC/DEC, хотя приоритеты определены.
    postfixExpr: selectorExpr
        | postfixExpr INC %prec POSTFIX_INC                    {}
        | postfixExpr DEC %prec POSTFIX_DEC                    {}
    ;

    //Можно бы указать exprNotAssign вместо expr, но это не имеет значения из-за приоритетов
    exprNotAssign: postfixExpr
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

    //список из одного
    exprList: expr 
        | exprList ',' expr
    ;

    exprStatement: ';'                                  {}
        | expr ';'                                      {}
    ;

    //-------------- ТИПИЗАЦИЯ --------------
    // --- обычная типизация

    //список из одного
    //используется IDENTIFIER, потому что кажется built-in идентификаторы не могут быть названием класса
    //Ркурсии изменена на правую для избежания конфликта с именем конструктора
    qualifiedName: IDENTIFIER
        | IDENTIFIER '.' qualifiedName
    ;

    typeName: qualifiedName
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

    //LOOK почему здесь не используются expr statement? //добавил, вроде не поменялось
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

    statements: statement statement
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
        /* | '(' ')' */
    ;

    namedConstructorSignature: IDENTIFIER '.' identifier formalParameterList
        | IDENTIFIER '.' identifier constructorFormalParameters
        | IDENTIFIER '.' identifier ambiguousArgumentsOrParameterList
    ;

    constantConstructorSignature: CONST IDENTIFIER formalParameterList
        | CONST IDENTIFIER constructorFormalParameters
        | CONST IDENTIFIER ambiguousArgumentsOrParameterList
        | CONST IDENTIFIER '.' identifier formalParameterList
        | CONST IDENTIFIER '.' identifier constructorFormalParameters
        | CONST IDENTIFIER '.' identifier ambiguousArgumentsOrParameterList
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
        | fieldInitializer
    ;

    fieldInitializer: THIS '.' identifier '=' exprNotAssign
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