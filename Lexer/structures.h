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
    statement_block
};

struct stmt_node{
    int id;
    
    struct expr_node* condition;
    struct stmt_node* body;

    struct stmt_node* elseBody;

    struct expr_node* returnExpr;

    enum stmt_type type;
};
stmt_node* create_while_node(struct expr_node* condition, struct stmt_node* body);
stmt_node* create_do_node(struct expr_node* condition, struct stmt_node* body);
stmt_node* create_if_node(struct expr_node* condition, struct stmt_node* body);
stmt_node* create_break_node();
stmt_node* create_continue_node();
stmt_node* create_return_node(struct expr_node* returnExpr);