#pragma once
#include <iostream>
#include <format>
#include <fstream>
#include <string>
#include <stdlib.h>
#include "structures.h"

void display(topLevelDeclaration_node* node);

void display(enum_node* node);

void display(identifier_node* node);