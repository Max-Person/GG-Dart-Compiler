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

            if(element.getElementsByTagName("args").getLength() > 0){
                unlinkList(element, "args").forEach(e->args.add(new ExprNode(e)));
            }
        }
    }

    public ExprNode toExpr(InitializerType type){
        ExprNode expr = new ExprNode();
        if(type == InitializerType.superNamedConstructor || type == InitializerType.superConstructor){
            expr.type = ExprType.constructSuper;
            expr.constructName = superConstructorName;
            expr.callArguments = args;
        } else if(type == InitializerType.thisAssign){
            expr.type = ExprType.assign;
            expr.operand = new ExprNode(ExprType.fieldAccess);
            expr.operand.operand = new ExprNode(ExprType.this_pr);
            expr.operand.identifierAccess = thisFieldId;
            expr.operand2 = value;
        }
        expr.lineNum = this.lineNum;
        return expr;
    }
}
