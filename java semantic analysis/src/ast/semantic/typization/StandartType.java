package ast.semantic.typization;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StandartType extends VariableType{
    private String descriptor;
    
    StandartType(String descriptor) {
        this.descriptor = descriptor;
        this.isNullable = false;
    }
    
    @Override
    public String descriptor() {
        return descriptor;
    }
    
    static Map<String, StandartType> standartTypes = new HashMap<>();
    static {
        standartTypes.put("Null", new StandartType("Null"));
        standartTypes.put("void", new StandartType("V"));
        standartTypes.put("int", new StandartType("I"));
        standartTypes.put("double", new StandartType("D"));
        standartTypes.put("num", new StandartType("num"));
        standartTypes.put("bool", new StandartType("Z"));   //FIXME ???
        standartTypes.put("String", new StandartType("Ljava/lang/String;"));
    }
    
    public static StandartType _null(){return standartTypes.get("Null");}
    public static StandartType _void(){return standartTypes.get("void");}
    public static StandartType _int(){return standartTypes.get("int");}
    public static StandartType _double(){return standartTypes.get("double");}
    public static StandartType _bool(){return standartTypes.get("bool");}
    public static StandartType _String(){return standartTypes.get("String");}
    
    public static boolean isStandartName(String name){
        return standartTypes.containsKey(name);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StandartType)) return false;
        if (!super.equals(o)) return false;
        StandartType that = (StandartType) o;
        return descriptor.equals(that.descriptor);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), descriptor);
    }
}
