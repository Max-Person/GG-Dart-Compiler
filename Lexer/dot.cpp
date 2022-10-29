#include "dot.h"
using namespace std;

ofstream out("dotOutput.txt", ofstream::trunc);

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
	//out << "{rank = same;" << id1 << ";" << id2 << ";}" << endl;
	link(id1, id2, "next");
}
string idDotListToStr(identifier_node* id) {
	string s = string(id->stringval);
	identifier_node* cur = id;
	while (cur->next != NULL) {
		s += string(cur->next->stringval);
		cur = cur->next;
	}
	return s;
}
string declaratorToStr(declarator_node* decl) {
	string s = "";
	if (decl->isStatic) s += "static ";
	if (decl->isLate) s += "late ";
	if (decl->isFinal) s += "final ";
	if (decl->isConst) s += "const ";
	if (decl->isTyped) s += idDotListToStr(decl->valueType->name);
	else if (s.empty()) s += "var";
	return s;
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
		display(node->classDecl);
		break;
	}
	case _function: {
		label(node->id, "funcDecl");
		link(node->id, node->functionDecl->id);
		display(node->functionDecl);
		break;
	}
	case _variable: {
		label(node->id, "varDecl");
		link(node->id, node->variableDecl->id);
		display(node->variableDecl);
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
void display(classDeclaration_node* node) {
	string l = "";
	if(node->isAbstract) {
		l += "abstract ";
	}
	if (node->isAlias) {
		l += "alias ";
	}
	l += "class: " + string(node->name->stringval);
	label(node->id, l);

	if (node->super != NULL) {
		link(node->id, node->super->id, "extends");
		display(node->super);
	}
	if (node->mixins != NULL) {
		link(node->id, node->mixins->id, "with");
		display(node->mixins);
	}
	if (node->interfaces != NULL) {
		link(node->id, node->interfaces->id, "implements");
		display(node->interfaces);
	}
	if (!node->isAlias) {
		link(node->id, node->classMembers->id, "members");
		//insert display(node->classMembers);
	}
}
void display(type_node* node) {
	if (node->type == _void) {
		label(node->id, "type: void");
		return;
	}
	if (node->type == dynamic) {
		label(node->id, "type: dynamic");
		return;
	}
	
	if (node->isNullable) {
		label(node->id, "type: " + idDotListToStr(node->name) + "?");
	}
	else {
		label(node->id, "type: " + idDotListToStr(node->name));
	}

	if (node->next != NULL) {
		linkList(node->id, node->next->id);
		display(node->next);
	}
}
void display(variableDeclaration_node* node) {
	label(node->id, "variable declaration");
	link(node->id, node->declarator->id, "declaration");
	display(node->declarator);
	link(node->id, node->idInitList->id, "variables");
	display(node->idInitList);
}
void display(declarator_node* node) {
	label(node->id, declaratorToStr(node));
}
void display(idInit_node* node) {
	if (node->isAssign) {
		label(node->id, "init");
		link(node->id, node->identifier->id, "id");
		display(node->identifier);
		link(node->id, node->value->id, "value");
		//insert display(node->value);
	}
	else {
		label(node->id, "not init");
		link(node->id, node->identifier->id);
		display(node->identifier);
	}

	if (node->next != NULL) {
		linkList(node->id, node->next->id);
		display(node->next);
	}
}
void display(functionDefinition_node* node) {
	label(node->id, "function declaration");
	link(node->id, node->signature->id, "signature");
	display(node->signature);
	link(node->id, node->body->id, "body");
	//insert display(node->body);
}
void display(signature_node* node) {
	string s = "";
	if (node->isStatic) s += "static ";
	if (node->isConst) s += "const ";
	if (node->type == construct) {
		if (node->isNamed) s += "named ";
		s += "costructor ";
	}
	s += "signature: " + idDotListToStr(node->name);
	label(node->id, s);
	if (node->type == construct) {
		if (node->isNamed) {
			link(node->id, node->name->id, "constructor name");
			display(node->name);
		}
		if (node->initializers != NULL) {
			link(node->id, node->initializers->id, "initializers");
			//insert display(node->initializers);
		}
		if (node->redirection != NULL) {
			link(node->id, node->redirection->id, "redirection");
			//insert display(node->redirection);
		}
	}
	else {
		link(node->id, node->returnType->id, "return type");
		display(node->returnType);
	}
	
	if (node->parameters != NULL) {
		link(node->id, node->parameters->id, "parameters");
		//insert display(node->parameters);
	}
}