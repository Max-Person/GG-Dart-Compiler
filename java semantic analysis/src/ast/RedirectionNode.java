package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class RedirectionNode extends Node{
    
    public boolean isNamed;
    public IdentifierNode name;
    public List<ExprNode> args = new ArrayList<>();

    public RedirectionNode(Element element) {
        super(element);
        isNamed = Boolean.parseBoolean(element.getAttribute("isNamed"));
        if(isNamed){
            name = new IdentifierNode(unlink(element, "name"));
        }

        if(Node.getImmediateChildByName(element, "args") != null){
            unlinkList(element, "args").forEach(e->args.add(new ExprNode(e)));
        }
    }

    public ExprNode toExpr(){
        ExprNode expr = new ExprNode(ExprType.constructRedirect, lineNum);
        expr.constructName = isNamed ? this.name : null;
        expr.callArguments = this.args;
        expr.lineNum = this.lineNum;
        return expr;
    }
    
    public StmtNode toStmt(){
        StmtNode redir = new StmtNode(StmtType.expr_statement, lineNum);
        redir.expr = this.toExpr();
        return redir;
    }
}
