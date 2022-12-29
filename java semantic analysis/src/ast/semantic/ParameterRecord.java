package ast.semantic;

import ast.*;
import ast.semantic.context.ClassInitContext;
import ast.semantic.typization.VariableType;

import static ast.semantic.SemanticCrawler.printError;

public class ParameterRecord extends LocalVarRecord{
    public boolean isField;
    
    public ParameterRecord(MethodRecord containerMethod, DeclaratorNode declarator, VariableType varType, String name, boolean isField) {
        super(containerMethod, declarator, varType, name);
        if(varType == null && !isField){
            throw new IllegalArgumentException();
        }
        this.isField = isField;
    }
    
    public ParameterRecord(MethodRecord containerMethod, FormalParameterNode parameter) {
        this(containerMethod,
                parameter.isField ? null : parameter.paramDecl.declarator,
                parameter.isField ? null : VariableType.from(containerMethod.containerClass.containerClassTable, parameter.paramDecl.declarator.valueType),
                parameter.name(),
                parameter.isField);
        
        if(this.isField){
            if(!Utils.filterByValue(containerMethod.containerClass.fields, f -> !f.isStatic).containsKey(this.name)){ //Не "nonStaticFields", потому что нельзя использовать унаследованные поля
                printError("Undefined field name '"+ this.name +"'.", parameter.lineNum);
            }
        }
        
        this.lineNum = parameter.lineNum;
    }
    
    public void normalize(){
        if(!isField){
            return;
        }
        if(this.varType == null){
            throw new IllegalStateException("normalize() was called before inferType() on a Field ParameterRecord");
        }
        ExprNode expr = new ExprNode(ExprType.assign, lineNum);
        
        expr.operand = new ExprNode(ExprType.fieldAccess, lineNum);
        expr.operand.operand = new ExprNode(ExprType.this_pr, lineNum);
        expr.operand.identifierAccess = new IdentifierNode(name);
        
        expr.operand2 = new ExprNode(ExprType.identifier, lineNum);
        expr.operand2.identifierAccess = new IdentifierNode(name);
    
        StmtNode initStmt = new StmtNode(StmtType.expr_statement, lineNum);
        initStmt.expr = expr;
        this.containerMethod.body.blockStmts.add(0, initStmt);
        this.isField = false;
    }
    
    public VariableType inferType(){
        throw new IllegalStateException();
    }
    
    public VariableType inferType(ClassInitContext context){
        if(this.varType == null){
            if(!this.isField){
                throw new IllegalStateException();
            }
            
            FieldRecord field = containerMethod.containerClass.nonStaticFields().get(this.name);
            this.varType = field.inferType(context);
        }
        return this.varType;
    }
    
}
