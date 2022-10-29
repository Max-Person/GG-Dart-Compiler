#pragma once
#include <iostream>
#include <format>
#include <fstream>
#include <string>
#include <stdlib.h>
#include "structures.h"

void displayInit(topLevelDeclaration_node* root);
void display(topLevelDeclaration_node* node);
void display(enum_node* node);
void display(identifier_node* node);
void display(classDeclaration_node* node);
void display(type_node* node);
void display(variableDeclaration_node* node);
void display(declarator_node* node);
void display(idInit_node* node);
void display(functionDefinition_node* node);
void display(signature_node* node);