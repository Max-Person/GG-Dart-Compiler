package ast.semantic;

import java.util.Objects;

public class ConstantRecord {
    
    public enum ConstantType{
        Utf8,
        Integer,
        Float,
        String,
        Class,
        NameAndType,
        FieldRef,
        MethodRef,
    }
    
    public int number;
    public final ConstantType type;
    public final int intval;
    public final double floatval;
    public final String stringval;
    public final ConstantRecord ref1;
    public final ConstantRecord ref2;
    
    
    private ConstantRecord(ConstantType type, int intval, double floatval, String stringval, ConstantRecord ref1, ConstantRecord ref2) {
        this.type = type;
        this.intval = intval;
        this.floatval = floatval;
        this.stringval = stringval;
        this.ref1 = ref1;
        this.ref2 = ref2;
    }
    
    public static ConstantRecord newUtf8(String stringval){
        return new ConstantRecord(ConstantType.Utf8, 0, Double.NaN, stringval, null, null);
    }
    public static ConstantRecord newInteger(int intval){
        return new ConstantRecord(ConstantType.Integer, intval, Double.NaN, null, null, null);
    }
    public static ConstantRecord newFloat(double floatval){
        return new ConstantRecord(ConstantType.Float, 0, floatval, null, null, null);
    }
    public static ConstantRecord newString(ConstantRecord value){
        if(value.type != ConstantType.Utf8)
            throw new IllegalArgumentException();
        return new ConstantRecord(ConstantType.String, 0, Double.NaN, null, value, null);
    }
    public static ConstantRecord newClass(ConstantRecord name){
        if(name.type != ConstantType.Utf8)
            throw new IllegalArgumentException();
        return new ConstantRecord(ConstantType.Class, 0, Double.NaN, null, name, null);
    }
    public static ConstantRecord newNameAndType(ConstantRecord name, ConstantRecord type){
        if(name.type != ConstantType.Utf8 || type.type != ConstantType.Utf8)
            throw new IllegalArgumentException();
        return new ConstantRecord(ConstantType.NameAndType, 0, Double.NaN, null, name, type);
    }
    public static ConstantRecord newFieldRef(ConstantRecord clazz, ConstantRecord nameAndType){
        if(clazz.type != ConstantType.Class || nameAndType.type != ConstantType.NameAndType)
            throw new IllegalArgumentException();
        return new ConstantRecord(ConstantType.FieldRef, 0, Double.NaN, null, clazz, nameAndType);
    }
    public static ConstantRecord newMethodRef(ConstantRecord clazz, ConstantRecord nameAndType){
        if(clazz.type != ConstantType.Class || nameAndType.type != ConstantType.NameAndType)
            throw new IllegalArgumentException();
        return new ConstantRecord(ConstantType.MethodRef, 0, Double.NaN, null, clazz, nameAndType);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstantRecord that = (ConstantRecord) o;
        return intval == that.intval && Double.compare(that.floatval, floatval) == 0 && type == that.type && Objects.equals(stringval, that.stringval) && Objects.equals(ref1, that.ref1) && Objects.equals(ref2, that.ref2);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, intval, floatval, stringval, ref1, ref2);
    }
}
