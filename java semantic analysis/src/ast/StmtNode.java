package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class StmtNode extends Node{
    
    public StmtType type;
    
    public ExprNode condition;    //для if, switch, while � for
    public StmtNode body;         //для if, while, for
    public List<StmtNode> blockStmts = new ArrayList<>();
    
    public StmtNode elseBody;
    
    public ExprNode returnExpr;
    
    public List<VariableDeclarationNode> variableDeclaration = new ArrayList<>();   //для variableDeclarationStatement
    
    public ExprNode expr; // для exprStatement
    
    public StmtNode forInitializerStmt;
    public List<ExprNode> forPostExpr = new ArrayList<>();
    public VariableDeclarationNode forEachVariableDecl;
    public IdentifierNode forEachVariableId;
    public ExprNode forContainerExpr;
    
    public FunctionDefinitionNode func;
    
    public List<SwitchCaseNode> switchCaseList = new ArrayList<>();
    public List<StmtNode> defaultSwitchActions = new ArrayList<>();

    public StmtNode(Element element) {
        super(element);
        type = StmtType.valueOf(element.getAttribute("type"));

        if(type == StmtType.block && element.getElementsByTagName("body").getLength() > 0){
            unlinkList(element, "body").forEach(e -> blockStmts.add(new StmtNode(e)));
        }

        if(type == StmtType.expr_statement && element.getElementsByTagName("expr").getLength() > 0){
            expr = new ExprNode(unlink(element, "expr")); //TODO подумать
        }

        if(type == StmtType.variable_declaration_statement && element.getElementsByTagName("variableDeclaration").getLength() > 0){
            unlinkList(element, "variableDeclaration").forEach(e -> variableDeclaration.add(new VariableDeclarationNode(e)));
        }

        if(type == StmtType.forN_statement){
            forInitializerStmt = new StmtNode(unlink(element, "forInitializerStmt"));
            body = new StmtNode(unlink(element, "body"));
            if(element.getElementsByTagName("condition").getLength() > 0){
                condition = new ExprNode(unlink(element, "condition"));
            }
            if(element.getElementsByTagName("forPostExpr").getLength() > 0){
                unlinkList(element, "forPostExpr").forEach(e->forPostExpr.add(new ExprNode(e)));
            }
        }

        if(type == StmtType.forEach_statement){
            if(element.getElementsByTagName("variableDeclaration").getLength() > 0){
                forEachVariableDecl = new VariableDeclarationNode(unlink(element, "variableDeclaration"));
            } else {
                forEachVariableId = new IdentifierNode(unlink(element, "forEachVariableId"));
            }
            forContainerExpr = new ExprNode(unlink(element, "forContainerExpr"));
            body = new StmtNode(unlink(element, "body"));
        }

        if(type == StmtType.while_statement || type == StmtType.do_statement){
            condition = new ExprNode(unlink(element, "condition"));
            body = new StmtNode(unlink(element, "body"));
        }

        if(type == StmtType.switch_statement){
            condition = new ExprNode(unlink(element, "condition"));
            unlinkList(element, "switchCaseList").forEach(e->switchCaseList.add(new SwitchCaseNode(e)));
            if(element.getElementsByTagName("defaultSwitchActions").getLength() > 0){
                unlinkList(element, "defaultSwitchActions").forEach(e->defaultSwitchActions.add(new StmtNode(e)));
            }
        }

        if(type == StmtType.if_statement){
            condition = new ExprNode(unlink(element, "condition"));
            body = new StmtNode(unlink(element, "body"));
            if(element.getElementsByTagName("elseBody").getLength() > 0){
                elseBody = new StmtNode(unlink(element, "elseBody"));
            }
        }

        if(type == StmtType.return_statement && element.getElementsByTagName("returnExpr").getLength() > 0){
            returnExpr = new ExprNode(unlink(element, "returnExpr"));
        }

        if(type == StmtType.local_function_declaration){
            func = new FunctionDefinitionNode(unlink(element, "func"));
        }
    }

    public StmtNode(StmtType type) {
        this.type = type;
    }

    public void validateStmt(){
        if(type == StmtType.block){
            for (StmtNode block: blockStmts) {
                block.validateStmt();
            }
        }
        if (type == StmtType.expr_statement){
            //expr.annotateTypes(, );
        }
        if(type == StmtType.if_statement){

        }
        if(type == StmtType.do_statement){

        }
        if(body == null)
            return; // TODO итс окей?????
        for (StmtNode block: body.blockStmts) {
            if(block.blockStmts != null){
                block.validateStmt();
            }
            //body.
        }
    }
}
