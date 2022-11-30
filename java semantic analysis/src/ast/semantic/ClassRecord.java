package ast.semantic;

import ast.*;
import ast.semantic.context.ClassInitContext;
import ast.semantic.typization.ClassType;
import ast.semantic.typization.VariableType;

import java.util.*;

import static ast.semantic.SemanticCrawler.printError;

public class ClassRecord implements NamedRecord{
    public Map<String, ClassRecord> containerClassTable = null;
    
    public String name;
    
    public Map<String, FieldRecord> fields = new HashMap<>();
    public Map<String, MethodRecord> methods = new HashMap<>();
    public Map<Integer, ConstantRecord> constants = new HashMap<>();
    public Map<String, MethodRecord> constructors = new HashMap<>();
    
    public ClassRecord _super = RTLClassRecord.object;
    public List<ClassRecord> _interfaces = new ArrayList<>();
    public List<ClassRecord> _mixins = new ArrayList<>();
    
    public boolean isJavaInterface = false;
    public List<ClassRecord> javaInterfaces = new ArrayList<>();

    public boolean isDeclResolved = false;
    
    public ClasslikeDeclaration declaration;
    
    public ClassRecord(Map<String, ClassRecord> containerClassTable, ClasslikeDeclaration declaration){
        this.containerClassTable = containerClassTable;
        this.declaration = declaration;
        this.name = declaration.name();
        addConstant(ConstantRecord.newUtf8("Code"));
        ConstantRecord className = ConstantRecord.newUtf8(declaration.name());
        addConstant(className);
        addConstant(ConstantRecord.newClass(className));
    }
    public static final String globalName = "<GLOBAL>";  //FIXME точно ли это работает...
    public ClassRecord(Map<String, ClassRecord> containerClassTable, String name, boolean isJavaInterface){
        this.containerClassTable = containerClassTable;
        this.declaration = null;
        this.name = name;
        this.isJavaInterface = isJavaInterface;
        addConstant(ConstantRecord.newUtf8("Code"));
        ConstantRecord className = ConstantRecord.newUtf8(name);
        addConstant(className);
        addConstant(ConstantRecord.newClass(className));
    }
    
    //-- НАПОЛНЕНИЕ КЛАССА
    
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

    public void addMethod(SignatureNode signature, StmtNode body){ //TODO статические методы и именованные конструкторы
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
            if(body == null){
                if(signature.isStatic){
                    printError("Static method '" + signature.name.stringVal + "' must have a method body.", signature.lineNum);
                }
                if(!this.isAbstract()){ // Абстрактный метод не абстрактного класса
                    printError("'" + signature.name.stringVal + "' must have a method body because '" + this.name() +  "' isn't abstract.", signature.lineNum);
                }
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
            // добавить поле
            FieldRecord field = new FieldRecord(this, false, false, false, true, VariableType._String(), "value");
            this.fields.put("value", field);
            // добавить конструктор
            ParameterRecord param = new ParameterRecord(null, null, null, "value", true);
            MethodRecord constructor = new MethodRecord(this, false, true, VariableType._void(), "", Collections.singletonList(param), null);
            this.constructors.put("", constructor);
            // для каждого из значений енама надо добавить статическое поле
            for (IdentifierNode value : ((EnumNode) declaration).values) {
                if(this.name.equals(value.stringVal)){
                    printError("The name of the enum constant can't be the same as the enum's name.", value.lineNum);
                }
                if(this.fields.containsKey(value.stringVal)){
                    printError("The name '" + value.stringVal + "' is already defined.", value.lineNum);
                }
                field = new FieldRecord(this, false, true, false, true, new ClassType(this), value.stringVal);
                field.initValue = new ExprNode(ExprType.constructNew);
                field.initValue.identifierAccess = new IdentifierNode(this.name);
                field.initValue.constructName = null;
                ExprNode arg = new ExprNode(ExprType.string_pr);
                arg.stringValue = value.stringVal;
                field.initValue.callArguments = Collections.singletonList(arg);
                this.fields.put(value.stringVal, field);
            }

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
            if(this.constructors.isEmpty()){
                //Создание конструктора по умолчанию
                if(!this._super.constructors.containsKey("") || !this._super.constructors.get("").parameters.isEmpty()){
                    printError("The superclass '"+ this._super.name() +"' doesn't have an a default (unnamed + zero argument) constructor.", declaration.lineNum());
                }
                
                MethodRecord defConstruct = new MethodRecord(this, false, true, VariableType._void(), "", new ArrayList<>(), null);
                InitializerNode defaultSuperConstructor = new InitializerNode(null, new ArrayList<>());
                defConstruct.initializers = List.of(defaultSuperConstructor);
                this.constructors.put("", defConstruct);
            }
        }
        if(!this.staticFields().isEmpty()){
            //Создание классового конструктора <clinit>
            StmtNode body = new StmtNode(StmtType.block);
            this.staticFields().values().forEach(f -> {
                if(f.initValue != null)
                    body.blockStmts.add(f.initStmt());
            });
        
            MethodRecord classConstruct = new MethodRecord(this, true, false, VariableType._void(), "<clinit>", new ArrayList<>(), body);
            this.methods.put("", classConstruct);
        }
    }
    
    //-- ВЫЧИСЛЕНИЕ ТИПОВ
    
    public void inferTypes(){
        for(FieldRecord fieldRecord : this.fields.values()){
            fieldRecord.inferType(new ClassInitContext(fieldRecord));
        }
        for(MethodRecord constructor : this.constructors.values()){
            constructor.inferType(new ClassInitContext(this, false));
        }
    }
    
    //-- ПРОВЕРКА ПРАВИЛЬНОСТИ НАСЛЕДОВАНИЙ
    
    public boolean inheritanceChecked = false;
    public void checkInheritance(){
        if(inheritanceChecked)
            return;
        
        if(this._super != null){
            this._super.checkInheritance();
            
            //Получить список всех наследуемых полей и убедиться, что все определенные в классе поля либо не переопределяют наследуемые, либо переопределяют их правильно
            for(FieldRecord inhField : this._super.nonStaticFields().values()){
                if(this.fields.containsKey(inhField.name()) && !this.fields.get(inhField.name()).isValidOverrideOf(inhField)){
                    printError("'" + this.name() + "." + inhField.name + "' isn’t a valid override of '" + _super.name() + "." + inhField.name + "'", this.declaration.lineNum());
                }
            }
    
            //Получить список всех наследуемых ДИНАМИЧЕСКИХ функций и убедиться,
            // что все определенные в классе функции либо не переопределяют наследуемые, либо переопределяют их правильно
            for(MethodRecord inhMethod : this._super.nonStaticMethods().values()){
                if(this.methods.containsKey(inhMethod.name()) && !this.methods.get(inhMethod.name()).isValidOverrideOf(inhMethod)){
                    printError("'" + this.name() + "." + inhMethod.name + "' isn’t a valid override of '" + _super.name() + "." + inhMethod.name + "'", this.declaration.lineNum());
                }
            }
        }
    
        //Проверка взаимного переопределения миксинов и копирование функций и переменных из них в текущий класс
        if(!this._mixins.isEmpty()){
            Map<String, MethodRecord> mixinMethods = new HashMap<>();
            Map<String, FieldRecord> mixinFields = new HashMap<>();
            for(ClassRecord mixin : this._mixins){
                mixin.checkInheritance();
                mixin.markInterfaceSplitting();
                
                if(mixinMethods.isEmpty()){
                    mixinMethods.putAll(mixin.nonStaticMethods());
                }
                else {
                    for(MethodRecord m : mixin.nonStaticMethods().values()){
                        if(mixinMethods.containsKey(m.name()) && m.isValidOverrideOf(mixinMethods.get(m.name()))){
                            if(!m.isAbstract()){
                                mixinMethods.put(m.name(), m);
                            }
                        }
                        else {
                            printError("'" + mixin.name() + "." + m.name() + "' isn’t a valid override of '" + mixinMethods.get(m.name()).containerClass.name() + "." + m.name() + "'", mixin.declaration.lineNum());
                        }
                    }
                }
    
                if(mixinFields.isEmpty()){
                    mixinFields.putAll(mixin.nonStaticFields());
                }
                else {
                    for(FieldRecord f : mixin.nonStaticFields().values()){
                        if(mixinFields.containsKey(f.name()) && f.isValidOverrideOf(mixinFields.get(f.name()))){
                            mixinFields.put(f.name(), f);
                        }
                        else {
                            printError("'" + mixin.name() + "." + f.name() + "' isn’t a valid override of '" + mixinFields.get(f.name()).containerClass.name() + "." + f.name() + "'", mixin.declaration.lineNum());
                        }
                    }
                }
            }
        
            for(MethodRecord m : mixinMethods.values()){
                if(!m.isAbstract() && !this.methods.containsKey(m.name())){
                    m.copyTo(this);
                }
            }
    
            for(FieldRecord f : mixinFields.values()){
                if(!this.fields.containsKey(f.name())){
                    f.copyTo(this);
                }
            }
        }
        
        for(ClassRecord i : _interfaces){
            i.checkInheritance();
            i.markInterfaceSplitting();
        }
    
        //Получить список всех наследуемых абстрактных функций и убедиться, что они правильно переопределены внутри класса
        //Все функции интерфейсов и миксинов считаются за абстрактные
        if(!this.isAbstract()){
            for(MethodRecord unresolved : this.unresolvedAbstractMethods()){
                printError("Missing concrete implementation of '" + unresolved.name() + "'.", this.declaration.lineNum());
            }
        }
        
        inheritanceChecked = true;
    }
    
    //-- РАЗДЕЛЕНИЕ НА ИНТЕРФЕЙСЫ И РЕАЛИЗАЦИИ
    
    private boolean shouldSplitAsInterface;
    private void markInterfaceSplitting(){
        this.shouldSplitAsInterface = true;
        if(_super != RTLClassRecord.object){
            _super.markInterfaceSplitting();
        }
        _interfaces.forEach(i -> i.markInterfaceSplitting());
        _mixins.forEach(m -> m.markInterfaceSplitting());
    }
    public ClassRecord associatedInterface = null;
    public void resolveInterfaces(){
        if(this.shouldSplitAsInterface){
            this.splitInterface();
        }
        else {
            if(_super != null && _super != RTLClassRecord.object && _super.shouldSplitAsInterface){
                _super.splitInterface();
                this.javaInterfaces.add(_super.associatedInterface);
            }
            for(ClassRecord i : _interfaces){
                i.splitInterface();
                this.javaInterfaces.add(i.associatedInterface);
            }
            _interfaces.clear();
            for(ClassRecord m : _mixins) {
                m.splitInterface();
                this.javaInterfaces.add(m.associatedInterface);
            }
            _mixins.clear();
        }
    }
    public void splitInterface(){
        if(this.associatedInterface != null || this.isJavaInterface)
            return;
        
        associatedInterface = this.asInterface();
        containerClassTable.put(associatedInterface.name(), associatedInterface);
        associatedInterface.javaInterfaces.addAll(this.javaInterfaces);
        this.javaInterfaces.clear();
        if(_super != RTLClassRecord.object){
            _super.splitInterface();
            associatedInterface.javaInterfaces.add(_super.associatedInterface);
        }
        for(ClassRecord i : _interfaces){
            i.splitInterface();
            associatedInterface.javaInterfaces.add(i.associatedInterface);
        }
        _interfaces.clear();
        for(ClassRecord m : _mixins){
            m.splitInterface();
            associatedInterface.javaInterfaces.add(m.associatedInterface);
        }
        _mixins.clear();
    }
    public ClassRecord asInterface(){
        if(this.isJavaInterface)
            throw new IllegalStateException();
        
        ClassRecord i = new ClassRecord(this.containerClassTable, "I!" + this.name, true);
        Utils.filterByValue(methods, method -> !method.isStatic()).values().forEach(method -> method.copyAsAbstractTo(i));
        return i;
    }
    
    public void finalizeTypes(){
        fields.values().forEach(f -> f.finalizeType());
        methods.values().forEach(m -> m.finalizeType());
        constructors.values().forEach(m -> m.finalizeType());
    }

    //-- ПРОВЕРКА МЕТОДОВ
    
    public void checkMethods(){
        for (MethodRecord method : methods.values()) {
            method.checkMethod();
        }
        for (MethodRecord constructor : constructors.values()) {
            constructor.checkMethod();
        }
    }
    
    //-- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ

    public Map<String, FieldRecord> staticFields(){
        return Utils.filterByValue(fields, field -> field.isStatic());
    }
    public Map<String, FieldRecord> nonStaticFields(){
        //TODO проверить
        Map<String, FieldRecord> res = Utils.filterByValue(fields, field -> !field.isStatic());
        if(_super != null){
            _super.nonStaticFields().forEach((name, field) -> res.putIfAbsent(name, field));
        }
        for(ClassRecord i : _interfaces){
            i.nonStaticFields().forEach((name, field) -> res.putIfAbsent(name, field));
        }
        List<ClassRecord> _mixinsRev = new ArrayList<>(_mixins);
        Collections.reverse(_mixinsRev);
        for(ClassRecord m : _mixinsRev){
            m.nonStaticFields().forEach((name, field) -> res.putIfAbsent(name, field));
        }
        return res;
    }
    public Map<String, MethodRecord> staticMethods(){
        return Utils.filterByValue(methods, method -> method.isStatic());
    }
    public Map<String, MethodRecord> nonStaticMethods(){
        //TODO проверить
        Map<String, MethodRecord> res = Utils.filterByValue(methods, method -> !method.isStatic());
        if(_super != null){
            _super.nonStaticMethods().forEach((name, method) -> res.putIfAbsent(name, method));
        }
        for(ClassRecord i : _interfaces){
            i.nonStaticMethods().forEach((name, method) -> res.putIfAbsent(name, method));
        }
        List<ClassRecord> _mixinsRev = new ArrayList<>(_mixins);
        Collections.reverse(_mixinsRev);
        for(ClassRecord m : _mixinsRev){
            m.nonStaticMethods().forEach((name, method) -> res.putIfAbsent(name, method));
        }
        return res;
    }
    private Map<String, MethodRecord>concreteMethods(){
        //TODO проверить
        Map<String, MethodRecord> res = Utils.filterByValue(methods, method -> !method.isAbstract());
        if(this._super != null){
            for(MethodRecord m : this._super.concreteMethods().values()){
                if(!this.methods.containsKey(m.name())){
                    res.put(m.name(), m);
                }
                else if(!this.methods.get(m.name()).isValidOverrideOf(m)){
                    printError("'" + this.name() + "." + m.name() + "' isn’t a valid override of '" + _super.name() + "." + m.name() + "'", _super.declaration.lineNum());
                }
            }
        }
        return res;
    }
    private List<MethodRecord> unresolvedAbstractMethods(){
        List<MethodRecord> res = new ArrayList<>(Utils.filterByValue(methods, method -> method.isAbstract()).values());
        if(this._super != null){
            for(MethodRecord m : this._super.unresolvedAbstractMethods()){
                if(!this.concreteMethods().containsKey(m.name())){
                    res.add(m);
                }
                else if(!this.concreteMethods().get(m.name()).isValidOverrideOf(m)){
                    printError("'" + this.name() + "." + m.name() + "' isn’t a valid override of '" + _super.name() + "." + m.name() + "'", _super.declaration.lineNum());
                }
            }
        }
        if(!this._interfaces.isEmpty()){
            for(ClassRecord i : this._interfaces){
                for(MethodRecord m : i.nonStaticMethods().values()){
                    if(!this.concreteMethods().containsKey(m.name())){
                        res.add(m);
                    }
                    else if(!this.concreteMethods().get(m.name()).isValidOverrideOf(m)){
                        printError("'" + this.name() + "." + m.name() + "' isn’t a valid override of '" + i.name() + "." + m.name() + "'", i.declaration.lineNum());
                    }
                }
            }
        }
        if(!this._mixins.isEmpty()){
            for(ClassRecord i : this._mixins){
                for(MethodRecord m : i.nonStaticMethods().values()){
                    if(!this.concreteMethods().containsKey(m.name())){
                        res.add(m);
                    }
                    else if(!this.concreteMethods().get(m.name()).isValidOverrideOf(m)){
                        printError("'" + this.name() + "." + m.name() + "' isn’t a valid override of '" + i.name() + "." + m.name() + "'", i.declaration.lineNum());
                    }
                }
            }
        }
        return res;
    }
    
    //-- ИНФОРМАЦИОННЫЕ МЕТОДЫ
    
    public String name(){
        return this.name;
    }
    public String descriptor() {
        return "Lggdart/gen/" + name() + ";";
    }
    public boolean isSubTypeOf(ClassRecord other){
        if(other == null) return false;
        
        if(this == other) return true;
        
        return (_super != null && _super.isSubTypeOf(other)) || _interfaces.stream().anyMatch(i -> i.isSubTypeOf(other)) || _mixins.stream().anyMatch(m -> m.isSubTypeOf(other)) ||
                (associatedInterface != null && associatedInterface.isSubTypeOf(other)) || javaInterfaces.stream().anyMatch(i -> i.isSubTypeOf(other));
    }
    public boolean isGlobal() {
        return this.name.equals(globalName);
    }
    public boolean isAbstract(){
        if(declaration == null) //FIXME ?
            return true;
        if(isEnum())
            return false;
        return ((ClassDeclarationNode) declaration).isAbstract;
    }
    public boolean isEnum(){
        return declaration != null && declaration instanceof EnumNode;
    }
    public String describe(){
        StringBuilder description = new StringBuilder(this.name());
        if(_super != null)
            description.append(" ext ").append(_super.name());
        if(associatedInterface != null || !javaInterfaces.isEmpty()){
            description.append(" impl");
            if(associatedInterface != null)
                description.append(" ").append(associatedInterface.name());
            javaInterfaces.forEach(i -> description.append(" ").append(i.name()));
        }
        description.append(":\n");
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
