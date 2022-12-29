package ast.semantic;

import ast.*;
import ast.semantic.context.MethodContext;
import ast.semantic.typization.VariableType;

import static ast.semantic.SemanticCrawler.printError;

public class LocalVarRecord extends VariableRecord{
    public MethodRecord containerMethod;
    public int number;
    
    public LocalVarRecord(MethodRecord containerMethod, VariableDeclarationNode declaration) {
        super(containerMethod.containerClass.containerClassTable, declaration);
        this.containerMethod = containerMethod;
    }
    
    public LocalVarRecord(MethodRecord containerMethod, DeclaratorNode declarator, VariableType varType, String name) {
        super(declarator, varType, name);
        this.containerMethod = containerMethod;
    }
    
    public VariableType inferType(MethodContext context){
        if(this.varType == null){
            this.initValue.annotateTypes(context);
            this.initValue.assertUsable(context);
            this.initValue.makeAssignableTo(VariableType._Object(), context);
            this.varType = initValue.annotatedType;
        }
        return this.varType;
    }

    public void resolveType(MethodContext context){
        if (this.varType == null){
            inferType(context);
        } else if (initValue != null){
            this.initValue.annotateTypes(context);
            if (!this.initValue.makeAssignableTo(this.varType, context)){
                printError("A value of type '" + initValue.annotatedType + "' can't be assigned to a variable of type '" + varType + "'.", initValue.lineNum);
            }
        }
    }
    
    public ExprNode toExpr(){
        if (initValue != null){
            ExprNode assign = new ExprNode(ExprType.assign, lineNum);
            assign.operand = new ExprNode(ExprType.identifier, lineNum);
            assign.operand.identifierAccess = new IdentifierNode(this.name);
            assign.operand2 = this.initValue;
            return assign;
        }
        return null;
    }
}
