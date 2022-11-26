package ast;

import org.w3c.dom.Element;

public class FormalParameterNode extends Node{
    
    public boolean isField;
    
    public VariableDeclarationNode paramDecl;
    public IdentifierNode initializedField;

    public FormalParameterNode(Element element) {
        super(element);
        isField = Boolean.parseBoolean(element.getAttribute("isField"));
        if(isField){
            initializedField = new IdentifierNode(unlink(element, "initializedField"));
        } else{
            paramDecl = new VariableDeclarationNode(unlink(element, "paramDecl"));
        }

    }

    public String name(){
        if(isField){
            return this.initializedField.stringVal;
        }
        else{
            return paramDecl.name();
        }
    }
}
