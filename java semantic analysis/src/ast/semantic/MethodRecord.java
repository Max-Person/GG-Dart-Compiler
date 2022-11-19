package ast.semantic;

import ast.SignatureNode;
import ast.StmtNode;
import ast.semantic.typization.FunctionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodRecord implements NamedRecord{
    public ClassRecord containerClass;
    
    public Map<String, Integer> locals = new HashMap<>();
    
    public SignatureNode signature;
    public StmtNode body;
    public FunctionType type;
    public ConstantRecord nameConst;
    public ConstantRecord descriptorConst;
    
    public MethodRecord(ClassRecord containerClass, SignatureNode signature, StmtNode body, FunctionType type,  ConstantRecord nameConst,  ConstantRecord descriptorConst){
        this.containerClass = containerClass;
        this.signature = signature;
        this.body = body;
        this.type = type;
        this.nameConst = nameConst;
        this.descriptorConst = descriptorConst;
    }
    
    public String name(){
        if(signature.isConstruct){
            return signature.isNamed ? "<init>" : "<init>"; //TODO
        }
        else return signature.name.stringVal;
    }
    public boolean isConstruct(){
        return signature.isConstruct;
    }
    public boolean isConst(){
        if(!signature.isConstruct)
            throw new IllegalStateException();
        return signature.isConst;
    }
    public boolean isStatic(){
        if(signature.isConstruct)
            throw new IllegalStateException();
        return signature.isStatic;
    }
    
    public void inferType(List<FieldRecord> dependencyStack){
        if(this.type == null){
            this.type = FunctionType.from(containerClass.containerClassTable, signature, containerClass, new ArrayList<>());
            this.descriptorConst = containerClass.addConstant(ConstantRecord.newUtf8(this.type.descriptor()));
        }
    }
}
