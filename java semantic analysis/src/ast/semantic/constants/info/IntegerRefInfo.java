package ast.semantic.constants.info;

import ast.semantic.constants.IntegerConstant;
import ast.semantic.context.Context;

public class IntegerRefInfo implements ConstantRefInfo<IntegerConstant> {
    public final IntegerConstant constant;
    
    public IntegerRefInfo(int value, Context context) {
        this.constant = (IntegerConstant) context.currentClass().addConstant(new IntegerConstant(value));
    }
    
    @Override
    public IntegerConstant constant() {
        return constant;
    }
}
