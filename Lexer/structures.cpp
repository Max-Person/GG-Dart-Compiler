#include "structures.h"
#include <stdio.h>
#include <stdlib.h>

identifier_node* create_identifier_node(bool isBuiltin, char* stringval) {
    identifier_node* node = (identifier_node*)malloc(sizeof(identifier_node));
    node->isBuiltin = isBuiltin;
    node->stringval = stringval;

    printf("prs: identifier created \"%s\" (builtin = %d)\n", node-> stringval, node->isBuiltin);

    return node;
}