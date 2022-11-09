package ast;

import org.w3c.dom.Element;

public class IdentifierNode extends Node{

    boolean isBuiltin;
    String stringVal;

    public IdentifierNode(Element element) {
        super(element);
        this.stringVal = element.getAttribute("stringval");
        this.isBuiltin = Boolean.parseBoolean(element.getAttribute("isBuiltin"));
    }
}
