#include "dot.h"
using namespace std;

ofstream out("dotOutput.txt", ofstream::trunc);

namespace dotOut {
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
		if (node->isAbstract) {
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
		if (!node->isAlias && node->classMembers != NULL) {
			link(node->id, node->classMembers->id, "members");
			display(node->classMembers);
		}
	}
	void display(classMemberDeclaration_node* node) {
		switch (node->type)
		{
		case field: {
			label(node->id, "fieldDecl");
			link(node->id, node->fieldDecl->id);
			display(node->fieldDecl);
			break;
		}
		case methodSignature: {
			label(node->id, "undefinedMethodDeclaration");
			link(node->id, node->signature->id);
			display(node->signature);
			break;
		}
		case constructSignature: {
			label(node->id, "constuctorWithoutBody");
			link(node->id, node->signature->id);
			display(node->signature);
			break;
		}
		case methodDefinition: {
			label(node->id, "methodDefinition");
			link(node->id, node->signature->id, "signature");
			display(node->signature);
			link(node->id, node->body->id, "body");
			display(node->body);
			break;
		}
		}

		if (node->next != NULL) {
			linkList(node->id, node->next->id);
			display(node->next);
		}
	}
	void display(type_node* node) {
		switch (node->type) {
		case _void: {
			label(node->id, "type: void");
			return;
		}
		case _named: {
			if (node->isNullable) {
				label(node->id, "type: " + string(node->name->stringval) + "?");
			}
			else {
				label(node->id, "type: " + string(node->name->stringval));
			}
			break;
		}
		case _list: {
			if (node->isNullable) {
				label(node->id, "type: List<...>?");
			}
			else {
				label(node->id, "type: List<...>");
			}
			link(node->id, node->listValueType->id, "value type");
			display(node->listValueType);
			break;
		}
		}

		if (node->next != NULL) {
			linkList(node->id, node->next->id);
			display(node->next);
		}
	}
	void display(singleVarDeclaration_node* node) {
		label(node->id, "variable declaration");
		link(node->id, node->declarator->id, "declaration");
		display(node->declarator);
		link(node->id, node->identifier->id, "id");
		display(node->identifier);
		if (node->isInitialized) {
			link(node->id, node->value->id, "value");
			display(node->value);
		}

		if (node->next != NULL) {
			linkList(node->id, node->next->id);
			display(node->next);
		}
	}
	void display(declarator_node* node) {
		string s = "";
		if (node->isStatic) s += "static ";
		if (node->isLate) s += "late ";
		if (node->isFinal) s += "final ";
		if (node->isConst) s += "const ";
		label(node->id, s + "decl");

		if (node->isTyped) {
			link(node->id, node->valueType->id, "type");
			display(node->valueType);
		}
	}
	void display(functionDefinition_node* node) {
		label(node->id, "function declaration");
		link(node->id, node->signature->id, "signature");
		display(node->signature);
		link(node->id, node->body->id, "body");
		display(node->body);
	}
	void display(signature_node* node) {
		string s = "";
		if (node->type == construct) {
			if (node->isNamed) s += "named ";
			if (node->isConst) s += "const ";
			s += "costructor ";
		}
		else if (node->isStatic) s += "static ";

		s += "signature";
		label(node->id, s);
		if (node->type == construct) {
			link(node->id, node->name->id, "class name");
			display(node->name);
			if (node->isNamed) {
				link(node->id, node->constructName->id, "constructor name");
				display(node->constructName);
			}
			if (node->initializers != NULL) {
				link(node->id, node->initializers->id, "initializers");
				display(node->initializers);
			}
			if (node->redirection != NULL) {
				link(node->id, node->redirection->id, "redirection");
				display(node->redirection);
			}
		}
		else {
			link(node->id, node->name->id, "name");
			display(node->name);
			link(node->id, node->returnType->id, "return type");
			display(node->returnType);
		}

		if (node->parameters != NULL) {
			link(node->id, node->parameters->id, "parameters");
			display(node->parameters);
		}
	}
	void display(initializer_node* node) {
		if (node->type == thisAssign) {
			label(node->id, "fieldInit");
			link(node->id, node->thisFieldId->id, "field");
			display(node->thisFieldId);
			link(node->id, node->value->id, "value");
			display(node->value);
		}
		else {
			label(node->id, "superConstructCall");
			if (node->type == superNamedConstructor) {
				link(node->id, node->superConstructorName->id, "constuctor name");
				display(node->superConstructorName);
			}
			if (node->args != NULL) {
				link(node->id, node->args->id, "arguments");
				display(node->args);
			}
		}

		if (node->next != NULL) {
			linkList(node->id, node->next->id);
			display(node->next);
		}
	}
	void display(redirection_node* node) {
		label(node->id, "redirection");
		if (node->isNamed) {
			link(node->id, node->name->id, "constuctor name");
			display(node->name);
		}
		if (node->args != NULL) {
			link(node->id, node->args->id, "arguments");
			display(node->args);
		}
	}
	void display(formalParameter_node* node) {
		if (node->isField) {
			label(node->id, "fieldInit param");
			link(node->id, node->initializedField->id, "field");
			display(node->initializedField);
		}
		else {
			label(node->id, "param");
			link(node->id, node->paramDecl->id, "declaration");
			display(node->paramDecl);
		}

		if (node->next != NULL) {
			linkList(node->id, node->next->id);
			display(node->next);
		}
	}
	void display(expr_node* node) {
		switch (node->type) {
		case this_pr: {
			label(node->id, "this");
			break;
		}
		case super_pr: {
			label(node->id, "super");
			break;
		}
		case null_pr: {
			label(node->id, "null");
			break;
		}
		case int_pr: {
			label(node->id, to_string(node->int_value));
			break;
		}
		case double_pr: {
			label(node->id, to_string(node->double_value));
			break;
		}
		case bool_pr: {
			label(node->id, node->bool_value ? "true" : "false");
			break;
		}
		case string_pr: {
			label(node->id, string(node->string_value));
			break;
		}
		case list_pr: {
			label(node->id, "list");
			if (node->operand != NULL) {
				link(node->id, node->operand->id, "values");
				display(node->operand);
			}
			break;
		}

		case string_interpolation: {
			label(node->id, "...${...}" + string(node->string_value));
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id, "interpolated expr");
			display(node->operand2);
			break;
		}

		case brackets: {
			label(node->id, "[ ]");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case fieldAccess: {
			label(node->id, ".");
			link(node->id, node->operand->id, "op");
			display(node->operand);
			link(node->id, node->identifierAccess->id, "field");
			display(node->identifierAccess);
			break;
		}
		case methodCall: {
			label(node->id, ".call()");
			link(node->id, node->operand->id, "op");
			display(node->operand);
			link(node->id, node->identifierAccess->id, "method");
			display(node->identifierAccess);
			if (node->callArguments != NULL) {
				link(node->id, node->callArguments->id, "arguments");
				display(node->callArguments);
			}
			break;
		}
		case constructNew: {
			label(node->id, "new");
			link(node->id, node->identifierAccess->id, "class");
			display(node->identifierAccess);
			if (node->constructName != NULL) {
				link(node->id, node->constructName->id, "construct name");
				display(node->constructName);
			}
			if (node->callArguments != NULL) {
				link(node->id, node->callArguments->id, "arguments");
				display(node->callArguments);
			}
			break;
		}
		case constructConst: {
			label(node->id, "const");
			link(node->id, node->identifierAccess->id, "constructor name");
			display(node->identifierAccess);
			if (node->constructName != NULL) {
				link(node->id, node->constructName->id, "construct name");
				display(node->constructName);
			}
			if (node->callArguments != NULL) {
				link(node->id, node->callArguments->id, "arguments");
				display(node->callArguments);
			}
			break;
		}

		case identifier: {
			label(node->id, "id");
			link(node->id, node->identifierAccess->id, "accessed as");
			display(node->identifierAccess);
			break;
		}
		case call: {
			label(node->id, "call");
			link(node->id, node->identifierAccess->id, "method accessed as");
			display(node->identifierAccess);
			if (node->callArguments != NULL) {
				link(node->id, node->callArguments->id, "arguments");
				display(node->callArguments);
			}
			break;
		}

		case ifnull: {
			label(node->id, "??");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case _or: {
			label(node->id, "||");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case _and: {
			label(node->id, "&&");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case eq: {
			label(node->id, "==");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case neq: {
			label(node->id, "!=");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case expr_type::greater: {
			label(node->id, ">");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case expr_type::less: {
			label(node->id, "<");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case greater_eq: {
			label(node->id, ">=");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case less_eq: {
			label(node->id, "<=");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case type_cast: {
			label(node->id, "as");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case type_check: {
			label(node->id, "is");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->typeForCheckOrCast->id);
			display(node->typeForCheckOrCast);
			break;
		}
		case neg_type_check: {
			label(node->id, "is!");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->typeForCheckOrCast->id);
			display(node->typeForCheckOrCast);
			break;
		}
		case add: {
			label(node->id, "+");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case sub: {
			label(node->id, "-");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case mul: {
			label(node->id, "*");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case _div: {
			label(node->id, "/");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case u_minus: {
			label(node->id, "-");
			link(node->id, node->operand->id);
			display(node->operand);
			break;
		}
		case _not: {
			label(node->id, "!(not)");
			link(node->id, node->operand->id);
			display(node->operand);
			break;
		}
		case prefix_inc: {
			label(node->id, "++(pre)");
			link(node->id, node->operand->id);
			display(node->operand);
			break;
		}
		case prefix_dec: {
			label(node->id, "--(pre)");
			link(node->id, node->operand->id);
			display(node->operand);
			break;
		}
		case postfix_inc: {
			label(node->id, "(post)++");
			link(node->id, node->operand->id);
			display(node->operand);
			break;
		}
		case postfix_dec: {
			label(node->id, "(post)--");
			link(node->id, node->operand->id);
			display(node->operand);
			break;
		}
		case bang: {
			label(node->id, "(bang)!");
			link(node->id, node->operand->id);
			display(node->operand);
			break;
		}

		case assign: {
			label(node->id, "=");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case and_assign: {
			label(node->id, "&=");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case or_assign: {
			label(node->id, "|=");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case xor_assign: {
			label(node->id, "^=");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case mul_assign: {
			label(node->id, "*=");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case div_assign: {
			label(node->id, "/=");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case add_assign: {
			label(node->id, "+=");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case sub_assign: {
			label(node->id, "-=");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		case ifnull_assign: {
			label(node->id, "??=");
			link(node->id, node->operand->id);
			display(node->operand);
			link(node->id, node->operand2->id);
			display(node->operand2);
			break;
		}
		}

		if (node->next != NULL) {
			linkList(node->id, node->next->id);
			display(node->next);
		}
	}
	void display(stmt_node* node) {
		switch (node->type) {
		case block: {
			if (node->body != NULL) {
				label(node->id, "{...}");
				link(node->id, node->body->id);
				display(node->body);
			}
			else label(node->id, "{ }");
			break;
		}
		case expr_statement: {
			if (node->expr != NULL) {
				label(node->id, "exprStmnt");
				link(node->id, node->expr->id);
				display(node->expr);
			}
			else label(node->id, "empty exprStmnt");
			break;
		}
		case variable_declaration_statement: {
			label(node->id, "varDeclStmnt");
			link(node->id, node->variableDeclaration->id);
			display(node->variableDeclaration);
			break;
		}
		case forN_statement: {
			label(node->id, "forNStmnt");
			link(node->id, node->forInitializerStmt->id, "(...;");
			display(node->forInitializerStmt);
			if (node->condition != NULL) {
				link(node->id, node->condition->id, ";..;");
				display(node->condition);
			}
			if (node->forPostExpr != NULL) {
				link(node->id, node->forPostExpr->id, ";...)");
				display(node->forPostExpr);
			}
			link(node->id, node->body->id, "body");
			display(node->body);
			break;
		}
		case forEach_statement: {
			label(node->id, "forEachStmnt");
			if (node->variableDeclaration != NULL) {
				link(node->id, node->variableDeclaration->id, "iter");
				display(node->variableDeclaration);
			}
			else {
				link(node->id, node->forEachVariableId->id, "iter");
				display(node->forEachVariableId);
			}
			link(node->id, node->forContainerExpr->id, "in");
			display(node->forContainerExpr);
			link(node->id, node->body->id, "body");
			display(node->body);
			break;
		}
		case while_statement: {
			label(node->id, "whileStmnt");
			link(node->id, node->condition->id, "condition");
			display(node->condition);
			link(node->id, node->body->id, "body");
			display(node->body);
			break;
		}
		case do_statement: {
			label(node->id, "doWhileStmnt");
			link(node->id, node->body->id, "body");
			display(node->body);
			link(node->id, node->condition->id, "condition");
			display(node->condition);
			break;
		}
		case switch_statement: {
			label(node->id, "switchStmnt");
			link(node->id, node->condition->id, "switch expr");
			display(node->condition);
			link(node->id, node->switchCaseList->id, "cases");
			display(node->switchCaseList);
			if (node->defaultSwitchActions != NULL) {
				link(node->id, node->defaultSwitchActions->id, "default action");
				display(node->defaultSwitchActions);
			}
			break;
		}
		case if_statement: {
			label(node->id, "ifStmnt");
			link(node->id, node->condition->id, "condition");
			display(node->condition);
			link(node->id, node->body->id, "then");
			display(node->body);
			if (node->elseBody != NULL) {
				link(node->id, node->elseBody->id, "else");
				display(node->elseBody);
			}
			break;
		}
		case break_statement: {
			label(node->id, "breakStmnt");
			break;
		}
		case continue_statement: {
			label(node->id, "continueStmnt");
			break;
		}
		case return_statement: {
			label(node->id, "returnStmnt");
			if (node->returnExpr != NULL) {
				link(node->id, node->returnExpr->id, "val");
				display(node->returnExpr);
			}
			break;
		}
		case local_function_declaration: {
			label(node->id, "funcDeclStmnt");
			link(node->id, node->func->id);
			display(node->func);
			break;
		}
		}

		if (node->nextStmt != NULL) {
			linkList(node->id, node->nextStmt->id);
			display(node->nextStmt);
		}
	}
	void display(switch_case_node* node) {
		label(node->id, "case");
		link(node->id, node->condition->id, "val");
		display(node->condition);
		if (node->actions != NULL) {
			link(node->id, node->actions->id, "actions");
			display(node->actions);
		}

		if (node->next != NULL) {
			linkList(node->id, node->next->id);
			display(node->next);
		}
	}
}