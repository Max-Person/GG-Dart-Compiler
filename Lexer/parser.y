%{
#include <stdio.h>
#include <stdlib.h>
//Сгенерированный макрос. Модифицирован чтобы обновлять глобальную переменную loc
# define YYLLOC_DEFAULT(Current, Rhs, N)                                \
    do                                                                  \
      if (N)                                                            \
        {                                                               \
          (Current).first_line   = YYRHSLOC (Rhs, 1).first_line;        \
          (Current).first_column = YYRHSLOC (Rhs, 1).first_column;      \
          (Current).last_line    = YYRHSLOC (Rhs, N).last_line;         \
          (Current).last_column  = YYRHSLOC (Rhs, N).last_column;       \
        }                                                               \
      else                                                              \
        {                                                               \
          (Current).first_line   = (Current).last_line   =              \
            YYRHSLOC (Rhs, 0).last_line;                                \
          (Current).first_column = (Current).last_column =              \
            YYRHSLOC (Rhs, 0).last_column;                              \
        }                                                               \
    while (0);                                                          \
    loc = (Current);

extern int yylineno;
extern FILE* yyin;

int yyparse();
int yylex();


void yyerror(char const *s) {
    fprintf(stderr, "Error: %s on line %d\n", s, yylineno);
    exit(1);
}
%}

%define parse.trace
%define parse.error detailed
%locations
%code requires{ #include "structures.h" }
%code provides{ extern topLevelDeclaration_node* root; }
%code provides{ extern YYLTYPE loc; }
%code {YYLTYPE loc;}

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
%nonassoc '>' '<' GREATER_EQ LESS_EQ AS IS
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
%token AS ASSERT BREAK CASE CATCH CLASS CONST CONTINUE DEFAULT DO ELSE ENUM EXTENDS FALSE FINAL FINALLY FOR IF IN IS NEW NULL_ RETHROW RETURN SUPER SWITCH THIS THROW TRUE TRY VAR VOID WHILE WITH
%token LIST

//Built-in идентификаторы
%token<_identifier_node> ABSTRACT COVARIANT DEFERRED DYNAMIC EXPORT EXTERNAL EXTENSION FACTORY FUNCTION GET IMPLEMENTS IMPORT INTERFACE LATE LIBRARY MIXIN OPERATOR PART REQUIRED SET STATIC TYPEDEF

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
    expr_node* _expr;
    type_node* _type_node;
    declarator_node* _declarator_node;
    singleVarDeclaration_node* _variableDeclaration_node;
    idInit_node* _idInit_node;
    stmt_node* _stmt_node;
    formalParameter_node* _formalParameter_node;
    initializer_node* _initializer_node;
    redirection_node* _redirection_node;
    signature_node* _signature_node;
    functionDefinition_node* _functionDefinition_node;
    switch_case_node* _switchCase_node;
    enum_node* _enumType_node;
    classMemberDeclaration_node* _classMemberDeclaration_node;
    supeclassOpt_node* _supeclassOpt_node;
    classDeclaration_node* _classDeclaration_node;
    topLevelDeclaration_node* _topLevelDeclaration_node;
}

%nterm<_identifier_node>identifier builtInIdentifier identifierList
%nterm<_expr>string primary postfixExpr exprNotAssign expr exprList arguments
%nterm<_type_node>typeNotVoid typeNotVoidList type mixins interfacesOpt
%nterm<_declarator_node>declarator
%nterm<_idInit_node>staticFinalDeclaration staticFinalDeclarationList initializedIdentifier initializedIdentifierList
%nterm<_variableDeclaration_node>variableDeclaration declaredIdentifier
%nterm<_stmt_node>whileStatement doStatement ifStatement breakStatement returnStatement continueStatement statement statements forInitializerStatement variableDeclarationStatement exprStatement forStatement statementBlock switchStatement functionBody
%nterm<_formalParameter_node>normalFormalParameter normalFormalParameterList formalParameterList fieldFormalParameter constructorFormalParameters constructorFormalParameterList
%nterm<_initializer_node>initializerListEntry initializers
%nterm<_redirection_node>redirection
%nterm<_signature_node>functionSignature methodSignature constructorSignature namedConstructorSignature constantConstructorSignature
%nterm<_switchCase_node> switchCase switchCases
%nterm<_enumType_node> enumType
%nterm<_functionDefinition_node>localFunctionDeclaration
%nterm<_classMemberDeclaration_node>declaration classMemberDeclaration classMemberDeclarations
%nterm<_supeclassOpt_node>superclassOpt
%nterm<_classDeclaration_node>classDeclaration
%nterm<_topLevelDeclaration_node>topLevelDeclaration partDeclaration

%%
    //-------------- ВЕРХНИЙ УРОВЕНЬ --------------

    partDeclaration: %empty                                 {$$ = NULL; root = $$;}
        | partDeclaration topLevelDeclaration               {$$ = topLevelDeclarationList_add($1, $2); root = $$;}
    ;

    //Дописать
    topLevelDeclaration: classDeclaration                   {$$ = create_class_topLevelDeclaration_node($1);}
        | enumType                                          {$$ = create_enum_topLevelDeclaration_node($1);}
        | functionSignature functionBody                    {$$ = create_func_topLevelDeclaration_node($1, $2);}
        | LATE FINAL type initializedIdentifierList ';'     {$$ = create_var_topLevelDeclaration_node(true, true, false, $3,     $4);}
        | LATE FINAL initializedIdentifierList ';'          {$$ = create_var_topLevelDeclaration_node(true, true, false, NULL,   $3);}
        | FINAL type staticFinalDeclarationList ';'         {$$ = create_var_topLevelDeclaration_node(false, true, false, $2,    $3);}
        | FINAL staticFinalDeclarationList ';'              {$$ = create_var_topLevelDeclaration_node(false, true, false, NULL,  $2);}
        | CONST type staticFinalDeclarationList ';'         {$$ = create_var_topLevelDeclaration_node(false, false, true, $2,    $3);}
        | CONST staticFinalDeclarationList ';'              {$$ = create_var_topLevelDeclaration_node(false, false, true, NULL,  $2);}
        | LATE VAR initializedIdentifierList ';'            {$$ = create_var_topLevelDeclaration_node(true, false, false, NULL,  $3);}
        | LATE type initializedIdentifierList ';'           {$$ = create_var_topLevelDeclaration_node(true, false, false, $2,    $3);}
        | VAR initializedIdentifierList ';'                 {$$ = create_var_topLevelDeclaration_node(false, false, false, NULL, $2);}
        | type initializedIdentifierList ';'                {$$ = create_var_topLevelDeclaration_node(false, false, false, $1,   $2);}
    ;

    //-------------- БАЗОВЫЕ ПОНЯТИЯ --------------

    builtInIdentifier: ABSTRACT            {$$ = $1;}
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
        | identifier                        {$$ = create_id_expr_node($1);}
        | '[' exprList ']'                  {$$ = create_listlit_expr_node($2);}
        | '[' ']'                           {$$ = create_listlit_expr_node(NULL);}
        | '(' expr ')'                      {$$ = $2;}
    ;

    //Можно вставить и в exprNotAssign, но по какой-то причине это вызывает конфликты c POSTFIX_INC/DEC, хотя приоритеты определены.
    postfixExpr: primary                                                       {$$ = $1;}
        | identifier arguments                                                  {$$ = create_call_expr_node($1, $2);}
        | NEW IDENTIFIER arguments                                              {$$ = create_constructNew_expr_node($2, $3);}
        | NEW IDENTIFIER '.' identifier arguments                               {$$ = create_constructNew_expr_node($2, $4, $5);}
        | CONST IDENTIFIER arguments                                            {$$ = create_constructConst_expr_node($2, $3);}
        | CONST IDENTIFIER '.' identifier arguments                             {$$ = create_constructConst_expr_node($2, $4, $5);}
        | postfixExpr '.' identifier arguments                                 {$$ = create_methodCall_expr_node($1, $3, $4);}
        | postfixExpr '[' expr ']'                                             {$$ = create_operator_expr_node(brackets, $1, $3);}
        | postfixExpr '.' identifier                                           {$$ = create_fieldAccess_expr_node($1, $3);}
        | postfixExpr INC %prec POSTFIX_INC     {$$ = create_operator_expr_node(postfix_inc, $1, NULL);}
        | postfixExpr DEC %prec POSTFIX_DEC     {$$ = create_operator_expr_node(postfix_dec, $1, NULL);}
        | postfixExpr '!'                       {$$ = create_operator_expr_node(bang, $1, NULL);}
    ;

    //Можно бы указать exprNotAssign вместо expr, но это не имеет значения из-за приоритетов
    exprNotAssign: postfixExpr          {$$ = $1;}
        | expr IFNULL expr              {$$ = create_operator_expr_node(ifnull, $1, $3);}
        | expr OR expr                  {$$ = create_operator_expr_node(_or, $1, $3);}
        | expr AND expr                 {$$ = create_operator_expr_node(_and, $1, $3);}
        | expr EQ expr                  {$$ = create_operator_expr_node(eq, $1, $3);}
        | expr NEQ expr                 {$$ = create_operator_expr_node(neq, $1, $3);}
        | expr '>' expr                 {$$ = create_operator_expr_node(greater, $1, $3);}
        | expr '<' expr                 {$$ = create_operator_expr_node(less, $1, $3);}
        | expr GREATER_EQ expr          {$$ = create_operator_expr_node(greater_eq, $1, $3);}
        | expr LESS_EQ expr             {$$ = create_operator_expr_node(less_eq, $1, $3);}
        | expr AS typeNotVoid           {$$ = create_typeOp_expr_node(type_cast, $1, $3);}
        | expr IS typeNotVoid           {$$ = create_typeOp_expr_node(type_check, $1, $3);}
        | expr IS '!' typeNotVoid       {$$ = create_typeOp_expr_node(neg_type_check, $1, $4);}
        | expr '+' expr                 {$$ = create_operator_expr_node(add, $1, $3);}
        | expr '-' expr                 {$$ = create_operator_expr_node(sub, $1, $3);}
        | expr '*' expr                 {$$ = create_operator_expr_node(mul, $1, $3);}
        | expr '/' expr                 {$$ = create_operator_expr_node(_div, $1, $3);}
        | '-'  expr %prec UMINUS        {$$ = create_operator_expr_node(u_minus, $2, NULL);}
        | '!'  expr                     {$$ = create_operator_expr_node(_not, $2, NULL);}
        | INC expr %prec PREFIX_INC     {$$ = create_operator_expr_node(prefix_inc, $2, NULL);}
        | DEC expr %prec PREFIX_DEC     {$$ = create_operator_expr_node(prefix_dec, $2, NULL);}
    ;

    expr: exprNotAssign                    {$$ = $1;}
        | expr '=' expr                    {$$ = create_operator_expr_node(assign, $1, $3);}
        | expr AND_ASSIGN expr             {$$ = create_operator_expr_node(and_assign, $1, $3);} 
        | expr OR_ASSIGN expr              {$$ = create_operator_expr_node(or_assign, $1, $3);}
        | expr MUL_ASSIGN expr             {$$ = create_operator_expr_node(mul_assign, $1, $3);}
        | expr DIV_ASSIGN expr             {$$ = create_operator_expr_node(div_assign, $1, $3);}
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
    
    typeNotVoid: IDENTIFIER   {$$ = create_named_type_node($1, false);}
        | IDENTIFIER '?'      {$$ = create_named_type_node($1, true);}
        | LIST '<' typeNotVoid '>'          {$$ = create_list_type_node($3, false);}
        | LIST '<' typeNotVoid '>' '?'      {$$ = create_list_type_node($3, true);}    
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

    declaredIdentifier: LATE FINAL type identifier      {$$ = create_single_variableDeclaration_node(true, true, false, $3,     $4);}
        | LATE FINAL identifier                         {$$ = create_single_variableDeclaration_node(true, true, false, NULL,   $3);}
        | FINAL type identifier                         {$$ = create_single_variableDeclaration_node(false, true, false, $2,    $3);}
        | FINAL identifier                              {$$ = create_single_variableDeclaration_node(false, true, false, NULL,  $2);}
        | CONST type identifier                         {$$ = create_single_variableDeclaration_node(false, false, true, $2,    $3);}
        | CONST identifier                              {$$ = create_single_variableDeclaration_node(false, false, true, NULL,  $2);}
        | LATE VAR identifier                           {$$ = create_single_variableDeclaration_node(true, false, false, NULL,  $3);}
        | LATE type identifier                          {$$ = create_single_variableDeclaration_node(true, false, false, $2,    $3);}
        | VAR identifier                                {$$ = create_single_variableDeclaration_node(false, false, false, NULL, $2);}
        | type identifier                               {$$ = create_single_variableDeclaration_node(false, false, false, $1,   $2);}
    ;

    initializedIdentifier: staticFinalDeclaration               {$$ = $1;}
        | identifier                                            {$$ = create_id_idInit_node($1);}
    ;

    initializedIdentifierList: initializedIdentifier            {$$ = $1;}
        | initializedIdentifierList ',' initializedIdentifier   {$$ = idInitList_add($1, $3);}
    ;

    variableDeclaration: LATE FINAL type initializedIdentifierList    {$$ = create_variableDeclaration_node(true, true, false, $3,     $4);}
        | LATE FINAL initializedIdentifierList                        {$$ = create_variableDeclaration_node(true, true, false, NULL,   $3);}
        | FINAL type initializedIdentifierList                        {$$ = create_variableDeclaration_node(false, true, false, $2,    $3);}
        | FINAL initializedIdentifierList                             {$$ = create_variableDeclaration_node(false, true, false, NULL,  $2);}
        | CONST type initializedIdentifierList                        {$$ = create_variableDeclaration_node(false, false, true, $2,    $3);}
        | CONST initializedIdentifierList                             {$$ = create_variableDeclaration_node(false, false, true, NULL,  $2);}
        | LATE VAR initializedIdentifierList                          {$$ = create_variableDeclaration_node(true, false, false, NULL,  $3);}
        | LATE type initializedIdentifierList                         {$$ = create_variableDeclaration_node(true, false, false, $2,    $3);}
        | VAR initializedIdentifierList                               {$$ = create_variableDeclaration_node(false, false, false, NULL, $2);}
        | type initializedIdentifierList                              {$$ = create_variableDeclaration_node(false, false, false, $1,   $2);}
    ;

    variableDeclarationStatement: variableDeclaration ';'   {$$ = create_variable_declaration_stmt_node($1);}
    ;

    //-------------- РАЗВИЛКИ --------------

    ifStatement: IF '(' expr ')' statement      {$$ = create_if_stmt_node($3, $5, NULL);}
        | IF '(' expr ')' statement ELSE statement  {$$ = create_if_stmt_node($3, $5, $7);}
    ;

    switchCase: CASE expr ':' statements        {$$ = create_switch_case_node($4, $2);}
    ;

    switchCases: switchCase                     {$$ = $1;}
        | switchCases switchCase                {$$ = switchCaseList_add($1, $2);}
    ;

    switchStatement: SWITCH '(' expr ')' '{' switchCases '}'        {$$ = create_switch_case_stmt_node($3, $6,  NULL);}
        | SWITCH '(' expr ')' '{' switchCases  DEFAULT ':' statements '}'       {$$ = create_switch_case_stmt_node($3, $6, $9);}
    ;

    //-------------- ЦИКЛЫ --------------

    forStatement: FOR '(' forInitializerStatement exprStatement exprList ')' statement      {$$ = create_forN_stmt_node($3, $4, $5, $7);}
        | FOR '(' forInitializerStatement exprStatement ')' statement                       {$$ = create_forN_stmt_node($3, $4, NULL, $6);}
        | FOR '(' declaredIdentifier IN expr ')' statement                                  {$$ = create_forEach_stmt_node($3, $5, $7);}
        | FOR '(' identifier IN expr ')' statement                                          {$$ = create_forEach_stmt_node($3, $5, $7);}
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
        | localFunctionDeclaration          {$$ = create_functionDefinition_stmt_node($1);}
        | statementBlock                    {$$ = $1;}
    ;

    statements: %empty                      {$$ = NULL;} // {statements statement statement}
        | statements statement              {$$ = stmtList_add($1, $2);}
    ;

    statementBlock: '{' statements '}'               {$$ = create_block_stmt_node($2);}
    ;

    //-------------- ФУНКЦИИ --------------

    formalParameterList: '(' normalFormalParameterList ',' ')'      {$$ = $2;}
        | '(' normalFormalParameterList ')'                         {$$ = $2;}
        | '(' ')'                                                   {$$ = NULL;}
    ;

    normalFormalParameter: declaredIdentifier       {$$ = create_normal_formalParameter_node($1);}
        /* | declarator THIS '.' identifier
        | THIS '.' identifier */       // см fieldFormalParameter
    ;

    normalFormalParameterList: normalFormalParameter            {$$ = $1;}
        | normalFormalParameterList ',' normalFormalParameter   {$$ = formalParameterList_add($1, $3);}
    ;

    arguments: '(' exprList ',' ')'     {$$ = $2;}
        | '(' exprList ')'              {$$ = $2;}
        | '(' ')'                       {$$ = NULL;}
    ;

    functionSignature: type identifier formalParameterList      {$$ = create_funcOrConstruct_signature_node($1, $2, $3);}
    ;
    
    functionBody: FUNC_ARROW expr ';'   {$$ = create_return_stmt_node($2);}
        | statementBlock                {$$ = $1;}
    ;

    localFunctionDeclaration: functionSignature functionBody    {$$ = create_functionDefinition_node($1, $2);}
    ;

    //-------------- ЕНАМ --------------

    enumType: ENUM identifier '{' identifierList '}'         {$$ = create_enum_node($2, $4);}
        | ENUM identifier '{' identifierList ',' '}'         {$$ = create_enum_node($2, $4);}
    ;

    //------- КЛАССЫ --------------

    mixins: WITH typeNotVoidList            {$$ = $2;}
    ;

    superclassOpt: %empty                   {$$ = create_supeclassOpt_node(NULL, NULL);}
        | EXTENDS typeNotVoid               {$$ = create_supeclassOpt_node($2, NULL);}
        | EXTENDS typeNotVoid mixins        {$$ = create_supeclassOpt_node($2, $3);}
        | mixins                            {$$ = create_supeclassOpt_node(NULL, $1);}
    ;

    interfacesOpt: %empty                   {$$ = NULL;}
        | IMPLEMENTS typeNotVoidList        {$$ = $2;}
    ;

    classDeclaration: CLASS IDENTIFIER superclassOpt interfacesOpt '{' classMemberDeclarations '}'  {$$ = create_normal_classDeclaration_node(false, $2, $3, $4, $6);}
        | ABSTRACT CLASS IDENTIFIER superclassOpt interfacesOpt '{' classMemberDeclarations '}'     {$$ = create_normal_classDeclaration_node(true, $3, $4, $5, $7);}
        | CLASS IDENTIFIER '=' typeNotVoid mixins interfacesOpt ';'                                 {$$ = create_alias_classDeclaration_node(false, $2, $4, $5, $6);}
        | ABSTRACT CLASS IDENTIFIER '=' typeNotVoid mixins interfacesOpt ';'                        {$$ = create_alias_classDeclaration_node(true, $3, $5, $6, $7);}
    ;

    staticFinalDeclaration: identifier '=' expr                     {$$ = create_assign_idInit_node($1, $3);}
    ;

    staticFinalDeclarationList: staticFinalDeclaration              {$$ = $1;}
        | staticFinalDeclarationList ',' staticFinalDeclaration     {$$ = idInitList_add($1, $3);}
    ;

    classMemberDeclaration: declaration ';'                     {$$ = $1;}
        | methodSignature functionBody                          {$$ = create_methodDefinition_classMemberDeclaration_node($1, $2);}
    ;

    classMemberDeclarations: %empty                             {$$ = NULL;}
        | classMemberDeclarations classMemberDeclaration        {$$ = classMemberDeclarationList_add($1, $2);}
    ;

    //Дописать
    declaration: STATIC CONST type staticFinalDeclarationList       {$$ = create_field_classMemberDeclaration_node(true, false, false, true, $3, $4);}
        | STATIC CONST staticFinalDeclarationList                   {$$ = create_field_classMemberDeclaration_node(true, false, false, true, NULL, $3);}
        | STATIC FINAL type staticFinalDeclarationList              {$$ = create_field_classMemberDeclaration_node(true, false, true, false, $3, $4);}
        | STATIC FINAL staticFinalDeclarationList                   {$$ = create_field_classMemberDeclaration_node(true, false, true, false, NULL, $3);}
        | STATIC LATE FINAL type initializedIdentifierList          {$$ = create_field_classMemberDeclaration_node(true, true, true, false, $4, $5);}
        | STATIC LATE FINAL initializedIdentifierList               {$$ = create_field_classMemberDeclaration_node(true, true, true, false, NULL, $4);}
        | STATIC LATE VAR initializedIdentifierList                 {$$ = create_field_classMemberDeclaration_node(true, true, false, false, NULL, $4);}
        | STATIC LATE type initializedIdentifierList                {$$ = create_field_classMemberDeclaration_node(true, true, false, false, $3, $4);}
        | STATIC VAR initializedIdentifierList                      {$$ = create_field_classMemberDeclaration_node(true, false, false, false, NULL, $3);}
        | STATIC type initializedIdentifierList                     {$$ = create_field_classMemberDeclaration_node(true, false, false, false, $2, $3);}
        | LATE FINAL type initializedIdentifierList                 {$$ = create_field_classMemberDeclaration_node(false, true, true, false, $3, $4);}
        | LATE FINAL initializedIdentifierList                      {$$ = create_field_classMemberDeclaration_node(false, true, true, false, NULL, $3);}
        | FINAL type initializedIdentifierList                      {$$ = create_field_classMemberDeclaration_node(false, false, true, false, $2, $3);}
        | FINAL initializedIdentifierList                           {$$ = create_field_classMemberDeclaration_node(false, false, true, false, NULL, $2);}
        | LATE VAR initializedIdentifierList                        {$$ = create_field_classMemberDeclaration_node(false, true, false, false, NULL, $3);}
        | LATE type initializedIdentifierList                       {$$ = create_field_classMemberDeclaration_node(false, true, false, false, $2, $3);}
        | VAR initializedIdentifierList                             {$$ = create_field_classMemberDeclaration_node(false, false, false, false, NULL, $2);}
        | type initializedIdentifierList                            {$$ = create_field_classMemberDeclaration_node(false, false, false, false, $1, $2);}
        | constantConstructorSignature                              {$$ = create_constructSignature_classMemberDeclaration_node($1);}
        | constantConstructorSignature redirection                  {$$ = create_constructSignature_classMemberDeclaration_node(signature_node_addRedirection($1, $2));}
        | constantConstructorSignature initializers                 {$$ = create_constructSignature_classMemberDeclaration_node(signature_node_addInitializers($1, $2));}
        | namedConstructorSignature                                 {$$ = create_constructSignature_classMemberDeclaration_node($1);}
        | namedConstructorSignature redirection                     {$$ = create_constructSignature_classMemberDeclaration_node(signature_node_addRedirection($1, $2));}
        | namedConstructorSignature initializers                    {$$ = create_constructSignature_classMemberDeclaration_node(signature_node_addInitializers($1, $2));}
        | constructorSignature                                      {$$ = create_constructSignature_classMemberDeclaration_node($1);}
        | constructorSignature redirection                          {$$ = create_constructSignature_classMemberDeclaration_node(signature_node_addRedirection($1, $2));}
        | constructorSignature initializers                         {$$ = create_constructSignature_classMemberDeclaration_node(signature_node_addInitializers($1, $2));}
        | functionSignature                                         {$$ = create_methodSignature_classMemberDeclaration_node($1);}
    ;

    // Дописать
    methodSignature: functionSignature              {$$ = $1;}
        | STATIC functionSignature                  {$$ = signature_node_setStatic($2);}
        | namedConstructorSignature                 {$$ = $1;}
        | namedConstructorSignature initializers    {$$ = signature_node_addInitializers($1, $2);}
        | constructorSignature                      {$$ = $1;}
        | constructorSignature initializers         {$$ = signature_node_addInitializers($1, $2);}
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

    constructorSignature: IDENTIFIER constructorFormalParameters        {$$ = create_construct_signature_node(false, $1, $2);}
        | IDENTIFIER formalParameterList                                {$$ = create_construct_signature_node(false, $1, $2);}
    ;

    namedConstructorSignature: IDENTIFIER '.' identifier formalParameterList            {$$ = create_construct_signature_node(false, $1, $3, $4);}
        | IDENTIFIER '.' identifier constructorFormalParameters                         {$$ = create_construct_signature_node(false, $1, $3, $4);}
    ;

    constantConstructorSignature: CONST IDENTIFIER '.' identifier formalParameterList       {$$ = create_construct_signature_node(true, $2, $4, $5);}
        | CONST IDENTIFIER '.' identifier constructorFormalParameters                       {$$ = create_construct_signature_node(true, $2, $4, $5);}
        | CONST IDENTIFIER formalParameterList                              {$$ = create_construct_signature_node(true, $2, $3);}
        | CONST IDENTIFIER constructorFormalParameters                      {$$ = create_construct_signature_node(true, $2, $3);}
    ;

    // вызвать именованный конструктор или другой конструктор 
    // Пример: Car.withoutABS(this.make, this.model, this.yearMade): this(make, model, yearMade, false);
    redirection: ':' THIS '.' identifier arguments                          {$$ = create_redirection_node($4, $5);}
        | ':' THIS arguments                                                {$$ = create_redirection_node(NULL, $3);}
    ;

    initializerListEntry: SUPER arguments                           {$$ = create_superConstructor_initializer_node($2);}
        | SUPER '.' identifier arguments                            {$$ = create_superNamedConstructor_initializer_node($3, $4);}
        | THIS '.' identifier '=' exprNotAssign                     {$$ = create_thisAssign_initializer_node($3, $5);}
        | identifier '=' exprNotAssign                              {$$ = create_thisAssign_initializer_node($1, $3);}
    ;

    initializers: ':' initializerListEntry                          {$$ = $2;}
        | initializers ',' initializerListEntry                     {$$ = initializerList_add($1, $3);}
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