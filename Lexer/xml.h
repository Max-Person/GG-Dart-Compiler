#pragma once
#include <iostream>
#include <format>
#include <fstream>
#include <string>
#include <stdlib.h>
#include <list>
#include "structures.h"
#include "tinyxml2.h"
using namespace tinyxml2;
using namespace std;

namespace xmlOut {
	void displayInit(topLevelDeclaration_node* root);
	list<XMLElement*> display(topLevelDeclaration_node* node);
	list<XMLElement*> display(enum_node* node);
	list<XMLElement*> display(identifier_node* node);
	list<XMLElement*> display(classDeclaration_node* node);
	list<XMLElement*> display(classMemberDeclaration_node* node);
	list<XMLElement*> display(type_node* node);
	list<XMLElement*> display(singleVarDeclaration_node* node);
	list<XMLElement*> display(declarator_node* node);
	list<XMLElement*> display(idInit_node* node);
	list<XMLElement*> display(functionDefinition_node* node);
	list<XMLElement*> display(signature_node* node);
	list<XMLElement*> display(initializer_node* node);
	list<XMLElement*> display(redirection_node* node);
	list<XMLElement*> display(formalParameter_node* node);
	list<XMLElement*> display(expr_node* node);
	list<XMLElement*> display(stmt_node* node);
	list<XMLElement*> display(switch_case_node* node);
}