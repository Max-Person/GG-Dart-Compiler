package ast.semantic;

import ast.TypeNode;
import ast.VariableDeclarationNode;

public class FieldRecord {
    public VariableDeclarationNode declaration;
    
    public FieldRecord(VariableDeclarationNode declaration){
        if(declaration == null)
            throw new IllegalArgumentException();
        
        this.declaration = declaration;
        this.descriptor = null; //TODO
    }
    
    public ConstantRecord descriptor;
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
    public TypeNode type(){return declaration.declarator.valueType;}
}
