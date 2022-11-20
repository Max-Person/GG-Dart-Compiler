package ast.semantic;

import ast.*;
import ast.semantic.typization.FunctionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ast.semantic.SemanticCrawler.printError;

public class MethodRecord implements NamedRecord{
    public ClassRecord containerClass;
    
    public Map<String, Integer> locals = new HashMap<>();
    
    public SignatureNode signature;
    public StmtNode body;
    public FunctionType type;
    public ConstantRecord nameConst;
    public ConstantRecord descriptorConst;
    
    public MethodRecord(ClassRecord containerClass, SignatureNode signature, StmtNode body, FunctionType type,  ConstantRecord nameConst,  ConstantRecord descriptorConst){
        this.containerClass = containerClass;
        this.signature = signature;
        this.body = body;
        this.type = type;
        this.nameConst = nameConst;
        this.descriptorConst = descriptorConst;
    }
    
    public String name(){
        if(signature.isConstruct){
            return signature.isNamed ? "<init>" : "<init>"; //TODO
        }
        else return signature.name.stringVal;
    }
    public boolean isConstruct(){
        return signature.isConstruct;
    }
    public boolean isConst(){
        if(!signature.isConstruct)
            throw new IllegalStateException();
        return signature.isConst;
    }
    public boolean isStatic(){
        if(signature.isConstruct)
            throw new IllegalStateException();
        return signature.isStatic;
    }
    
    public void inferType(List<FieldRecord> dependencyStack){
        if(this.type == null){
            this.type = FunctionType.from(containerClass.containerClassTable, signature, containerClass, new ArrayList<>());
            this.descriptorConst = containerClass.addConstant(ConstantRecord.newUtf8(this.type.descriptor()));
        }
    }

    public void checkMethod(){
        if(this.signature.isConstruct){
            if(this.body == null){
                this.body = new StmtNode(StmtType.block);
            }
            RedirectionNode redirection = signature.redirection;
            if(redirection != null){
                StmtNode redir = new StmtNode(StmtType.expr_statement);
                redir.expr = redirection.toExpr();
                this.body.blockStmts.add(0, redir);
            }
            if(this.signature.initializers != null){
                boolean isSuper = false;
                for (InitializerNode initializer : signature.initializers) {
                    if(initializer.type == InitializerType.superConstructor || initializer.type == InitializerType.superNamedConstructor){
                        if(!isSuper){
                            isSuper = true;
                        }else {
                            printError("A constructor can have at most one 'super' initializer.", initializer.lineNum);
                        }
                        if(!signature.initializers.get(signature.initializers.size() - 1).equals(initializer)) {
                            printError("The superconstructor call must be last in an initializer list: 'Object'.", initializer.lineNum);
                        }
                    }
                    if(initializer.type == InitializerType.thisAssign){
                        if(signature.initializers.stream().filter(initializerNode -> initializerNode.type == InitializerType.thisAssign
                                && initializerNode.thisFieldId.stringVal.equals(initializer.thisFieldId.stringVal)).count() > 1){
                            printError("The field '" + initializer.thisFieldId.stringVal + "' can't be initialized twice in the same constructor.", initializer.thisFieldId.lineNum);
                        }
                        if(signature.parameters.stream().anyMatch(formalParameterNode -> formalParameterNode.isField
                                && formalParameterNode.initializedField.stringVal.equals(initializer.thisFieldId.stringVal))){
                            printError("'" + initializer.thisFieldId.stringVal +"' was already initialized by this constructor.", initializer.thisFieldId.lineNum);
                        }
                    }
                }
            }
        }
    }
}
