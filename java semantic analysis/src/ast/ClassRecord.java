package ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassRecord {
    public Map<String, FieldRecord> fields = new HashMap<>();
    public Map<String, MethodRecord> methods = new HashMap<>();
    public Map<Integer, ConstantRecord> constants = new HashMap<>();
    
    public ClassRecord _super = null;
    public List<ClassRecord> _interfaces = new ArrayList<>();
    public List<ClassRecord> _mixins = new ArrayList<>();
    
    public ClasslikeDeclaration declaration;
    
    public ClassRecord(ClasslikeDeclaration declaration){
        this.declaration = declaration;
        addConstant(ConstantRecord.newUtf8("Code"));
        ConstantRecord className = ConstantRecord.newUtf8(declaration.name());
        addConstant(className);
        addConstant(ConstantRecord.newClass(className));
    }
    
    public static final String globalName = "<GLOBAL>";  //FIXME точно ли это работает...
    private ClassRecord(){
        this.declaration = null;
        addConstant(ConstantRecord.newUtf8("Code"));
        ConstantRecord className = ConstantRecord.newUtf8(globalName);
        addConstant(className);
        addConstant(ConstantRecord.newClass(className));
    }
    public static ClassRecord globalClass(){
        return new ClassRecord();
    }
    
    private int constantCount = 0;
    public void addConstant(ConstantRecord constant){
        if(constants.values().stream().noneMatch(c -> c.equals(constant))){
            constantCount++;
            constant.number = constantCount;
            constants.put(constant.number, constant);
        }
    }
    
    public String name(){
        return declaration != null ? declaration.name() : globalName;
    }
    public boolean isAbstract(){
        if(declaration == null)
            return true;
        if(!(declaration instanceof ClassDeclarationNode))
            throw new IllegalStateException();
        return ((ClassDeclarationNode) declaration).isAbstract;
    }
    public boolean isEnum(){
        return declaration != null && declaration instanceof EnumNode;
    }
}
