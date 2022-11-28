package ast.semantic.context;

import ast.semantic.LocalVarRecord;
import ast.semantic.MethodRecord;
import ast.semantic.NamedRecord;

import java.util.HashMap;
import java.util.Map;

import static ast.semantic.SemanticCrawler.printError;

public class MethodContext extends ClassContext{
    public MethodRecord methodRecord;

    public boolean isSkippable = false;
    
    public MethodContext(MethodRecord method) {
        super(method.containerClass, method.isStatic());
        methodRecord = method;
    }

    @Override
    public NamedRecord lookup(String name) {
        if(localsScope.containsKey(name)){
            return localsScope.get(name);
        }
        if(methodRecord.parameters.stream().anyMatch(p -> p.name().equals(name))){
            return methodRecord.parameters.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null); //FIXME ? мб сделать как то получше?
        }
        return super.lookup(name);
    }
    
    public boolean isSytheticGetterOrSetter(){
        return this.methodRecord.name().contains("!");
    }

    public MethodContext skippableChildScope(){
        MethodContext methodContext = this.childScope();
        methodContext.isSkippable = true;
        return methodContext;
    }

    public MethodContext childScope(){
        MethodContext methodContext = new MethodContext(methodRecord);
        methodContext.localsScope = new HashMap<>(this.localsScope);
        methodContext.isSkippable = this.isSkippable;
        return methodContext;
    }

    public Map<String, LocalVarRecord> localsScope = new HashMap<>();

    public void addLocalToScope(LocalVarRecord var){
        if(localsScope.containsKey(var.name)){
            printError("The name '" + var.name + "' is already defined.", -1); // TODO номер строки
        }
        methodRecord.addLocalVar(var);
        localsScope.put(var.name, var);
    }
}
