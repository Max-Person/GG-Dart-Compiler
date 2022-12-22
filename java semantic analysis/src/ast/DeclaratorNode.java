package ast;

import org.w3c.dom.Element;

public class DeclaratorNode extends Node{
    
    public boolean isStatic;
    public boolean isLate;
    public boolean isFinal;
    public boolean isConst;
    public boolean isTyped;
    
    public TypeNode valueType;

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
    
    public DeclaratorNode(boolean isStatic, boolean isLate, boolean isFinal, boolean isConst, TypeNode valueType) {
        this.isStatic = isStatic;
        this.isLate = isLate;
        this.isFinal = isFinal;
        this.isConst = isConst;
        this.isTyped = valueType != null;
        
        if(isTyped){
            this.valueType = valueType;
        }
    }
}
