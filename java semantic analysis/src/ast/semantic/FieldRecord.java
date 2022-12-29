package ast.semantic;

import ast.*;
import ast.semantic.constants.UTF8Constant;
import ast.semantic.context.ClassInitContext;
import ast.semantic.context.Context;
import ast.semantic.context.MethodContext;
import ast.semantic.typization.VariableType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    public FieldRecord(ClassRecord containerClass, boolean isLate, boolean isStatic, boolean isConst, boolean isFinal, VariableType varType, String name) {
        super(isLate, isStatic, isConst, isFinal, varType, name);
        this.containerClass = containerClass;
        if(varType != null){
            this.containerClass.methods.put(associatedGetter().name(), associatedGetter());
            this.containerClass.methods.put(associatedSetter().name(), associatedSetter());
        }
    }

    public VariableType inferType(ClassInitContext context){
        if(this.varType == null){
            if(context.dependencyStack.contains(this)){
                printError("The type of '"+this.name()+"' can't be inferred because it depends on itself through the dependency cycle.", initValue.lineNum); //TODO Вывести цикл зависимости
            }
            this.initValue.annotateTypes(context.dependantContext(this));
            this.initValue.assertNotVoid();
            this.initValue.makeAssignableTo(VariableType._Object(), context);
            this.varType = initValue.annotatedType;
            
            this.containerClass.methods.put(associatedGetter().name(), associatedGetter());
            this.containerClass.methods.put(associatedSetter().name(), associatedSetter());
        }
        return this.varType;
    }
    
    public StmtNode initStmt(){
        if(this.initValue == null)
            return null;
        
        ExprNode expr = new ExprNode(ExprType.assign, initValue.lineNum);
        expr.operand2 = initValue;
        if(this.isStatic){
            expr.operand = new ExprNode(ExprType.identifier, initValue.lineNum);
            expr.operand.identifierAccess = new IdentifierNode(this.name);
        }
        else {
            expr.operand = new ExprNode(ExprType.fieldAccess, initValue.lineNum);
            expr.operand.operand = new ExprNode(ExprType.this_pr, initValue.lineNum);
            expr.operand.identifierAccess = new IdentifierNode(this.name);
        }
        
        StmtNode init = new StmtNode(StmtType.expr_statement, initValue.lineNum);
        init.expr = expr;
        return init;
    }
    
    private MethodRecord getter = null;
    public void addAssociatedGetter(MethodRecord getter){
        if(!(this.containerClass instanceof RTLClassRecord))
            throw new IllegalStateException();
        this.getter = getter;
    }
    public MethodRecord associatedGetter(){
        if(getter == null){
            if(this.varType == null){
                throw new IllegalStateException();
            }
            
            StmtNode getterBody = new StmtNode(StmtType.return_statement, lineNum);
            getterBody.returnExpr = new ExprNode(ExprType.identifier, lineNum);
            getterBody.returnExpr.identifierAccess = new IdentifierNode(name);
    
            this.getter = new MethodRecord(this.containerClass, this.isStatic, false, this.varType.clone(), MethodRecord.getterPrefix+this.name(), new ArrayList<>(), getterBody);
        }
        return getter;
    }
    
    private MethodRecord setter = null;
    public MethodRecord associatedSetter(){
        if(setter == null){
            if(this.varType == null){
                throw new IllegalStateException();
            }
            
            String paramName = "new";
            ParameterRecord setterParameter = new ParameterRecord(null, null, this.varType.clone(), paramName, false);
    
            StmtNode setterBody = new StmtNode(StmtType.return_statement, lineNum);
            ExprNode expr = new ExprNode(ExprType.assign, lineNum);
            expr.operand = new ExprNode(ExprType.identifier, lineNum);
            expr.operand.identifierAccess = new IdentifierNode(this.name);
            expr.operand2 = new ExprNode(ExprType.identifier, lineNum);
            expr.operand2.identifierAccess = new IdentifierNode(paramName);
            setterBody.returnExpr = expr;
    
            this.setter = new MethodRecord(this.containerClass, this.isStatic, false, this.varType.clone(), MethodRecord.setterPrefix+this.name(), List.of(setterParameter), setterBody);
        }
        return setter;
    }
    
    //FIXME?
    public boolean isAssignable(Context context){
        if(!(context instanceof MethodContext))
            return true;
        
        MethodRecord methodRecord = ((MethodContext) context).methodRecord;
        if(this.isFinal){
            return this.isStatic && methodRecord.containerClass.equals(this.containerClass) && methodRecord.name.equals("<clinit>") ||
                    !this.isStatic && methodRecord.containerClass.equals(this.containerClass) && (methodRecord.isSyntheticConstructor() || methodRecord.isConstruct);
        }
        return true;
    }
    
    public boolean isValidOverrideOf(FieldRecord other){
        return this.name.equals(other.name) && other.varType.isAssignableFrom(this.varType);
    }
    
    public void finalizeType(){
        this.varType.finalyze();
        this.descriptorConst = containerClass.addConstant(new UTF8Constant(this.varType.descriptor()));
        this.nameConst = containerClass.addConstant(new UTF8Constant(this.name()));
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

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.writeShort(0x0001  | // public
                0 | // private
                0 | // protected
                (this.isStatic   ? 0x0008 : 0) | // static
                0 |    //final TODO делаем ли мы файнал?
                0 |    // volatile
                0 |    // transient
                0 | //TODO проверить synthetic
                (this.containerClass.isEnum() && this.isStatic ? 0x4000 : 0) //enum
                );

        bytes.writeShort(nameConst.number); // name_index
        bytes.writeShort(descriptorConst.number); // descriptor_index

        bytes.writeShort(0); //  attributes_count
        return _bytes.toByteArray();
    }
}
