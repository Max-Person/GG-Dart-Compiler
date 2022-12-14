package ast.semantic;

import ast.StmtNode;
import ast.StmtType;
import ast.semantic.typization.ListType;
import ast.semantic.typization.PlainType;
import ast.semantic.typization.VariableType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RTLListClassRecord extends RTLClassRecord{
    VariableType valueType;
    public static RTLListClassRecord basic = new RTLListClassRecord(null, VariableType._Object());
    
    public RTLListClassRecord(Map<String, ClassRecord> containerClassTable, VariableType valueType) {
        super(containerClassTable, "List", false);
        this.valueType = valueType;
        
        MethodRecord defConstruct = new MethodRecord(this, false, true, VariableType._void(), "", new ArrayList<>(), new StmtNode(StmtType.block));
        this.constructors.put("", defConstruct);
        
        ParameterRecord param = new ParameterRecord(null, null, PlainType._int(), "index", false);
        this.methods.put("elementAt", new MethodRecord(this, valueType.clone(), "elementAt", List.of(param), new StmtNode(StmtType.block)));
    
        this.methods.put("length", new MethodRecord(this, PlainType._int(), "length", List.of(), new StmtNode(StmtType.block)));
        
        param = new ParameterRecord(null, null, valueType.clone(), "obj", false);
        this.methods.put("add", new MethodRecord(this, VariableType._void(), "add", List.of(param), new StmtNode(StmtType.block)));

        param = new ParameterRecord(null, null, valueType.clone(), "obj", false);
        this.methods.put("remove", new MethodRecord(this, PlainType._bool(), "remove", List.of(param), new StmtNode(StmtType.block)));
    
        this.methods.put("isEmpty", new MethodRecord(this, PlainType._bool(), "isEmpty", List.of(), new StmtNode(StmtType.block)));
    
        param = new ParameterRecord(null, null, PlainType._int(), "index", false);
        ParameterRecord param2 = new ParameterRecord(null, null, valueType.clone(), "obj", false);
        this.methods.put("set", new MethodRecord(this, valueType.clone(), "isEmpty", Arrays.asList(param, param2), new StmtNode(StmtType.block)));
    
        param = new ParameterRecord(null, null, PlainType._int(), "index", false);
        param2 = new ParameterRecord(null, null, valueType.clone(), "obj", false);
        this.methods.put("insert", new MethodRecord(this, VariableType._void(), "insert", Arrays.asList(param, param2), new StmtNode(StmtType.block)));

        param = new ParameterRecord(null, null, valueType.clone(), "obj", false);
        MethodRecord with =  new MethodRecord(this, new ListType(valueType), "with", List.of(param), new StmtNode(StmtType.block));
        with.visible = false;
        this.methods.put("with",with);
    }
    
    @Override
    public boolean isSubTypeOf(ClassRecord other) {
        return super.isSubTypeOf(other) || (other instanceof RTLListClassRecord && this.valueType.isSubtypeOf(((RTLListClassRecord) other).valueType));
    }
}
