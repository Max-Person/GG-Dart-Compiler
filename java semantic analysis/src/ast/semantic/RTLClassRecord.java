package ast.semantic;

import ast.StmtNode;
import ast.StmtType;
import ast.semantic.typization.PlainType;
import ast.semantic.typization.VariableType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RTLClassRecord extends ClassRecord{
    public RTLClassRecord(Map<String, ClassRecord> containerClassTable, String name, boolean isStandartJavaClass) {
        super(containerClassTable, name, false);
        this.isStandartJavaClass = isStandartJavaClass;
        this.javaName = name;
    }
    
    private final boolean isStandartJavaClass;
    public String javaName;
    public String javaName(){
        return isStandartJavaClass ? javaName : name;
    }
    public String packageName(){
        return isStandartJavaClass ? "java/lang/" : "ggdart/core/";
    }
    
    //Здесь как бы должна быть куча переопределений методов из ClassRecord, но они все равно никогда не вызовутся...
    
    
    public static RTLClassRecord object = null;
    public static RTLClassRecord string = null;

    public static RTLClassRecord _integer = null;
    public static RTLClassRecord _double = null;
    public static RTLClassRecord _bool = null;
    public static RTLClassRecord io = null;
    static {
        object = new RTLClassRecord(null, "Object", true);
        MethodRecord defConstruct = new MethodRecord(object, false, true, VariableType._void(), "", new ArrayList<>(), new StmtNode(StmtType.block));
        object.constructors.put("", defConstruct);
        defConstruct = new MethodRecord(object, false, false, VariableType._void(), "<init>", new ArrayList<>(), new StmtNode(StmtType.block));
        object.methods.put("<init>", defConstruct);
    
        _integer = new RTLClassRecord(null, "int", true);
        _integer.javaName = "Integer";
        ParameterRecord valParam = new ParameterRecord(null, null, PlainType._int(), "val", false);
        MethodRecord valueOf = new MethodRecord(_integer, true, false, VariableType._int(), "valueOf", List.of(valParam), new StmtNode(StmtType.block));
        valueOf.visible = false;
        _integer.methods.put("valueOf", valueOf);
        MethodRecord value = new MethodRecord(_integer, false, false, PlainType._int(), "intValue", new ArrayList<>(), new StmtNode(StmtType.block));
        value.visible = false;
        _integer.methods.put("intValue", value);
    
        _double = new RTLClassRecord(null, "double", true);
        _double.javaName = "Double";
        valParam = new ParameterRecord(null, null, PlainType._double(), "val", false);
        valueOf = new MethodRecord(_double, true, false, VariableType._double(), "valueOf", List.of(valParam), new StmtNode(StmtType.block));
        valueOf.visible = false;
        _double.methods.put("valueOf", valueOf);
        value = new MethodRecord(_double, false, false, PlainType._double(), "doubleValue", new ArrayList<>(), new StmtNode(StmtType.block));
        value.visible = false;
        _double.methods.put("doubleValue", value);
    
        _bool = new RTLClassRecord(null, "bool", true);
        _bool.javaName = "Boolean";
        valParam = new ParameterRecord(null, null, PlainType._bool(), "val", false);
        valueOf = new MethodRecord(_bool, true, false, VariableType._bool(), "valueOf", List.of(valParam), new StmtNode(StmtType.block));
        valueOf.visible = false;
        _bool.methods.put("valueOf", valueOf);
        value = new MethodRecord(_bool, false, false, PlainType._bool(), "booleanValue", new ArrayList<>(), new StmtNode(StmtType.block));
        value.visible = false;
        _bool.methods.put("booleanValue", value);
    
        string = new RTLClassRecord(null, "String", true);
        string._super = object;
        ParameterRecord otherStringParam = new ParameterRecord(null, null, VariableType._String(), "o", false);
        MethodRecord equals = new MethodRecord(string, false, false, VariableType._bool(), "equals", List.of(otherStringParam), new StmtNode(StmtType.block));
        equals.visible = false;
        string.methods.put("equals", equals);
        MethodRecord concat = new MethodRecord(string, false, false, VariableType._String(), "concat", List.of(otherStringParam), new StmtNode(StmtType.block));
        concat.visible = false;
        string.methods.put("concat", concat);
    
        MethodRecord toString =  new MethodRecord(object, false, false, VariableType._String(), "toString", new ArrayList<>(),  new StmtNode(StmtType.block));
        object.methods.put("toString", toString);
        
        io = new RTLClassRecord(null, "InputOutput", false);
        VariableType any = VariableType._Object();
        any.isNullable = true;
        ParameterRecord anyParam = new ParameterRecord(null, null, any, "obj", false);
        io.methods.put("print",  new MethodRecord(io, true, false, VariableType._void(), "print", List.of(anyParam), new StmtNode(StmtType.block)));
        io.methods.put("readInt", new MethodRecord(io, true, false, PlainType._int(), "readInt", new ArrayList<>(), new StmtNode(StmtType.block)));
        io.methods.put("readDouble", new MethodRecord(io, true, false, PlainType._double(), "readDouble", new ArrayList<>(), new StmtNode(StmtType.block)));
        io.methods.put("readBool", new MethodRecord(io, true, false, PlainType._bool(), "readBool", new ArrayList<>(), new StmtNode(StmtType.block)));
        io.methods.put("readString", new MethodRecord(io, true, false, VariableType._String(), "readString", new ArrayList<>(), new StmtNode(StmtType.block)));
        io.methods.put("!stringify", new MethodRecord(io, true, false, VariableType._String(), "!stringify", List.of(anyParam), new StmtNode(StmtType.block)));
        
    }
}
