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


struct ambiguousArgumentsOrParameterList_node {
    int id;

    struct identifier_node* argsOrParams;
};
ambiguousArgumentsOrParameterList_node* create_ambiguousArgumentsOrParameterList_node(identifier_node* argsOrParams);


struct arguments_node {
    int id;

    struct expr_node* args;
};
arguments_node* create_arguments_node(expr_node* args);
arguments_node* convert_ambiguous_to_arguments(ambiguousArgumentsOrParameterList_node* ambiguous); //TODO


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
    struct arguments_node* callArguments;
};
selector_node* create_brackets_selector_node(expr_node* inBrackets);
selector_node* create_access_selector_node(identifier_node* accessList);
selector_node* create_methodCall_selector_node(identifier_node* accessList, arguments_node* callArguments);


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
    excl,
    tilde,
    prefix_inc,
    prefix_dec,
    postfix_inc,
    postfix_dec,

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
    struct arguments_node* callArguments;

    struct selector_node* selector;

    struct expr_node* operand;
    struct expr_node* operand2;

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
expr_node* create_call_expr_node(identifier_node* accessList, arguments_node* callArguments);
expr_node* create_constructNew_expr_node(identifier_node* accessList, arguments_node* callArguments);
expr_node* create_constructConst_expr_node(identifier_node* accessList, arguments_node* callArguments);
expr_node* create_selector_expr_node(expr_node* operand, selector_node* selector);
expr_node* create_operator_expr_node(expr_type type, expr_node* operand, expr_node* operand2);
expr_node* exprList_add(expr_node* start, expr_node* added);

enum stmt_type {
    expr_statement,
    variable_declaration_statement,
    for_statement,
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
    
    struct expr_node* condition;
    struct stmt_node* body;

    struct stmt_node* elseBody;

    struct expr_node* returnExpr;

    struct variableDeclaration_node* variableDeclaration;

    struct expr_node* expr;

    enum stmt_type type;
};
stmt_node* create_while_stmt_node(expr_node* condition, stmt_node* body);
stmt_node* create_do_stmt_node(stmt_node* body, expr_node* condition);
stmt_node* create_if_stmt_node(expr_node* condition, stmt_node* body, stmt_node* elseBody);
stmt_node* create_break_stmt_node();
stmt_node* create_continue_stmt_node();
stmt_node* create_return_stmt_node(expr_node* returnExpr);
stmt_node* create_variable_declaration_stmt_node(variableDeclaration_node* variableDeclaration);
stmt_node* create_expr_stmt_node(expr_node* expr);

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
type_node* create_dynamic_type_node(bool isNullable);
type_node* create_void_type_node();
type_node* type_node_makeNullable(type_node* node, bool isNullable);
type_node* typeList_add(type_node* start, type_node* added);

struct declarator_node {
    int id;

    bool isLate;
    bool isFinal;
    bool isConst;
    bool isTyped;

    struct type_node* valueType;
};
declarator_node* create_declarator_node(bool isLate, bool isFinal, bool isConst, type_node* valueType);

struct declaredIdentifier_node {
    int id;

    struct declarator_node* declarator;
    struct identifier_node* identifier;
};
declaredIdentifier_node* create_declaredIdentifier_node(declarator_node* declarator, identifier_node* identifier);
declaredIdentifier_node* create_declaredIdentifier_node(bool isLate, bool isFinal, bool isConst, type_node* valueType, identifier_node* identifier);

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

    bool isAssign;
    struct declaredIdentifier_node* declaredIdentifier;
    struct expr_node* value;
    struct idInit_node* idInitList = NULL;
};
variableDeclaration_node* create_nonAssign_variableDeclaration_node(declaredIdentifier_node* declaredIdentifier);
variableDeclaration_node* create_assign_variableDeclaration_node(declaredIdentifier_node* declaredIdentifier, expr_node* value);
variableDeclaration_node* variableDeclaration_idInitList_add(variableDeclaration_node* declaration, idInit_node* added);
