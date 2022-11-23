package ast.semantic.context;

import ast.semantic.ClassRecord;
import ast.semantic.NamedRecord;
import ast.semantic.typization.ClassType;
import ast.semantic.typization.VariableType;

import java.util.Map;

public class ClassInitContext extends GlobalContext{
    public ClassRecord classRecord;
    private boolean isStatic;
    
    public ClassInitContext(ClassRecord classRecord, boolean isStatic) {
        super(classRecord.containerClassTable);
        this.classRecord = classRecord;
        this.isStatic = isStatic;
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
        return super.lookup(name);
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
