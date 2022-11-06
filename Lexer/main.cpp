#include "parser.tab.h"
#include "dot.h"
#include "xml.h"

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
        if (argc > 2 && strcmp(argv[2], "-d")==0) yydebug = 1;
        int res = yyparse();
        if(res == 0){
            dotOut::displayInit(root);
            xmlOut::displayInit(root);
            printf("Successful\n");
        }
        fclose(yyin);
    }
    else {
        yyerror("Not found file");
    }
}