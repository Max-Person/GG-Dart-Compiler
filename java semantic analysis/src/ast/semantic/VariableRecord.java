package ast.semantic;

import ast.VariableDeclarationNode;
import ast.semantic.typization.VariableType;

public abstract class VariableRecord implements NamedRecord{
    public VariableDeclarationNode declaration;
    public VariableType varType;
    
    public VariableRecord(VariableDeclarationNode declaration, VariableType varType) {
        this.declaration = declaration;
        this.varType = varType;
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
