package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

enum StmtType {
    block,
    expr_statement,
    variable_declaration_statement,
    forN_statement,
    forEach_statement,
    while_statement,
    do_statement,
    switch_statement,
    if_statement,
    break_statement,
    continue_statement,
    return_statement,
    local_function_declaration,
}

public class StmtNode extends Node{

    StmtType type;

    ExprNode condition;    //для if, switch, while � for
    StmtNode body;         //для if, while, for и block

    StmtNode elseBody;

    ExprNode returnExpr;

    List<VariableDeclarationNode> variableDeclaration;   //для variableDeclarationStatement

    ExprNode expr; // для exprStatement

    StmtNode forInitializerStmt;
    List<ExprNode> forPostExpr = new ArrayList<>();
    VariableDeclarationNode forEachVariableDecl;
    IdentifierNode forEachVariableId;
    ExprNode forContainerExpr;

    FunctionDefinitionNode func;

    List<SwitchCaseNode> switchCaseList = new ArrayList<>();
    List<StmtNode> defaultSwitchActions = new ArrayList<>();

    public StmtNode(Element element) {
        super(element);
        type = StmtType.valueOf(element.getAttribute("type"));

        if(type == StmtType.block && element.getElementsByTagName("body").getLength() > 0){
            body = new StmtNode(unlink(element, "body")); // TODO подумать
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
}
