package ast.semantic.typization;

import ast.semantic.ClassRecord;
import ast.semantic.RTLClassRecord;
import ast.semantic.RTLListClassRecord;

import java.util.Objects;

public class ListType extends VariableType{
    public VariableType valueType;
    
    public ListType(VariableType valueType) {
        this(valueType, false);
    }
    
    public ListType(VariableType valueType, boolean isNullable){
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
        return super.isAssignableFrom(o) || (o instanceof ListType && ((ListType) o).valueType == null);
    }
    
    @Override
    public ClassRecord associatedClass() {
        return new RTLListClassRecord(null, valueType != null ? valueType : VariableType._Object()); //FIXED ? null
    }
    
    @Override
    public String toString() {
        return "List<" + valueType + ">" + (isNullable? "?" : "");
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
