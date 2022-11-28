package ast;

import ast.semantic.LocalVarRecord;
import ast.semantic.VariableRecord;
import ast.semantic.context.MethodContext;
import ast.semantic.typization.ListType;
import ast.semantic.typization.StandartType;
import ast.semantic.typization.VariableType;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import static ast.semantic.SemanticCrawler.printError;

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

    public void validateStmt(MethodContext context) {
        if (type == StmtType.block) {
            MethodContext scope = context.childScope();
            for (StmtNode block : blockStmts) {
                block.validateStmt(scope);
            }
            return;
        }
        if (type == StmtType.expr_statement) {
            expr.annotateTypes(context);
            return;
        }
        if(type == StmtType.variable_declaration_statement){
            for (VariableDeclarationNode variable: variableDeclaration) {
                LocalVarRecord localVarRecord = new LocalVarRecord(context.methodRecord, variable);
                localVarRecord.resolveType();
                context.addLocalToScope(localVarRecord);
            }
            return;
        }
        if (type == StmtType.if_statement || type == StmtType.while_statement || type == StmtType.do_statement) {
            this.condition.annotateTypes(context);
            if (!StandartType._bool().isAssignableFrom(this.condition.annotatedType)) {
                printError("Conditions must have a static type of 'bool'.", this.condition.lineNum);
            }

            this.body.validateStmt(type == StmtType.if_statement ? context.childScope() : context.skippableChildScope());
            if (type == StmtType.if_statement && this.elseBody != null)
                this.elseBody.validateStmt(context.childScope());
            return;
        }
        if (type == StmtType.return_statement) {
            VariableType type = this.returnExpr != null ? this.returnExpr.annotateTypes(context) : StandartType._void();
            if (!context.methodRecord.returnType.isAssignableFrom(type)) {
                printError("A value of type '" + type + "' can't be returned from the method '" + context.methodRecord.name + "' because it has a return type of '" + context.methodRecord.returnType + "'.", this.lineNum);
            }
            return;
        }
        if (type == StmtType.break_statement || type == StmtType.continue_statement) {
            if (!context.isSkippable) {
                printError("A " + this.type + " can't be used outside of a loop or switch statement.", this.lineNum);
            }
            return;
        }
        if(type == StmtType.forEach_statement){
            MethodContext forContext = context.skippableChildScope();
            forContainerExpr.annotateTypes(forContext); //FIXME ? мб надо аннотировать после объявления переменной цикла
            if(!(forContainerExpr.annotatedType instanceof ListType)){
                printError("The type '" + forContainerExpr.annotatedType + "' used in the 'for' loop must be a list type.", forContainerExpr.lineNum);
            }
            VariableType type;
            if(forEachVariableId != null){
                VariableRecord foundVar = forContext.lookupVariable(forEachVariableId.stringVal);
                if(foundVar == null){
                    printError("Undefined name '" + forEachVariableId.stringVal + "'.", forEachVariableId.lineNum);
                }
                type = foundVar.varType;
            }
            else {
                LocalVarRecord localVarRecord = new LocalVarRecord(forContext.methodRecord, forEachVariableDecl);
                localVarRecord.resolveType();
                forContext.addLocalToScope(localVarRecord);
                type = localVarRecord.varType;
            }

            if(!type.isAssignableFrom(((ListType) forContainerExpr.annotatedType).valueType)){
                printError("The type 'List<" + ((ListType) forContainerExpr.annotatedType).valueType +">' used in the 'for' loop must have a type argument that can be assigned to '" + type + "'.", forContainerExpr.lineNum);
            }
            body.validateStmt(forContext);
            return;
        }
        if(type == StmtType.forN_statement){
            MethodContext forContext = context.skippableChildScope();

            forInitializerStmt.validateStmt(forContext);
            condition.annotateTypes(forContext);
            if (!StandartType._bool().isAssignableFrom(this.condition.annotatedType)) {
                printError("Conditions must have a static type of 'bool'.", this.condition.lineNum);
            }
            forPostExpr.forEach(exprNode -> exprNode.annotateTypes(forContext));
            body.validateStmt(forContext);
            return;
        }
        if(type == StmtType.switch_statement){
            condition.annotateTypes(context);
            for (SwitchCaseNode switchNode : switchCaseList) {
                switchNode.condition.annotateTypes(context);
                if(!condition.annotatedType.isAssignableFrom(switchNode.condition.annotatedType)){ //TODO должен быть сабтайпом
                    printError("The switch case expression type '" + switchNode.condition.annotatedType + "' must be a subtype of the switch expression type '" + condition.annotatedType + "'.", switchNode.condition.lineNum);
                }
                MethodContext switchContext = context.skippableChildScope();
                for (StmtNode action : switchNode.actions) {
                    action.validateStmt(switchContext);
                }
            }

        }
    }
}
