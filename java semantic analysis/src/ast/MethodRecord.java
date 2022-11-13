package ast;

import java.util.HashMap;
import java.util.Map;

public class MethodRecord {
    public Map<String, Integer> locals = new HashMap<>();
    
    public FunctionDefinitionNode definition;
    
    public MethodRecord(FunctionDefinitionNode definition){
        this.definition = definition; //TODO
    }
    
    public ConstantRecord descriptor;
    public String name(){
        if(definition.signature.isConstruct){
            return definition.signature.isNamed ? "<init>" : "<init>"; //TODO
        }
        else return definition.signature.name.stringVal;
    }
    public boolean isConst(){
        if(!definition.signature.isConstruct)
            throw new IllegalStateException();
        return definition.signature.isConst;
    }
    public boolean isStatic(){
        if(definition.signature.isConstruct)
            throw new IllegalStateException();
        return definition.signature.isStatic;
    }
}
