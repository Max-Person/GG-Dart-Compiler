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
        this.containerClass = containerClass;
    
        this.isStatic = false;
        this.isConst = false;
    
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.parameters.forEach(p -> p.containerMethod = this);
        
        this.isConstruct = false;
        this.constructName = null;
        
        this.body = body;
    }
    
    public MethodRecord(ClassRecord containerClass, SignatureNode signature, StmtNode body){
        this.containerClass = containerClass;
        
        this.isStatic = signature.isStatic;
        this.isConst = signature.isConst;
        this.isConstruct = signature.isConstruct;
        this.name = signature.isConstruct ? "<init>" : signature.name.stringVal;
        this.constructName = signature.isConstruct && signature.isNamed ? signature.constructName.stringVal : null;
        
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
    
        for (FormalParameterNode parameterNode : signature.parameters) {
            ParameterRecord parameter = new ParameterRecord(this, parameterNode);
            if(this.parameters.stream().anyMatch(p-> p.name().equals(parameter.name()))){
                printError("The name '" + parameter.name() +"' is already defined.", parameterNode.lineNum);
                return;
            }
            this.parameters.add(parameter);
        }
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
            if(redirection != null){
                if(parameters.stream().anyMatch(p -> p.isField)){
                    printError("The redirecting constructor can't have a field initializer.", redirection.lineNum);
                }
                RedirectionNode curRedir = redirection;
                while(curRedir != null && containerClass.constructors.containsKey(curRedir.isNamed ? curRedir.name.stringVal : "")){
                    MethodRecord constructor = containerClass.constructors.get(curRedir.isNamed ? curRedir.name.stringVal : "");
                    if(constructor.equals(this)){
                        printError("Constructors can't redirect to themselves either directly or indirectly.", redirection.lineNum);
                    }
                    curRedir = constructor.redirection;
                }
                StmtNode redir = new StmtNode(StmtType.expr_statement);
                redir.expr = redirection.toExpr();
                body.blockStmts.add(0, redir);
                redirection = null; //Зачем хранить инфу о том чего больше не существует...
            }
            if(initializers != null){
                boolean isSuper = false;
                for (InitializerNode initializer : initializers) {
                    if(initializer.type == InitializerType.superConstructor || initializer.type == InitializerType.superNamedConstructor){
                        if(isSuper){
                            printError("A constructor can have at most one 'super' initializer.", initializer.lineNum);
                        }
                        isSuper = true;
                        if(!initializers.get(initializers.size() - 1).equals(initializer)) {
                            printError("The superconstructor call must be last in an initializer list: 'Object'.", initializer.lineNum);
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
                    StmtNode init = new StmtNode(StmtType.expr_statement);
                    init.expr = initializer.toExpr();
                    body.blockStmts.add(0, init);
                }
                initializers = null; //Зачем хранить инфу о том чего больше не существует...
            }
            parameters.forEach(param-> param.normalize());
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
}
