package ast.semantic.context;

import ast.semantic.ClassRecord;
import ast.semantic.NamedRecord;
import ast.semantic.typization.VariableType;

import java.util.Map;

public class GlobalContext extends Context{
    public GlobalContext(Map<String, ClassRecord> classTable) {
        super(classTable);
    }
    
    @Override
    public boolean isStatic() {
        return true;
    }
    
    @Override
    public NamedRecord lookup(String name) {
        if (classTable.containsKey(name)){
            return classTable.get(name);
        }
        else if(classTable.get(ClassRecord.globalName).methods.containsKey(name)){
            classTable.get(ClassRecord.globalName).methods.get(name);
        }
        else if(classTable.get(ClassRecord.globalName).fields.containsKey(name)){
            classTable.get(ClassRecord.globalName).fields.get(name);
        }
        return null;
    }
    
    @Override
    public VariableType thisType() {
        return null;
    }
    
    @Override
    public ClassRecord currentClass() {
        return classTable.get(ClassRecord.globalName);
    }
}
