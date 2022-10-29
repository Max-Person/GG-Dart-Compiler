#include "dot.h"
using namespace std;

ofstream out("dotOutput.txt", ofstream::trunc);
bool init = true;

void label(int id, string label) {
	out << id << "[label=\"" << label << "\"]" << endl;
}
void link(int id1, int id2) {
	out << id1 << "->" << id2 << endl;
}
void link(int id1, int id2, string label) {
	out << id1 << "->" << id2 << "[label=\"" << label << "\"]" << endl;
}
void linkList(int id1, int id2) {
	out << "{rank = same;" << id1 << ";" << id2 << ";}" << endl;
	link(id1, id2);
}

void displayInit(topLevelDeclaration_node* root) {
	out << "digraph {" << endl;
	out << "subgraph {" << endl;
	display(root);
	out << "}" << endl;
	out << "}" << endl;
}

void display(topLevelDeclaration_node* node) {
	if (node == NULL) {
		out << "empty" << endl;
		return;
	}

	switch (node->type) {
	case _enum: {
		label(node->id, "enumDecl");
		link(node->id, node->enumDecl->id);
		display(node->enumDecl);
		break;
	}
	case _class: {
		label(node->id, "classDecl");
		link(node->id, node->classDecl->id);
		break;
	}
	case _function: {
		label(node->id, "funcDecl");
		link(node->id, node->functionDecl->id);
		break;
	}
	case _variable: {
		label(node->id, "varDecl");
		link(node->id, node->variableDecl->id);
		break;
	}
	}

	if (node->next != NULL) {
		linkList(node->id, node->next->id);
		display(node->next);
	}
}

void display(enum_node* node) {
	label(node->id, "enum: " + string(node->name->stringval));
	link(node->id, node->values->id, "values");
	display(node->values);
}

void display(identifier_node* node) {
	label(node->id, node->stringval);

	if (node->next != NULL) {
		linkList(node->id, node->next->id);
		display(node->next);
	}
}