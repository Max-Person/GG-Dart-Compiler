package ast.semantic;

import ast.FunctionDefinitionNode;
import ast.SignatureNode;
import ast.StmtNode;
import ast.semantic.typization.FunctionType;

import java.util.HashMap;
import java.util.Map;

public class MethodRecord {
    public Map<String, Integer> locals = new HashMap<>();
    
    public SignatureNode signature;
    public StmtNode body;
    public FunctionType type;
    public ConstantRecord nameConst;
    public ConstantRecord descriptorConst;
    
    public MethodRecord(SignatureNode signature, StmtNode body, FunctionType type,  ConstantRecord nameConst,  ConstantRecord descriptorConst){
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
}
