#include "structures.h"
#include <stdio.h>
#include <stdlib.h>

int id = 0;

int newID() {
    id += 1;
    return id;
}

identifier_node* create_identifier_node(bool isBuiltin, char* stringval) {
    identifier_node* node = (identifier_node*)malloc(sizeof(identifier_node));
    node->id = newID();

    node->isBuiltin = isBuiltin;
    node->stringval = stringval;
    node->next = NULL;

    printf("prs: identifier created \"%s\" (builtin = %d)\n", node-> stringval, node->isBuiltin);

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

expr_node* convert_ambiguous_to_arguments(identifier_node* argsOrParams) {
    if (argsOrParams == NULL) {
        return NULL;
    }

    identifier_node* curId = argsOrParams;
    expr_node* exprList = create_idAccess_expr_node(curId);
    while (curId->next != NULL) {
        curId = curId->next;
        exprList_add(exprList, create_idAccess_expr_node(curId));
    }

    return exprList;
}
formalParameter_node* convert_ambiguous_to_parameters(identifier_node* argsOrParams) {
    if (argsOrParams == NULL) {
        return NULL;
    }

    identifier_node* curId = argsOrParams;
    formalParameter_node* exprList = create_normal_formalParameter_node(curId);
    while (curId->next != NULL) {
        curId = curId->next;
        formalParameterList_add(exprList, create_normal_formalParameter_node(curId));
    }

    return exprList;
}

selector_node* create_brackets_selector_node(expr_node* inBrackets){
    selector_node* node = (selector_node*)malloc(sizeof(selector_node));
    node->id = newID();

    node->type = brackets;
    node->inBrackets = inBrackets;

    printf("prs: brackets selector created \n");

    return node;
}
selector_node* create_access_selector_node(identifier_node* accessList){
    selector_node* node = (selector_node*)malloc(sizeof(selector_node));
    node->id = newID();

    node->type = fieldAccess;
    node->accessList = accessList;

    printf("prs: fieldAccess selector created \n");

    return node;
}
selector_node* create_methodCall_selector_node(identifier_node* accessList, expr_node* callArguments){
    selector_node* node = (selector_node*)malloc(sizeof(selector_node));
    node->id = newID();

    node->type = methodCall;
    node->accessList = accessList;
    node->callArguments = callArguments;

    printf("prs: methodCall selector created \n");

    return node;
}

expr_node* create_this_expr_node(){
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->next = NULL;

    node->type = this_pr;

    return node;
}
expr_node* create_super_expr_node() {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->next = NULL;

    node->type = super_pr;

    return node;
}
expr_node* create_null_expr_node() {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->next = NULL;

    node->type = null_pr;

    return node;
}
expr_node* create_intlit_expr_node(long long value) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->next = NULL;

    node->type = int_pr;
    node->int_value = value;

    return node;
}
expr_node* create_doublelit_expr_node(double value) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->next = NULL;

    node->type = double_pr;
    node->double_value = value;

    return node;
}
expr_node* create_boollit_expr_node(bool value) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->next = NULL;

    node->type = bool_pr;
    node->bool_value = value;

    return node;
}
expr_node* create_strlit_expr_node(char* value) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->next = NULL;

    node->type = string_pr;
    node->string_value = value;

    return node;
}
expr_node* create_strInterpolation_expr_node(expr_node* before, expr_node* interpol, char* after) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->next = NULL;

    node->type = string_interpolation;
    node->operand = before; //TODO �������� ����� ��������� ���
    node->operand2 = interpol;
    node->string_value = after;

    return node;
}
expr_node* create_idAccess_expr_node(identifier_node* accessList) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->next = NULL;

    node->type = idAccess;
    node->accessList = accessList;

    return node;
}
expr_node* create_call_expr_node(identifier_node* accessList, expr_node* callArguments) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->next = NULL;

    node->type = idAccess;
    node->accessList = accessList;
    node->callArguments = callArguments;

    return node;
}
expr_node* create_constructNew_expr_node(identifier_node* accessList, expr_node* callArguments) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->next = NULL;

    node->type = constructNew;
    node->accessList = accessList;
    node->callArguments = callArguments;

    return node;
}
expr_node* create_constructConst_expr_node(identifier_node* accessList, expr_node* callArguments) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->next = NULL;

    node->type = constructConst;
    node->accessList = accessList;
    node->callArguments = callArguments;

    return node;
}
expr_node* create_selector_expr_node(expr_node* operand, selector_node* selector) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->next = NULL;

    node->type = selector_expr;
    node->operand = operand;
    node->selector = selector;

    return node;
}
expr_node* create_operator_expr_node(expr_type type, expr_node* operand, expr_node* operand2) {
    expr_node* node = (expr_node*)malloc(sizeof(expr_node));
    node->id = newID();
    node->next = NULL;

    node->type = type; //TODO ��������� ��� ����� ��������, � ���� assign �� ��������� ������ ������� �� ������������ 
    node->operand = operand;
    node->operand2 = operand2;

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

    node->body = body;
    node->condition = condition;
    node->type = while_statement;

    return node;
}
stmt_node* create_do_stmt_node(stmt_node* body, expr_node* condition){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();

    node->body = body;
    node->condition = condition;
    node->type = do_statement;

    return node;
}
stmt_node* create_break_stmt_node(){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();

    node->body = NULL;
    node->condition = NULL;
    node->type = break_statement;

    return node;
}
stmt_node* create_continue_stmt_node(){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();

    node->body = NULL;
    node->condition = NULL;
    node->type = continue_statement;

    return node;
}
stmt_node* create_return_stmt_node(expr_node* returnExpr){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();

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

    node->body = body;
    node->condition = condition;
    node->elseBody = elseBody;
    node->returnExpr = NULL;
    node->type = if_statement;

    return node;
}
stmt_node* create_variable_declaration_stmt_node(variableDeclaration_node* variableDeclaration){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->type = variable_declaration_statement;
    node->variableDeclaration = variableDeclaration;

    return node;
}
stmt_node* create_expr_stmt_node(expr_node* expr){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->type = expr_statement;
    node->expr = expr;

    return node;
}
stmt_node* create_forN_stmt_node(stmt_node* forInitializerStmt, stmt_node* condition, expr_node* forPostExpr, stmt_node* body) {
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();

    node->type = forN_statement;
    node->forInitializerStmt = forInitializerStmt;
    node->condition = condition->expr;
    node->forPostExpr = forPostExpr;
    node->body = body;

    return node;
}
stmt_node* create_forEach_stmt_node(declaredIdentifier_node* declaredIdentifier, expr_node* expr, stmt_node* body) {
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();

    node->type = forEach_statement;
    node->forEachDeclarator = declaredIdentifier->declarator;
    node->forEachIdentifier = declaredIdentifier->identifier;
    node->forContainerExpr = expr;
    node->body = body;

    free(declaredIdentifier); //look

    return node;
}
stmt_node* create_forEach_stmt_node(identifier_node* identifier, expr_node* expr, stmt_node* body) {
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();

    node->type = forEach_statement;
    node->forEachDeclarator = NULL;
    node->forEachIdentifier = identifier;
    node->forContainerExpr = expr;
    node->body = body;

    return node;
}
stmt_node* create_functionDefinition_stmt_node(struct functionDefinition_node* func) {
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();

    node->type = local_function_declaration;
    node->func = func;

    return node;
}
stmt_node* stmtList_add(stmt_node* start, stmt_node* added){
    added->nextStmt = NULL;     //look нужно решить че с блоками делать
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

stmt_node* create_switch_case_stmt_node(expr_node* condition, switch_case_node* switchCaseList, stmt_node* defaultSwitchActions){
    stmt_node* node = (stmt_node*)malloc(sizeof(stmt_node));
    node->id = newID();
    node->type = switch_statement;
    node->switchCaseList = switchCaseList;
    node->condition = condition;
    node->defaultSwitchActions = defaultSwitchActions;

    return node;
}
type_node* create_named_type_node(identifier_node* name, bool isNullable) {
    type_node* node = (type_node*)malloc(sizeof(type_node));
    node->id = newID();
    node->next = NULL;

    node->type = named;
    node->name = name;
    node->isNullable = isNullable;

    return node;
}
type_node* create_dynamic_type_node(bool isNullable) {
    type_node* node = (type_node*)malloc(sizeof(type_node));
    node->id = newID();
    node->next = NULL;

    node->type = dynamic;
    node->isNullable = isNullable;

    return node;
}
type_node* create_void_type_node() {
    type_node* node = (type_node*)malloc(sizeof(type_node));
    node->id = newID();
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
    declarator_node* node = (declarator_node*)malloc(sizeof(declarator_node));
    node->id = newID();

    node->isLate = isLate;
    node->isFinal = isFinal;
    node->isConst = isConst;
    node->isTyped = valueType != NULL;
    node->valueType = valueType;

    return node;
}

declaredIdentifier_node* create_declaredIdentifier_node(declarator_node* declarator, identifier_node* identifier) {
    declaredIdentifier_node* node = (declaredIdentifier_node*)malloc(sizeof(declaredIdentifier_node));
    node->id = newID();

    node->declarator = declarator;
    node->identifier = identifier;

    return node;
}
declaredIdentifier_node* create_declaredIdentifier_node(bool isLate, bool isFinal, bool isConst, type_node* valueType, identifier_node* identifier) {
    return create_declaredIdentifier_node(create_declarator_node(isLate, isFinal, isConst, valueType), identifier);
}

idInit_node* create_id_idInit_node(identifier_node* identifier) {
    idInit_node* node = (idInit_node*)malloc(sizeof(idInit_node));
    node->id = newID();
    node->next = NULL;

    node->isAssign = false;
    node->identifier = identifier;

    return node;
}
idInit_node* create_assign_idInit_node(identifier_node* identifier, expr_node* value) {
    idInit_node* node = (idInit_node*)malloc(sizeof(idInit_node));
    node->id = newID();
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

variableDeclaration_node* create_nonAssign_variableDeclaration_node(declaredIdentifier_node* declaredIdentifier) {
    variableDeclaration_node* node = (variableDeclaration_node*)malloc(sizeof(variableDeclaration_node));
    node->id = newID();
    node->idInitList = NULL;

    node->isAssign = false;
    node->declaredIdentifier = declaredIdentifier;

    return node;
}
variableDeclaration_node* create_assign_variableDeclaration_node(declaredIdentifier_node* declaredIdentifier, expr_node* value) {
    variableDeclaration_node* node = (variableDeclaration_node*)malloc(sizeof(variableDeclaration_node));
    node->id = newID();
    node->idInitList = NULL;

    node->isAssign = true;
    node->declaredIdentifier = declaredIdentifier;
    node->value = value;

    return node;
}
variableDeclaration_node* variableDeclaration_idInitList_add(variableDeclaration_node* declaration, idInit_node* added) {
    if (declaration->idInitList == NULL) {
        declaration->idInitList = added;
    }
    else {
        declaration->idInitList = idInitList_add(declaration->idInitList, added);
    }
    return declaration;
}

formalParameter_node* create_normal_formalParameter_node(declaredIdentifier_node* declaredIdentifier) {
    formalParameter_node* node = (formalParameter_node*)malloc(sizeof(formalParameter_node));
    node->id = newID();
    node->next = NULL;

    node->isField = false;
    node->isDeclared = true;
    node->declarator = declaredIdentifier->declarator;
    node->identifier = declaredIdentifier->identifier;

    free(declaredIdentifier); //todo не уверен здесь

    return node;
}
formalParameter_node* create_normal_formalParameter_node(identifier_node* identifier) {
    formalParameter_node* node = (formalParameter_node*)malloc(sizeof(formalParameter_node));
    node->id = newID();
    node->next = NULL;

    node->isField = false;
    node->isDeclared = false;
    node->declarator = NULL;
    node->identifier = identifier;

    return node;
}
formalParameter_node* create_field_formalParameter_node(declarator_node* declarator, identifier_node* identifier) {
    formalParameter_node* node = (formalParameter_node*)malloc(sizeof(formalParameter_node));
    node->id = newID();
    node->next = NULL;

    node->isField = true;
    node->isDeclared = declarator != NULL;
    node->declarator = declarator;
    node->identifier = identifier;

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
    node->next = NULL;

    node->type = superConstructor;
    node->args = args;

    return node;
}
initializer_node* create_superNamedConstructor_initializer_node(identifier_node* superConstructorName, expr_node* args) {
    initializer_node* node = (initializer_node*)malloc(sizeof(initializer_node));
    node->id = newID();
    node->next = NULL;

    node->type = superNamedConstructor;
    node->superConstructorName = superConstructorName;
    node->args = args;

    return node;
}
initializer_node* create_thisAssign_initializer_node(identifier_node* thisFieldId, expr_node* value) {
    initializer_node* node = (initializer_node*)malloc(sizeof(initializer_node));
    node->id = newID();
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

    node->isNamed = name != NULL;
    node->name = name;
    node->args = args;

    return node;
}

signature_node* create_funcOrConstruct_signature_node(type_node* returnType, identifier_node* name, formalParameter_node* parameters) {
    signature_node* node = (signature_node*)malloc(sizeof(signature_node));
    node->id = newID();

    node->type = funcOrConstruct;
    node->isStatic = false;
    node->returnType = returnType;
    node->name = name;
    node->parameters = parameters;

    return node;
}
signature_node* create_construct_signature_node(bool isConst, identifier_node* name, formalParameter_node* parameters) {
    if (name->next != NULL && name->next->next != NULL) {
        throw -1;   // имя конструктора может иметь только тип Car.name - дот-лист из двух элементов
    }

    signature_node* node = (signature_node*)malloc(sizeof(signature_node));
    node->id = newID();

    node->type = construct;
    node->isStatic = false;
    node->isConst = isConst;
    node->isNamed = name->next != NULL;
    node->name = name;
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

    node->signature = signature;
    node->body = body;

    return node;
}

switch_case_node* create_switch_case_node(stmt_node* actions, expr_node* condition){
    switch_case_node* node = (switch_case_node*)malloc(sizeof(switch_case_node));

    node->id = newID();
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

    node->name = name;
    node->values = values;

    return node;
}
