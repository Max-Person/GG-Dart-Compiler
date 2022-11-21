package ast.semantic.typization;

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
    public String descriptor() {
        return "[" + valueType.descriptor();
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