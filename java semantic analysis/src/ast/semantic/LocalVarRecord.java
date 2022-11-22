package ast.semantic;

import ast.VariableDeclarationNode;
import ast.semantic.context.MethodContext;
import ast.semantic.typization.VariableType;

import java.util.ArrayList;

public class LocalVarRecord extends VariableRecord{
    MethodRecord containerMethod;
    int number;
    
    public LocalVarRecord(MethodRecord containerMethod, VariableDeclarationNode declaration, VariableType varType) {
        super(declaration, varType);
        this.containerMethod = containerMethod;
    }
    
    public VariableType inferType(){
        if(this.varType == null){
            this.varType = this.declaration.value.annotateTypes(new ArrayList<>(), new MethodContext(containerMethod));
        }
        return this.varType;
    }
}
