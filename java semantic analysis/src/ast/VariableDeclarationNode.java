package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class VariableDeclarationNode extends Node{

    DeclaratorNode declarator;
    IdentifierNode identifier;
    boolean isAssign;
    ExprNode value;

    public VariableDeclarationNode(Element element) {
        super(element);
        declarator = new DeclaratorNode(unlink(element, "declarator"));
        identifier = new IdentifierNode(unlink(element, "identifier"));
        isAssign = Boolean.parseBoolean(element.getAttribute("isInitialized"));
        if(isAssign){
            value = new ExprNode(unlink(element, "value"));
        }
    }
}
