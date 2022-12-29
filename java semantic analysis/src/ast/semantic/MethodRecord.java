package ast.semantic;

import ast.*;
import ast.semantic.constants.UTF8Constant;
import ast.semantic.context.ClassInitContext;
import ast.semantic.context.MethodContext;
import ast.semantic.typization.ClassType;
import ast.semantic.typization.JavaArrayType;
import ast.semantic.typization.VariableType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ast.semantic.SemanticCrawler.printError;

public class MethodRecord implements NamedRecord, Cloneable{
    public ClassRecord containerClass;
    
    public Map<Integer, LocalVarRecord> locals = new HashMap<>();
    public int localVarNumber = 0;

    protected boolean isStatic, isConst;
    protected boolean isConstruct;
    public VariableType returnType;
    public String name;
    public String constructName;
    public List<ParameterRecord> parameters = new ArrayList<>();
    
    public RedirectionNode redirection = null;
    public List<InitializerNode> initializers = null;
    
    public StmtNode body;
    public int lineNum = -1;
    
    public ConstantRecord nameConst;
    public ConstantRecord descriptorConst;
    
    //Для простых синтетических методов
    public MethodRecord(ClassRecord containerClass, VariableType returnType, String name, List<ParameterRecord> parameters, StmtNode body){
        this(containerClass, false, false, returnType, name, parameters, body);
    }
    
    //Для синтетических методов
    public MethodRecord(ClassRecord containerClass, boolean isStatic, boolean isConstruct, VariableType returnType, String name, List<ParameterRecord> parameters, StmtNode body){
        this.containerClass = containerClass;
        
        this.isStatic = isStatic && !isConstruct;
        this.isConst = false;
        
        this.returnType = isConstruct ? VariableType._void() : returnType;
        this.name = isConstruct ? "<init>" : name;
        
        if(!this.isStatic){
            localVarNumber++;
        }
        parameters.forEach(p -> {
            p.containerMethod = this;
            p.number = localVarNumber;
            localVarNumber++;
            this.parameters.add(p);
        });
        
        this.isConstruct = isConstruct;
        this.constructName = isConstruct ? name : null;
        
        this.body = body;
        if(this.isConstruct) {
            if (this.body == null) {
                this.body = new StmtNode(StmtType.block, lineNum);
            } else if (this.body.type == StmtType.return_statement) {
                printError("Constructors can't return values.", body.lineNum);
            }
        }
    }
    
    public MethodRecord(ClassRecord containerClass, SignatureNode signature, StmtNode body){
        this.containerClass = containerClass;
        
        this.isStatic = signature.isStatic;
        this.isConst = signature.isConst;
        this.isConstruct = signature.isConstruct;
        this.name = signature.isConstruct ? "<init>" : signature.name.stringVal;
        this.constructName = signature.isConstruct && signature.isNamed ? signature.constructName.stringVal : "";
        
        this.redirection = signature.redirection;
        this.initializers = signature.initializers;
    
        this.lineNum = signature.lineNum;
        this.body = body;
        if(this.isConstruct) {
            if (this.body == null) {
                this.body = new StmtNode(StmtType.block, this.lineNum);
            } else if (this.body.type == StmtType.return_statement) {
                printError("Constructors can't return values.", body.lineNum);
            }
        }
    
        if(this.isConstruct){
            this.returnType = VariableType._void();
        }
        else {
            this.returnType = VariableType.from(containerClass.containerClassTable, signature.returnType);
            if(returnType == null) return;
        }
        if(!this.isStatic){
            localVarNumber++;
        }
        if(containerClass.isGlobal() && this.name.equals("main")){ //Проверка на мейн FIXME ? сделать ее нормальной
            if(!signature.parameters.isEmpty()){
                printError("GG-Dart's main function must have no parameters.", signature.lineNum);
                return;
            }
            ParameterRecord parameter = new ParameterRecord(this, null, new JavaArrayType(VariableType._String()), "args", false);
            parameter.number = localVarNumber;
            localVarNumber++;
            this.parameters.add(parameter);
        }
        for (FormalParameterNode parameterNode : signature.parameters) {
            ParameterRecord parameter = new ParameterRecord(this, parameterNode);
            if(this.parameters.stream().anyMatch(p-> p.name().equals(parameter.name()))){
                printError("The name '" + parameter.name() +"' is already defined.", parameterNode.lineNum);
                return;
            }
            parameter.number = localVarNumber;
            localVarNumber++;
            this.parameters.add(parameter);
        }
    }

    public void addLocalVar(LocalVarRecord var){
        var.number = ++localVarNumber;
        //Проверки на существование см. в MethodContext
        locals.put(var.number, var);
    }
    
    public String name(){
        return name;
    }
    public boolean isConstruct(){
        return isConstruct;
    }
    public boolean isConst(){
        if(!isConstruct)
            throw new IllegalStateException();
        return isConst;
    }
    public boolean isStatic(){
        if(isConstruct)
            return false;
        return isStatic;
    }
    public boolean isAbstract(){
        return this.body == null;
    }
    
    public void inferType(ClassInitContext context){
        for(ParameterRecord p : parameters){
            p.inferType(context);
        }
    }
    
    public String descriptor(){
        StringBuilder descriptor = new StringBuilder("(");
        for(ParameterRecord p : parameters){
            descriptor.append(p.varType.descriptor());
        }
        descriptor.append(")").append(returnType.descriptor());
        return descriptor.toString();
    }
    
    public boolean isValidOverrideOf(MethodRecord other){
        if(this.isConstruct || other.isConstruct)
            return false;
    
        if(this.isStatic || other.isStatic)
            return false;
            
        boolean override = other.returnType.isAssignableFrom(this.returnType) && this.name.equals(other.name) && this.parameters.size() == other.parameters.size();
        for(int i = 0; override && i < other.parameters.size(); i++){
            override = override &&
                    other.parameters.get(i).varType.isAssignableFrom(this.parameters.get(i).varType) &&
                    this.parameters.get(i).varType.isAssignableFrom(other.parameters.get(i).varType); //FIXME так ли это работает? Возможно просто надо сделать equals
        }
        
        return override;
    }

    public void normalizeConstructor(){
        if(!this.isConstruct){
            throw new IllegalStateException();
        }

        if(redirection != null){
            if(parameters.stream().anyMatch(p -> p.isField)){
                printError("The redirecting constructor can't have a field initializer.", redirection.lineNum);
            }
            RedirectionNode curRedir = redirection;
            while(curRedir != null && containerClass.constructors.containsKey(curRedir.isNamed ? curRedir.name.stringVal : "")){
                MethodRecord constructor = containerClass.constructors.get(curRedir.isNamed ? curRedir.name.stringVal : "");
                if(constructor == null){
                    printError("The constructor '" + containerClass.name()  + (curRedir.isNamed ? "." + curRedir.name : "") + "' couldn't be found in '" + containerClass.name() + "'.", redirection.lineNum);
                }
                if(constructor.equals(this)){
                    printError("Constructors can't redirect to themselves either directly or indirectly.", redirection.lineNum);
                }
                curRedir = constructor.redirection;
            }
            body.blockStmts.add(0, redirection.toStmt());
            redirection = null; //Зачем хранить инфу о том чего больше не существует...
        }
        else {
            ArrayList<FieldRecord> initializedFields = new ArrayList<>();
            Map<String, FieldRecord> canBeInitializedFields = Utils.filterByValue(this.containerClass.fields, f -> !f.isStatic);
            boolean superCalled = false;
            //обработка сигнатурных инициализаторов
            if (initializers != null) {
                for (InitializerNode initializer : initializers) {
                    if (initializer.type == InitializerType.superConstructor || initializer.type == InitializerType.superNamedConstructor) {
                        if (superCalled) {
                            printError("A constructor can have at most one 'super' initializer.", initializer.lineNum);
                        }
                        superCalled = true;
                        if (!initializers.get(initializers.size() - 1).equals(initializer)) {
                            printError("The superconstructor call must be last in an initializer list: '" + this.containerClass._super.name + "'.", initializer.lineNum);
                        }
                    }
                    else if (initializer.type == InitializerType.thisAssign) {
                        FieldRecord init = canBeInitializedFields.get(initializer.thisFieldId.stringVal); //Не "nonStaticFields", потому что нельзя использовать унаследованные поля
                        if (init == null) {
                            printError("'" + this.name + "' isn't a non-static field in the enclosing class.", lineNum);
                        }
                        if (initializedFields.contains(init)) {
                            printError("'" + init.name + "' was already initialized by this constructor.", initializer.thisFieldId.lineNum);
                        }
                        initializedFields.add(init);
                    }
                    body.blockStmts.add(0, initializer.toStmt());
                }
                initializers = null; //Зачем хранить инфу о том чего больше не существует...
            }
            if (!superCalled) {
                //Неявный вызов супер-конструктора
                if (containerClass._super != null) {
                    if (!containerClass._super.constructors.containsKey("")) {
                        printError("The class '" + containerClass._super.name() + "' doesn't have an unnamed constructor.", lineNum);
                    } else if (!containerClass._super.constructors.get("").parameters.isEmpty()) {
                        printError("The implicitly invoked unnamed constructor from '" + containerClass._super.name() + "' has required parameters.", lineNum);
                    }
                }
                ExprNode expr = new ExprNode(ExprType.constructSuper, lineNum);
                expr.constructName = null;
                expr.callArguments = new ArrayList<>();
                StmtNode init = new StmtNode(StmtType.expr_statement, lineNum);
                init.expr = expr;
                body.blockStmts.add(0, init);
            }
            //Обработка field-параметров как инициализаторов
            //FIXME ? возможно плохо что вызов суперконструктора получается после присваивания полей
            for (ParameterRecord param : parameters) {
                if (param.isField) {
                    FieldRecord init = canBeInitializedFields.get(param.name); //Не "nonStaticFields", потому что нельзя использовать унаследованные поля
                    if (init == null) {
                        printError("'" + param.name + "' isn't a non-static field in the enclosing class.", lineNum);
                    }
                    if (initializedFields.contains(init)) {
                        printError("'" + init.name + "' was already initialized by this constructor.", param.lineNum);
                    }
                    initializedFields.add(init);
                }
                param.normalize();
            }
    
            //добавление инициализаторов при полях в конструкторы
            canBeInitializedFields.values().forEach(f -> {
                if (f.initValue != null) {
                    body.blockStmts.add(0, f.initStmt());
                    initializedFields.add(f);
                }
            });
    
            Utils.filterByValue(containerClass.fields, f -> !f.isStatic && !f.varType.isNullable).values().forEach(f -> {
                if (!initializedFields.contains(f)) {
                    printError("The non-nullable variable '" + f.name + "' must be initialized.", lineNum);
                }
            });
        }

        this.containerClass.methods.put(associatedMethod().name, associatedMethod());
    }

    public void checkMethod(){
        if(isConstruct){
            throw new IllegalStateException();
        }

        if(!this.isAbstract()){
            this.body.validateStmt(new MethodContext(this));
            if(!this.body.endsWith(StmtType.return_statement)){
                if(this.returnType.equals(VariableType._void())){
                    this.body.blockStmts.add(new StmtNode(StmtType.return_statement, body.lineNum));
                }
                else {
                    if(!this.returnType.isNullable){
                        printError("The body might complete normally, causing 'null' to be returned, but the return type, '" + this.returnType + "', is a non-nullable type.", body.lineNum);
                    }
                    StmtNode returnal = new StmtNode(StmtType.return_statement, body.lineNum);
                    returnal.returnExpr = new ExprNode(ExprType.null_pr, body.lineNum);
                    this.body.blockStmts.add(returnal);
                }
            }
        }
    }
    public static final String constructorPrefix = "init!";
    public static final String getterPrefix = "get!";
    public static final String setterPrefix = "set!";

    public boolean isSyntheticConstructor(){
        return name.startsWith(constructorPrefix);
    }
    public boolean isSyntheticGetter(){
        return name.startsWith(getterPrefix);
    }
    public boolean isSyntheticSetter(){
        return name.startsWith(setterPrefix);
    }
    public boolean isSynthetic(){
        return isSyntheticConstructor() || isSyntheticGetter() || isSyntheticSetter();
    }

    private MethodRecord associatedMethod = null;
    public MethodRecord associatedMethod(){
        if(!this.isConstruct){
            throw new IllegalStateException();
        }
        if(associatedMethod == null){
            associatedMethod = new MethodRecord(this.containerClass, new ClassType(this.containerClass), constructorPrefix + this.constructName, this.parameters, this.body);
            associatedMethod.finalizeType();
            StmtNode constructReturn = new StmtNode(StmtType.return_statement, lineNum);
            constructReturn.returnExpr = new ExprNode(ExprType.this_pr, lineNum);
            associatedMethod.body.blockStmts.add(constructReturn); //FIXME? используется то же самое тело, может привести к ошибкам
            //this.containerClass.methods.put(associatedMethod.name, associatedMethod);
        }
        return associatedMethod;
    }
    
    public void copyTo(ClassRecord classRecord) {
        MethodRecord copy = null; //FIXME? Плохой клон может повлиять на что-то
        try {
            copy = (MethodRecord) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        copy.body = this.body.deepCopy();
        copy.containerClass = classRecord;
        classRecord.methods.put(copy.name(), copy);
    }
    
    public void copyAsAbstractTo(ClassRecord classRecord) {
        MethodRecord copy = null; //FIXME? Плохой клон может повлиять на что-то
        try {
            copy = (MethodRecord) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        copy.body = null;
        copy.containerClass = classRecord;
        classRecord.methods.put(copy.name(), copy);
    }
    
    public void finalizeType(){
        this.returnType.finalyze();
        this.parameters.forEach(p -> p.varType.finalyze());
        this.descriptorConst = containerClass.addConstant(new UTF8Constant(this.descriptor()));
        this.nameConst = containerClass.addConstant(new UTF8Constant(this.name()));
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.writeShort(0x0001  | // public
                0 | // private
                0 | // protected
                (this.isStatic ? 0x0008 : 0) | // static
                0 |    //final
                0 |    //synchronized
                0 |    //TODO?  A bridge method, generated by the compiler.
                0 |    // Declared with variable number of arguments.
                0 |     // native
                (this.isAbstract() ? 0x0400 : 0) | // abstract
                0 |     // strictfp
                (this.isSynthetic() ? 0x1000 : 0) //synthetic
        );

        bytes.writeShort(nameConst.number); // name_index
        bytes.writeShort(descriptorConst.number); // descriptor_index

        if(isAbstract()){
            bytes.writeShort(0); //  attributes_count
            return _bytes.toByteArray();
        }

        bytes.writeShort(1); //  attributes_count


        bytes.writeShort(1); // константа Code

        Bytecode bytecode = new Bytecode();
        body.toBytecode(bytecode);
        byte[] code = bytecode.toBytes();

        bytes.writeInt(code.length + 12); // attribute_length
        bytes.writeShort(1000); //TODO? max_stack
        bytes.writeShort(localVarNumber + 1); //TODO? max_locals
        bytes.writeInt(code.length); // code_length
        bytes.write(code); //code

        bytes.writeShort(0); //exception_table_length
        bytes.writeShort(0); //  attributes_count
        return _bytes.toByteArray();
    }

    public boolean visible = true;
}
