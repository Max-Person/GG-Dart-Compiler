package ast.semantic.typization;

public abstract class ValueType implements Cloneable {
    public abstract String descriptor();
    
    public ValueType clone(){
        try {
            return (ValueType) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
