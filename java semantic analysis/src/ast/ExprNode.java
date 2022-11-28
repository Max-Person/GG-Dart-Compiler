package ast;

import ast.semantic.*;
import ast.semantic.context.ClassInitContext;
import ast.semantic.context.Context;
import ast.semantic.context.MethodContext;
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
    public NamedRecord annotatedRecord = null;

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

    public ExprNode(ExprType type) {
        this.type = type;
    }

    public ExprNode() {
    }
    
    private boolean isAssign(){
        return type == ExprType.assign || type == ExprType.and_assign
                || type == ExprType.or_assign || type == ExprType.xor_assign || type == ExprType.mul_assign || type == ExprType.div_assign || type == ExprType.add_assign
                || type == ExprType.sub_assign || type == ExprType.ifnull_assign;
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
    public VariableType annotateTypes(Context context){
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
                VariableType cur = el.annotateTypes(context);
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
            this.operand.annotateTypes(context);
            this.operand2.annotateTypes(context); //TODO если не стринг то вызвать .toString()
            result = StandartType._String();
        }
        else if(this.type == ExprType.constructNew || this.type == ExprType.constructConst ||
                this.type == ExprType.constructRedirect || this.type == ExprType.constructSuper){
            
            ClassRecord constructed;
            if(this.type == ExprType.constructRedirect){
                constructed = context.currentClass();
            } else if (this.type == ExprType.constructSuper) {
                constructed = context.currentClass()._super; //TODO сделать чтобы у всех классов был супер
            }else {
                constructed = context.lookupClass(this.identifierAccess.stringVal);
            }
            if(constructed == null){
                printError("Unknown class '"+ this.identifierAccess.stringVal +"'.", this.identifierAccess.lineNum);
            }
            String constructorName = this.constructName != null ? this.constructName.stringVal : "";
            MethodRecord constructor = constructed.constructors.get(constructorName);
            if(constructor == null){
                printError("Cannot find constructor '"+ constructorName +"' in '"+ constructed.name() +"'.", this.lineNum);
            }
            if(context instanceof ClassInitContext){
                constructor.inferType((ClassInitContext) context);
            }
            checkCallArgumentsTyping(constructor, context);
            this.annotatedRecord = constructor;
            result = this.type == ExprType.constructNew || this.type == ExprType.constructConst ? new ClassType(constructed) : StandartType._void();
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
            else if(foundRecord instanceof FieldRecord){
                FieldRecord field = (FieldRecord) foundRecord;
                if(context instanceof ClassInitContext){
                    if(field.containerClass.equals(((ClassInitContext) context).classRecord) && !field.isStatic()){
                        printError("The instance member '" + field.name() + "' can't be accessed in an initializer.", this.lineNum);
                    }
                    field.inferType((ClassInitContext) context);
                }
                if(context instanceof MethodContext && ((MethodContext) context).isSytheticGetterOrSetter()){
                    this.annotatedRecord = field;
                    result = field.varType;
                }
                else {
                    //замена доступа к полю на вызов метода
                    this.type = ExprType.call;
                    this.identifierAccess.stringVal = field.associatedGetter().name();
                    this.callArguments = new ArrayList<>();
                    return this.annotateTypes(context);
                }
            }
            else {
                this.annotatedRecord = foundRecord;
                result = ((LocalVarRecord) foundRecord).varType;
            }
        }
        else if(this.type == ExprType.call){
            NamedRecord foundRecord = context.lookup(this.identifierAccess.stringVal);
            if(foundRecord == null){
                printError("Undefined name '"+ this.identifierAccess.stringVal +"'.", this.lineNum);
            }
            else if(foundRecord instanceof VariableRecord){
                printError("'"+ this.identifierAccess.stringVal + "' is a variable and cannot be called", this.lineNum);
            }
            else if(foundRecord instanceof ClassRecord){
                //неименованный конструктор вызван без new или const
                this.type = ExprType.constructNew;
                return this.annotateTypes(context);
            }
            else {
                MethodRecord method = (MethodRecord) foundRecord;
                checkCallArgumentsTyping(method, context);
                this.annotatedRecord = method;
                result = method.returnType;
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
                VariableType op = operand.annotateTypes(context);
                if(!(op instanceof ClassType)){
                    printError("Cannot find field '"+ this.identifierAccess.stringVal +"' in '"+ op.toString() +"'.", this.lineNum);
                }
                classRecord = ((ClassType) op).clazz;
                field = classRecord.nonStaticFields().get(this.identifierAccess.stringVal);
            }
            if(field == null){
                printError("Cannot find field '"+ this.identifierAccess.stringVal +"' in '"+ classRecord.name() +"'.", this.lineNum);
            }
            if(context instanceof ClassInitContext){
                //FIXME instance member-ы недоступны  при инициализации только если они принадлежат this, а не просто тому же классу
                if(field.containerClass.equals(((ClassInitContext) context).classRecord) && !field.isStatic()){
                    printError("The instance member '" + field.name() + "' can't be accessed in an initializer.", this.lineNum);
                }
                field.inferType((ClassInitContext) context);
            }
            if(context instanceof MethodContext && ((MethodContext) context).isSytheticGetterOrSetter()){
                this.annotatedRecord = field;
                result = field.varType;
            }
            else {
                //замена доступа к полю на вызов метода
                this.type = ExprType.methodCall;
                this.identifierAccess.stringVal = field.associatedGetter().name();
                this.callArguments = new ArrayList<>();
                return this.annotateTypes(context);
            }
            
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
                    return this.annotateTypes(context);
                }
            }
            else {
                VariableType op = operand.annotateTypes(context);
                if(!(op instanceof ClassType)){ //TODO у стандартных типов и листов тоже могут быть методы.
                    printError("Cannot find method '"+ this.identifierAccess.stringVal +"' in '"+ op.toString() +"'.", this.lineNum);
                }
                classRecord = ((ClassType) op).clazz;
                method = classRecord.nonStaticMethods().get(this.identifierAccess.stringVal);
            }
            if(method == null){
                printError("Cannot find method '"+ this.identifierAccess.stringVal +"' in '"+ classRecord.name() +"'.", this.lineNum);
            }
            //TODO ? Здесь можно сделать проверку на instance member, но т.к. у нас функция не объект, оно нам не мешает
            checkCallArgumentsTyping(method, context);
            result = method.returnType;
        }
        else if(this.type == ExprType.type_cast){
            operand.annotateTypes(context);
            result = VariableType.from(context.classTable(), this.typeForCheckOrCast);
        }
        else if (this.type == ExprType.type_check || this.type == ExprType.neg_type_check) {
            operand.annotateTypes(context);
            result = StandartType._bool();
        }
        else if(this.isAssign()){
            if(this.type == ExprType.assign){
                if(operand.type != ExprType.identifier && operand.type != ExprType.fieldAccess && operand.type != ExprType.brackets){
                    printError("Value must be assignable.", operand.lineNum);
                }
    
                //замена присвоения на вызов сеттера
                if(!(context instanceof MethodContext && ((MethodContext) context).isSytheticGetterOrSetter())){
                    //FIXME ? Здесь очень большой повтор кода, но я не знаю как сделать лучше без особых запарок
                    if(operand.type == ExprType.identifier && context.lookupField(operand.identifierAccess.stringVal) != null){
                        FieldRecord field = context.lookupField(operand.identifierAccess.stringVal);
                        if(context instanceof ClassInitContext){
                            //FIXME instance member-ы недоступны  при инициализации только если они принадлежат this, а не просто тому же классу
                            //(Для indentifier это не проблема ?)
                            if(field.containerClass.equals(((ClassInitContext) context).classRecord) && !field.isStatic()){
                                printError("The instance member '" + field.name() + "' can't be accessed in an initializer.", this.lineNum);
                            }
                            field.inferType((ClassInitContext) context);
                        }
                        this.type = ExprType.call;
                        this.identifierAccess.stringVal = field.associatedSetter().name();
                        this.callArguments = List.of(this.operand2);
                        this.operand = null;
                        this.operand2 = null;
                        return this.annotateTypes(context);
                    }
                    else if(operand.type == ExprType.fieldAccess){
                        FieldRecord field = null;
                        ClassRecord classRecord = null;
                        if(operand.operand.type == ExprType.identifier && context.lookupClass(operand.operand.identifierAccess.stringVal) != null){
                            classRecord = context.lookupClass(operand.operand.identifierAccess.stringVal);
                            field = classRecord.staticFields().get(operand.identifierAccess.stringVal);
                        }
                        else {
                            VariableType op = operand.operand.annotateTypes(context);
                            if(!(op instanceof ClassType)){
                                printError("Cannot find field '"+ operand.identifierAccess.stringVal +"' in '"+ op.toString() +"'.", operand.lineNum);
                            }
                            classRecord = ((ClassType) op).clazz;
                            field = classRecord.nonStaticFields().get(operand.identifierAccess.stringVal);
                        }
                        if(field == null){
                            printError("Cannot find field '"+ operand.identifierAccess.stringVal +"' in '"+ classRecord.name() +"'.", operand.lineNum);
                        }
                        if(context instanceof ClassInitContext){
                            //FIXME instance member-ы недоступны  при инициализации только если они принадлежат this, а не просто тому же классу
                            if(field.containerClass.equals(((ClassInitContext) context).classRecord) && !field.isStatic()){
                                printError("The instance member '" + field.name() + "' can't be accessed in an initializer.", this.lineNum);
                            }
                            field.inferType((ClassInitContext) context);
                        }
                        this.type = ExprType.methodCall;
                        this.identifierAccess.stringVal = field.associatedSetter().name();
                        this.callArguments = List.of(this.operand2);
                        this.operand = this.operand.operand;
                        this.operand2 = null;
                        return this.annotateTypes(context);
                    }
                }
                
                operand.annotateTypes(context);
                operand2.annotateTypes(context);
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
                return this.annotateTypes(context);
            }
        }
        else if(this.isBinaryOp()){
            operand.annotateTypes(context);
            operand2.annotateTypes(context);
            if(this.type == ExprType.add || this.type == ExprType.sub || this.type == ExprType.mul || this.type == ExprType._div){
                if(!StandartType._double().isAssignableFrom(operand.annotatedType)){
                    printError("Cannot perform arithmetic on type '"+ operand.annotatedType.toString() +"'.", operand.lineNum);
                }
                if(!StandartType._double().isAssignableFrom(operand2.annotatedType)){
                    printError("Cannot perform arithmetic on type '"+ operand2.annotatedType.toString() +"'.", operand2.lineNum);
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
                    printError("Cannot perform logic on type '"+ operand.annotatedType.toString() +"'.", operand.lineNum);
                }
                if(!StandartType._bool().isAssignableFrom(operand2.annotatedType)){
                    printError("Cannot perform logic on type '"+ operand2.annotatedType.toString() +"'.", operand2.lineNum);
                }
                result = StandartType._bool();
            }
            
        }
        else {
            //Унарные операторы
            operand.annotateTypes(context);
            if(this.type == ExprType._not){
                if(!StandartType._bool().isAssignableFrom(operand.annotatedType)){
                    printError("Cannot perform logic on type '"+ operand.annotatedType.toString() +"'.", operand.lineNum);
                }
                result = StandartType._bool();
            }
            else if(this.type == ExprType.bang){
                result = operand.annotatedType.clone(); //TODO проверить
                result.isNullable = false;
            }
            else {
                //Арифметические унарные
                if(!StandartType._double().isAssignableFrom(operand.annotatedType)){
                    printError("Cannot perform arithmetic on type '"+ operand.annotatedType.toString() +"'.", operand.lineNum);
                }
                result = operand.annotatedType;
            }
        
        }
    
        this.annotatedType = result;
        return result;
    }
    
    private void checkCallArgumentsTyping(MethodRecord method, Context context){
        if(this.callArguments == null) throw new IllegalStateException();
        
        List<VariableType> argTypes = new ArrayList<>();
        this.callArguments.forEach(arg -> argTypes.add(arg.annotateTypes(context)));
        if(argTypes.size() != method.parameters.size()){
            printError("Parameter count mismatch", this.lineNum); //TODO улучшить сообщение
        }
        for(int i = 0; i< method.parameters.size(); i++){
            VariableType paramType = method.parameters.get(i).varType;
            VariableType argType = argTypes.get(i);
            if(!paramType.isAssignableFrom(argType)){
                printError("The argument type '" + argType.toString() + "' can't be assigned to the parameter type '"+ paramType.toString()+"'.", this.lineNum);
            }
        }
    }

}
