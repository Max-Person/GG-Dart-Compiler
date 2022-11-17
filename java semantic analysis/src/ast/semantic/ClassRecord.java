package ast.semantic;

import ast.ClassDeclarationNode;
import ast.ClasslikeDeclaration;
import ast.EnumNode;
import ast.SignatureNode;
import ast.StmtNode;
import ast.VariableDeclarationNode;
import ast.semantic.typization.FunctionType;
import ast.semantic.typization.VariableType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ast.semantic.SemanticCrawler.printError;

public class ClassRecord {
    public Map<String, FieldRecord> fields = new HashMap<>();
    public Map<String, MethodRecord> methods = new HashMap<>();
    public Map<Integer, ConstantRecord> constants = new HashMap<>();
    public Map<String, MethodRecord> constructors = new HashMap<>();
    
    public ClassRecord _super = null;
    public List<ClassRecord> _interfaces = new ArrayList<>();
    public List<ClassRecord> _mixins = new ArrayList<>();

    public boolean isDeclResolved = false;
    
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
    public ConstantRecord addConstant(ConstantRecord constant){
        ConstantRecord existing = constants.values().stream().filter(c -> c.number == constant.number).findFirst().orElse(null);
        if(existing != null){
            constantCount++;
            constant.number = constantCount;
            constants.put(constant.number, constant);
            return constant;
        }
        else return existing;
    }

    public void addField(Map<String, ClassRecord> classTable, VariableDeclarationNode var){
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
            type = VariableType.from(classTable, var.declarator.valueType);
            if(type == null) return;
        }
        ConstantRecord nameConst = addConstant(ConstantRecord.newUtf8(var.name()));
        ConstantRecord descriptorConst = type != null ? addConstant(ConstantRecord.newUtf8(type.descriptor())) : null;
        
        FieldRecord fieldRecord = new FieldRecord(var, type, nameConst, descriptorConst);
        fields.put(fieldRecord.name(), fieldRecord);
    }

    public void addMethod(Map<String, ClassRecord> classTable, SignatureNode signature, StmtNode body){
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
            // TODO проверка на this.поле
            FunctionType type = FunctionType.from(classTable, signature);
            if(type == null) return;
            ConstantRecord nameConst = addConstant(ConstantRecord.newUtf8("<init>")); // TODO если конструктор именованный то другое имя????????
            ConstantRecord descriptorConst = addConstant(ConstantRecord.newUtf8(type.descriptor()));
            MethodRecord methodRecord = new MethodRecord(signature, body, type, nameConst, descriptorConst); //TODO переделать
            if (signature.name.stringVal.equals(this.name())){
                methods.put("", methodRecord);
            }
            methods.put(signature.name.stringVal, methodRecord);
        }
        else{
            if(!this.isAbstract() && body == null){ // Абстрактный метод не абстрактного класса
                printError("'" + signature.name.stringVal + "' must have a method body because '" + this.name() +  "' isn't abstract.", signature.lineNum);
            }
            if(fields.containsKey(signature.name.stringVal) || methods.containsKey(signature.name.stringVal)){ //TODO В дарте нельзя объявить поле и метод с одинаковым именем, но у нас мб можно??
                printError("The name '" + signature.name.stringVal + "' is already defined.", signature.name.lineNum);
            }
            FunctionType type = FunctionType.from(classTable, signature);
            if(type == null) return;
            ConstantRecord nameConst = addConstant(ConstantRecord.newUtf8(signature.name.stringVal));
            ConstantRecord descriptorConst = addConstant(ConstantRecord.newUtf8(type.descriptor()));

            MethodRecord methodRecord = new MethodRecord(signature, body, type, nameConst, descriptorConst);
            methods.put(signature.name.stringVal, methodRecord);
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
