package ast.semantic.typization;

import ast.FormalParameterNode;
import ast.SignatureNode;
import ast.TypeNode;
import ast.semantic.ClassRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ast.semantic.SemanticCrawler.printError;

public class FunctionType extends VariableType{

    public VariableType returnType;
    public List<VariableType> paramTypes;
    
    FunctionType(VariableType returnType, List<VariableType> paramTypes) {
        this.returnType = returnType;
        this.paramTypes = paramTypes;
    }
    
    public static FunctionType from(Map<String, ClassRecord> classTable, SignatureNode signatureNode){
        VariableType returnType = VariableType.from(classTable, signatureNode.returnType);
        if(returnType == null) return null;
        List<VariableType> paramTypes = new ArrayList<>();
        
        for (FormalParameterNode param : signatureNode.parameters) {
            VariableType paramType = VariableType.from(classTable, param.paramDecl.declarator.valueType);
            if(paramType == null) return null;
            if(signatureNode.parameters.stream().anyMatch(parameter->parameter.paramDecl.identifier.stringVal.equals(param.paramDecl.identifier.stringVal))){
                printError("The name '" + param.paramDecl.identifier.stringVal +"' is already defined.", param.paramDecl.identifier.lineNum);
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
