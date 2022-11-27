package ast.semantic.context;

import ast.semantic.MethodRecord;
import ast.semantic.NamedRecord;

public class MethodContext extends ClassInitContext{
    public MethodRecord methodRecord;

    public boolean isSkippable = false;
    
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

    public MethodContext asSkippableContext(){
        MethodContext methodContext = new MethodContext(methodRecord);
        methodContext.isSkippable = true;
        return methodContext;
    }
}
