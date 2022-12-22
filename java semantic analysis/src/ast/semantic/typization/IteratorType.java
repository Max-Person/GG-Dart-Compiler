package ast.semantic.typization;

import ast.semantic.ClassRecord;
import ast.semantic.RTLIteratorClassRecord;

import java.util.Objects;

public class IteratorType extends VariableType{
    public VariableType valueType;
    
    public IteratorType(VariableType valueType) {
        this(valueType, false);
    }
    
    public IteratorType(VariableType valueType, boolean isNullable){
        this.valueType = valueType;
        this.isNullable = isNullable;
    }
    
    @Override
    public void finalyze() {
        valueType.finalyze();
    }
    
    @Override
    public String descriptor() {
        return associatedClass().descriptor();
    }
    
    @Override
    public boolean isAssignableFrom(VariableType o) {
        return super.isAssignableFrom(o) || (o instanceof IteratorType && ((IteratorType) o).valueType == null);
    }
    
    @Override
    public ClassRecord associatedClass() {
        return new RTLIteratorClassRecord(null, valueType != null ? valueType : VariableType._Object()); //FIXED ? null
    }
    
    @Override
    public String toString() {
        return "Iterator<" + valueType + ">" + (isNullable? "?" : "");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IteratorType)) return false;
        if (!super.equals(o)) return false;
        IteratorType iteratorType = (IteratorType) o;
        return valueType.equals(iteratorType.valueType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), valueType);
    }
}
