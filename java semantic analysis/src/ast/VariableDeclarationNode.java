package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class VariableDeclarationNode extends Node implements Declaration{
    
    public DeclaratorNode declarator;
    public IdentifierNode identifier;
    public boolean isAssign;
    public ExprNode value;

    public VariableDeclarationNode(Element element) {
        super(element);
        declarator = new DeclaratorNode(unlink(element, "declarator"));
        identifier = new IdentifierNode(unlink(element, "identifier"));
        isAssign = Boolean.parseBoolean(element.getAttribute("isInitialized"));
        if(isAssign){
            value = new ExprNode(unlink(element, "value"));
        }
    }
    
    public VariableDeclarationNode(String name, ExprNode value) {
        declarator = new DeclaratorNode(false, false, false, false, null);
        identifier = new IdentifierNode(name);
        isAssign = value != null;
        if(isAssign){
            this.value = value;
        }
    }
    
    @Override
    public String name() {
        return identifier.stringVal;
    }
}
