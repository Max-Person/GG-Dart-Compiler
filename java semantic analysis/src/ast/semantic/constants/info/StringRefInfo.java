package ast.semantic.constants.info;

import ast.semantic.constants.StringConstant;
import ast.semantic.constants.UTF8Constant;
import ast.semantic.context.Context;

public class StringRefInfo implements ConstantRefInfo<StringConstant> {
    public final StringConstant constant;
    
    public StringRefInfo(String value, Context context) {
        UTF8Constant valueConst = (UTF8Constant) context.currentClass().addConstant(new UTF8Constant(value));
        this.constant = (StringConstant) context.currentClass().addConstant(new StringConstant(valueConst));
    }
    
    @Override
    public StringConstant constant() {
        return constant;
    }
}
