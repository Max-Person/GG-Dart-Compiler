package ast.semantic;

import ast.StmtNode;
import ast.StmtType;
import ast.semantic.typization.VariableType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RTLListClassRecord extends RTLClassRecord{
    public RTLListClassRecord(Map<String, ClassRecord> containerClassTable, VariableType valueType) {
        super(containerClassTable, "List", false);
        
        ParameterRecord param = new ParameterRecord(null, null, VariableType._int(), "index", false);
        this.methods.put("elementAt", new MethodRecord(this, valueType.clone(), "elementAt", List.of(param), new StmtNode(StmtType.block)));
    
        this.methods.put("length", new MethodRecord(this, VariableType._int(), "length", List.of(), new StmtNode(StmtType.block)));
        
        param = new ParameterRecord(null, null, valueType.clone(), "obj", false);
        this.methods.put("add", new MethodRecord(this, VariableType._void(), "add", List.of(param), new StmtNode(StmtType.block)));
    
        param = new ParameterRecord(null, null, valueType.clone(), "obj", false);
        this.methods.put("remove", new MethodRecord(this, VariableType._bool(), "remove", List.of(param), new StmtNode(StmtType.block)));
    
        this.methods.put("isEmpty", new MethodRecord(this, VariableType._bool(), "isEmpty", List.of(), new StmtNode(StmtType.block)));
    
        param = new ParameterRecord(null, null, VariableType._int(), "index", false);
        ParameterRecord param2 = new ParameterRecord(null, null, valueType.clone(), "obj", false);
        this.methods.put("set", new MethodRecord(this, valueType.clone(), "isEmpty", Arrays.asList(param, param2), new StmtNode(StmtType.block)));
    
        param = new ParameterRecord(null, null, VariableType._int(), "index", false);
        param2 = new ParameterRecord(null, null, valueType.clone(), "obj", false);
        this.methods.put("insert", new MethodRecord(this, VariableType._void(), "insert", Arrays.asList(param, param2), new StmtNode(StmtType.block)));
    }
}
