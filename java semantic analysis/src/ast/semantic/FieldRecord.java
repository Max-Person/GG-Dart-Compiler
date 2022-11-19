package ast.semantic;

import ast.VariableDeclarationNode;
import ast.semantic.context.ClassInitContext;
import ast.semantic.context.GlobalContext;
import ast.semantic.typization.VariableType;

import java.util.List;

import static ast.semantic.SemanticCrawler.printError;

public class FieldRecord implements NamedRecord{
    public ClassRecord containerClass;
    
    public VariableDeclarationNode declaration;
    public ConstantRecord descriptorConst;
    public ConstantRecord nameConst;
    public VariableType varType;
    
    public FieldRecord(ClassRecord containerClass, VariableDeclarationNode declaration, VariableType varType, ConstantRecord nameConst,  ConstantRecord descriptorConst){
        if(containerClass == null)
            throw new IllegalArgumentException();
        if(declaration == null)
            throw new IllegalArgumentException();
        
        this.containerClass = containerClass;
        this.declaration = declaration;
        this.varType = varType;
        this.nameConst = nameConst;
        this.descriptorConst = descriptorConst;
    }

    public String name(){
        return declaration.identifier.stringVal;
    }
    public boolean isLate(){
        return declaration.declarator.isLate;
    }
    public boolean isStatic(){
        return declaration.declarator.isStatic;
    }
    public boolean isConst(){
        return declaration.declarator.isConst;
    }
    public boolean isFinal(){
        return declaration.declarator.isFinal;
    }
    public VariableType type(){return varType;}
    
    public VariableType inferType(List<FieldRecord> dependencyStack){
        if(dependencyStack.contains(this)){
            printError("The type of '"+this.name()+"' can't be inferred because it depends on itself through the dependency cycle.", declaration.lineNum); //TODO Вывести цикл зависимости
        }
        dependencyStack.add(this);
        if(this.varType == null){
            this.varType = this.declaration.value.annotateTypes(dependencyStack,
                    containerClass.isGlobal() ?
                    new GlobalContext(containerClass.containerClassTable) :
                    new ClassInitContext(containerClass.containerClassTable, containerClass, this.isStatic())
            );
            this.descriptorConst = containerClass.addConstant(ConstantRecord.newUtf8(this.varType.descriptor()));
        }
        return this.varType;
    }
}
