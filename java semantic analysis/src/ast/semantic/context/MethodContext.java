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
        if(scopeLocals.containsKey(name)){
            return scopeLocals.get(name);
        }
        if(outsideLocals.containsKey(name)){
            return outsideLocals.get(name);
        }
        if(methodRecord.parameters.stream().anyMatch(p -> p.name().equals(name))){
            return methodRecord.parameters.stream().filter(p -> p.name().equals(name)).findFirst().orElse(null); //FIXME ? мб сделать как то получше?
        }
        return super.lookup(name);
    }
    
    public boolean isSytheticGetterOrSetter(){
        return methodRecord.isSyntheticSetter() || methodRecord.isSyntheticGetter();
    }

    public MethodContext skippableChildScope(){
        MethodContext methodContext = this.childScope();
        methodContext.isSkippable = true;
        return methodContext;
    }

    public MethodContext childScope(){
        MethodContext methodContext = new MethodContext(methodRecord);
        methodContext.outsideLocals = new HashMap<>(this.outsideLocals);
        methodContext.outsideLocals.putAll(this.scopeLocals);
        methodContext.isSkippable = this.isSkippable;
        return methodContext;
    }

    public Map<String, LocalVarRecord> outsideLocals = new HashMap<>();
    public Map<String, LocalVarRecord> scopeLocals = new HashMap<>();

    public void addLocalToScope(LocalVarRecord var){
        if(scopeLocals.containsKey(var.name)){
            printError("The name '" + var.name + "' is already defined.", -1); // TODO номер строки
        }
        methodRecord.addLocalVar(var);
        scopeLocals.put(var.name, var);
    }
}
