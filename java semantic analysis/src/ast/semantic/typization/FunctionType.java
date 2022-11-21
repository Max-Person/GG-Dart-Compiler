package ast.semantic.typization;

import ast.FormalParameterNode;
import ast.SignatureNode;
import ast.semantic.ClassRecord;
import ast.semantic.FieldRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ast.semantic.SemanticCrawler.printError;

public class FunctionType extends ValueType{

    public VariableType returnType;
    public List<VariableType> paramTypes;
    
    FunctionType(VariableType returnType, List<VariableType> paramTypes) {
        this.returnType = returnType;
        this.paramTypes = paramTypes;
    }
    
    public static FunctionType from(Map<String, ClassRecord> classTable, SignatureNode signatureNode){
        return from(classTable, signatureNode, null, null);
    }
    public static FunctionType from(Map<String, ClassRecord> classTable, SignatureNode signatureNode, ClassRecord currentClass, List<FieldRecord> typeInferenceDependencies){
        VariableType returnType;
        if(signatureNode.isConstruct){
            returnType = StandartType._void();
        }
        else {
            returnType = VariableType.from(classTable, signatureNode.returnType);
            if(returnType == null) return null;
        }
        List<VariableType> paramTypes = new ArrayList<>();
        
        for (FormalParameterNode param : signatureNode.parameters) {
            VariableType paramType;
            String paramName;
            //FIXME Не очень нравится нагромождение в этом методе, мб пофиксить как-то...
            if(param.isField){
                if(typeInferenceDependencies != null){
                    paramName = param.initializedField.stringVal;
                    FieldRecord field = currentClass.nonStaticFields().get(paramName);
                    if(field == null){
                        printError("Undefined field name '"+ paramName +"'.", param.lineNum);
                    }
                    paramType = field.inferType(typeInferenceDependencies);
                }
                else throw new IllegalStateException();
            }
            else {
                paramName = param.paramDecl.identifier.stringVal;
                paramType = VariableType.from(classTable, param.paramDecl.declarator.valueType);
                if(paramType == null) return null;
            }
            if(signatureNode.parameters.subList(0, signatureNode.parameters.indexOf(param))
                    .stream().anyMatch(parameter-> parameter.name().equals(paramName))){
                printError("The name '" + paramName +"' is already defined.", param.lineNum);
                return null;
            }
            paramTypes.add(paramType);
        }
        return new FunctionType(returnType, paramTypes);
    }

    @Override
    public String descriptor() {
        StringBuilder descriptor = new StringBuilder("(");
        for(ValueType param: paramTypes){
            descriptor.append(param.descriptor());
        }
        descriptor.append(")").append(returnType.descriptor());
        return descriptor.toString();
    }
}
