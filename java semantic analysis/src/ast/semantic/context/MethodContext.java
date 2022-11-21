package ast.semantic.context;

import ast.semantic.ClassRecord;
import ast.semantic.NamedRecord;
import ast.semantic.typization.VariableType;

import java.util.Map;

public class MethodContext extends Context{
    public MethodContext(Map<String, ClassRecord> classTable) {
        super(classTable);
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public NamedRecord lookup(String name) {
        return null;
    }

    @Override
    public VariableType thisType() {
        return null;
    }
}
