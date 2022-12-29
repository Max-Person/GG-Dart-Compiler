package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class InitializerNode extends Node{
    
    public InitializerType type;
    
    public IdentifierNode superConstructorName;
    public List<ExprNode> args = new ArrayList<>();
    
    public IdentifierNode thisFieldId;
    public ExprNode value;

    public InitializerNode(Element element) {
        super(element);
        type = InitializerType.valueOf(element.getAttribute("type"));
        if(type == InitializerType.thisAssign){
            thisFieldId = new IdentifierNode(unlink(element, "thisFieldId"));
            value = new ExprNode(unlink(element, "value"));
        } else {
            if(type == InitializerType.superNamedConstructor) {
                superConstructorName = new IdentifierNode(unlink(element, "superConstructorName"));
            }

            if(Node.getImmediateChildByName(element, "args") != null){
                unlinkList(element, "args").forEach(e->args.add(new ExprNode(e)));
            }
        }
    }
    
    public InitializerNode(IdentifierNode superConstructorName, List<ExprNode> args){
        this.superConstructorName = superConstructorName;
        
        if(superConstructorName != null)
            this.type = InitializerType.superNamedConstructor;
        else
            this.type = InitializerType.superConstructor;
        
        this.args = args;
    }

    public ExprNode toExpr(){
        ExprNode expr = null;
        if(type == InitializerType.superNamedConstructor || type == InitializerType.superConstructor){
            expr = new ExprNode(ExprType.constructSuper, lineNum);
            expr.constructName = superConstructorName;
            expr.callArguments = args;
        } else if(type == InitializerType.thisAssign){
            expr = new ExprNode(ExprType.assign, lineNum);
            expr.operand = new ExprNode(ExprType.fieldAccess, lineNum);
            expr.operand.operand = new ExprNode(ExprType.this_pr, lineNum);
            expr.operand.identifierAccess = thisFieldId;
            expr.operand2 = value;
        }
        return expr;
    }
    
    public StmtNode toStmt(){
        StmtNode init = new StmtNode(StmtType.expr_statement, lineNum);
        init.expr = this.toExpr();
        return init;
    }
}
