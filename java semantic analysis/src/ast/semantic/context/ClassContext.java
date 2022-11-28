package ast.semantic.context;

import ast.semantic.ClassRecord;
import ast.semantic.NamedRecord;
import ast.semantic.typization.ClassType;
import ast.semantic.typization.VariableType;

import java.util.Map;

public abstract class ClassContext implements Context{
    public ClassRecord classRecord;
    private boolean isStatic;

    public ClassContext(ClassRecord classRecord, boolean isStatic) {
        this.classRecord = classRecord;
        this.isStatic = isStatic;
    }

    @Override
    public Map<String, ClassRecord> classTable() {
        return this.classRecord.containerClassTable;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public NamedRecord lookup(String name) {
        if(isStatic){
            if(classRecord.staticFields().containsKey(name)){
                return classRecord.staticFields().get(name);
            }
            else if(classRecord.staticMethods().containsKey(name)){
                return classRecord.staticMethods().get(name);
            }
        }
        else {
            if(classRecord.fields.containsKey(name)){
                return classRecord.fields.get(name);
            }
            else if(classRecord.methods.containsKey(name)){
                return classRecord.methods.get(name);
            }
        }

        ClassRecord global = classRecord.containerClassTable.get(ClassRecord.globalName);

        if (classRecord.containerClassTable.containsKey(name)){
            return classRecord.containerClassTable.get(name);
        }
        else if(global.methods.containsKey(name)){
            global.methods.get(name);
        }
        else if(global.fields.containsKey(name)){
            global.fields.get(name);
        }

        return null;
    }

    @Override
    public VariableType thisType() {
        return isStatic? null : new ClassType(classRecord);
    }

    @Override
    public ClassRecord currentClass() {
        return classRecord;
    }
}
