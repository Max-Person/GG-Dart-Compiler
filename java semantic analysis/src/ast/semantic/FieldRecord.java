package ast.semantic;

import ast.TypeNode;
import ast.VariableDeclarationNode;
import ast.semantic.typization.VariableType;

public class FieldRecord {
    public VariableDeclarationNode declaration;
    public ConstantRecord descriptorConst;
    public ConstantRecord nameConst;
    public VariableType varType;
    
    public FieldRecord(VariableDeclarationNode declaration, VariableType varType, ConstantRecord nameConst,  ConstantRecord descriptorConst){
        if(declaration == null)
            throw new IllegalArgumentException();
        
        this.declaration = declaration;
        this.varType = varType;
        this.nameConst = nameConst;
        this.descriptorConst = descriptorConst;
    }

    public String name(){
        return declaration.identifier.stringVal;
    }
    public boolean isLate(){
        return declaration.declarator.isLate;
    }
    public boolean isStatic(){
        return declaration.declarator.isStatic;
    }
    public boolean isConst(){
        return declaration.declarator.isConst;
    }
    public boolean isFinal(){
        return declaration.declarator.isFinal;
    }
    public VariableType type(){return varType;}
}
