#include "dot.h"
using namespace std;

ofstream out("dotOutput.txt");

void display(topLevelDeclaration_node* node) {
	if (node == NULL) {
		out << "empty" << endl;
		return;
	}

	switch (node->type) {
	case _enum: {
		out << node->id << "[label=\"enumDecl\"]" << endl;
		out << node->id << "->" << node->enumDecl->id << endl;
		display(node->enumDecl);
		break;
	}
	case _class: {
		out << node->id << "[label=\"classDecl\"]" << endl;
		out << node->id << "->" << node->classDecl->id << endl;
		break;
	}
	case _function: {
		out << node->id << "[label=\"funcDecl\"]" << endl;
		out << node->id << "->" << node->functionDecl->id << endl;
		break;
	}
	case _variable: {
		out << node->id << "[label=\"varDecl\"]" << endl;
		out << node->id << "->" << node->variableDecl->id << endl;
		break;
	}
	}

	if (node->next != NULL) {
		out << "{rank = same;" << node->id << ";" << node->next->id << ";}" << endl;
		out << node->id << "->" << node->next->id << endl;
		display(node->next);
	}
}

void display(enum_node* node) {
	out << node->id << "[label=\"enum: " << node->name->stringval << "\"]" << endl;
	out << node->id << "->" << node->values->id << "[label=\"values\"]" << endl;
	display(node->values);
}

void display(identifier_node* node) {
	out << node->id << "[label=\"" << node->stringval << "\"]" << endl;

	if (node->next != NULL) {
		out << "{rank = same;" << node->id << ";" << node->next->id << ";}" << endl;
		out << node->id << "->" << node->next->id << endl;
		display(node->next);
	}
}