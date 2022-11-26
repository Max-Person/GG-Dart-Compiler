package ast.semantic;

import ast.*;
import ast.semantic.typization.VariableType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ast.semantic.SemanticCrawler.printError;

public class ClassRecord implements NamedRecord{
    public Map<String, ClassRecord> containerClassTable = null;
    
    public Map<String, FieldRecord> fields = new HashMap<>();
    public Map<String, MethodRecord> methods = new HashMap<>();
    public Map<Integer, ConstantRecord> constants = new HashMap<>();
    public Map<String, MethodRecord> constructors = new HashMap<>();
    
    public ClassRecord _super = null;
    public List<ClassRecord> _interfaces = new ArrayList<>();
    public List<ClassRecord> _mixins = new ArrayList<>();

    public boolean isDeclResolved = false;
    
    public ClasslikeDeclaration declaration;
    
    public ClassRecord(Map<String, ClassRecord> containerClassTable, ClasslikeDeclaration declaration){
        this.containerClassTable = containerClassTable;
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
    public ConstantRecord addConstant(ConstantRecord constant){
        ConstantRecord existing = constants.values().stream().filter(c -> c.number == constant.number).findFirst().orElse(null);
        if(existing == null){
            constantCount++;
            constant.number = constantCount;
            constants.put(constant.number, constant);
            return constant;
        }
        else return existing;
    }

    public void addField(VariableDeclarationNode var){
        String varName = var.name();
        if(name().equals(varName)){
            printError("a class member can't have the same name as the enclosing class.", var.lineNum);
            return;
        }
        if(fields.containsKey(varName) || methods.containsKey(varName)){ //TODO В дарте нельзя объявить поле и метод с одинаковым именем, но у нас мб можно??
            printError("The name '" + varName  + "' is already defined.", var.lineNum);
            return;
        }
        VariableType type = null;
        if(var.declarator.isTyped){
            type = VariableType.from(containerClassTable, var.declarator.valueType);
            if(type == null) return;
        }
        
        FieldRecord fieldRecord = new FieldRecord(this, var);
        fields.put(fieldRecord.name(), fieldRecord);
    }

    public void addMethod(SignatureNode signature, StmtNode body){
        if(signature.isConstruct){
            if(!signature.name.stringVal.equals(this.name())){
                printError("The name of a constructor must match the name of the enclosing class.", signature.lineNum);
            }
            if(signature.isNamed && constructors.containsKey(signature.constructName.stringVal)){
                printError("The constructor with name '" + signature.name.stringVal +"' is already defined.", signature.lineNum);
            }
            if(!signature.isNamed && constructors.containsKey("")){
                printError("The unnamed constructor is already defined.", signature.lineNum);
            }
            
            MethodRecord methodRecord = new MethodRecord(this, signature, body);
            constructors.put(signature.isNamed ? signature.constructName.stringVal : "", methodRecord);
        }
        else{
            if(!this.isAbstract() && body == null){ // Абстрактный метод не абстрактного класса
                printError("'" + signature.name.stringVal + "' must have a method body because '" + this.name() +  "' isn't abstract.", signature.lineNum);
            }
            if(fields.containsKey(signature.name.stringVal) || methods.containsKey(signature.name.stringVal)){ //TODO В дарте нельзя объявить поле и метод с одинаковым именем, но у нас мб можно??
                printError("The name '" + signature.name.stringVal + "' is already defined.", signature.name.lineNum);
            }
            
            MethodRecord methodRecord = new MethodRecord(this, signature, body);
            methods.put(methodRecord.name(), methodRecord);
        }
    }
    
    public void resolveClassMembers(){
        if(this.isGlobal()) return;
        
        if(this.isEnum()){
            //TODO ?
        }
        else {
            ClassDeclarationNode clazz = (ClassDeclarationNode) declaration;
            for(ClassMemberDeclarationNode classMember : clazz.classMembers){
                if(classMember.type == ClassMemberDeclarationType.field){
                    for(VariableDeclarationNode var: classMember.fieldDecl){
                        this.addField(var);
                    }
                }
                else{
                    StmtNode body = null;
                    if(classMember.type == ClassMemberDeclarationType.methodDefinition) body = classMember.body;
                    this.addMethod(classMember.signature, body);
                }
            }
        }
    }
    
    public void inferTypes(){
        if(this.isEnum()){
            return; //TODO ?
        }
        for(FieldRecord fieldRecord : this.fields.values()){
            fieldRecord.inferType(new ArrayList<>());
        }
        for(MethodRecord constructor : this.constructors.values()){
            constructor.inferType(new ArrayList<>());
        }
    }

    public void checkMethods(){
        if(this.isEnum()){
            return; //TODO ?
        }
        for (MethodRecord method : methods.values()) {
            method.checkMethod();
        }
        for (MethodRecord constructor : constructors.values()) {
            constructor.checkMethod();
        }
    }

    public Map<String, FieldRecord> staticFields(){
        return Utils.filterByValue(fields, field -> field.isStatic());
    }
    public Map<String, FieldRecord> nonStaticFields(){
        return Utils.filterByValue(fields, field -> !field.isStatic());
    }
    public Map<String, MethodRecord> staticMethods(){
        return Utils.filterByValue(methods, method -> method.isStatic());
    }
    
    public String name(){
        return declaration != null ? declaration.name() : globalName;
    }
    public boolean isGlobal() {
        //FIXME не сработает для определения других синтетических классов
        return declaration == null;
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
    public String describe(){
        StringBuilder description = new StringBuilder(this.name() + ":\n");
        for(FieldRecord fieldRecord : fields.values()){
            description.append("\t").append(fieldRecord.descriptor()).append(" ").append(fieldRecord.name()).append("\n");
        }
        for(MethodRecord methodRecord : methods.values()){
            description.append("\t").append(methodRecord.descriptor()).append(" ").append(methodRecord.name()).append("\n");
        }
        for(MethodRecord methodRecord : constructors.values()){
            description.append("\t").append(methodRecord.descriptor()).append(" ").append(methodRecord.name()).append("\n");
        }
        return description.toString();
    }
}
