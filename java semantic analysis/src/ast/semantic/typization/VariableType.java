package ast.semantic.typization;

import java.util.Map;
import java.util.Objects;

import ast.TypeNode;
import ast.semantic.ClassRecord;
import ast.semantic.RTLClassRecord;
import ast.semantic.RTLIteratorClassRecord;
import ast.semantic.RTLListClassRecord;

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
                    if(isStandartName(typeNode.name.stringVal)){
                        result = standartType(typeNode.name.stringVal);
                    }
                    else{
                        printError("Undefined class '" + typeNode.name.stringVal + "'", typeNode.lineNum);
                        return null;
                    }
                }
                else
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
    
    public static VariableType from(ClassRecord clazz){
        if(clazz instanceof RTLListClassRecord){
            return new ListType(((RTLListClassRecord) clazz).valueType);
        }
        if(clazz instanceof RTLIteratorClassRecord){
            return new IteratorType(((RTLIteratorClassRecord) clazz).valueType);
        }
        return new ClassType(clazz);
    }
    
    public static boolean isStandartName(String name){
        return standartType(name) != null;
    }
    
    public static VariableType standartType(String name){
        if(name.equals("Null")) return new PlainType("Null", "Ljava/lang/Object;");
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
        return ((this.associatedClass() != null ? o.isSubtypeOf(this) : this.descriptor().equals(o.descriptor())) || o.equals(VariableType._null())) &&
                (this.isNullable || !o.isNullable);
    }
    
    public boolean isExactlyAssignableFrom(VariableType o){
        return (this.descriptor().equals(o.descriptor()) &&
                (this.isNullable || !o.isNullable));
    }
    
    public static VariableType supertype(VariableType a, VariableType b){
        if(a.isAssignableFrom(b))
            return a;
        if(b.isAssignableFrom(a))
            return b;
        
        if(a.equals(VariableType._null())){
            VariableType res = b.clone();
            res.isNullable = true;
            return res;
        }
        if(b.equals(VariableType._null())){
            VariableType res = a.clone();
            res.isNullable = true;
            return res;
        }
        if(a.associatedClass() != null && b.associatedClass() != null){
            ClassRecord superclass = ClassRecord.lastCommonSuper(a.associatedClass(), b.associatedClass());
            return  VariableType.from(superclass);
        }
        return null;
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
