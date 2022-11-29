package ast.semantic;

import ast.StmtNode;
import ast.StmtType;
import ast.semantic.typization.VariableType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RTLClassRecord extends ClassRecord{
    public RTLClassRecord(Map<String, ClassRecord> containerClassTable, String name, boolean isStandartJavaClass) {
        super(containerClassTable, name, false);
        this.isStandartJavaClass = isStandartJavaClass;
    }
    
    private final boolean isStandartJavaClass;
    public String descriptor() {
        if(isStandartJavaClass){
            return "Ljava/lang" + name() + ";";
        }
        else {
            return "Lggdart/core/" + name() + ";";
        }
    }
    
    //Здесь как бы должна быть куча переопределений методов из ClassRecord, но они все равно никогда не вызовутся...
    
    
    public static RTLClassRecord object = null;
    public static RTLClassRecord string = null;
    public static RTLClassRecord io = null;
    static {
        object = new RTLClassRecord(null, "Object", true);
        MethodRecord defConstruct = new MethodRecord(object, false, true, VariableType._void(), "", new ArrayList<>(), new StmtNode(StmtType.block));
        object.constructors.put("", defConstruct);
    
        string = new RTLClassRecord(null, "String", true);
        string._super = object;
        ParameterRecord equalsParam = new ParameterRecord(null, null, VariableType._String(), "o", false);
        MethodRecord equals = new MethodRecord(string, false, false, VariableType._bool(), "equals", List.of(equalsParam), new StmtNode(StmtType.block));
        string.methods.put("equals", equals);
    
        MethodRecord toString =  new MethodRecord(object, false, false, VariableType._String(), "toString", new ArrayList<>(),  new StmtNode(StmtType.block));
        object.methods.put("toString", toString);
        
        io = new RTLClassRecord(null, "InputOutput", false);
        ParameterRecord printParam = new ParameterRecord(null, null, VariableType._Object(), "obj", false);
        MethodRecord print = new MethodRecord(io, true, false, VariableType._void(), "print", List.of(printParam), new StmtNode(StmtType.block));
        io.methods.put("print", print);
        ParameterRecord readParam = new ParameterRecord(null, null, VariableType._int(), "obj", false);
        io.methods.put("readInt", new MethodRecord(io, true, false, VariableType._int(), "readInt", List.of(readParam), new StmtNode(StmtType.block)));
        readParam = new ParameterRecord(null, null, VariableType._double(), "obj", false);
        io.methods.put("readDouble", new MethodRecord(io, true, false, VariableType._double(), "readDouble", List.of(readParam), new StmtNode(StmtType.block)));
        readParam = new ParameterRecord(null, null, VariableType._bool(), "obj", false);
        io.methods.put("readBool", new MethodRecord(io, true, false, VariableType._bool(), "readBool", List.of(readParam), new StmtNode(StmtType.block)));
        readParam = new ParameterRecord(null, null, VariableType._String(), "obj", false);
        io.methods.put("readString", new MethodRecord(io, true, false, VariableType._String(), "readString", List.of(readParam), new StmtNode(StmtType.block)));
        
        //TODO сделать некоторые методы невидимыми для пользователей
    
    }
}
