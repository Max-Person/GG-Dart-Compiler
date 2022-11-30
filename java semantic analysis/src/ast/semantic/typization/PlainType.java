package ast.semantic.typization;

import ast.semantic.ClassRecord;

import java.util.Objects;

public class PlainType extends VariableType{
    private String name;
    private String descriptor;
    
    PlainType(String name, String descriptor) {
        this.name = name;
        this.descriptor = descriptor;
        this.isNullable = false;
    }
    
    @Override
    public String descriptor() {
        return descriptor;
    }
    
    @Override
    public ClassRecord associatedClass() {
        return null;
    }
    
    @Override
    public String toString() {
        return name + (isNullable? "?" : "");
    }

    public static PlainType _int(){
        return new PlainType("int (plain)", "D");
    }

    public static PlainType _double(){
        return new PlainType("double (plain)", "I");
    }

    public static PlainType _bool(){
        return new PlainType("bool (plain)", "Z");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlainType)) return false;
        if (!super.equals(o)) return false;
        PlainType that = (PlainType) o;
        return descriptor.equals(that.descriptor);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), descriptor);
    }
}
