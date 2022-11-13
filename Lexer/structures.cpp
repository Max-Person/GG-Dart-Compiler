#include "parser.tab.h"
#include "structures.h"
#include <stdio.h>
#include <stdlib.h>

int id = 0;

int newID() {
    id += 1;
    return id;
}

extern void yyerror(char const* s);
extern int yylineno;

identifier_node* create_identifier_node(bool isBuiltin, char* stringval) {
    identifier_node* node = (identifier_node*)malloc(sizeof(identifier_node));
    node->id = newID();
    node->line = yylineno;  //Потому что индентификаторы создаются в лексере
    node->next = NULL;

    node->isBuiltin = isBuiltin;
    node->stringval = stringval;
    node->next = NULL;

    return node;
}
identifier_node* identifierLists_add(identifier_node* start, identifier_node* added) {
    identifier_node* cur = start;
    while (cur->next != NULL) {
        cur = cur->next;
    }
    added->next = NULL;
    cur->next = added;

    return start;
}
identifier_node* identifierLists_getLast(identifier_node* start){
    identifier_node* cur = start;
    while (cur->next != NULL) {
        cur = cur->next;
    }
    return cur;
}
identifier_node* identifierLists_takeLast(identifier_node* start){
    if(start->next == NULL){
        printf("prs: ERR expected at least two identifiers in a dot-list\n");
        throw -1;
    }

    identifier_node* cur = start;
    identifier_node* next = cur->next;
    while (next->next != NULL) {
        cur = cur->next;
        next = next->next;
    }
    cur->next = NULL;
    return next;
}
bool isAssignable(expr_node* expr) {
    if (expr->type != identifier && expr->type != fieldAccess && expr->type != brackets) {
        return false;
    }
    expr_node* cur = expr;
    while (cur != NULL) {
        if (cur->type != identifier &&
            cur->type != this_pr &&
            cur->type != super_pr &&
            cur->type != call &&
            cur->type != fieldAccess &&
            cur->type != methodCall &&
            cur->type != brackets) {
            return false;
        }
        if ((cur->type == this_pr || cur->type == super_pr) && cur == expr) {
            return false;
        }
        if (cur->type == identifier ||
            cur->type == this_pr ||
            cur->type == super_pr ||
            cur->type == call) {
            return true;
        }
        cur = cur->operand;
    }
    return true;
}
bool isTypeInferrable(variableDeclaration_node* variableDeclaration) {
    if (!variableDeclaration->declarator->isTyped) {
        idInit_node* cur = variableDeclaration->idInitList;
        while (cur != NULL) {
            if (!cur->isAssign) {
                return false;
                throw - 1;
            }
            cur = cur->next;
        }
    }
    return true;
}

expr_node* create_this_expr_node(){
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = this_pr;

    return node;
}
expr_node* create_super_expr_node() {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = super_pr;

    return node;
}
expr_node* create_null_expr_node() {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = null_pr;

    return node;
}
expr_node* create_intlit_expr_node(long long value) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = int_pr;
    node->int_value = value;

    return node;
}
expr_node* create_doublelit_expr_node(double value) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = double_pr;
    node->double_value = value;

    return node;
}
expr_node* create_boollit_expr_node(bool value) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = bool_pr;
    node->bool_value = value;

    return node;
}
expr_node* create_strlit_expr_node(char* value) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = string_pr;
    node->string_value = value;

    return node;
}
expr_node* create_listlit_expr_node(expr_node* list) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = list_pr;
    node->operand = list;

    return node;
}
expr_node* create_strInterpolation_expr_node(expr_node* before, expr_node* interpol, char* after) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = string_interpolation;
    node->operand = before;
    if (node->operand->type != string_interpolation && node->operand->type != string_pr)
        yyerror("Compiler err: using string_interpolation node on a non-string expr");
    node->operand2 = interpol;
    node->string_value = after;

    return node;
}
expr_node* create_id_expr_node(identifier_node* identifierAccess) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = identifier;
    node->identifierAccess = identifierAccess;

    return node;
}
expr_node* create_call_expr_node(identifier_node* identifierAccess, expr_node* callArguments) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = call;
    node->identifierAccess = identifierAccess;
    node->callArguments = callArguments;

    return node;
}
expr_node* create_constructNew_expr_node(identifier_node* className, expr_node* callArguments) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = constructNew;
    node->identifierAccess = className;
    node->callArguments = callArguments;

    return node;
}
expr_node* create_constructNew_expr_node(identifier_node* className, identifier_node* constructname, expr_node* callArguments) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = constructNew;
    node->identifierAccess = className;
    node->constructName = constructname;
    node->callArguments = callArguments;

    return node;
}
expr_node* create_constructConst_expr_node(identifier_node* className, expr_node* callArguments) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = constructConst;
    node->identifierAccess = className;
    node->callArguments = callArguments;

    return node;
}
expr_node* create_constructConst_expr_node(identifier_node* className, identifier_node* constructname, expr_node* callArguments) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = constructConst;
    node->identifierAccess = className;
    node->constructName = constructname;
    node->callArguments = callArguments;

    return node;
}
expr_node* create_fieldAccess_expr_node(expr_node* op, identifier_node* field) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = fieldAccess;
    node->operand = op;
    node->identifierAccess = field;

    return node;
}
expr_node* create_methodCall_expr_node(expr_node* op, identifier_node* method, expr_node* callArguments) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = methodCall;
    node->operand = op;
    node->identifierAccess = method;
    node->callArguments = callArguments;

    return node;
}
expr_node* create_operator_expr_node(expr_type type, expr_node* operand, expr_node* operand2) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    if (type < brackets) yyerror("Compiler err: using operator node on a non-operator expr");
    if (type >= assign && !isAssignable(operand)) yyerror("Unassignable expr used in assignment");
    node->type = type;
    node->operand = operand;
    node->operand2 = operand2;

    return node;
}
expr_node* create_typeOp_expr_node(expr_type type, expr_node* operand, type_node* typeOp) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    if (type != type_cast && type != type_check && type != neg_type_check)
        yyerror("Compiler err: using typeOp node on an incompatible expr");
    node->type = type;
    node->operand = operand;
    node->typeForCheckOrCast = typeOp;

    return node;
}
expr_node* exprList_add(expr_node* start, expr_node* added){
    expr_node* cur = start;
    while (cur->next != NULL) {
        cur = cur->next;
    }
    added->next = NULL;
    cur->next = added;

    return start;
}

stmt_node* create_while_stmt_node(expr_node* condition, stmt_node* body){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->line = loc.first_line;
    node->nextStmt = NULL;

    node->body = body;
    node->condition = condition;
    node->type = while_statement;

    return node;
}
stmt_node* create_do_stmt_node(stmt_node* body, expr_node* condition){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->line = loc.first_line;
    node->nextStmt = NULL;

    node->body = body;
    node->condition = condition;
    node->type = do_statement;

    return node;
}
stmt_node* create_break_stmt_node(){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->line = loc.first_line;
    node->nextStmt = NULL;

    node->body = NULL;
    node->condition = NULL;
    node->type = break_statement;

    return node;
}
stmt_node* create_continue_stmt_node(){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->line = loc.first_line;
    node->nextStmt = NULL;

    node->body = NULL;
    node->condition = NULL;
    node->type = continue_statement;

    return node;
}
stmt_node* create_return_stmt_node(expr_node* returnExpr){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->line = loc.first_line;
    node->nextStmt = NULL;

    node->body = NULL;
    node->condition = NULL;
    node->elseBody = NULL;
    node->returnExpr = returnExpr;
    node->type = return_statement;

    return node;
}
stmt_node* create_if_stmt_node(expr_node* condition, stmt_node* body, stmt_node* elseBody){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->line = loc.first_line;
    node->nextStmt = NULL;

    node->body = body;
    node->condition = condition;
    node->elseBody = elseBody;
    node->returnExpr = NULL;
    node->type = if_statement;

    return node;
}
stmt_node* create_switch_case_stmt_node(expr_node* condition, switch_case_node* switchCaseList, stmt_node* defaultSwitchActions) {
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->line = loc.first_line;
    node->nextStmt = NULL;

    node->type = switch_statement;
    node->switchCaseList = switchCaseList;
    node->condition = condition;
    node->defaultSwitchActions = defaultSwitchActions;

    return node;
}
stmt_node* create_variable_declaration_stmt_node(variableDeclaration_node* variableDeclaration){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->line = loc.first_line;
    node->nextStmt = NULL;

    node->type = variable_declaration_statement;
    if(!isTypeInferrable(variableDeclaration)) yyerror("types can only be inferred if all declared variables are initialized");
    node->variableDeclaration = variableDeclaration;

    return node;
}
stmt_node* create_expr_stmt_node(expr_node* expr){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->line = loc.first_line;
    node->nextStmt = NULL;

    node->type = expr_statement;
    node->expr = expr;

    return node;
}
stmt_node* create_forN_stmt_node(stmt_node* forInitializerStmt, stmt_node* condition, expr_node* forPostExpr, stmt_node* body) {
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->line = loc.first_line;
    node->nextStmt = NULL;

    node->type = forN_statement;
    node->forInitializerStmt = forInitializerStmt;
    node->condition = condition->expr;
    node->forPostExpr = forPostExpr;
    node->body = body;

    return node;
}
stmt_node* create_forEach_stmt_node(variableDeclaration_node* declaredIdentifier, expr_node* expr, stmt_node* body) {
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->line = loc.first_line;
    node->nextStmt = NULL;

    node->type = forEach_statement;
    node->variableDeclaration = declaredIdentifier;
    node->forEachVariableId = NULL;
    node->forContainerExpr = expr;
    node->body = body;

    return node;
}
stmt_node* create_forEach_stmt_node(identifier_node* identifier, expr_node* expr, stmt_node* body) {
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->line = loc.first_line;
    node->nextStmt = NULL;

    node->type = forEach_statement;
    node->variableDeclaration = NULL;
    node->forEachVariableId = identifier;
    node->forContainerExpr = expr;
    node->body = body;

    return node;
}
stmt_node* create_functionDefinition_stmt_node(struct functionDefinition_node* func) {
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->line = loc.first_line;
    node->nextStmt = NULL;

    node->type = local_function_declaration;
    node->func = func;

    return node;
}
stmt_node* create_block_stmt_node(stmt_node* inner) {
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->line = loc.first_line;
    node->nextStmt = NULL;

    node->type = block;
    node->body = inner;

    return node;
}
stmt_node* stmtList_add(stmt_node* start, stmt_node* added){
    added->nextStmt = NULL;
    if (start == NULL) {
        return added;
    }

    stmt_node* cur = start;
    while (cur->nextStmt != NULL) {
        cur = cur->nextStmt;
    }
    cur->nextStmt = added;

    return start;
}

type_node* create_named_type_node(identifier_node* name, bool isNullable) {
    type_node* node = (type_node*)malloc(sizeof(type_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = _named;
    node->name = name;
    node->isNullable = isNullable;

    return node;
}
type_node* create_list_type_node(type_node* listValueType, bool isNullable) {
    type_node* node = (type_node*)malloc(sizeof(type_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = _list;
    node->listValueType = listValueType;
    node->isNullable = isNullable;

    return node;
}
type_node* create_void_type_node() {
    type_node* node = (type_node*)malloc(sizeof(type_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = _void;

    return node;
}
type_node* type_node_makeNullable(type_node* node, bool isNullable) {
    if (node->type != _void) {
        node->isNullable = isNullable;
    }
    return node;
}
type_node* typeList_add(type_node* start, type_node* added) {
    type_node* cur = start;
    while (cur->next != NULL) {
        cur = cur->next;
    }
    added->next = NULL;
    cur->next = added;

    return start;
}

declarator_node* create_declarator_node(bool isLate, bool isFinal, bool isConst, type_node* valueType) {
    return create_declarator_node(false, isLate, isFinal, isConst, valueType);
}
declarator_node* create_declarator_node(bool isStatic, bool isLate, bool isFinal, bool isConst, type_node* valueType) {
    declarator_node* node = (declarator_node*)malloc(sizeof(declarator_node));
    node->id = newID();
    node->line = loc.first_line;

    node->isStatic = isStatic;
    node->isLate = isLate;
    node->isFinal = isFinal;
    node->isConst = isConst;
    node->isTyped = valueType != NULL;
    node->valueType = valueType;

    return node;
}

idInit_node* create_id_idInit_node(identifier_node* identifier) {
    idInit_node* node = (idInit_node*)malloc(sizeof(idInit_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->isAssign = false;
    node->identifier = identifier;

    return node;
}
idInit_node* create_assign_idInit_node(identifier_node* identifier, expr_node* value) {
    idInit_node* node = (idInit_node*)malloc(sizeof(idInit_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->isAssign = true;
    node->identifier = identifier;
    node->value = value;

    return node;
}
idInit_node* idInitList_add(idInit_node* start, idInit_node* added) {
    idInit_node* cur = start;
    while (cur->next != NULL) {
        cur = cur->next;
    }
    added->next = NULL;
    cur->next = added;

    return start;
}

variableDeclaration_node* create_variableDeclaration_node(declarator_node* declarator, idInit_node* identifiers) {
    variableDeclaration_node* node = (variableDeclaration_node*)malloc(sizeof(variableDeclaration_node));
    node->id = newID();
    node->line = loc.first_line;

    node->declarator = declarator;
    node->idInitList = identifiers;
    

    return node;
}
variableDeclaration_node* create_variableDeclaration_node(bool isLate, bool isFinal, bool isConst, type_node* valueType, idInit_node* identifiers) {
    return create_variableDeclaration_node(create_declarator_node(isLate, isFinal, isConst, valueType), identifiers);
}
variableDeclaration_node* create_single_variableDeclaration_node(bool isLate, bool isFinal, bool isConst, type_node* valueType, identifier_node* identifier) {
    return create_variableDeclaration_node(create_declarator_node(isLate, isFinal, isConst, valueType), create_id_idInit_node(identifier));
}
variableDeclaration_node* create_variableDeclaration_node(bool isStatic, bool isLate, bool isFinal, bool isConst, type_node* valueType, idInit_node* identifiers) {
    return create_variableDeclaration_node(create_declarator_node(isStatic, isLate, isFinal, isConst, valueType), identifiers);
}

formalParameter_node* create_normal_formalParameter_node(variableDeclaration_node* declaredIdentifier) {
    formalParameter_node* node = (formalParameter_node*)malloc(sizeof(formalParameter_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->isField = false;
    if (declaredIdentifier->declarator->isStatic) yyerror("Can't have modifier \"static\" here");
    if (declaredIdentifier->declarator->isConst) yyerror("Can't have modifier \"const\" here");
    if (declaredIdentifier->declarator->isLate) yyerror("Can't have modifier \"late\" here");
    if (!declaredIdentifier->declarator->isTyped) yyerror("Untyped function parameter");
    node->paramDecl = declaredIdentifier;

    return node;
}
formalParameter_node* create_field_formalParameter_node(declarator_node* declarator, identifier_node* identifier) {
    formalParameter_node* node = (formalParameter_node*)malloc(sizeof(formalParameter_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->isField = true;
    node->initializedField = identifier;
    if (declarator != NULL) {
        if (declarator->isStatic) yyerror("Can't have modifier \"static\" here");
        if (declarator->isConst) yyerror("Can't have modifier \"const\" here");
        if (declarator->isLate) yyerror("Can't have modifier \"late\" here");

        free(declarator);   //look
    }

    return node;
}
formalParameter_node* formalParameterList_add(formalParameter_node* start, formalParameter_node* added) {
    formalParameter_node* cur = start;
    while (cur->next != NULL) {
        cur = cur->next;
    }
    added->next = NULL;
    cur->next = added;

    return start;
}

initializer_node* create_superConstructor_initializer_node(expr_node* args) {
    initializer_node* node = (initializer_node*)malloc(sizeof(initializer_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = superConstructor;
    node->args = args;

    return node;
}
initializer_node* create_superNamedConstructor_initializer_node(identifier_node* superConstructorName, expr_node* args) {
    initializer_node* node = (initializer_node*)malloc(sizeof(initializer_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = superNamedConstructor;
    node->superConstructorName = superConstructorName;
    node->args = args;

    return node;
}
initializer_node* create_thisAssign_initializer_node(identifier_node* thisFieldId, expr_node* value) {
    initializer_node* node = (initializer_node*)malloc(sizeof(initializer_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = thisAssign;
    node->thisFieldId = thisFieldId;
    node->value = value;

    return node;
}
initializer_node* initializerList_add(initializer_node* start, initializer_node* added) {
    initializer_node* cur = start;
    while (cur->next != NULL) {
        cur = cur->next;
    }
    added->next = NULL;
    cur->next = added;

    return start;
}

redirection_node* create_redirection_node(identifier_node* name, expr_node* args) {
    redirection_node* node = (redirection_node*)malloc(sizeof(redirection_node));
    node->id = newID();
    node->line = loc.first_line;

    node->isNamed = name != NULL;
    node->name = name;
    node->args = args;

    return node;
}

signature_node* create_funcOrConstruct_signature_node(type_node* returnType, identifier_node* name, formalParameter_node* parameters) {
    signature_node* node = (signature_node*)malloc(sizeof(signature_node));
    node->id = newID();
    node->line = loc.first_line;

    node->type = funcOrConstruct;
    node->isStatic = false;
    node->returnType = returnType;
    node->name = name;
    node->parameters = parameters;  

    return node;
}
signature_node* create_construct_signature_node(bool isConst, identifier_node* className, formalParameter_node* parameters) {
    signature_node* node = (signature_node*)malloc(sizeof(signature_node));
    node->id = newID();
    node->line = loc.first_line;

    node->type = construct;
    node->isStatic = false;
    node->isConst = isConst;
    node->isNamed = false;
    node->name = className;
    node->parameters = parameters;

    node->initializers = NULL;
    node->redirection = NULL;

    return node;
}
signature_node* create_construct_signature_node(bool isConst, identifier_node* className, identifier_node* name, formalParameter_node* parameters) {
    signature_node* node = (signature_node*)malloc(sizeof(signature_node));
    node->id = newID();
    node->line = loc.first_line;

    node->type = construct;
    node->isStatic = false;
    node->isConst = isConst;
    node->isNamed = true;
    node->name = className;
    node->constructName = name;
    node->parameters = parameters;

    node->initializers = NULL;
    node->redirection = NULL;

    return node;
}
signature_node* signature_node_setStatic(signature_node* signature) {
    signature->isStatic = true;
    return signature;
}
signature_node* signature_node_addInitializers(signature_node* signature, initializer_node* initializers) {
    signature->initializers = initializers;
    return signature;
}
signature_node* signature_node_addRedirection(signature_node* signature, redirection_node* redirection) {
    signature->redirection = redirection;
    return signature;
}

functionDefinition_node* create_functionDefinition_node(signature_node* signature, stmt_node* body) {
    functionDefinition_node* node = (functionDefinition_node*)malloc(sizeof(functionDefinition_node));
    node->id = newID();
    node->line = loc.first_line;

    node->signature = signature;
    node->body = body;

    return node;
}

switch_case_node* create_switch_case_node(stmt_node* actions, expr_node* condition){
    switch_case_node* node = (switch_case_node*)malloc(sizeof(switch_case_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->actions = actions;
    node->condition = condition;

    return node;
}
switch_case_node* switchCaseList_add(switch_case_node* start, switch_case_node* added){
    switch_case_node* cur = start;
    while (cur->next != NULL) {
        cur = cur->next;
    }
    added->next = NULL;
    cur->next = added;

    return start;
}

enum_node* create_enum_node(identifier_node* name, identifier_node* values){
    enum_node* node = (enum_node*)malloc(sizeof(enum_node));
    node->id = newID();
    node->line = loc.first_line;

    node->name = name;
    node->values = values;

    return node;
}

classMemberDeclaration_node* create_field_classMemberDeclaration_node(bool isStatic, bool isLate, bool isFinal, bool isConst, type_node* valueType, idInit_node* idList) {
    classMemberDeclaration_node* node = (classMemberDeclaration_node*)malloc(sizeof(classMemberDeclaration_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = field;
    node->fieldDecl = create_variableDeclaration_node(create_declarator_node(isStatic, isLate, isFinal, isConst, valueType), idList);
    if (!isTypeInferrable(node->fieldDecl)) yyerror("types can only be inferred if all declared variables are initialized");

    return node;
}
classMemberDeclaration_node* create_constructSignature_classMemberDeclaration_node(signature_node* signature) {
    classMemberDeclaration_node* node = (classMemberDeclaration_node*)malloc(sizeof(classMemberDeclaration_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = constructSignature;
    node->signature = signature;

    return node;
}
classMemberDeclaration_node* create_methodSignature_classMemberDeclaration_node(signature_node* signature) {
    classMemberDeclaration_node* node = (classMemberDeclaration_node*)malloc(sizeof(classMemberDeclaration_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = methodSignature;
    node->signature = signature;

    return node;
}
classMemberDeclaration_node* create_methodDefinition_classMemberDeclaration_node(signature_node* signature, stmt_node* body) {
    classMemberDeclaration_node* node = (classMemberDeclaration_node*)malloc(sizeof(classMemberDeclaration_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = methodDefinition;
    node->signature = signature;
    node->body = body;

    return node;
}
classMemberDeclaration_node* classMemberDeclarationList_add(classMemberDeclaration_node* start, classMemberDeclaration_node* added) {
    added->next = NULL;
    if (start == NULL) {
        return added;
    }
    classMemberDeclaration_node* cur = start;
    while (cur->next != NULL) {
        cur = cur->next;
    }
    cur->next = added;

    return start;
}

supeclassOpt_node* create_supeclassOpt_node(type_node* superclass, type_node* mixins) {
    supeclassOpt_node* node = (supeclassOpt_node*)malloc(sizeof(supeclassOpt_node));
    node->superclass = superclass;
    node->mixins = mixins;
    return node;
}
classDeclaration_node* create_normal_classDeclaration_node(bool isAbstract, identifier_node* name, supeclassOpt_node* superOpt, type_node* interfaces, classMemberDeclaration_node* members) {
    classDeclaration_node* node = (classDeclaration_node*)malloc(sizeof(classDeclaration_node));
    node->id = newID();
    node->line = loc.first_line;

    node->isAlias = false;
    node->isAbstract = isAbstract;
    node->name = name;
    node->super = superOpt->superclass;
    node->mixins = superOpt->mixins;
    free(superOpt);     //look
    node->interfaces = interfaces;
    node->classMembers = members;

    return node;
}
classDeclaration_node* create_alias_classDeclaration_node(bool isAbstract, identifier_node* name, type_node* super, type_node* mixins, type_node* interfaces) {
    classDeclaration_node* node = (classDeclaration_node*)malloc(sizeof(classDeclaration_node));
    node->id = newID();
    node->line = loc.first_line;

    node->isAlias = true;
    node->isAbstract = isAbstract;
    node->name = name;
    node->super = super;
    node->mixins = mixins;
    node->interfaces = interfaces;

    return node;
}

topLevelDeclaration_node* create_class_topLevelDeclaration_node(classDeclaration_node* classDecl) {
    topLevelDeclaration_node* node = (topLevelDeclaration_node*)malloc(sizeof(topLevelDeclaration_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = _class;
    node->classDecl = classDecl;

    return node;
}
topLevelDeclaration_node* create_func_topLevelDeclaration_node(signature_node* signature, stmt_node* body) {
    topLevelDeclaration_node* node = (topLevelDeclaration_node*)malloc(sizeof(topLevelDeclaration_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = _function;
    node->functionDecl = create_functionDefinition_node(signature, body);

    return node;
}
topLevelDeclaration_node* create_enum_topLevelDeclaration_node(enum_node* enumDecl) {
    topLevelDeclaration_node* node = (topLevelDeclaration_node*)malloc(sizeof(topLevelDeclaration_node));
    node->id = newID();
    node->line = loc.first_line;
    node->next = NULL;

    node->type = _enum;
    node->enumDecl = enumDecl;

    return node;
}
topLevelDeclaration_node* create_var_topLevelDeclaration_node(bool isLate, bool isFinal, bool isConst, type_node* valueType, idInit_node* identifiers) {
    topLevelDeclaration_node* node = (topLevelDeclaration_node*)malloc(sizeof(topLevelDeclaration_node));
    node->id = newID();
    node->line = loc.first_line;

    node->type = _variable;
    node->variableDecl = create_variableDeclaration_node(create_declarator_node(isLate, isFinal, isConst, valueType), identifiers);

    return node;
}
topLevelDeclaration_node* topLevelDeclarationList_add(topLevelDeclaration_node* start, topLevelDeclaration_node* added) {
    added->next = NULL;
    if (start == NULL) {
        return added;
    }

    topLevelDeclaration_node* cur = start;
    while (cur->next != NULL) {
        cur = cur->next;
    }
    cur->next = added;

    return start;
}
