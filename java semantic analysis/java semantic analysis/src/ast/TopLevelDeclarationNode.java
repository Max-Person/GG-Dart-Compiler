package ast;

import org.w3c.dom.Element;

enum TopLevelDeclarationType {
    _class,
    _function,
    _enum,
    _variable,
}

public class TopLevelDeclarationNode extends Node{

    TopLevelDeclarationType type;
    ClassDeclarationNode classDecl;
    FunctionDefinitionNode functionDecl;
    EnumNode enumDecl;
    VariableDeclarationNode variableDecl;

    public TopLevelDeclarationNode(Element element) {
        super(element);
        type = TopLevelDeclarationType.valueOf(element.getAttribute("type"));

        if(type == TopLevelDeclarationType._enum){
            enumDecl = new EnumNode(unlink(element, "enumDecl"));
        } else if(type == TopLevelDeclarationType._function){
            functionDecl = new FunctionDefinitionNode(unlink(element, "functionDecl"));
        } else if(type == TopLevelDeclarationType._class){
            classDecl = new ClassDeclarationNode(unlink(element, "classDecl"));
        } else {
            variableDecl = new VariableDeclarationNode(unlink(element, "variableDecl"));
        }
    }
}
