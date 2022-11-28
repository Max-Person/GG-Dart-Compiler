package ast.semantic.context;

import ast.semantic.ClassRecord;
import ast.semantic.FieldRecord;

import java.util.ArrayList;
import java.util.List;

public class ClassInitContext extends ClassContext{
    public List<FieldRecord> dependencyStack = new ArrayList<>();

    public ClassInitContext(ClassRecord classRecord, boolean isStatic) {
        super(classRecord, isStatic);
    }

    //Мб может ввести в заблуждение что оно сразу не помещается в стак?
    public ClassInitContext(FieldRecord field) {
        super(field.containerClass, field.isStatic());
    }

    public ClassInitContext dependantContext(FieldRecord field){
        ClassInitContext copy = new ClassInitContext(field);
        copy.dependencyStack = new ArrayList<>(this.dependencyStack);
        copy.dependencyStack.add(field);
        return copy;
    }
}
