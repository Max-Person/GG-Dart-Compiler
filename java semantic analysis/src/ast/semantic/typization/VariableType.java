package ast.semantic.typization;

import java.util.Map;
import java.util.Objects;

import ast.TypeNode;
import ast.semantic.ClassRecord;

import static ast.semantic.SemanticCrawler.printError;

public abstract class VariableType extends ValueType{
    public boolean isNullable;

    public static VariableType from(Map<String, ClassRecord> classTable, TypeNode typeNode){
        VariableType result = null;
        switch (typeNode.type){
            case _void -> {
                return StandartType._void();
            }
            case _named -> {
                if(StandartType.isStandartName((typeNode.name.stringVal))){
                    result = (VariableType) StandartType.standartTypes.get(typeNode.name.stringVal).clone();
                }
                else if(classTable.containsKey(typeNode.name.stringVal)){
                    result = new ClassType(classTable.get(typeNode.name.stringVal));
                }
                else {
                    printError("Undefined class '" + typeNode.name.stringVal+"'", typeNode.lineNum);
                    return null;
                }
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
    
    @Override
    public String toString() {
        return descriptor() + (isNullable? "?" : ""); //TODO расписать нормально для всех типов
    }
    
    public boolean isAssignableFrom(VariableType o){
        return (this.descriptor().equals(o.descriptor()) || (this.descriptor().equals("D") && o.descriptor().equals("I")) || o.descriptor().equals("Null")) &&
                (this.isNullable || !o.isNullable);
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
