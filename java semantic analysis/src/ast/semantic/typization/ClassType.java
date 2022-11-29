package ast.semantic.typization;

import ast.semantic.ClassRecord;

import java.util.Objects;

public class ClassType extends VariableType{
    public ClassRecord clazz;
    
    public ClassType(ClassRecord clazz) {
        this(clazz, false);
    }
    
    public ClassType(ClassRecord clazz, boolean isNullable){
        this.clazz = clazz;
        this.isNullable = isNullable;
    }
    
    @Override
    public void finalyze() {
        if(this.clazz.associatedInterface!= null){
            this.clazz = this.clazz.associatedInterface;
        }
    }
    
    @Override
    public String descriptor() {
        return clazz.descriptor();
    }
    
    @Override
    public ClassRecord associatedClass() {
        return clazz;
    }
    
    @Override
    public String toString() {
        return clazz.name() + (isNullable? "?" : "");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassType)) return false;
        if (!super.equals(o)) return false;
        ClassType classType = (ClassType) o;
        return clazz.equals(classType.clazz);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), clazz);
    }
}
