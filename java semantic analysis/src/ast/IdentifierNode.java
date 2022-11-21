package ast;

import org.w3c.dom.Element;

public class IdentifierNode extends Node implements Cloneable{
    
    public boolean isBuiltin;
    public String stringVal;

    public IdentifierNode(Element element) {
        super(element);
        this.stringVal = element.getAttribute("stringval");
        this.isBuiltin = Boolean.parseBoolean(element.getAttribute("isBuiltin"));
    }

    public IdentifierNode(String name) {
        super();
        this.stringVal = name;
        this.isBuiltin = false;
    }

    @Override
    public IdentifierNode clone() {
        try {
            IdentifierNode clone = (IdentifierNode) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
