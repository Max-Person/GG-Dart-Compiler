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
    selector_node* _selector_node;
    expr_node* _expr;
    type_node* _type_node;
    declarator_node* _declarator_node;
    declaredIdentifier_node* _declaredIdentifier_node;
    variableDeclaration_node* _variableDeclaration_node;
    idInit_node* _idInit_node;
    stmt_node* _stmt_node;
    formalParameter_node* _formalParameter_node;
    initializer_node* _initializer_node;
    redirection_node* _redirection_node;
    signature_node* _signature_node;
}

%nterm<_identifier_node>identifier builtInIdentifier identifierList IDDotList idDotList ambiguousArgumentsOrParameterList
%nterm<_selector_node>assignableSelector selector
%nterm<_expr>string primary selectorExpr postfixExpr exprNotAssign expr exprList arguments
%nterm<_type_node>typeName typeNotVoid typeNotVoidList type
%nterm<_declarator_node>declarator
%nterm<_declaredIdentifier_node>declaredIdentifier
%nterm<_idInit_node>staticFinalDeclaration staticFinalDeclarationList initializedIdentifier initializedIdentifierList
%nterm<_variableDeclaration_node>variableDeclaration
%nterm<_stmt_node>whileStatement doStatement ifStatement breakStatement returnStatement continueStatement statement statements forInitializerStatement variableDeclarationStatement exprStatement forStatement statementBlock
%nterm<_formalParameter_node>normalFormalParameter normalFormalParameterList formalParameterList fieldFormalParameter constructorFormalParameters constructorFormalParameterList
%nterm<_initializer_node>initializerListEntry initializers
%nterm<_redirection_node>redirection
%nterm<_signature_node>functionSignature methodSignature namedConstructorSignature constantConstructorSignature

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
        | '[' exprList ']'                  {$$ = $2;}
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

    exprStatement: ';'                                  {$$ = create_expr_stmt_node(NULL);}
        | expr ';'                                      {$$ = create_expr_stmt_node($1);}
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

    variableDeclarationStatement: variableDeclaration ';'   {$$ = create_variable_declaration_stmt_node($1);}
    ;

    //-------------- РАЗВИЛКИ --------------

    ifStatement: IF '(' expr ')' statement      {$$ = create_if_stmt_node($3, $5, NULL);}
        | IF '(' expr ')' statement ELSE statement  {$$ = create_if_stmt_node($3, $5, $7);}
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

    forStatement: FOR '(' forInitializerStatement exprStatement exprList ')' statement      {//$$ = create_for_stmt();
    }
        | FOR '(' forInitializerStatement exprStatement ')' statement                       {//$$ = create_for_stmt($3, $4, $6, NULL);
        }
        | FOR '(' declaredIdentifier IN expr ')' statement                                  {//$$ = create_for_stmt($3, $5, , NULL);
        }
        | FOR '(' identifier IN expr ')' statement
    ;

    forInitializerStatement: variableDeclarationStatement       {$$ = $1;}
        | exprStatement                                         {$$ = $1;}
    ;

    whileStatement: WHILE '(' expr ')' statement    {$$ = create_while_stmt_node($3, $5);}
    ;

    doStatement: DO statement WHILE '(' expr ')' ';'    {$$ = create_do_stmt_node($2, $5);}
    ;

    //Здесь убрал идентификаторы, потому что это относится к лейблам
    breakStatement: BREAK ';'       {$$ = create_break_stmt_node();}
    ;

    continueStatement: CONTINUE ';'     {$$ = create_continue_stmt_node();}
    ;

    returnStatement: RETURN expr ';'       {$$ = create_return_stmt_node($2);}
        | RETURN ';'            {$$ = create_return_stmt_node(NULL);}
    ;

    //-------------- СТЕЙТМЕНТЫ ОБЩЕЕ --------------
    
    statement: exprStatement                {$$ = $1;}
        | variableDeclarationStatement      {$$ = $1;}
        | forStatement                      {$$ = $1;}
        | whileStatement                    {$$ = $1;}
        | doStatement                       {$$ = $1;}
        | switchStatement                   {$$ = $1;}
        | ifStatement                       {$$ = $1;}
        | breakStatement                    {$$ = $1;}
        | continueStatement                 {$$ = $1;}
        | returnStatement                   {$$ = $1;}
        | localFunctionDeclaration          {$$ = $1;}
        | statementBlock                    {$$ = $1;}
    ;

    statements: %empty                      {$$ = NULL;}
        | statements statement              {$$ = stmtList_add($1, $2);}
    ;

    statementBlock: '{' statements '}'               {$$ = $2;}
    ;

    //-------------- ФУНКЦИИ --------------

    formalParameterList: '(' normalFormalParameterList ',' ')'      {$$ = $2;}
        | '(' normalFormalParameterList ')'                         {$$ = $2;}
        /* | '(' ')' */
    ;

    normalFormalParameter: declaredIdentifier       {$$ = create_normal_formalParameter_node($1);}
        /* | identifier */      // покрывается ambiguousArgumentsOrParameterList
        /* | declarator THIS '.' identifier
        | THIS '.' identifier */       // см fieldFormalParameter
    ;

    normalFormalParameterList: normalFormalParameter            {$$ = $1;}
        | normalFormalParameterList ',' normalFormalParameter   {$$ = formalParameterList_add($1, $3);}
    ;

    ambiguousArgumentsOrParameterList: '(' identifierList ')'   {$$ = $2;}
        | '(' identifierList ',' ')'                            {$$ = $2;}
        | '(' ')'                                               {$$ = NULL;}
    ;

    arguments: '(' exprList ',' ')'     {$$ = $2;}
        | '(' exprList ')'              {$$ = $2;}
    ;

    functionSignature: type identifier formalParameterList      {$$ = create_funcOrConstruct_signature_node($1, $2, $3);}
        | identifier formalParameterList                        {$$ = create_funcOrConstruct_signature_node(NULL, $1, $2);}
        | type identifier ambiguousArgumentsOrParameterList     {$$ = create_funcOrConstruct_signature_node($1, $2, convert_ambiguous_to_parameters($3));}
        | identifier ambiguousArgumentsOrParameterList          {$$ = create_funcOrConstruct_signature_node(NULL, $1, convert_ambiguous_to_parameters($2));}
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
    methodSignature: functionSignature              {$$ = $1;}
        | STATIC functionSignature                  {$$ = signature_node_setStatic($2);}
        | namedConstructorSignature                 {$$ = $1;}
        | namedConstructorSignature initializers    {$$ = signature_node_addInitializers($1, $2);}
    ;


    //------- КОНСТРУКТОРЫ --------------

    fieldFormalParameter: declarator THIS '.' identifier    {$$ = create_field_formalParameter_node($1, $4);}
        | THIS '.' identifier                               {$$ = create_field_formalParameter_node(NULL, $3);}
    ;

    constructorFormalParameterList: fieldFormalParameter                {$$ = $1;}
        | normalFormalParameterList ',' fieldFormalParameter            {$$ = formalParameterList_add($1, $3);}
        | constructorFormalParameterList ',' normalFormalParameter      {$$ = formalParameterList_add($1, $3);}
        | constructorFormalParameterList ',' fieldFormalParameter       {$$ = formalParameterList_add($1, $3);}
    ;

    constructorFormalParameters: '(' constructorFormalParameterList ',' ')'     {$$ = $2;}
        | '(' constructorFormalParameterList ')'                                {$$ = $2;}
    ;

    namedConstructorSignature: IDDotList formalParameterList            {$$ = create_construct_signature_node(false, $1, $2);}
        | IDDotList constructorFormalParameters                         {$$ = create_construct_signature_node(false, $1, $2);}
        | IDDotList ambiguousArgumentsOrParameterList                   {$$ = create_construct_signature_node(false, $1, convert_ambiguous_to_parameters($2));}
    ;

    constantConstructorSignature: CONST IDDotList formalParameterList       {$$ = create_construct_signature_node(true, $2, $3);}
        | CONST IDDotList constructorFormalParameters                       {$$ = create_construct_signature_node(true, $2, $3);}
        | CONST IDDotList ambiguousArgumentsOrParameterList                 {$$ = create_construct_signature_node(true, $2, convert_ambiguous_to_parameters($3));}
        | CONST IDENTIFIER formalParameterList                              {$$ = create_construct_signature_node(true, $2, $3);}
        | CONST IDENTIFIER constructorFormalParameters                      {$$ = create_construct_signature_node(true, $2, $3);}
        | CONST IDENTIFIER ambiguousArgumentsOrParameterList                {$$ = create_construct_signature_node(true, $2, convert_ambiguous_to_parameters($3));}
    ;

    // вызвать именованный конструктор или другой конструктор 
    // Пример: Car.withoutABS(this.make, this.model, this.yearMade): this(make, model, yearMade, false);
    redirection: ':' THIS '.' identifier arguments                          {$$ = create_redirection_node($4, $5);}
        | ':' THIS arguments                                                {$$ = create_redirection_node(NULL, $3);}
        | ':' THIS '.' identifier ambiguousArgumentsOrParameterList         {$$ = create_redirection_node($4, convert_ambiguous_to_arguments($5));}
        | ':' THIS ambiguousArgumentsOrParameterList                        {$$ = create_redirection_node(NULL, convert_ambiguous_to_arguments($3));}
    ;

    initializerListEntry: SUPER arguments                           {$$ = create_superConstructor_initializer_node($2);}
        | SUPER '.' identifier arguments                            {$$ = create_superNamedConstructor_initializer_node($3, $4);}
        | SUPER ambiguousArgumentsOrParameterList                   {$$ = create_superConstructor_initializer_node(convert_ambiguous_to_arguments($2));}
        | SUPER '.' identifier ambiguousArgumentsOrParameterList    {$$ = create_superNamedConstructor_initializer_node($3, convert_ambiguous_to_arguments($4));}
        | THIS '.' identifier '=' exprNotAssign                     {$$ = create_thisAssign_initializer_node($3, $5);}
    ;

    initializers: ':' initializerListEntry                          {$$ = $2;}
        | initializers ',' initializerListEntry                     {$$ = initializerList_add($1, $3);}
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