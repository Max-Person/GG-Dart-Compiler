package ast.semantic.context;

import ast.semantic.*;
import ast.semantic.typization.VariableType;

import java.util.Map;

public interface Context {

    Map<String, ClassRecord> classTable();
    boolean isStatic();
    
    NamedRecord lookup(String name);
    default ClassRecord lookupClass(String name){
        NamedRecord found = lookup(name);
        return found instanceof ClassRecord ? (ClassRecord) found : null;
    }
    default MethodRecord lookupMethod(String name){
        NamedRecord found = lookup(name);
        return found instanceof MethodRecord ? (MethodRecord) found : null;
    }
    default FieldRecord lookupField(String name){
        NamedRecord found = lookup(name);
        return found instanceof FieldRecord ? (FieldRecord) found : null;
    }
    default VariableRecord lookupVariable(String name){
        NamedRecord found = lookup(name);
        return found instanceof VariableRecord ? (VariableRecord) found : null;
    }
    VariableType thisType();
    ClassRecord currentClass();
}
