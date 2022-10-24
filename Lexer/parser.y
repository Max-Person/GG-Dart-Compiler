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

%token END
%token INC
%token DEC
%right '?'
%right ';'
%right ':'

//операторы
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

//литералы
%token <intval>INTEGER_LITERAL
%token <doubleval>DOUBLE_LITERAL
%token <stringval>STRING_LITERAL
%token <boolval> BOOLEAN_LITERAL
%token <_identifier_node>IDENTIFIER

//Ключевые слова
%token ASSERT BREAK CASE CATCH CLASS CONST CONTINUE DEFAULT DO ELSE ENUM EXTENDS FALSE FINAL FINALLY FOR IF IN IS NEW NULL_ RETHROW RETURN SUPER SWITCH THIS THROW TRUE TRY VAR VOID WHILE WITH

//Built-in идентификаторы
%token<_identifier_node> ABSTRACT AS COVARIANT DEFERRED DYNAMIC EXPORT EXTERNAL EXTENSION FACTORY FUNCTION GET IMPLEMENTS IMPORT INTERFACE LATE LIBRARY MIXIN OPERATOR PART REQUIRED SET STATIC TYPEDEF

//"другие" индентификаторы - надо записать в обычные
%token ASYNC HIDE OF ON SHOW SYNC YIELD FUNC_ARROW DOUBLE_DOT VOPROS_DOT

//служебное
%precedence PRIMARY
%left INTERPOLATION_CONCAT

//---

%union {
    long long intval;
    double doubleval;
    char *stringval;
    bool boolval;
    
    identifier_node* _identifier_node;
    ambiguousArgumentsOrParameterList_node* _ambiguousArgumentsOrParameterList_node;
    arguments_node* _arguments_node;
    selector_node* _selector_node;
    expr_node* _expr;
    type_node* _type_node;
    declarator_node* _declarator_node;
    declaredIdentifier_node* _declaredIdentifier_node;
    variableDeclaration_node* _variableDeclaration_node;
    idInit_node* _idInit_node;
}

%nterm<_identifier_node>identifier builtInIdentifier identifierList IDDotList idDotList
%nterm<_ambiguousArgumentsOrParameterList_node>ambiguousArgumentsOrParameterList
%nterm<_arguments_node>arguments
%nterm<_selector_node>assignableSelector selector
%nterm<_expr>string primary selectorExpr postfixExpr exprNotAssign expr exprList
%nterm<_type_node>typeName typeNotVoid typeNotVoidList type
%nterm<_declarator_node>declarator
%nterm<_declaredIdentifier_node>declaredIdentifier
%nterm<_idInit_node>staticFinalDeclaration staticFinalDeclarationList initializedIdentifier initializedIdentifierList
%nterm<_variableDeclaration_node>variableDeclaration

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

    identifierList: identifier              {$$ = $1;}
        | identifierList ',' identifier     {$$ = identifierLists_add($1, $3);}
    ;

    string: string INTERPOLATION_CONCAT expr INTERPOLATION_CONCAT STRING_LITERAL %prec INTERPOLATION_CONCAT     {$$ = create_strInterpolation_expr_node($1, $3, $5);}
        | STRING_LITERAL                    {$$ = create_strlit_expr_node($1);}
    ;

    //-------------- ВЫРАЖЕНИЯ --------------

    primary: THIS                           {$$ = create_this_expr_node();}
        | SUPER                             {$$ = create_super_expr_node();}
        | NULL_                             {$$ = create_null_expr_node();}
        | INTEGER_LITERAL                   {$$ = create_intlit_expr_node($1);}
        | DOUBLE_LITERAL                    {$$ = create_doublelit_expr_node($1);}
        | BOOLEAN_LITERAL                   {$$ = create_boollit_expr_node($1);}
        | string            %prec PRIMARY   {$$ = $1;}
        | '(' expr ')'                      {$$ = $2;}
    ;

    assignableSelector: '[' expr ']'        {$$ = create_brackets_selector_node($2);}
        | '.' idDotList                     {$$ = create_access_selector_node($2);} 
        | '.' IDDotList                     {$$ = create_access_selector_node($2);}
    ;

    selector: '.' identifier arguments      {$$ = create_methodCall_selector_node($2, $3);}
        | '.' identifier ambiguousArgumentsOrParameterList  {$$ = create_methodCall_selector_node($2, convert_ambiguous_to_arguments($3));}
        | assignableSelector                {$$ = $1;}
    ;

    selectorExpr: idDotList arguments                           {$$ = create_call_expr_node($1, $2);}
        | idDotList ambiguousArgumentsOrParameterList           {$$ = create_call_expr_node($1, convert_ambiguous_to_arguments($2));}
        | IDDotList arguments                                   {$$ = create_call_expr_node($1, $2);}
        | IDDotList ambiguousArgumentsOrParameterList           {$$ = create_call_expr_node($1, convert_ambiguous_to_arguments($2));}
        | identifier arguments                                  {$$ = create_call_expr_node($1, $2);}
        | identifier ambiguousArgumentsOrParameterList          {$$ = create_call_expr_node($1, convert_ambiguous_to_arguments($2));}
        | idDotList '[' expr ']'                                {$$ = create_selector_expr_node(create_idAccess_expr_node($1), create_brackets_selector_node($3));}
        | IDDotList '[' expr ']'                                {$$ = create_selector_expr_node(create_idAccess_expr_node($1), create_brackets_selector_node($3));}
        | identifier '[' expr ']'                               {$$ = create_selector_expr_node(create_idAccess_expr_node($1), create_brackets_selector_node($3));}
        | NEW IDDotList arguments                               {$$ = create_constructNew_expr_node($2, $3);}
        | NEW IDDotList ambiguousArgumentsOrParameterList       {$$ = create_constructNew_expr_node($2, convert_ambiguous_to_arguments($3));}
        | NEW IDENTIFIER arguments                              {$$ = create_constructNew_expr_node($2, $3);}
        | NEW IDENTIFIER ambiguousArgumentsOrParameterList      {$$ = create_constructNew_expr_node($2, convert_ambiguous_to_arguments($3));}
        | CONST IDDotList arguments                             {$$ = create_constructConst_expr_node($2, $3);}
        | CONST IDDotList ambiguousArgumentsOrParameterList     {$$ = create_constructConst_expr_node($2, convert_ambiguous_to_arguments($3));}
        | CONST IDENTIFIER arguments                            {$$ = create_constructConst_expr_node($2, $3);}
        | CONST IDENTIFIER ambiguousArgumentsOrParameterList    {$$ = create_constructConst_expr_node($2, convert_ambiguous_to_arguments($3));}
        | primary                                               {$$ = $1;}
        | selectorExpr selector                                 {$$ = create_selector_expr_node($1, $2);}
    ;

    //Можно вставить и в exprNotAssign, но по какой-то причине это вызывает конфликты c POSTFIX_INC/DEC, хотя приоритеты определены.
    postfixExpr: idDotList                      {$$ = create_idAccess_expr_node($1);}
        | IDDotList                             {$$ = create_idAccess_expr_node($1);}
        | identifier                            {$$ = create_idAccess_expr_node($1);}
        | selectorExpr                          {$$ = $1;}
        | postfixExpr INC %prec POSTFIX_INC     {$$ = create_operator_expr_node(postfix_inc, $1, NULL);}
        | postfixExpr DEC %prec POSTFIX_DEC     {$$ = create_operator_expr_node(postfix_dec, $1, NULL);}
    ;

    //Можно бы указать exprNotAssign вместо expr, но это не имеет значения из-за приоритетов
    exprNotAssign: postfixExpr          {$$ = $1;}
        /* | expr '?' expr ':' expr           {} */
        | expr IFNULL expr              {$$ = create_operator_expr_node(ifnull, $1, $3);}
        | expr OR expr                  {$$ = create_operator_expr_node(_or, $1, $3);}
        | expr AND expr                 {$$ = create_operator_expr_node(_and, $1, $3);}
        | expr EQ expr                  {$$ = create_operator_expr_node(eq, $1, $3);}
        | expr NEQ expr                 {$$ = create_operator_expr_node(neq, $1, $3);}
        | expr '>' expr                 {$$ = create_operator_expr_node(greater, $1, $3);}
        | expr '<' expr                 {$$ = create_operator_expr_node(less, $1, $3);}
        | expr GREATER_EQ expr          {$$ = create_operator_expr_node(greater_eq, $1, $3);}
        | expr LESS_EQ expr             {$$ = create_operator_expr_node(less_eq, $1, $3);}
        | expr '|' expr                 {$$ = create_operator_expr_node(b_or, $1, $3);}
        | expr '^' expr                 {$$ = create_operator_expr_node(b_xor, $1, $3);}
        | expr '&' expr                 {$$ = create_operator_expr_node(b_and, $1, $3);}
        | expr '+' expr                 {$$ = create_operator_expr_node(add, $1, $3);}
        | expr '-' expr                 {$$ = create_operator_expr_node(sub, $1, $3);}
        | expr '*' expr                 {$$ = create_operator_expr_node(mul, $1, $3);}
        | expr '/' expr                 {$$ = create_operator_expr_node(_div, $1, $3);}
        | expr '%' expr                 {$$ = create_operator_expr_node(mod, $1, $3);}
        | expr TRUNCDIV  expr           {$$ = create_operator_expr_node(truncdiv, $1, $3);}
        | '-'  expr %prec UMINUS        {$$ = create_operator_expr_node(u_minus, $2, NULL);}
        | '!'  expr                     {$$ = create_operator_expr_node(excl, $2, NULL);}
        | '~'  expr                     {$$ = create_operator_expr_node(tilde, $2, NULL);}
        | INC expr %prec PREFIX_INC     {$$ = create_operator_expr_node(prefix_inc, $2, NULL);}
        | DEC expr %prec PREFIX_DEC     {$$ = create_operator_expr_node(prefix_dec, $2, NULL);}
    ;

    expr: exprNotAssign                    {$$ = $1;}
        | expr '=' expr                    {$$ = create_operator_expr_node(assign, $1, $3);}
        | expr AND_ASSIGN expr             {$$ = create_operator_expr_node(and_assign, $1, $3);} 
        | expr OR_ASSIGN expr              {$$ = create_operator_expr_node(or_assign, $1, $3);}
        | expr XOR_ASSIGN expr             {$$ = create_operator_expr_node(xor_assign, $1, $3);}
        | expr MUL_ASSIGN expr             {$$ = create_operator_expr_node(mul_assign, $1, $3);}
        | expr DIV_ASSIGN expr             {$$ = create_operator_expr_node(div_assign, $1, $3);}
        | expr TRUNC_DIV_ASSIGN expr       {$$ = create_operator_expr_node(trunc_div_assign, $1, $3);}
        | expr MOD_ASSIGN expr             {$$ = create_operator_expr_node(mod_assign, $1, $3);}
        | expr ADD_ASSIGN expr             {$$ = create_operator_expr_node(add_assign, $1, $3);}
        | expr SUB_ASSIGN expr             {$$ = create_operator_expr_node(sub_assign, $1, $3);}
        | expr IFNULL_ASSIGN expr          {$$ = create_operator_expr_node(ifnull_assign, $1, $3);}
    ;

    exprList: expr              {$$ = $1;}
        | exprList ',' expr     {$$ = exprList_add($1, $3);}
    ;

    exprStatement: ';'                                  {}
        | expr ';'                                      {}
    ;

    //-------------- ТИПИЗАЦИЯ --------------
    
    IDDotList: IDENTIFIER '.' IDENTIFIER    {$$ = identifierLists_add($1, $3);}
        | IDDotList '.' IDENTIFIER          {$$ = identifierLists_add($1, $3);}
    ;

    idDotList: builtInIdentifier '.' IDENTIFIER     {$$ = identifierLists_add($1, $3);}   
        | builtInIdentifier '.' builtInIdentifier   {$$ = identifierLists_add($1, $3);}
        | IDDotList '.' builtInIdentifier           {$$ = identifierLists_add($1, $3);}
        | idDotList '.' IDENTIFIER                  {$$ = identifierLists_add($1, $3);}
        | idDotList '.' builtInIdentifier           {$$ = identifierLists_add($1, $3);}
    ;

    typeName: IDENTIFIER    {$$ = create_named_type_node($1, false);}
        | IDDotList         {$$ = create_named_type_node($1, false);}
        | DYNAMIC           {$$ = create_dynamic_type_node(false);}
    ;

    typeNotVoid: typeName   {$$ = $1;}
        | typeName '?'      {$$ = type_node_makeNullable($1, true);}
    ; 

    typeNotVoidList: typeNotVoid                {$$ = $1;}
        | typeNotVoidList ',' typeNotVoid       {$$ = typeList_add($1, $3);}
    ;

    type: typeNotVoid       {$$ = $1;}
        | VOID              {$$ = create_void_type_node();}
    ;

    //finalConstVarOrType
    declarator: LATE FINAL type     {$$ = create_declarator_node(true, true, false, $3);}
        | LATE FINAL                {$$ = create_declarator_node(true, true, false, NULL);}
        | FINAL type                {$$ = create_declarator_node(false, true, false, $2);}
        | FINAL                     {$$ = create_declarator_node(false, true, false, NULL);}
        | CONST type                {$$ = create_declarator_node(false, false, true, $2);}
        | CONST                     {$$ = create_declarator_node(false, false, true, NULL);}
        | LATE VAR                  {$$ = create_declarator_node(true, false, false, NULL);}
        | LATE type                 {$$ = create_declarator_node(true, false, false, $2);}
        | VAR                       {$$ = create_declarator_node(false, false, false, NULL);}
        | type                      {$$ = create_declarator_node(false, false, false, $1);}
    ;

    //-------------- ПЕРЕМЕННЫЕ И ИНИЦИАЛИЗАЦИЯ --------------

    declaredIdentifier: LATE FINAL type identifier      {$$ = create_declaredIdentifier_node(true, true, false, $3,     $4);}
        | LATE FINAL identifier                         {$$ = create_declaredIdentifier_node(true, true, false, NULL,   $3);}
        | FINAL type identifier                         {$$ = create_declaredIdentifier_node(false, true, false, $2,    $3);}
        | FINAL identifier                              {$$ = create_declaredIdentifier_node(false, true, false, NULL,  $2);}
        | CONST type identifier                         {$$ = create_declaredIdentifier_node(false, false, true, $2,    $3);}
        | CONST identifier                              {$$ = create_declaredIdentifier_node(false, false, true, NULL,  $2);}
        | LATE VAR identifier                           {$$ = create_declaredIdentifier_node(true, false, false, NULL,  $3);}
        | LATE type identifier                          {$$ = create_declaredIdentifier_node(true, false, false, $2,    $3);}
        | VAR identifier                                {$$ = create_declaredIdentifier_node(false, false, false, NULL, $2);}
        | type identifier                               {$$ = create_declaredIdentifier_node(false, false, false, $1,   $2);}
    ;

    initializedIdentifier: staticFinalDeclaration               {$$ = $1;}
        | identifier                                            {$$ = create_id_idInit_node($1);}
    ;

    initializedIdentifierList: initializedIdentifier            {$$ = $1;}
        | initializedIdentifierList ',' initializedIdentifier   {$$ = idInitList_add($1, $3);}
    ;

    variableDeclaration: declaredIdentifier                 {$$ = create_nonAssign_variableDeclaration_node($1);}
        | declaredIdentifier '=' expr                       {$$ = create_assign_variableDeclaration_node($1, $3);}
        | variableDeclaration ',' initializedIdentifier     {$$ = variableDeclaration_idInitList_add($1, $3);}
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

    ambiguousArgumentsOrParameterList: '(' identifierList ')'   {$$ = create_ambiguousArgumentsOrParameterList_node($2);}
        | '(' identifierList ',' ')'                            {$$ = create_ambiguousArgumentsOrParameterList_node($2);}
        | '(' ')'                                               {$$ = create_ambiguousArgumentsOrParameterList_node(NULL);}
    ;

    arguments: '(' exprList ',' ')'     {$$ = create_arguments_node($2);}
        | '(' exprList ')'              {$$ = create_arguments_node($2);}
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

    staticFinalDeclaration: identifier '=' expr                     {$$ = create_assign_idInit_node($1, $3);}
    ;

    staticFinalDeclarationList: staticFinalDeclaration              {$$ = $1;}
        | staticFinalDeclarationList ',' staticFinalDeclaration     {$$ = idInitList_add($1, $3);}
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