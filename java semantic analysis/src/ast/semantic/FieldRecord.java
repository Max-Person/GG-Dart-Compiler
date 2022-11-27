package ast.semantic;

import ast.*;
import ast.semantic.context.ClassInitContext;
import ast.semantic.context.GlobalContext;
import ast.semantic.typization.VariableType;

import java.util.ArrayList;
import java.util.List;

import static ast.semantic.SemanticCrawler.printError;

public class FieldRecord extends VariableRecord{
    public ClassRecord containerClass;
    
    public ConstantRecord descriptorConst;
    public ConstantRecord nameConst;
    
    public FieldRecord(ClassRecord containerClass, VariableDeclarationNode declaration){
        super(containerClass.containerClassTable, declaration);
        
        this.containerClass = containerClass;
        if(varType != null){
            this.containerClass.methods.put(associatedGetter().name(), associatedGetter());
            this.containerClass.methods.put(associatedSetter().name(), associatedSetter());
        }
    }
    
    public VariableType inferType(List<FieldRecord> dependencyStack){
        if(this.varType == null){
            if(dependencyStack.contains(this)){
                printError("The type of '"+this.name()+"' can't be inferred because it depends on itself through the dependency cycle.", initValue.lineNum); //TODO Вывести цикл зависимости
            }
            dependencyStack.add(this);
            this.varType = this.initValue.annotateTypes(dependencyStack,
                    containerClass.isGlobal() ?
                    new GlobalContext(containerClass.containerClassTable) :
                    new ClassInitContext(containerClass, this.isStatic())
            );
            dependencyStack.remove(this); //TODO убедиться что депенденси стак работает как стак и ниче не портит...
            
            this.containerClass.methods.put(associatedGetter().name(), associatedGetter());
            this.containerClass.methods.put(associatedSetter().name(), associatedSetter());
        }
        return this.varType;
    }
    
    public StmtNode initStmt(){
        if(this.initValue == null)
            return null;
        
        ExprNode expr = new ExprNode(ExprType.assign);
        expr.operand2 = initValue;
        if(this.isStatic){
            expr.operand = new ExprNode(ExprType.identifier);
            expr.operand.identifierAccess = new IdentifierNode(this.name);
        }
        else {
            expr.operand = new ExprNode(ExprType.fieldAccess);
            expr.operand.operand = new ExprNode(ExprType.this_pr);
            expr.operand.identifierAccess = new IdentifierNode(this.name);
        }
        
        StmtNode init = new StmtNode(StmtType.expr_statement);
        init.expr = expr;
        return init;
    }
    
    private MethodRecord getter = null;
    public MethodRecord associatedGetter(){
        if(getter == null){
            if(this.varType == null){
                throw new IllegalStateException();
            }
            
            StmtNode getterBody = new StmtNode(StmtType.return_statement);
            getterBody.returnExpr = new ExprNode(ExprType.identifier);
            getterBody.returnExpr.identifierAccess = new IdentifierNode(name);
    
            this.getter = new MethodRecord(this.containerClass, this.varType.clone(), "get!"+this.name(), new ArrayList<>(), getterBody);
        }
        return getter;
    }
    
    private MethodRecord setter = null;
    public MethodRecord associatedSetter(){
        if(setter == null){
            if(this.varType == null){
                throw new IllegalStateException();
            }
    
            ParameterRecord setterParameter = new ParameterRecord(null, null, this.varType.clone(), this.name, false);
    
            StmtNode setterBody = new StmtNode(StmtType.return_statement);
            ExprNode expr = new ExprNode(ExprType.assign);
            expr.operand = new ExprNode(ExprType.fieldAccess);
            expr.operand.operand = new ExprNode(ExprType.this_pr);
            expr.operand.identifierAccess = new IdentifierNode(this.name);
            expr.operand2 = new ExprNode(ExprType.identifier);
            expr.operand2.identifierAccess = new IdentifierNode(this.name);
            setterBody.returnExpr = expr;
    
            this.setter = new MethodRecord(this.containerClass, this.varType.clone(), "set!"+this.name(), List.of(setterParameter), setterBody);
        }
        return setter;
    }
    
    public boolean isValidOverrideOf(FieldRecord other){
        return this.name.equals(other.name) && other.varType.isAssignableFrom(this.varType);
    }
    
    public void finalizeType(){
        this.varType.finalyze();
        this.descriptorConst = containerClass.addConstant(ConstantRecord.newUtf8(this.varType.descriptor()));
        this.nameConst = containerClass.addConstant(ConstantRecord.newUtf8(this.name()));
    }
    
    public void copyTo(ClassRecord classRecord) {
        FieldRecord copy = null; //FIXME? Плохой клон может повлиять на что-то
        try {
            copy = (FieldRecord) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        copy.containerClass = classRecord;
        classRecord.fields.put(copy.name(), copy);
    }
}
