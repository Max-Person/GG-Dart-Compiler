package ast.semantic;

import ast.DeclaratorNode;
import ast.VariableDeclarationNode;
import ast.semantic.context.MethodContext;
import ast.semantic.typization.VariableType;

import java.util.ArrayList;

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
    
    public VariableType inferType(){
        if(this.varType == null){
            this.varType = this.initValue.annotateTypes(new ArrayList<>(), new MethodContext(containerMethod));
        }
        return this.varType;
    }
}
