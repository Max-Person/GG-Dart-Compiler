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

bool flagged(int argc, char** argv, const char* flag) {
    for (int i = 0; i < argc; i++) {
        if (strcmp(argv[i], flag) == 0) return true;
    }
    return false;
}

int main(int argc, char** argv) {
    if (argc > 1) {
        yyin = fopen(argv[1], "r");
        if (flagged(argc, argv, "-d")) yydebug = 1;
        int res = yyparse();
        if(res == 0){
            dotOut::displayInit(root);
            xmlOut::displayInit(root);
            printf("C++ Segment (lexer + parser): SUCCESS!\n");
        }
        else fprintf(stderr, "C++ Segment (lexer + parser) ERR: parse FAILED;\n");
        fclose(yyin);

        if (!flagged(argc, argv, "--no-sem")) {
            //string command = "java -cp \"..\\java semantic analysis\\out\\production\\java semantic analysis\" Main xmlOutput.xml";
            string command = "java -jar \"java semantic analysis.jar\" xmlOutput.xml";
            system(command.c_str());
        }
    }
    else {
        fprintf(stderr, "C++ Segment (lexer + parser) ERR: FILE NOT FOUND;\n");
    }
}
