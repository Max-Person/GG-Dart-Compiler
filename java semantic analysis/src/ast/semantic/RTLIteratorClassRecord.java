package ast.semantic;

import ast.semantic.typization.PlainType;
import ast.semantic.typization.VariableType;

import java.util.List;
import java.util.Map;

//Привет всем кто читает этот код, я конечно согласен что плодить разные классрекорды для генерик классов это тупо, но мне лень делать по умному...
public class RTLIteratorClassRecord extends RTLClassRecord{
    public VariableType valueType;
    public static RTLIteratorClassRecord basic = new RTLIteratorClassRecord(null, VariableType._Object());
    public String packageName(){
        return "java/util/";
    }
    
    public RTLIteratorClassRecord(Map<String, ClassRecord> containerClassTable, VariableType valueType) {
        super(containerClassTable, "Iterator", false);
        this.valueType = valueType;
        this.isJavaInterface = true;
    
        this.methods.put("hasNext", new MethodRecord(this, PlainType._bool(), "hasNext", List.of(), null));
    
        this.methods.put("next", new MethodRecord(this, valueType.clone(), "next", List.of(), null));
    }
}
