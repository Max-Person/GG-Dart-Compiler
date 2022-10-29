#include "parser.tab.h"
#include "dot.h"

#include <stdio.h>
#include <stdlib.h>

extern int yylineno;
extern FILE* yyin;

int yyparse();
int yylex();
topLevelDeclaration_node* root;

extern void yyerror(char const* s);

int main(int argc, char** argv) {
    if (argc > 1) {
        yyin = fopen(argv[1], "r");
        yyparse();
        display(root);
        fclose(yyin);
    }
    else {
        yyerror("Not found file");
    }
}