package ast;

import org.w3c.dom.Element;

public class DeclaratorNode extends Node{

    boolean isStatic;
    boolean isLate;
    boolean isFinal;
    boolean isConst;
    boolean isTyped;

    TypeNode valueType;

    public DeclaratorNode(Element element) {
        super(element);
        isStatic = Boolean.parseBoolean(element.getAttribute("isStatic"));
        isLate = Boolean.parseBoolean(element.getAttribute("isLate"));
        isFinal = Boolean.parseBoolean(element.getAttribute("isFinal"));
        isConst = Boolean.parseBoolean(element.getAttribute("isConst"));
        isTyped = Boolean.parseBoolean(element.getAttribute("isTyped"));

        if(isTyped){
            valueType = new TypeNode(unlink(element, "valueType"));
        }
    }
}
