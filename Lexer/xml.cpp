#include "xml.h"
using namespace std;
using namespace tinyxml2;


namespace xmlOut {
	XMLDocument xmlDoc;

	void link(XMLElement* el, list<XMLElement*> nodes) {
		for(XMLElement* child : nodes) {
			el->InsertEndChild(child);
		}
	}
	void link(XMLElement* el, list<XMLElement*> nodes, string container) {
		XMLElement* cont = xmlDoc.NewElement(container.c_str());
		for (XMLElement* child : nodes) {
			cont->InsertEndChild(child);
		}
		el->InsertEndChild(cont);
	}

	void displayInit(topLevelDeclaration_node* root) {
		XMLElement* xmlRoot = xmlDoc.NewElement("Root");
		xmlDoc.InsertFirstChild(xmlRoot);
		link(xmlRoot, display(root));
		xmlDoc.SaveFile("xmlOutput.xml");
	}
	list<XMLElement*> display(topLevelDeclaration_node* node) {
		list<XMLElement*> gen;
		if (node == NULL) {
			return gen;
		}
		XMLElement* xml = xmlDoc.NewElement("topLevelDeclaration_node");

		switch (node->type) {
		case _enum: {
			xml->SetAttribute("type", "_enum");
			link(xml, display(node->enumDecl), "enumDecl");
			break;
		}
		case _class: {
			xml->SetAttribute("type", "_class");
			link(xml, display(node->classDecl), "classDecl");
			break;
		}
		case _function: {
			xml->SetAttribute("type", "_function");
			link(xml, display(node->functionDecl), "functionDecl");
			break;
		}
		case _variable: {
			xml->SetAttribute("type", "_variable");
			link(xml, display(node->variableDecl), "variableDecl");
			break;
		}
		}

		gen.push_back(xml);
		if (node->next != NULL) gen.splice(gen.end(), display(node->next));
		return gen;
	}
	list<XMLElement*> display(enum_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("enum_node");
		link(xml, display(node->name), "name");
		link(xml, display(node->values), "values");
		
		gen.push_back(xml);
		return gen;
	}
	list<XMLElement*> display(identifier_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("identifier_node");
		xml->SetAttribute("stringval", node->stringval);
		xml->SetAttribute("isBuiltIn", node->isBuiltin);

		gen.push_back(xml);
		if (node->next != NULL) gen.splice(gen.end(), display(node->next));
		return gen;
	}
	list<XMLElement*> display(classDeclaration_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("classDeclaration_node");
		xml->SetAttribute("isAbstract", node->isAbstract);
		xml->SetAttribute("isAlias", node->isAlias);

		if (node->super != NULL) {
			link(xml , display(node->super), "super");
		}
		if (node->mixins != NULL) {
			link(xml, display(node->mixins), "mixins");
		}
		if (node->interfaces != NULL) {
			link(xml, display(node->interfaces), "interfaces");
		}
		if (!node->isAlias && node->classMembers != NULL) {
			link(xml, display(node->classMembers), "classMembers");
		}

		gen.push_back(xml);
		return gen;
	}
	list<XMLElement*> display(classMemberDeclaration_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("classMemberDeclaration_node");
		switch (node->type)
		{
		case field: {
			xml->SetAttribute("type", "field");
			link(xml, display(node->fieldDecl), "fieldDecl");
			break;
		}
		case methodSignature: {
			xml->SetAttribute("type", "methodSignature");
			link(xml, display(node->signature), "signature");
			break;
		}
		case constructSignature: {
			xml->SetAttribute("type", "constructSignature");
			link(xml, display(node->signature), "signature");
			break;
		}
		case methodDefinition: {
			xml->SetAttribute("type", "methodDefinition");
			link(xml, display(node->signature), "signature");
			link(xml, display(node->body), "body");
			break;
		}
		}

		gen.push_back(xml);
		if (node->next != NULL) gen.splice(gen.end(), display(node->next));
		return gen;
	}
	list<XMLElement*> display(type_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("type_node");
		xml->SetAttribute("isVoid", node->isVoid);
		xml->SetAttribute("isNullable", node->isNullable);
		if (!node->isVoid) {
			link(xml, display(node->name), "name");
		}

		gen.push_back(xml);
		if (node->next != NULL) gen.splice(gen.end(), display(node->next));
		return gen;
	}
	list<XMLElement*> display(variableDeclaration_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("variableDeclaration_node");
		link(xml, display(node->declarator), "declarator");
		link(xml, display(node->idInitList), "idInitList");

		gen.push_back(xml);
		return gen;
	}
	list<XMLElement*> display(declarator_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("declarator_node");
		xml->SetAttribute("isStatic", node->isStatic);
		xml->SetAttribute("isLate", node->isLate);
		xml->SetAttribute("isFinal", node->isFinal);
		xml->SetAttribute("isConst", node->isConst);
		xml->SetAttribute("isTyped", node->isTyped);
		if (node->isTyped) link(xml, display(node->valueType), "valueType");

		gen.push_back(xml);
		return gen;
	}
	list<XMLElement*> display(idInit_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("idInit_node");
		xml->SetAttribute("isAssign", node->isAssign);
		link(xml, display(node->identifier), "identifier");
		if (node->isAssign) link(xml, display(node->value), "value");

		gen.push_back(xml);
		if (node->next != NULL) gen.splice(gen.end(), display(node->next));
		return gen;
	}
	list<XMLElement*> display(functionDefinition_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("functionDefinition_node");
		link(xml, display(node->signature), "signature");
		link(xml, display(node->body), "body");
		
		gen.push_back(xml);
		return gen;
	}
	list<XMLElement*> display(signature_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("signature_node");
		xml->SetAttribute("isConstruct", node->type == construct);
		if (node->type == construct) {
			xml->SetAttribute("isConst", node->isConst);
			xml->SetAttribute("isNamed", node->isNamed);
		}
		else xml->SetAttribute("isStatic", node->isStatic);

		link(xml, display(node->name), "name");

		if (node->type == construct) {
			if (node->isNamed) link(xml, display(node->constructName), "constructName");
			if (node->initializers != NULL) link(xml, display(node->initializers), "initializers");
			if (node->redirection != NULL) link(xml, display(node->redirection), "redirection");
		}
		else link(xml, display(node->returnType), "returnType");

		if (node->parameters != NULL) link(xml, display(node->parameters), "parameters");

		gen.push_back(xml);
		return gen;
	}
	list<XMLElement*> display(initializer_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("initializer_node");
		if (node->type == thisAssign) {
			xml->SetAttribute("type", "thisAssign");
			link(xml, display(node->thisFieldId), "thisFieldId");
			link(xml, display(node->value), "value");
		}
		else {
			if (node->type == superNamedConstructor) {
				xml->SetAttribute("type", "superNamedConstructor");
				link(xml, display(node->superConstructorName), "superConstructorName");
			}
			else xml->SetAttribute("type", "superConstructor");

			if (node->args != NULL) link(xml, display(node->args), "args");
		}

		gen.push_back(xml);
		if (node->next != NULL) gen.splice(gen.end(), display(node->next));
		return gen;
	}
	list<XMLElement*> display(redirection_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("redirection_node");
		xml->SetAttribute("isNamed", node->isNamed);
		if (node->isNamed) link(xml, display(node->name), "name");
		if (node->args != NULL) link(xml, display(node->args), "args");

		gen.push_back(xml);
		return gen;
	}
	list<XMLElement*> display(formalParameter_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("formalParameter_node");
		xml->SetAttribute("isField", node->isField);
		if (node->isField) link(xml, display(node->initializedField), "initializedField");
		else link(xml, display(node->paramDecl), "paramDecl");

		gen.push_back(xml);
		if (node->next != NULL) gen.splice(gen.end(), display(node->next));
		return gen;
	}
	list<XMLElement*> display(expr_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("expr_node");
		switch (node->type) {
		case this_pr: {
			xml->SetAttribute("type", "this_pr");
			break;
		}
		case super_pr: {
			xml->SetAttribute("type", "super_pr");
			break;
		}
		case null_pr: {
			xml->SetAttribute("type", "null_pr");
			break;
		}
		case int_pr: {
			xml->SetAttribute("type", "int_pr");
			xml->SetAttribute("int_value", node->int_value);
			break;
		}
		case double_pr: {
			xml->SetAttribute("type", "double_pr");
			xml->SetAttribute("double_value", node->double_value);
			break;
		}
		case bool_pr: {
			xml->SetAttribute("type", "bool_pr");
			break;
		}
		case string_pr: {
			xml->SetAttribute("type", "string_pr");
			xml->SetAttribute("string_value", node->string_value);
			break;
		}
		case list_pr: {
			xml->SetAttribute("type", "list_pr");
			if(node->operand != NULL) link(xml, display(node->operand), "values");
			break;
		}

		case string_interpolation: {
			xml->SetAttribute("type", "string_interpolation");
			xml->SetAttribute("string_value", node->string_value);
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}

		case brackets: {
			xml->SetAttribute("type", "brackets");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case fieldAccess: {
			xml->SetAttribute("type", "fieldAccess");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->identifierAccess), "identifierAccess");
			break;
		}
		case methodCall: {
			xml->SetAttribute("type", "methodCall");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->identifierAccess), "identifierAccess");
			if (node->callArguments != NULL) link(xml, display(node->callArguments), "callArguments");
			break;
		}
		case constructNew: {
			xml->SetAttribute("type", "constructNew");
			link(xml, display(node->identifierAccess), "identifierAccess");
			if (node->callArguments != NULL) link(xml, display(node->callArguments), "callArguments");
			break;
		}
		case constructConst: {
			xml->SetAttribute("type", "constructConst");
			link(xml, display(node->identifierAccess), "identifierAccess");
			if (node->callArguments != NULL) link(xml, display(node->callArguments), "callArguments");
			break;
		}

		case identifier: {
			xml->SetAttribute("type", "identifier");
			link(xml, display(node->identifierAccess), "identifierAccess");
			break;
		}
		case call: {
			xml->SetAttribute("type", "call");
			link(xml, display(node->identifierAccess), "identifierAccess");
			if (node->callArguments != NULL) link(xml, display(node->callArguments), "callArguments");
			break;
		}

		case ifnull: {
			xml->SetAttribute("type", "ifnull");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case _or: {
			xml->SetAttribute("type", "_or");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case _and: {
			xml->SetAttribute("type", "_and");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case eq: {
			xml->SetAttribute("type", "eq");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case neq: {
			xml->SetAttribute("type", "neq");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case expr_type::greater: {
			xml->SetAttribute("type", "greater");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case expr_type::less: {
			xml->SetAttribute("type", "less");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case greater_eq: {
			xml->SetAttribute("type", "greater_eq");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case less_eq: {
			xml->SetAttribute("type", "less_eq");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case type_cast: {
			xml->SetAttribute("type", "type_cast");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->typeForCheckOrCast), "typeForCheckOrCast");
			break;
		}
		case type_check: {
			xml->SetAttribute("type", "type_check");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->typeForCheckOrCast), "typeForCheckOrCast");
			break;
		}
		case neg_type_check: {
			xml->SetAttribute("type", "neg_type_check");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->typeForCheckOrCast), "typeForCheckOrCast");
			break;
		}
		case add: {
			xml->SetAttribute("type", "add");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case sub: {
			xml->SetAttribute("type", "sub");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case mul: {
			xml->SetAttribute("type", "mul");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case _div: {
			xml->SetAttribute("type", "_div");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case u_minus: {
			xml->SetAttribute("type", "u_minus");
			link(xml, display(node->operand), "operand");
			break;
		}
		case _not: {
			xml->SetAttribute("type", "_not");
			link(xml, display(node->operand), "operand");
			break;
		}
		case prefix_inc: {
			xml->SetAttribute("type", "prefix_inc");
			link(xml, display(node->operand), "operand");
			break;
		}
		case prefix_dec: {
			xml->SetAttribute("type", "prefix_dec");
			link(xml, display(node->operand), "operand");
			break;
		}
		case postfix_inc: {
			xml->SetAttribute("type", "postfix_inc");
			link(xml, display(node->operand), "operand");
			break;
		}
		case postfix_dec: {
			xml->SetAttribute("type", "postfix_dec");
			link(xml, display(node->operand), "operand");
			break;
		}
		case bang: {
			xml->SetAttribute("type", "bang");
			link(xml, display(node->operand), "operand");
			break;
		}

		case assign: {
			xml->SetAttribute("type", "assign");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case and_assign: {
			xml->SetAttribute("type", "and_assign");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case or_assign: {
			xml->SetAttribute("type", "or_assign");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case xor_assign: {
			xml->SetAttribute("type", "xor_assign");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case mul_assign: {
			xml->SetAttribute("type", "mul_assign");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case div_assign: {
			xml->SetAttribute("type", "div_assign");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case add_assign: {
			xml->SetAttribute("type", "add_assign");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case sub_assign: {
			xml->SetAttribute("type", "sub_assign");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		case ifnull_assign: {
			xml->SetAttribute("type", "ifnull_assign");
			link(xml, display(node->operand), "operand");
			link(xml, display(node->operand2), "operand2");
			break;
		}
		}

		gen.push_back(xml);
		if (node->next != NULL) gen.splice(gen.end(), display(node->next));
		return gen;
	}
	list<XMLElement*> display(stmt_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("stmt_node");
		switch (node->type) {
		case block: {
			xml->SetAttribute("type", "block");
			if (node->body != NULL) link(xml, display(node->body), "body");
			break;
		}
		case expr_statement: {
			xml->SetAttribute("type", "expr_statement");
			if (node->expr != NULL) link(xml, display(node->expr), "expr");
			break;
		}
		case variable_declaration_statement: {
			xml->SetAttribute("type", "variable_declaration_statement");
			if (node->expr != NULL) link(xml, display(node->variableDeclaration), "variableDeclaration");
			break;
		}
		case forN_statement: {
			xml->SetAttribute("type", "forN_statement");
			link(xml, display(node->forInitializerStmt), "forInitializerStmt");
			if (node->condition != NULL) link(xml, display(node->condition), "condition");
			if (node->forPostExpr != NULL) link(xml, display(node->forPostExpr), "forPostExpr");
			link(xml, display(node->body), "body");
			break;
		}
		case forEach_statement: {
			xml->SetAttribute("type", "forEach_statement");
			if (node->variableDeclaration != NULL) link(xml, display(node->variableDeclaration), "variableDeclaration");
			else link(xml, display(node->forEachVariableId), "forEachVariableId");
			link(xml, display(node->forContainerExpr), "forContainerExpr");
			link(xml, display(node->body), "body");
			break;
		}
		case while_statement: {
			xml->SetAttribute("type", "while_statement");
			link(xml, display(node->condition), "condition");
			link(xml, display(node->body), "body");
			break;
		}
		case do_statement: {
			xml->SetAttribute("type", "do_statement");
			link(xml, display(node->condition), "condition");
			link(xml, display(node->body), "body");
			break;
		}
		case switch_statement: {
			xml->SetAttribute("type", "switch_statement");
			link(xml, display(node->condition), "condition");
			link(xml, display(node->switchCaseList), "switchCaseList");
			if (node->defaultSwitchActions != NULL) link(xml, display(node->defaultSwitchActions), "defaultSwitchActions");
			break;
		}
		case if_statement: {
			xml->SetAttribute("type", "if_statement");
			link(xml, display(node->condition), "condition");
			link(xml, display(node->body), "body");
			if (node->elseBody != NULL) link(xml, display(node->elseBody), "elseBody");
			break;
		}
		case break_statement: {
			xml->SetAttribute("type", "break_statement");
			break;
		}
		case continue_statement: {
			xml->SetAttribute("type", "continue_statement");
			break;
		}
		case return_statement: {
			xml->SetAttribute("type", "return_statement");
			if (node->returnExpr != NULL) link(xml, display(node->returnExpr), "returnExpr");
			break;
		}
		case local_function_declaration: {
			xml->SetAttribute("type", "local_function_declaration");
			link(xml, display(node->func), "func");
			break;
		}
		}

		gen.push_back(xml);
		if (node->nextStmt != NULL) gen.splice(gen.end(), display(node->nextStmt));
		return gen;
	}
	list<XMLElement*> display(switch_case_node* node) {
		list<XMLElement*> gen;
		XMLElement* xml = xmlDoc.NewElement("switch_case_node");
		link(xml, display(node->condition), "condition");
		if (node->actions != NULL) link(xml, display(node->actions), "actions");

		gen.push_back(xml);
		if (node->next != NULL) gen.splice(gen.end(), display(node->next));
		return gen;
	}
}