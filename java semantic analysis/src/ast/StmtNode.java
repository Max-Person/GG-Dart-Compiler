package ast;

import ast.semantic.Bytecode;
import ast.semantic.LocalVarRecord;
import ast.semantic.VariableRecord;
import ast.semantic.context.MethodContext;
import ast.semantic.typization.ListType;
import ast.semantic.typization.PlainType;
import ast.semantic.typization.VariableType;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ast.semantic.SemanticCrawler.printError;

public class StmtNode extends Node{
    
    public StmtType type;
    
    public ExprNode condition;    //для if, switch, while � for
    public StmtNode body;         //для if, while, for
    public List<StmtNode> blockStmts = new ArrayList<>();
    
    public StmtNode elseBody;
    
    public ExprNode returnExpr;
    
    public List<VariableDeclarationNode> variableDeclaration = new ArrayList<>();   //для variableDeclarationStatement
    public List<ExprNode> variableDeclarationAssignments = new ArrayList<>();
    
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

        if(type == StmtType.block && Node.getImmediateChildByName(element, "body") != null){
            unlinkList(element, "body").forEach(e -> blockStmts.add(new StmtNode(e)));
        }

        if(type == StmtType.expr_statement && Node.getImmediateChildByName(element, "expr") != null){
            expr = new ExprNode(unlink(element, "expr")); //TODO подумать
        }

        if(type == StmtType.variable_declaration_statement && Node.getImmediateChildByName(element, "variableDeclaration") != null){
            unlinkList(element, "variableDeclaration").forEach(e -> variableDeclaration.add(new VariableDeclarationNode(e)));
        }

        if(type == StmtType.forN_statement){
            forInitializerStmt = new StmtNode(unlink(element, "forInitializerStmt"));
            body = new StmtNode(unlink(element, "body"));
            if(Node.getImmediateChildByName(element, "condition") != null){
                condition = new ExprNode(unlink(element, "condition"));
            }
            if(Node.getImmediateChildByName(element, "forPostExpr") != null){
                unlinkList(element, "forPostExpr").forEach(e->forPostExpr.add(new ExprNode(e)));
            }
        }

        if(type == StmtType.forEach_statement){
            if(Node.getImmediateChildByName(element, "variableDeclaration") != null){
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
            if(Node.getImmediateChildByName(element, "defaultSwitchActions") != null){
                unlinkList(element, "defaultSwitchActions").forEach(e->defaultSwitchActions.add(new StmtNode(e)));
            }
        }

        if(type == StmtType.if_statement){
            condition = new ExprNode(unlink(element, "condition"));
            body = new StmtNode(unlink(element, "body"));
            if(Node.getImmediateChildByName(element, "elseBody") != null){
                elseBody = new StmtNode(unlink(element, "elseBody"));
            }
        }

        if(type == StmtType.return_statement && Node.getImmediateChildByName(element, "returnExpr") != null){
            returnExpr = new ExprNode(unlink(element, "returnExpr"));
        }

        if(type == StmtType.local_function_declaration){
            func = new FunctionDefinitionNode(unlink(element, "func"));
        }
    }

    public StmtNode(StmtType type) {
        this.type = type;
    }

    public StmtNode() {
    }

    public StmtNode deepCopy(){
        StmtNode copy = new StmtNode();
        copy.type = this.type;
        copy.condition = this.condition == null ? null : this.condition.deepCopy(); //TODO
        copy.body = this.body == null ? null : this.body.deepCopy();
        copy.blockStmts = blockStmts.stream().map(StmtNode::deepCopy).collect(Collectors.toList());
        copy.elseBody = this.elseBody == null ? null : this.elseBody.deepCopy();
        copy.returnExpr = this.returnExpr == null ? null : this.returnExpr.deepCopy();
        copy.variableDeclaration = new ArrayList<>(variableDeclaration);
        copy.variableDeclarationAssignments = variableDeclarationAssignments.stream().map(ExprNode::deepCopy).collect(Collectors.toList());
        copy.expr = this.expr == null ? null : this.expr.deepCopy();
        copy.forInitializerStmt = this.forInitializerStmt == null ? null : this.forInitializerStmt.deepCopy();
        copy.forPostExpr = forPostExpr.stream().map(ExprNode::deepCopy).collect(Collectors.toList());
        copy.forEachVariableDecl = forEachVariableDecl;
        copy.forEachVariableId = this.forEachVariableId == null ? null : new IdentifierNode(this.forEachVariableId.stringVal);
        copy.forContainerExpr = this.forContainerExpr == null ? null : this.forContainerExpr.deepCopy();
        copy.func = func;
        copy.switchCaseList = switchCaseList.stream().map(SwitchCaseNode::deepCopy).collect(Collectors.toList());
        copy.defaultSwitchActions = defaultSwitchActions.stream().map(StmtNode::deepCopy).collect(Collectors.toList());

        return copy;
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
            if(expr != null) expr.annotateTypes(context);
            return;
        }
        if(type == StmtType.variable_declaration_statement){
            for (VariableDeclarationNode variable: variableDeclaration) {
                LocalVarRecord localVarRecord = new LocalVarRecord(context.methodRecord, variable);
                context.addLocalToScope(localVarRecord);
                localVarRecord.resolveType(context);
                context.resolvePendingLocal();
                ExprNode assign = localVarRecord.toExpr();
                if(assign != null){
                    assign.annotateTypes(context);
                    variableDeclarationAssignments.add(assign);
                }
            }
            return;
        }
        if (type == StmtType.if_statement || type == StmtType.while_statement || type == StmtType.do_statement) {
            this.condition.annotateTypes(context);
            if (!this.condition.makeAssignableTo(PlainType._bool(), context)) {
                printError("Conditions must have a static type of 'bool'.", this.condition.lineNum);
            }

            this.body.validateStmt(type == StmtType.if_statement ? context.childScope() : context.skippableChildScope());
            if (type == StmtType.if_statement && this.elseBody != null)
                this.elseBody.validateStmt(context.childScope());
            return;
        }
        if (type == StmtType.return_statement) {
            if( this.returnExpr != null){
                this.returnExpr.annotateTypes(context);
                if (!returnExpr.makeAssignableTo(context.methodRecord.returnType, context)) {
                    printError("A value of type '" + returnExpr.annotatedType + "' can't be returned from the method '" + context.methodRecord.name + "' because it has a return type of '" + context.methodRecord.returnType + "'.", this.lineNum);
                }
            }
            else if (!context.methodRecord.returnType.equals(VariableType._void())) {
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
            MethodContext forContext = context.childScope();
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
                localVarRecord.resolveType(forContext);
                //forContext.addLocalToScope(localVarRecord);
                type = localVarRecord.varType;
            }

            if(!type.isAssignableFrom(((ListType) forContainerExpr.annotatedType).valueType)){
                printError("The type 'List<" + ((ListType) forContainerExpr.annotatedType).valueType +">' used in the 'for' loop must have a type argument that can be assigned to '" + type + "'.", forContainerExpr.lineNum);
            }
            
            //Преобразовать for in в обычный for
            {
                this.type = StmtType.forN_statement;
                //преддействие
                ExprNode iterCall = new ExprNode(ExprType.methodCall);
                iterCall.isSynthetic = true;
                iterCall.operand = forContainerExpr;
                iterCall.callArguments = new ArrayList<>();
                iterCall.identifierAccess = new IdentifierNode("iterator");
                VariableDeclarationNode iterDeclNode = new VariableDeclarationNode("!iter", iterCall);
                StmtNode iterDecl = new StmtNode(StmtType.variable_declaration_statement);
                iterDecl.variableDeclaration.add(iterDeclNode);
                this.forInitializerStmt = iterDecl;
                //условие
                this.condition = new ExprNode(ExprType.methodCall);
                this.condition.isSynthetic = true;
                this.condition.identifierAccess = new IdentifierNode("hasNext");
                this.condition.callArguments = new ArrayList<>();
                this.condition.operand = new ExprNode(ExprType.identifier);
                this.condition.operand.identifierAccess = new IdentifierNode("!iter");
                //постдействие
                this.forPostExpr = new ArrayList<>();
                //тело
                ExprNode nextExpr = new ExprNode(ExprType.methodCall);
                nextExpr.isSynthetic = true;
                nextExpr.identifierAccess = new IdentifierNode("next");
                nextExpr.callArguments = new ArrayList<>();
                nextExpr.operand = new ExprNode(ExprType.identifier);
                nextExpr.operand.identifierAccess = new IdentifierNode("!iter");
                StmtNode next;
                if(forEachVariableId != null){
                    ExprNode assign = new ExprNode(ExprType.assign);
                    assign.operand = new ExprNode(ExprType.identifier);
                    assign.operand.identifierAccess = forEachVariableId;
                    assign.operand2 = nextExpr;
                    next = new StmtNode(StmtType.expr_statement);
                    next.expr = assign;
                }
                else {
                    forEachVariableDecl.isAssign = true;
                    forEachVariableDecl.value = nextExpr;
                    next = new StmtNode(StmtType.variable_declaration_statement);
                    next.variableDeclaration.add(forEachVariableDecl);
                }
                StmtNode newBody = new StmtNode(StmtType.block);
                newBody.blockStmts.add(next);
                StmtNode wrapper = this.body;
                if(this.body.type != StmtType.block){
                    wrapper = new StmtNode(StmtType.block);
                    wrapper.blockStmts.add(this.body);
                }
                newBody.blockStmts.add(wrapper);
                this.body = newBody;
                this.forEachVariableId = null;
                this.forEachVariableDecl = null;
                this.forContainerExpr = null;
            }
        }
        if(type == StmtType.forN_statement){
            MethodContext forContext = context.childScope();
    
            forInitializerStmt.validateStmt(forContext);
            if(condition == null){
                condition = new ExprNode(ExprType.bool_pr);
                condition.boolValue = true;
            }
            condition.annotateTypes(forContext);
            if (!this.condition.makeAssignableTo(PlainType._bool(), context)) {
                printError("Conditions must have a static type of 'bool'.", this.condition.lineNum);
            }
            forPostExpr.forEach(exprNode -> exprNode.annotateTypes(forContext));
            body.validateStmt(forContext.skippableChildScope());
            return;
        }
        if(type == StmtType.switch_statement){
            condition.annotateTypes(context);
            condition.makeAssignableTo(VariableType._Object(), context);
            for (SwitchCaseNode caseNode : switchCaseList) {
                caseNode.condition.annotateTypes(context);
                caseNode.condition.makeAssignableTo(VariableType._Object(), context);
                if(!condition.annotatedType.isSubtypeOf(caseNode.condition.annotatedType)){
                    printError("The switch case expression type '" + caseNode.condition.annotatedType + "' must be a subtype of the switch expression type '" + condition.annotatedType + "'.", caseNode.condition.lineNum);
                }
                MethodContext caseContext = context.skippableChildScope();
                for (StmtNode action : caseNode.actions) {
                    action.validateStmt(caseContext);
                }
                if(caseNode.actions.stream().noneMatch(stmt -> stmt.endsWith(StmtType.break_statement, StmtType.return_statement, StmtType.continue_statement))){ //FIXME ? как поступать с континью без лейблов?
                    printError("The 'case' shouldn't complete normally.", caseNode.lineNum);
                }
            }
            for (StmtNode action : defaultSwitchActions) {
                action.validateStmt(context.skippableChildScope());
            }
        }
    }
    
    public boolean endsWith(StmtType... types){
        if(Arrays.asList(types).contains(this.type))
            return true;
        
        if (type == StmtType.expr_statement ||
                type == StmtType.return_statement ||
                type == StmtType.break_statement ||
                type == StmtType.continue_statement ||
                type == StmtType.while_statement ||
                type == StmtType.forN_statement ||
                type == StmtType.forEach_statement ||
                type == StmtType.variable_declaration_statement) {
            return false;
        }
        
        if (type == StmtType.block) {
            return blockStmts.stream().anyMatch(smt -> smt.endsWith(types));
        }
        if (type == StmtType.if_statement) {
            if(elseBody != null)
                return body.endsWith(types) && elseBody.endsWith(types);
            else
                return false;
        }
        if(type == StmtType.do_statement){
            return body.endsWith(types);
        }
        if(type == StmtType.switch_statement){
            if(!defaultSwitchActions.isEmpty()){
                boolean ends = defaultSwitchActions.stream().anyMatch(stmt -> stmt.endsWith(types));
                for (SwitchCaseNode switchNode : switchCaseList) {
                    ends = ends && switchNode.actions.stream().anyMatch(stmt -> stmt.endsWith(types));
                }
                return ends;
            }
            else
                return false;
        }
        return false;
    }

    public int toBytecode(Bytecode outBytecode) throws IOException {
        Bytecode bytecode = new Bytecode();
        int startOffset = bytecode.currentOffset();
        boolean expectZero = false;
        if (type == StmtType.block) {
            for (StmtNode stmt: blockStmts) {
                stmt.toBytecode(bytecode);
            }
        }
        if (type == StmtType.expr_statement) {
            if(expr != null){
                expr.toBytecode(bytecode);
                if(expr.annotatedType.equals(PlainType._double()))
                    bytecode.writeSimple(Bytecode.Instruction.pop2);
                else if(!expr.annotatedType.equals(VariableType._void()))
                    bytecode.writeSimple(Bytecode.Instruction.pop);
            }
            else expectZero = true;
            
        }
        if(type == StmtType.variable_declaration_statement){
            for (ExprNode expr : variableDeclarationAssignments) {
                expr.toBytecode(bytecode);
                if (expr.annotatedType.equals(PlainType._double()))
                    bytecode.writeSimple(Bytecode.Instruction.pop2);
                else if (!expr.annotatedType.equals(VariableType._void()))
                    bytecode.writeSimple(Bytecode.Instruction.pop);
            }
            expectZero = variableDeclarationAssignments.isEmpty();
        }
        if (type == StmtType.if_statement) {
            condition.toBytecode(bytecode);
            Bytecode bodyBytecode = new Bytecode();
            int bodySize = body.toBytecode(bodyBytecode);
            bytecode.write(Bytecode.jump(Bytecode.Instruction.ifeq, 3 + bodySize + (elseBody != null ? 3 : 0)));
            bytecode.write(bodyBytecode);
            if(elseBody != null){
                Bytecode elseBytecode = new Bytecode();
                int elseSize = elseBody.toBytecode(elseBytecode);
                bytecode.write(Bytecode.jump(Bytecode.Instruction._goto, 3 + elseSize));
                bytecode.write(elseBytecode);
            }
        }
        if (type == StmtType.return_statement) {
            if(returnExpr == null){
                bytecode.writeSimple(Bytecode.Instruction._return);
            }
            else {
                returnExpr.toBytecode(bytecode);
                bytecode.writeSimple(Bytecode.Instruction.areturn);
            }
        }
        if (type == StmtType.break_statement) {
            bytecode.markBreak();
        }
        if(type == StmtType.continue_statement) {
            bytecode.markContinue();
        }
        if (type == StmtType.while_statement) {
            Bytecode bodyBytecode = new Bytecode();
            int bodySize = body.toBytecode(bodyBytecode);

            int continueLocation = bytecode.currentOffset();
            int condSize = condition.toBytecode(bytecode);
            bytecode.write(Bytecode.jump(Bytecode.Instruction.ifeq, 3 + bodySize + 3));
            bytecode.write(bodyBytecode);
            bytecode.write(Bytecode.jump(Bytecode.Instruction._goto, -condSize - bodySize - 3));
            
            bytecode.resolveBreaks(bytecode.currentOffset());
            bytecode.resolveContinues(continueLocation);
        }
        if (type == StmtType.do_statement) {
            int bodySize = body.toBytecode(bytecode);
            bytecode.resolveContinues(bytecode.currentOffset());
            int condSize = condition.toBytecode(bytecode);
            bytecode.write(Bytecode.jump(Bytecode.Instruction.ifne, -condSize - bodySize));
            bytecode.resolveBreaks(bytecode.currentOffset());
        }
        if(type == StmtType.forN_statement){
            forInitializerStmt.toBytecode(bytecode);
            int initOffset = bytecode.currentOffset();

            Bytecode tmp = new Bytecode();
            int bodySize = body.toBytecode(tmp);

            int postSize = 0;
            for (ExprNode post : forPostExpr) {
                postSize+=post.toBytecode(tmp);
            }
            bytecode.write(Bytecode.jump(Bytecode.Instruction._goto, 3 + bodySize + postSize));
            bytecode.write(tmp);
            int condSize = condition.toBytecode(bytecode);

            bytecode.write(Bytecode.jump(Bytecode.Instruction.ifne, -bodySize - postSize - condSize));
            bytecode.resolveContinues(initOffset + 3 + bodySize);
            bytecode.resolveBreaks(bytecode.currentOffset());
        }
        if(type == StmtType.switch_statement){

        }
    
        int written = bytecode.currentOffset() - startOffset;
        if(written == 0 && !expectZero){
            throw new IllegalStateException();
        }
        outBytecode.write(bytecode);
        return written;
    }
}
