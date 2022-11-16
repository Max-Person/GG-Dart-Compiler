package ast;

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
    ifnull_assign
}
