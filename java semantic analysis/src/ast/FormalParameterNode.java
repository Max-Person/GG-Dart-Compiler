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

    public ExprNode normalize(){
        if(!isField){
            throw new IllegalStateException("aboba");
        }
        ExprNode expr = new ExprNode(ExprType.assign);
        expr.lineNum = this.lineNum;

        expr.operand = new ExprNode(ExprType.fieldAccess);
        expr.operand.operand = new ExprNode(ExprType.this_pr);
        expr.operand.identifierAccess = initializedField.clone();

        expr.operand2 = new ExprNode(ExprType.identifier);
        expr.operand2.identifierAccess = initializedField;
        return expr;
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
