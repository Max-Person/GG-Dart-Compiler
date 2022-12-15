package ast.semantic.constants.info;

import ast.semantic.ClassRecord;
import ast.semantic.FieldRecord;
import ast.semantic.constants.FieldRefConstant;
import ast.semantic.context.Context;

public class FieldRefInfo implements ConstantRefInfo<FieldRefConstant> {
    public final FieldRefConstant constant;
    public final FieldRecord field;
    
    public FieldRefInfo(FieldRecord field, ClassRecord owner, Context context){
        this.constant = context.currentClass().addFieldRefConstant(owner, field);
        this.field = field;
    }
    
    public FieldRefInfo(FieldRecord field, Context context){
        this.constant = context.currentClass().addFieldRefConstant(field.containerClass, field);
        this.field = field;
    }
    
    @Override
    public FieldRefConstant constant() {
        return constant;
    }
    
    public boolean isStatic(){
        return field.isStatic();
    }
}
