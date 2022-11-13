#pragma once
#include <iostream>
#include <format>
#include <fstream>
#include <string>
#include <stdlib.h>
#include "structures.h"

namespace dotOut {
	void displayInit(topLevelDeclaration_node* root);
	void display(topLevelDeclaration_node* node);
	void display(enum_node* node);
	void display(identifier_node* node);
	void display(classDeclaration_node* node);
	void display(classMemberDeclaration_node* node);
	void display(type_node* node);
	void display(singleVarDeclaration_node* node);
	void display(declarator_node* node);
	void display(functionDefinition_node* node);
	void display(signature_node* node);
	void display(initializer_node* node);
	void display(redirection_node* node);
	void display(formalParameter_node* node);
	void display(expr_node* node);
	void display(stmt_node* node);
	void display(switch_case_node* node);
}