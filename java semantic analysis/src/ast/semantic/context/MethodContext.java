package ast.semantic.context;

import ast.semantic.ClassRecord;
import ast.semantic.MethodRecord;
import ast.semantic.NamedRecord;
import ast.semantic.typization.VariableType;

import java.util.Map;

public class MethodContext extends ClassInitContext{
    MethodRecord methodRecord;
    
    public MethodContext(MethodRecord method) {
        super(method.containerClass, method.isStatic());
        methodRecord = method;
    }

    @Override
    public NamedRecord lookup(String name) {
        if(methodRecord.locals.containsKey(name)){
            return methodRecord.locals.get(name);
        }
        return super.lookup(name);
    }
    
    public boolean isSytheticGetterOrSetter(){
        return this.methodRecord.name().contains("!");
    }
}
