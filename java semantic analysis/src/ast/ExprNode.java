package ast;

import ast.semantic.ClassRecord;
import ast.semantic.FieldRecord;
import ast.semantic.MethodRecord;
import ast.semantic.NamedRecord;
import ast.semantic.context.ClassInitContext;
import ast.semantic.context.Context;
import ast.semantic.typization.ClassType;
import ast.semantic.typization.ListType;
import ast.semantic.typization.StandartType;
import ast.semantic.typization.VariableType;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import static ast.semantic.SemanticCrawler.printError;

public class ExprNode extends Node {
    
    public ExprType type;
    
    public long intValue;
    public double doubleValue;
    public boolean boolValue;
    public String stringValue;
    
    public List<ExprNode> listValues = new ArrayList<>();
    public IdentifierNode identifierAccess;
    public List<ExprNode> callArguments = new ArrayList<>();
    
    public IdentifierNode constructName;
    
    public ExprNode operand;
    public ExprNode operand2;
    public TypeNode typeForCheckOrCast;
    
    public VariableType annotatedType = null;

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
    
    //Для преобразование бинарных операторов
    private ExprNode(ExprType type, ExprNode operand, ExprNode operand2){
        this.type = type;
        this.operand = operand;
        this.operand2 = operand2;
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
    
    //TODO преобразовать в resolveExpr - запихнуть всю логику статической типизации и преобразований выражений сюда.
    public VariableType annotateTypes(List<FieldRecord> dependencyStack, Context context){
        VariableType result = null;
        if(this.type == ExprType.this_pr){
            VariableType type = context.thisType();
            if(type == null){
                printError("invoking 'this' in a static context", this.lineNum);
            }
            result = type;
        }
        else if(this.type == ExprType.super_pr){ //FIXME убедиться
            printError("Can't use 'super' as an expression", this.lineNum);
        }
        else if(this.type == ExprType.null_pr){
            result = StandartType._null();
        }
        else if(this.type == ExprType.int_pr){
            result = StandartType._int();
        }
        else if(this.type == ExprType.double_pr){
            result = StandartType._double();
        }
        else if(this.type == ExprType.bool_pr){
            result = StandartType._bool();
        }
        else if(this.type == ExprType.string_pr){
            result = StandartType._String();
        }
        else if(this.type == ExprType.list_pr){
            VariableType element = null;
            for(ExprNode el: this.listValues){
                VariableType cur = el.annotateTypes(dependencyStack, context);
                if(element == null){
                    element = cur;
                }
                if(!element.equals(cur)){
                    printError("elements of a List must be of the same type.", el.lineNum);
                }
            }
            result = new ListType(element);
        }
        else if(this.type == ExprType.string_interpolation){
            this.operand.annotateTypes(dependencyStack, context);
            this.operand2.annotateTypes(dependencyStack, context); //TODO если не стринг то вызвать .toString()
            result = StandartType._String();
        }
        else if(this.type == ExprType.constructNew || this.type == ExprType.constructConst){
            ClassRecord constructed = context.lookupClass(this.identifierAccess.stringVal);
            if(constructed == null){
                printError("Unknown class '"+ this.identifierAccess.stringVal +"'.", this.identifierAccess.lineNum);
            }
            String constructorName = this.constructName != null ? this.constructName.stringVal : "";    //TODO записывать неименованный конструктор под пустым именем при сборе информации
            MethodRecord constructor = constructed.constructors.get(constructorName);
            if(constructor == null){
                printError("Cannot find constructor '"+ constructorName +"' in '"+ constructed.name() +"'.", this.lineNum);
            }
            
            constructor.inferType(dependencyStack);
            checkCallArgumentsTyping(dependencyStack, constructor, context);
            result = new ClassType(constructed);
        }
        else if(this.type == ExprType.identifier){
            //TODO вставить проверку на нуллабельность?
            NamedRecord foundRecord = context.lookup(this.identifierAccess.stringVal);
            if(foundRecord == null){
                printError("Undefined name '"+ this.identifierAccess.stringVal +"'.", this.lineNum);
            }
            else if(foundRecord instanceof ClassRecord){
                printError("Can't use class name '"+ this.identifierAccess.stringVal + "' as an expression", this.lineNum);
            }
            else if(foundRecord instanceof MethodRecord){
                printError("'"+ this.identifierAccess.stringVal + "' is a method and must be called", this.lineNum);
            }
            else {
                FieldRecord field = (FieldRecord) foundRecord;
                field.inferType(dependencyStack);
                if(context instanceof ClassInitContext && field.containerClass.equals(((ClassInitContext) context).classRecord) && !context.isStatic() && !field.isStatic()){
                    printError("The instance member '" + field.name() + "' can't be accessed in an initializer.", this.lineNum);
                }
                result = field.varType;
            }
        }
        else if(this.type == ExprType.call){
            NamedRecord foundRecord = context.lookup(this.identifierAccess.stringVal);
            if(foundRecord == null){
                printError("Undefined name '"+ this.identifierAccess.stringVal +"'.", this.lineNum);
            }
            else if(foundRecord instanceof FieldRecord){
                printError("'"+ this.identifierAccess.stringVal + "' is a variable and cannot be called", this.lineNum);
            }
            else if(foundRecord instanceof ClassRecord){
                //неименованный конструктор вызван без new или const
                this.type = ExprType.constructNew;
                return this.annotateTypes(dependencyStack, context);
            }
            else {
                MethodRecord method = (MethodRecord) foundRecord;
                checkCallArgumentsTyping(dependencyStack, method, context);
                result = method.type.returnType;
            }
        }
        else if(this.type == ExprType.fieldAccess){
            //TODO вставить проверку на нуллабельность?
            FieldRecord field = null;
            ClassRecord classRecord = null;
            if(operand.type == ExprType.identifier && context.lookupClass(operand.identifierAccess.stringVal) != null){
                classRecord = context.lookupClass(operand.identifierAccess.stringVal);
                field = classRecord.staticFields().get(this.identifierAccess.stringVal);
            }
            else {
                VariableType op = operand.annotateTypes(dependencyStack, context);
                if(!(op instanceof ClassType)){
                    printError("Cannot find field '"+ this.identifierAccess.stringVal +"' in '"+ op.toString() +"'.", this.lineNum);
                }
                classRecord = ((ClassType) op).clazz;
                field = classRecord.fields.get(this.identifierAccess.stringVal);
            }
            if(field == null){
                printError("Cannot find field '"+ this.identifierAccess.stringVal +"' in '"+ classRecord.name() +"'.", this.lineNum);
            }
            field.inferType(dependencyStack);
            if(context instanceof ClassInitContext && field.containerClass.equals(((ClassInitContext) context).classRecord) && !context.isStatic() && !field.isStatic()){
                printError("The instance member '" + field.name() + "' can't be accessed in an initializer.", this.lineNum);
            }
            result = field.varType;
            
        }
        else if (this.type == ExprType.methodCall) {
            MethodRecord method = null;
            ClassRecord classRecord = null;
            if(operand.type == ExprType.identifier && context.lookupClass(operand.identifierAccess.stringVal) != null){
                classRecord = context.lookupClass(operand.identifierAccess.stringVal);
                method = classRecord.staticMethods().get(this.identifierAccess.stringVal);
                if(method == null && classRecord.constructors.containsKey(operand.identifierAccess.stringVal)){
                    //Именованный конструктор вызван без new или const
                    this.type = ExprType.constructNew;
                    this.constructName = this.identifierAccess;
                    this.identifierAccess = this.operand.identifierAccess;
                    this.operand = null;
                    return this.annotateTypes(dependencyStack, context);
                }
            }
            else {
                VariableType op = operand.annotateTypes(dependencyStack, context);
                if(!(op instanceof ClassType)){ //TODO у стандартных типов и листов тоже могут быть методы.
                    printError("Cannot find method '"+ this.identifierAccess.stringVal +"' in '"+ op.toString() +"'.", this.lineNum);
                }
                classRecord = ((ClassType) op).clazz;
                method = classRecord.methods.get(this.identifierAccess.stringVal);
            }
            if(method == null){
                printError("Cannot find method '"+ this.identifierAccess.stringVal +"' in '"+ classRecord.name() +"'.", this.lineNum);
            }
            //TODO ? Здесь можно сделать проверку на instance member, но т.к. у нас функция не объект, оно нам не мешает
            checkCallArgumentsTyping(dependencyStack, method, context);
            result = method.type.returnType;
        }
        else if(this.type == ExprType.type_cast){
            operand.annotateTypes(dependencyStack, context);
            result = VariableType.from(context.classTable, this.typeForCheckOrCast);
        }
        else if (this.type == ExprType.type_check || this.type == ExprType.neg_type_check) {
            operand.annotateTypes(dependencyStack, context);
            result = StandartType._bool();
        }
        else if(this.isBinaryOp()){
            operand.annotateTypes(dependencyStack, context);
            operand2.annotateTypes(dependencyStack, context);
            if(this.type == ExprType.add || this.type == ExprType.sub || this.type == ExprType.mul || this.type == ExprType._div){
                if(!StandartType._double().isAssignableFrom(operand.annotatedType)){
                    printError("Cannot perform arithmetic on type '"+ operand.annotatedType.toString() +"' .", operand.lineNum);
                }
                if(!StandartType._double().isAssignableFrom(operand2.annotatedType)){
                    printError("Cannot perform arithmetic on type '"+ operand2.annotatedType.toString() +"' .", operand2.lineNum);
                }
                
                if(this.type == ExprType._div){
                    result = StandartType._double();
                }
                else{
                    result = operand.annotatedType.equals(StandartType._int()) && operand2.annotatedType.equals(StandartType._int()) ?
                            StandartType._int() :
                            StandartType._double();
                }
            }
            else if(this.type == ExprType.brackets) {
                if(!(operand.annotatedType instanceof ListType)){
                    printError("Cannot perform brackets op on type '"+ operand.annotatedType.toString() +"' .", operand.lineNum);
                }
                if(!StandartType._int().isAssignableFrom(operand2.annotatedType)){
                    printError("The value type '" + operand2.annotatedType.toString() + "' can't be assigned to the expected type 'int'.", operand2.lineNum);
                }
                result = ((ListType)operand.annotatedType).valueType;
            }
            else if(this.type == ExprType.ifnull){
                if(!operand.annotatedType.equals(operand2.annotatedType)){
                    printError("Types of operands in ?? operator must be equal for the expression to be statically typed.", this.lineNum);
                }
                result = operand.annotatedType;
            }
            else if(this.type == ExprType.eq || this.type == ExprType.neq){
                result = StandartType._bool();
            }
            else if(this.type == ExprType.greater || this.type == ExprType.greater_eq || this.type == ExprType.less || this.type == ExprType.less_eq){
                if(!StandartType._double().isAssignableFrom(operand.annotatedType)){
                    printError("Cannot perform comparisons on type '"+ operand.annotatedType.toString() +"' .", operand.lineNum);
                }
                if(!StandartType._double().isAssignableFrom(operand2.annotatedType)){
                    printError("Cannot perform comparisons on type '"+ operand2.annotatedType.toString() +"' .", operand2.lineNum);
                }
                result = StandartType._bool();
            }
            else if(this.type == ExprType._or || this.type == ExprType._and){
                if(!StandartType._bool().isAssignableFrom(operand.annotatedType)){
                    printError("Cannot perform logic on type '"+ operand.annotatedType.toString() +"' .", operand.lineNum);
                }
                if(!StandartType._bool().isAssignableFrom(operand2.annotatedType)){
                    printError("Cannot perform logic on type '"+ operand2.annotatedType.toString() +"' .", operand2.lineNum);
                }
                result = StandartType._bool();
            }
            else if(this.type == ExprType.assign){
                if(operand.type != ExprType.identifier && operand.type != ExprType.fieldAccess && operand.type != ExprType.brackets){
                    printError("Value must be assignable.", operand.lineNum);
                }
                if(!operand.annotatedType.isAssignableFrom(operand2.annotatedType)){
                    printError("The value of type '" + operand2.annotatedType.toString() + "' can't be assigned to the value of type '"+operand.annotatedType.toString()+"'.", this.lineNum);
                }
                result = operand2.annotatedType;
            }
            else {
                //Комплексные ассигнменты
                ExprNode expanded = new ExprNode(ExprType.complexAssignToOp.get(this.type), this.operand, this.operand2);
                this.type = ExprType.assign;
                this.operand2 = expanded;
                return this.annotateTypes(dependencyStack, context);
            }
            
        }
        else {
            //Унарные операторы
            operand.annotateTypes(dependencyStack, context);
            if(this.type == ExprType._not){
                if(!StandartType._bool().isAssignableFrom(operand.annotatedType)){
                    printError("Cannot perform logic on type '"+ operand.annotatedType.toString() +"' .", operand.lineNum);
                }
                result = StandartType._bool();
            }
            else if(this.type == ExprType.bang){
                result = (VariableType) operand.annotatedType.clone(); //TODO проверить
                result.isNullable = false;
            }
            else {
                //Арифметические унарные
                if(!StandartType._double().isAssignableFrom(operand.annotatedType)){
                    printError("Cannot perform arithmetic on type '"+ operand.annotatedType.toString() +"' .", operand.lineNum);
                }
                result = operand.annotatedType;
            }
        
        }
    
        this.annotatedType = result;
        return result;
    }
    
    private void checkCallArgumentsTyping(List<FieldRecord> dependencyStack, MethodRecord method, Context context){
        if(this.callArguments == null) throw new IllegalStateException();
        
        List<VariableType> argTypes = new ArrayList<>();
        this.callArguments.forEach(arg -> argTypes.add(arg.annotateTypes(dependencyStack, context)));
        if(argTypes.size() != method.type.paramTypes.size()){
            printError("Parameter count mismatch", this.lineNum); //TODO улучшить сообщение
        }
        for(int i = 0; i< method.type.paramTypes.size(); i++){
            VariableType paramType = method.type.paramTypes.get(i);
            VariableType argType = argTypes.get(i);
            if(!paramType.isAssignableFrom(argType)){
                printError("The argument type '" + argType.toString() + "' can't be assigned to the parameter type '"+ paramType.toString()+"'.", this.lineNum);
            }
        }
    }

}
