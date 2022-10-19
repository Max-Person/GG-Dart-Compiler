#include <stdlib.h>

struct identifier_node {
    bool isBuiltin;
    char* stringval;

    identifier_node* next = NULL;
};


identifier_node* create_identifier_node(bool isBuiltin, char* stringval);

enum stmt_type{
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

enum expr_type{
    int_et,
    double_et,
    plus,
    minus, 
    u_minus,

};

struct expr_node{

};

struct stmt_node{
    struct stmt_node* condition;
    struct expr_node* body;
    enum stmt_type type;
};