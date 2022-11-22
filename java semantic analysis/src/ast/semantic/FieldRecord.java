package ast.semantic;

import ast.VariableDeclarationNode;
import ast.semantic.context.ClassInitContext;
import ast.semantic.context.GlobalContext;
import ast.semantic.typization.VariableType;

import java.util.List;

import static ast.semantic.SemanticCrawler.printError;

public class FieldRecord extends VariableRecord{
    public ClassRecord containerClass;
    
    public ConstantRecord descriptorConst;
    public ConstantRecord nameConst;
    
    public FieldRecord(ClassRecord containerClass, VariableDeclarationNode declaration, VariableType varType, ConstantRecord nameConst,  ConstantRecord descriptorConst){
        super(declaration, varType);
        
        this.containerClass = containerClass;
        this.nameConst = nameConst;
        this.descriptorConst = descriptorConst;
    }
    
    public VariableType inferType(List<FieldRecord> dependencyStack){
        if(this.varType == null){
            if(dependencyStack.contains(this)){
                printError("The type of '"+this.name()+"' can't be inferred because it depends on itself through the dependency cycle.", declaration.lineNum); //TODO Вывести цикл зависимости
            }
            dependencyStack.add(this);
            this.varType = this.declaration.value.annotateTypes(dependencyStack,
                    containerClass.isGlobal() ?
                    new GlobalContext(containerClass.containerClassTable) :
                    new ClassInitContext(containerClass, this.isStatic())
            );
            this.descriptorConst = containerClass.addConstant(ConstantRecord.newUtf8(this.varType.descriptor()));
            dependencyStack.remove(this); //TODO убедиться что депенденси стак работает как стак и ниче не портит...
        }
        return this.varType;
    }
}
