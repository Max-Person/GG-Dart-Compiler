package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

enum ExprType {
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

public class ExprNode extends Node {

    ExprType type;

    long intValue;
    double doubleValue;
    boolean boolValue;
    String stringValue;

    List<ExprNode> listValues = new ArrayList<>();
    IdentifierNode identifierAccess;
    List<ExprNode> callArguments = new ArrayList<>();

    IdentifierNode constructName;

    ExprNode operand;
    ExprNode operand2;
    TypeNode typeForCheckOrCast;

    TypeNode annotatedType = null;

    public ExprNode(Element element) {
        super(element);
        type = ExprType.valueOf(element.getAttribute("type"));

        if(type == ExprType.int_pr){
            intValue = Long.parseLong(element.getAttribute("int_value"));
        }
        if(type == ExprType.double_pr){
            doubleValue = Long.parseLong(element.getAttribute("double_value"));
        }
        if(type == ExprType.string_pr){
            stringValue = element.getAttribute("string_value");
        }
        if(type == ExprType.bool_pr){
            boolValue = Boolean.parseBoolean(element.getAttribute("bool_value"));
        }
        if(type == ExprType.list_pr && element.getElementsByTagName("values").getLength() > 0){
            unlinkList(element, "values").forEach(e -> listValues.add(new ExprNode(e)));
        }

        if(type == ExprType.string_interpolation){
            stringValue = element.getAttribute("string_value");
            operand = new ExprNode(unlink(element, "operand"));
            operand2 = new ExprNode(unlink(element, "operand2"));
        }

        if(isBinaryOp()){
            operand = new ExprNode(unlink(element, "operand"));
            operand2 = new ExprNode(unlink(element, "operand2"));
        }

        if(type == ExprType.fieldAccess){
            operand = new ExprNode(unlink(element, "operand"));
            identifierAccess = new IdentifierNode(unlink(element, "identifierAccess"));
        }

        if(type == ExprType.methodCall){
            operand = new ExprNode(unlink(element, "operand"));
            identifierAccess = new IdentifierNode(unlink(element, "identifierAccess"));
            if(element.getElementsByTagName("callArguments").getLength() > 0){
                unlinkList(element, "callArguments").forEach(e -> callArguments.add(new ExprNode(e)));
            }
        }

        if(type == ExprType.constructNew || type == ExprType.constructConst){
            identifierAccess = new IdentifierNode(unlink(element, "identifierAccess"));
            if(element.getElementsByTagName("callArguments").getLength() > 0){
                unlinkList(element, "callArguments").forEach(e -> callArguments.add(new ExprNode(e)));
            }
            if(element.getElementsByTagName("constructName").getLength() > 0){
                constructName = new IdentifierNode(unlink(element, "constructName"));
            }
        }

        if(type == ExprType.call){
            identifierAccess = new IdentifierNode(unlink(element, "identifierAccess"));
            if(element.getElementsByTagName("callArguments").getLength() > 0){
                unlinkList(element, "callArguments").forEach(e -> callArguments.add(new ExprNode(e)));
            }
        }

        if(type == ExprType.identifier){
            identifierAccess = new IdentifierNode(unlink(element, "identifierAccess"));
        }

        if(type == ExprType.type_cast || type == ExprType.neg_type_check || type == ExprType.type_check){
            operand = new ExprNode(unlink(element, "operand"));
            typeForCheckOrCast = new TypeNode(unlink(element, "typeForCheckOrCast"));
        }

        if(isUnaryOp()){
            operand = new ExprNode(unlink(element, "operand"));
        }

    }

    private boolean isBinaryOp(){
        return type == ExprType.brackets || type == ExprType.ifnull || type == ExprType._or || type == ExprType._and || type == ExprType.eq
                || type == ExprType.neq || type == ExprType.greater || type == ExprType.less || type == ExprType.greater_eq || type == ExprType.less_eq
                || type == ExprType.add || type == ExprType.sub || type == ExprType.mul || type == ExprType._div || type == ExprType.assign || type == ExprType.and_assign
                || type == ExprType.or_assign || type == ExprType.xor_assign || type == ExprType.mul_assign || type == ExprType.div_assign || type == ExprType.add_assign
                || type == ExprType.sub_assign || type == ExprType.ifnull_assign;
    }

    private boolean isUnaryOp(){
        return type == ExprType.u_minus || type == ExprType._not || type == ExprType.prefix_inc || type == ExprType.prefix_dec || type == ExprType.postfix_inc || type == ExprType.postfix_dec || type == ExprType.bang;
    }

}
