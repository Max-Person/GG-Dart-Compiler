package ast.semantic.constants.info;

import ast.semantic.ClassRecord;
import ast.semantic.constants.ClassConstant;
import ast.semantic.context.Context;

public class ClassRefInfo implements ConstantRefInfo<ClassConstant> {
    public final ClassConstant constant;
    
    public ClassRefInfo(ClassRecord clazz, Context context) {
        this.constant = (ClassConstant) context.currentClass().addClassConstant(clazz);
    }
    
    @Override
    public ClassConstant constant() {
        return constant;
    }
}
