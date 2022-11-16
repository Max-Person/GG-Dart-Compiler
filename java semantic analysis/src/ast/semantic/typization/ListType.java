package ast.semantic.typization;

public class ListType extends VariableType{
    public ValueType valueType;
    
    ListType(ValueType valueType) {
        this.valueType = valueType;
    }
    
    @Override
    public String descriptor() {
        return "[" + valueType.descriptor();
    }
}
