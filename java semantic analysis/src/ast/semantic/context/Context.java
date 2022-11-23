package ast.semantic.context;

import ast.semantic.ClassRecord;
import ast.semantic.FieldRecord;
import ast.semantic.MethodRecord;
import ast.semantic.NamedRecord;
import ast.semantic.typization.VariableType;

import java.util.Map;

public abstract class Context {
    public Map<String, ClassRecord> classTable;
    public Context(Map<String, ClassRecord> classTable){
        this.classTable = classTable;
    }
    public abstract boolean isStatic();
    
    public abstract NamedRecord lookup(String name);
    public ClassRecord lookupClass(String name){
        NamedRecord found = lookup(name);
        return found instanceof ClassRecord ? (ClassRecord) found : null;
    }
    public MethodRecord lookupMethod(String name){
        NamedRecord found = lookup(name);
        return found instanceof MethodRecord ? (MethodRecord) found : null;
    }
    public FieldRecord lookupField(String name){ //FIXME учесть локалки
        NamedRecord found = lookup(name);
        return found instanceof FieldRecord ? (FieldRecord) found : null;
    }
    public abstract VariableType thisType();
    public abstract ClassRecord currentClass();
}
