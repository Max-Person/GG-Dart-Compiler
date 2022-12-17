package ast.semantic.typization;

import ast.semantic.ClassRecord;

import java.util.Objects;

//TODO ? стандартные джава-массивы используются только в мейне, так что особого функционала здесь нет
public class JavaArrayType extends VariableType{
    public VariableType valueType;
    
    public JavaArrayType(VariableType valueType){
        this.valueType = valueType;
        this.isNullable = false;
    }
    
    @Override
    public void finalyze() {
        valueType.finalyze();
    }
    
    @Override
    public String descriptor() {
        return "[" + valueType.descriptor();
    }
    
    @Override
    public ClassRecord associatedClass() {
        return null;
    }
    
    @Override
    public boolean isSubtypeOf(VariableType other){
        return other instanceof JavaArrayType && this.valueType.isSubtypeOf(((JavaArrayType) other).valueType);
    }
    
    @Override
    public String toString() {
        return valueType + "[]";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListType)) return false;
        if (!super.equals(o)) return false;
        ListType listType = (ListType) o;
        return valueType.equals(listType.valueType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), valueType);
    }
}
