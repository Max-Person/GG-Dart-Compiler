package ast;

import java.util.HashMap;
import java.util.Map;

public enum ExprType {
    this_pr,
    super_pr,
    null_pr,
    int_pr,
    double_pr,
    bool_pr,
    string_pr,
    list_pr,

    string_interpolation,

    constructNew,
    constructConst,
    constructRedirect,
    constructSuper,

    identifier,
    call,

    fieldAccess,
    methodCall,

    brackets,
    ifnull,
    _or,
    _and,
    eq,
    neq,
    greater,
    less,
    greater_eq,
    less_eq,
    type_cast,
    type_check,
    neg_type_check,
    add,
    sub,
    mul,
    _div,
    u_minus,
    _not,
    prefix_inc,
    prefix_dec,
    postfix_inc,
    postfix_dec,
    bang,

    assign,
    and_assign,
    or_assign,
    xor_assign,
    mul_assign,
    div_assign,
    add_assign,
    sub_assign,
    ifnull_assign;
    
    static final Map<ExprType, ExprType> complexAssignToOp = new HashMap<>();
    static {
        complexAssignToOp.put(and_assign, _and);
        complexAssignToOp.put(or_assign, _or);
        complexAssignToOp.put(mul_assign, mul);
        complexAssignToOp.put(div_assign, _div);
        complexAssignToOp.put(add_assign, add);
        complexAssignToOp.put(sub_assign, sub);
        complexAssignToOp.put(ifnull_assign, ifnull);
    }
}
