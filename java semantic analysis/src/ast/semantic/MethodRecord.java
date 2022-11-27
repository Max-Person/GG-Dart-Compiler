package ast.semantic;

import ast.*;
import ast.semantic.typization.StandartType;
import ast.semantic.typization.VariableType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ast.semantic.SemanticCrawler.printError;

public class MethodRecord implements NamedRecord, Cloneable{
    public ClassRecord containerClass;
    
    public Map<String, LocalVarRecord> locals = new HashMap<>(); //TODO реализовать добавление локалок с увеличением номера + добавление локалок для this и параметров при создании метода
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
        
        this.returnType = isConstruct ? StandartType._void() : returnType;
        this.name = isConstruct ? "<init>" : name;
        this.parameters = parameters;
        this.parameters.forEach(p -> p.containerMethod = this);
        
        this.isConstruct = isConstruct;
        this.constructName = isConstruct ? name : null;
        
        this.body = body;
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
        
        this.body = body;
        if(this.isConstruct) {
            if (this.body == null) {
                this.body = new StmtNode(StmtType.block);
            } else if (this.body.type == StmtType.return_statement) {
                printError("Constructors can't return values.", body.lineNum);
            }
        }
    
        if(this.isConstruct){
            this.returnType = StandartType._void();
        }
        else {
            this.returnType = VariableType.from(containerClass.containerClassTable, signature.returnType);
            if(returnType == null) return;
        }
        if(!this.isStatic){
            localVarNumber++;
        }
        for (FormalParameterNode parameterNode : signature.parameters) {
            ParameterRecord parameter = new ParameterRecord(this, parameterNode);
            if(this.parameters.stream().anyMatch(p-> p.name().equals(parameter.name()))){
                printError("The name '" + parameter.name() +"' is already defined.", parameterNode.lineNum);
                return;
            }
            parameter.number = ++localVarNumber;
            this.parameters.add(parameter);
        }
    }

    public void addLocalVar(LocalVarRecord var){
        var.number = ++localVarNumber;
        if(locals.containsKey(var.name)){
            printError("The name '" + var.name + "' is already defined.", -1); // TODO номер строки
        }
        locals.put(var.name, var);
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
            throw new IllegalStateException();
        return isStatic;
    }
    public boolean isAbstract(){
        return this.body == null;
    }
    
    public void inferType(List<FieldRecord> dependencyStack){
        for(ParameterRecord p : parameters){
            p.inferType(dependencyStack);
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

    public void checkMethod(){
        if(isConstruct){
            boolean constructorChain = false;
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
                constructorChain = true;
                redirection = null; //Зачем хранить инфу о том чего больше не существует...
            }
            else if(initializers != null){
                boolean superCalled = false;
                for (InitializerNode initializer : initializers) {
                    if(initializer.type == InitializerType.superConstructor || initializer.type == InitializerType.superNamedConstructor){
                        if(superCalled){
                            printError("A constructor can have at most one 'super' initializer.", initializer.lineNum);
                        }
                        superCalled = true;
                        if(!initializers.get(initializers.size() - 1).equals(initializer)) {
                            printError("The superconstructor call must be last in an initializer list: 'Object'.", initializer.lineNum); //FIXME не Object а суперкласс
                        }
                    }
                    else if(initializer.type == InitializerType.thisAssign){
                        if(initializers.stream().anyMatch(i -> i.type == InitializerType.thisAssign && i.thisFieldId.stringVal.equals(initializer.thisFieldId.stringVal))){
                            printError("The field '" + initializer.thisFieldId.stringVal + "' can't be initialized twice in the same constructor.", initializer.thisFieldId.lineNum);
                        }
                        if(parameters.stream().anyMatch(p -> p.isField && p.name().equals(initializer.thisFieldId.stringVal))){
                            printError("'" + initializer.thisFieldId.stringVal +"' was already initialized by this constructor.", initializer.thisFieldId.lineNum);
                        }
                    }
                    body.blockStmts.add(0, initializer.toStmt());
                }
                constructorChain = superCalled;
                initializers = null; //Зачем хранить инфу о том чего больше не существует...
            }
            if(!constructorChain){
                //Неявный вызов супер-конструктора
                if(containerClass._super != null){
                    if(!containerClass._super.constructors.containsKey("")){
                        printError("The class '"+ containerClass._super.name() +"' doesn't have an unnamed constructor.", -1); //TODO номер строки
                    }
                    else if(!containerClass._super.constructors.get("").parameters.isEmpty()){
                        printError("The implicitly invoked unnamed constructor from '"+ containerClass._super.name() +"' has required parameters.", -1); //TODO номер строки
                    }
                }
                ExprNode expr = new ExprNode();
                expr.type = ExprType.constructSuper;
                expr.constructName = new IdentifierNode("");
                expr.callArguments = new ArrayList<>();
                StmtNode init = new StmtNode(StmtType.expr_statement);
                init.expr = expr;
                body.blockStmts.add(0, init);
            }
            parameters.forEach(param-> param.normalize()); //FIXME ? возможно плохо что вызов суперконструктора получается после присваивания полей
            
            //добавление инициализаторов при полях в конструкторы
            Utils.filterByValue(containerClass.fields, f -> !f.isStatic()).values().forEach(f -> {
                if(f.initValue != null)
                    body.blockStmts.add(f.initStmt());
            });
        }
    }
    
    public void copyTo(ClassRecord classRecord) {
        MethodRecord copy = null; //FIXME? Плохой клон может повлиять на что-то
        try {
            copy = (MethodRecord) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
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
        this.descriptorConst = containerClass.addConstant(ConstantRecord.newUtf8(this.descriptor()));
        this.nameConst = containerClass.addConstant(ConstantRecord.newUtf8(this.name()));
    }
}
