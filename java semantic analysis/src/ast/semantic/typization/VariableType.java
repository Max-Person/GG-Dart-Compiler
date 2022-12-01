package ast.semantic.typization;

import java.util.Map;
import java.util.Objects;

import ast.TypeNode;
import ast.semantic.ClassRecord;
import ast.semantic.RTLClassRecord;

import static ast.semantic.SemanticCrawler.printError;

public abstract class VariableType implements Cloneable {
    public boolean isNullable;

    public static VariableType from(Map<String, ClassRecord> classTable, TypeNode typeNode){
        VariableType result = null;
        switch (typeNode.type){
            case _void -> {
                return _void();
            }
            case _named -> {
                ClassRecord clazz = ClassRecord.lookup(classTable, typeNode.name.stringVal);
                if(clazz == null) {
                    printError("Undefined class '" + typeNode.name.stringVal + "'", typeNode.lineNum);
                    return null;
                }
                result = new ClassType(clazz.associatedInterface != null ? clazz.associatedInterface : clazz);
            }
            case _list -> {
                VariableType el = VariableType.from(classTable, typeNode.listValueType);
                if(el != null){
                    result = new ListType(el);
                }
                else return null;
            }
        }
        result.isNullable = typeNode.isNullable;
        return result;
    }
    
    public static boolean isStandartName(String name){
        return standartType(name) != null;
    }
    
    public static VariableType standartType(String name){
        if(name.equals("Null")) return new PlainType("Null", "Null");
        if(name.equals("void")) return new PlainType("void","V");
        if(name.equals("int")) return new ClassType(RTLClassRecord._integer);
        if(name.equals("double")) return new ClassType(RTLClassRecord._double);
        if(name.equals("bool")) return new ClassType(RTLClassRecord._bool);
        if(name.equals("String")) return new ClassType(RTLClassRecord.string);
        if(name.equals("Object")) return new ClassType(RTLClassRecord.object);
        return null;
    }
    
    public static VariableType _null(){return standartType("Null");}
    public static VariableType _void(){return standartType("void");}
    public static VariableType _int(){return standartType("int");}
    public static VariableType _double(){return standartType("double");}
    public static VariableType _bool(){return standartType("bool");}
    public static VariableType _String(){return standartType("String");}
    public static VariableType _Object(){return standartType("Object");}
    
    public abstract String descriptor();
    
    public abstract ClassRecord associatedClass();
    
    public boolean isSubtypeOf(VariableType other){
        return this.associatedClass() != null && this.associatedClass().isSubTypeOf(other.associatedClass());
    }
    
    public boolean isAssignableFrom(VariableType o){
        return (o.isSubtypeOf(this) || this.descriptor().equals(o.descriptor()) || o.descriptor().equals("Null")) &&
                (this.isNullable || !o.isNullable);
    }
    
    public void finalyze(){}
    
    public VariableType clone(){
        try {
            return (VariableType) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariableType)) return false;
        VariableType that = (VariableType) o;
        return isNullable == that.isNullable;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(isNullable);
    }
}
