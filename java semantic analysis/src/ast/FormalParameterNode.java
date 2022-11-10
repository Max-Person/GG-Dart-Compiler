package ast;

import org.w3c.dom.Element;

public class FormalParameterNode extends Node{

    boolean isField;

    VariableDeclarationNode paramDecl;
    IdentifierNode initializedField;

    public FormalParameterNode(Element element) {
        super(element);
        isField = Boolean.parseBoolean(element.getAttribute("isField"));
        if(isField){
            initializedField = new IdentifierNode(unlink(element, "initializedField"));
        } else{
            paramDecl = new VariableDeclarationNode(unlink(element, "paramDecl"));
        }

    }
}
