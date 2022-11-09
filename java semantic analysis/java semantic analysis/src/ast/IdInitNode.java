package ast;

import org.w3c.dom.Element;

public class IdInitNode extends Node{

    boolean isAssign;
    IdentifierNode identifier;
    ExprNode value;

    public IdInitNode(Element element) {
        super(element);
        isAssign = Boolean.parseBoolean(element.getAttribute("isAssign"));
        identifier = new IdentifierNode(unlink(element, "identifier"));
        if(isAssign){
            value = new ExprNode(unlink(element, "value"));
        }
    }
}
