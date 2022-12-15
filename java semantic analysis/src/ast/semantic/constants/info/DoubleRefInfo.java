package ast.semantic.constants.info;

import ast.semantic.constants.DoubleConstant;
import ast.semantic.context.Context;

public class DoubleRefInfo implements ConstantRefInfo<DoubleConstant> {
    public final DoubleConstant constant;
    
    public DoubleRefInfo(double value, Context context) {
        this.constant = (DoubleConstant) context.currentClass().addConstant(new DoubleConstant(value));
    }
    
    @Override
    public DoubleConstant constant() {
        return constant;
    }
}
