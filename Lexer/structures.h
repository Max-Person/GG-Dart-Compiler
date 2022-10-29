#include <stdlib.h>


struct identifier_node {
    int id;

    bool isBuiltin;
    char* stringval;

    struct identifier_node* next = NULL;
};
identifier_node* create_identifier_node(bool isBuiltin, char* stringval);
identifier_node* identifierLists_add(identifier_node* start, identifier_node* added);
identifier_node* identifierLists_getLast(identifier_node* start);


struct expr_node* convert_ambiguous_to_arguments(identifier_node* argsOrParams);
struct formalParameter_node* convert_ambiguous_to_parameters(identifier_node* argsOrParams);


enum selector_type{
    brackets,
    fieldAccess,
    methodCall,
};
struct selector_node{
    int id;

    selector_type type;
    struct expr_node* inBrackets;
    struct identifier_node* accessList;
    struct expr_node* callArguments;
};
selector_node* create_brackets_selector_node(expr_node* inBrackets);
selector_node* create_access_selector_node(identifier_node* accessList);
selector_node* create_methodCall_selector_node(identifier_node* accessList, expr_node* callArguments);


enum expr_type{
    this_pr,
    super_pr,
    null_pr,
    int_pr,
    double_pr,
    bool_pr,
    string_pr,

    string_interpolation,

    selector_expr,

    constructNew,
    constructConst,

    idAccess,
    call,
    
    ifnull,
    _or,
    _and,
    eq,
    neq,
    greater,
    less,
    greater_eq,
    less_eq,
    type_cast,
    type_check,
    neg_type_check,
    b_or,
    b_xor,
    b_and,
    add,
    sub,
    mul,
    _div,
    mod,
    truncdiv,
    u_minus,
    _not,
    tilde,
    prefix_inc,
    prefix_dec,
    postfix_inc,
    postfix_dec,
    bang,

    assign,
    and_assign,
    or_assign,
    xor_assign,
    mul_assign,
    div_assign,
    trunc_div_assign,
    mod_assign,
    add_assign,
    sub_assign,
    ifnull_assign,

};
struct expr_node{
    int id;

    expr_type type;

    long long int_value;
    double double_value;
    bool bool_value;
    char* string_value;
    
    struct identifier_node* accessList;
    struct expr_node* callArguments;

    struct selector_node* selector;

    struct expr_node* operand;
    struct expr_node* operand2;
    struct type_node* typeForCheckOrCast;

    struct expr_node* next = NULL;
};
expr_node* create_this_expr_node();
expr_node* create_super_expr_node();
expr_node* create_null_expr_node();
expr_node* create_intlit_expr_node(long long value);
expr_node* create_doublelit_expr_node(double value);
expr_node* create_boollit_expr_node(bool value);
expr_node* create_strlit_expr_node(char* value);
expr_node* create_strInterpolation_expr_node(expr_node* before, expr_node* interpol, char* after);
expr_node* create_idAccess_expr_node(identifier_node* accessList);
expr_node* create_call_expr_node(identifier_node* accessList, expr_node* callArguments);
expr_node* create_constructNew_expr_node(identifier_node* accessList, expr_node* callArguments);
expr_node* create_constructConst_expr_node(identifier_node* accessList, expr_node* callArguments);
expr_node* create_selector_expr_node(expr_node* operand, selector_node* selector);
expr_node* create_operator_expr_node(expr_type type, expr_node* operand, expr_node* operand2);
expr_node* create_typeOp_expr_node(expr_type type, expr_node* operand, type_node* typeOp);
expr_node* exprList_add(expr_node* start, expr_node* added);

enum stmt_type {
    block,
    expr_statement,
    variable_declaration_statement,
    forN_statement,
    forEach_statement,
    while_statement,
    do_statement,
    switch_statement,
    if_statement,
    break_statement,
    continue_statement,
    return_statement,
    local_function_declaration,
    statement_block,
    for_initializer_statement
};

struct stmt_node{
    int id;

    enum stmt_type type;
    
    struct expr_node* condition;    //для if, switch, while � for
    struct stmt_node* body;         //для if, while, for и block

    struct stmt_node* elseBody;

    struct expr_node* returnExpr;

    struct variableDeclaration_node* variableDeclaration;   //для variableDeclarationStatement и forEach

    struct expr_node* expr; // для exprStatement

    struct stmt_node* forInitializerStmt;
    struct expr_node* forPostExpr;
    struct identifier_node* forEachVariableId;
    struct expr_node* forContainerExpr;

    struct functionDefinition_node* func;

    struct switch_case_node* switchCaseList;
    struct stmt_node* defaultSwitchActions;

    struct stmt_node* nextStmt;
};
stmt_node* create_while_stmt_node(expr_node* condition, stmt_node* body);
stmt_node* create_do_stmt_node(stmt_node* body, expr_node* condition);
stmt_node* create_if_stmt_node(expr_node* condition, stmt_node* body, stmt_node* elseBody);
stmt_node* create_break_stmt_node();
stmt_node* create_continue_stmt_node();
stmt_node* create_return_stmt_node(expr_node* returnExpr);
stmt_node* create_variable_declaration_stmt_node(variableDeclaration_node* variableDeclaration);
stmt_node* create_expr_stmt_node(expr_node* expr);
stmt_node* create_forN_stmt_node(stmt_node* forInitializerStmt, stmt_node* exprStmt, expr_node* exprList, stmt_node* body);
stmt_node* create_forEach_stmt_node(struct variableDeclaration_node* declaredIdentifier, struct expr_node* expr, struct stmt_node* body);
stmt_node* create_forEach_stmt_node(struct identifier_node* identifier, struct expr_node* expr, struct stmt_node* body);
stmt_node* create_switch_case_stmt_node(expr_node* condition, switch_case_node* switchCaseList, stmt_node* defaultSwitchActions);
stmt_node* create_functionDefinition_stmt_node(struct functionDefinition_node* func);
stmt_node* create_block_stmt_node(stmt_node* inner);
stmt_node* stmtList_add(stmt_node* start, stmt_node* added);

enum type_type {
    named,
    dynamic,
    _void,
};
struct type_node {
    int id;

    type_type type;
    bool isNullable;

    identifier_node* name;

    type_node* next = NULL;
};
type_node* create_named_type_node(identifier_node* name, bool isNullable);
type_node* create_dynamic_type_node();
type_node* create_void_type_node();
type_node* type_node_makeNullable(type_node* node, bool isNullable);
type_node* typeList_add(type_node* start, type_node* added);

struct declarator_node {
    int id;

    bool isStatic;
    bool isLate;
    bool isFinal;
    bool isConst;
    bool isTyped;

    struct type_node* valueType;
};
declarator_node* create_declarator_node(bool isLate, bool isFinal, bool isConst, type_node* valueType);
declarator_node* create_declarator_node(bool isStatic, bool isLate, bool isFinal, bool isConst, type_node* valueType);

struct idInit_node {
    int id;

    bool isAssign;
    struct identifier_node* identifier;
    struct expr_node* value;

    struct idInit_node* next = NULL;
};
idInit_node* create_id_idInit_node(identifier_node* identifier);
idInit_node* create_assign_idInit_node(identifier_node* identifier, expr_node* value);
idInit_node* idInitList_add(idInit_node* start, idInit_node* added);

struct variableDeclaration_node {
    int id;

    struct declarator_node* declarator;
    struct idInit_node* idInitList;
};
variableDeclaration_node* create_variableDeclaration_node(declarator_node* declarator, idInit_node* identifiers);
variableDeclaration_node* create_variableDeclaration_node(bool isLate, bool isFinal, bool isConst, type_node* valueType, idInit_node* identifiers);
variableDeclaration_node* create_single_variableDeclaration_node(bool isLate, bool isFinal, bool isConst, type_node* valueType, identifier_node* identifier);
variableDeclaration_node* create_variableDeclaration_node(bool isStatic, bool isLate, bool isFinal, bool isConst, type_node* valueType, idInit_node* identifiers);

struct formalParameter_node {
    int id;

    bool isField;

    struct variableDeclaration_node* paramDecl;
    struct identifier_node* initializedField;

    struct formalParameter_node* next = NULL;
};
formalParameter_node* create_normal_formalParameter_node(variableDeclaration_node* declaredIdentifier);
formalParameter_node* create_normal_formalParameter_node(identifier_node* identifier);
formalParameter_node* create_field_formalParameter_node(declarator_node* declarator, identifier_node* identifier);
formalParameter_node* formalParameterList_add(formalParameter_node* start, formalParameter_node* added);

enum initializer_type {
    superConstructor,
    superNamedConstructor,
    thisAssign,
};
struct initializer_node {
    int id;

    initializer_type type;

    identifier_node* superConstructorName;
    expr_node* args;

    identifier_node* thisFieldId;
    expr_node* value;

    initializer_node* next = NULL;
};
initializer_node* create_superConstructor_initializer_node(expr_node* args);
initializer_node* create_superNamedConstructor_initializer_node(identifier_node* superConstructorName, expr_node* args);
initializer_node* create_thisAssign_initializer_node(identifier_node* thisFieldId, expr_node* value);
initializer_node* initializerList_add(initializer_node* start, initializer_node* added);

struct redirection_node {
    int id;

    bool isNamed;
    identifier_node* name;
    expr_node* args;
};
redirection_node* create_redirection_node(identifier_node* name, expr_node* args);

enum signature_type {
    funcOrConstruct,
    construct,
};
struct signature_node {
    int id;

    bool isStatic;
    signature_type type;

    type_node* returnType;
    identifier_node* name;
    formalParameter_node* parameters;

    bool isNamed;
    bool isConst;
    initializer_node* initializers;
    redirection_node* redirection;
};
signature_node* create_funcOrConstruct_signature_node(type_node* returnType, identifier_node* name, formalParameter_node* parameters);
signature_node* create_construct_signature_node(bool isConst, identifier_node* name, formalParameter_node* parameters);
signature_node* signature_node_setStatic(signature_node* signature);
signature_node* signature_node_addInitializers(signature_node* signature, initializer_node* initializers);
signature_node* signature_node_addRedirection(signature_node* signature, redirection_node* redirection);

struct functionDefinition_node {
    int id;

    signature_node* signature;
    stmt_node* body;
};
functionDefinition_node* create_functionDefinition_node(signature_node* signature, stmt_node* body);

struct switch_case_node {
    int id;

    switch_case_node* next;
    stmt_node* actions;
    expr_node* condition;
};
switch_case_node* create_switch_case_node(stmt_node* actions, expr_node* condition);
switch_case_node* switchCaseList_add(switch_case_node* start, switch_case_node* added);

struct enum_node {
    int id;

    identifier_node* name;
    identifier_node* values;
};
enum_node* create_enum_node(identifier_node* name, identifier_node* values);

enum classMemberDeclaration_type {
    field,
    constructSignature,
    methodDefinition,
};
struct classMemberDeclaration_node {
    int id;

    classMemberDeclaration_type type;

    variableDeclaration_node* fieldDecl;

    signature_node* signature;
    stmt_node* body;

    classMemberDeclaration_node* next = NULL;
};
classMemberDeclaration_node* create_field_classMemberDeclaration_node(bool isStatic, bool isLate, bool isFinal, bool isConst, type_node* valueType, idInit_node* idList);
classMemberDeclaration_node* create_constructSignature_classMemberDeclaration_node(signature_node* signature);
classMemberDeclaration_node* create_methodDefinition_classMemberDeclaration_node(signature_node* signature, stmt_node* body);
classMemberDeclaration_node* classMemberDeclarationList_add(classMemberDeclaration_node* start, classMemberDeclaration_node* added);

struct supeclassOpt_node {
    type_node* superclass;
    type_node* mixins;
};
supeclassOpt_node* create_supeclassOpt_node(type_node* superclass, type_node* mixins);
struct classDeclaration_node {
    int id;

    bool isAlias;
    bool isAbstract;
    type_node* super;
    type_node* mixins;
    type_node* interfaces;
    identifier_node* name;

    classMemberDeclaration_node* classMembers;
};
classDeclaration_node* create_normal_classDeclaration_node(bool isAbstract, supeclassOpt_node* superOpt, type_node* interfaces, classMemberDeclaration_node* members, identifier_node* name);
classDeclaration_node* create_alias_classDeclaration_node(bool isAbstract, type_node* super, type_node* mixins, type_node* interfaces, identifier_node* name);

enum topLevelDeclaration_type {
    _class,
    _function,
    _enum,
    _variable,
};
struct topLevelDeclaration_node {
    int id;

    topLevelDeclaration_type type;
    classDeclaration_node* classDecl;
    functionDefinition_node* functionDecl;
    enum_node* enumDecl;
    variableDeclaration_node* variableDecl;

    topLevelDeclaration_node* next = NULL;
};
topLevelDeclaration_node* create_class_topLevelDeclaration_node(classDeclaration_node* classDecl);
topLevelDeclaration_node* create_func_topLevelDeclaration_node(signature_node* signature, stmt_node* body);
topLevelDeclaration_node* create_enum_topLevelDeclaration_node(enum_node* enumDecl);
topLevelDeclaration_node* create_var_topLevelDeclaration_node(bool isLate, bool isFinal, bool isConst, type_node* valueType, idInit_node* identifiers);
topLevelDeclaration_node* topLevelDeclarationList_add(topLevelDeclaration_node* start, topLevelDeclaration_node* added);