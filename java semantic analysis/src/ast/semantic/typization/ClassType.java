package ast.semantic.typization;

import ast.semantic.ClassRecord;

public class ClassType extends VariableType{
    public ClassRecord clazz;
    
    public ClassType(ClassRecord clazz) {
        this.clazz = clazz;
    }
    
    @Override
    public String descriptor() {
        return "Lggdart/gen/" + clazz.name() + ";";
    }
}
