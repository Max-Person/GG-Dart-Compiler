package ast;

import ast.semantic.*;
import ast.semantic.constants.info.*;
import ast.semantic.context.ClassInitContext;
import ast.semantic.context.Context;
import ast.semantic.context.MethodContext;
import ast.semantic.typization.ClassType;
import ast.semantic.typization.ListType;
import ast.semantic.typization.PlainType;
import ast.semantic.typization.VariableType;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    public RefInfo refInfo = null;

    public ExprNode(Element element) {
        super(element);
        type = ExprType.valueOf(element.getAttribute("type"));

        if(type == ExprType.int_pr){
            intValue = Long.parseLong(element.getAttribute("int_value"));
        }
        if(type == ExprType.double_pr){
            doubleValue = Double.parseDouble(element.getAttribute("double_value"));
        }
        if(type == ExprType.string_pr){
            stringValue = element.getAttribute("string_value");
        }
        if(type == ExprType.bool_pr){
            boolValue = Boolean.parseBoolean(element.getAttribute("bool_value"));
        }
        if(type == ExprType.list_pr && Node.getImmediateChildByName(element, "values") != null){
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
            if(Node.getImmediateChildByName(element, "callArguments") != null){
                unlinkList(element, "callArguments").forEach(e -> callArguments.add(new ExprNode(e)));
            }
        }

        if(type == ExprType.constructNew || type == ExprType.constructConst){
            identifierAccess = new IdentifierNode(unlink(element, "identifierAccess"));
            if(Node.getImmediateChildByName(element, "callArguments") != null){
                unlinkList(element, "callArguments").forEach(e -> callArguments.add(new ExprNode(e)));
            }
            if(Node.getImmediateChildByName(element, "constructName") != null){
                constructName = new IdentifierNode(unlink(element, "constructName"));
            }
        }

        if(type == ExprType.call){
            identifierAccess = new IdentifierNode(unlink(element, "identifierAccess"));
            if(Node.getImmediateChildByName(element, "callArguments") != null){
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
    
    public void mimic(ExprNode other){
        this.type = other.type;
        this.intValue =  other.intValue;
        this.doubleValue =  other.doubleValue;
        this.boolValue =  other.boolValue;
        this.stringValue =  other.stringValue;
        this.listValues =  new ArrayList<>(other.listValues);
        this.identifierAccess =  other.identifierAccess;
        this.callArguments =  new ArrayList<>(other.callArguments);
        this.constructName =  other.constructName;
        this.operand =  other.operand;
        this.operand2 =  other.operand2;
        this.typeForCheckOrCast =  other.typeForCheckOrCast;
        this.annotatedType =  other.annotatedType;
        this.refInfo =  other.refInfo;
        
        this.getterTransformsAllowed = other.getterTransformsAllowed;
        this.isSynthetic = other.isSynthetic;
    }
    
    public void clear(){
        this.mimic(new ExprNode());
    }
    
    public ExprNode(ExprNode other){
        this.mimic(other);
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
    
    private void wrapPlainAsClass(){
        ExprNode val = new ExprNode(this);
        this.type = ExprType.methodCall;
        this.operand = new ExprNode(ExprType.identifier);
        
        VariableType result;
        if(this.annotatedType.equals(PlainType._bool())){
            this.operand.identifierAccess = new IdentifierNode(RTLClassRecord._bool.name());
            result = VariableType._bool();
        }
        else if(this.annotatedType.equals(PlainType._int())){
            this.operand.identifierAccess = new IdentifierNode(RTLClassRecord._integer.name());
            result = VariableType._int();
        }
        else if(this.annotatedType.equals(PlainType._double())){
            this.operand.identifierAccess = new IdentifierNode(RTLClassRecord._double.name());
            result = VariableType._double();
        }
        else {
            throw new IllegalStateException();
        }
        this.isSynthetic = true;
        this.identifierAccess = new IdentifierNode("valueOf");
        this.callArguments = List.of(val);
        this.annotatedType = result;
    }
    
    private void unwrapClassToPlain(){
        ExprNode op = new ExprNode(this);
        this.type = ExprType.methodCall;
        this.operand = op;
        
        VariableType result;
        if(this.annotatedType.equals(VariableType._bool())){
            this.identifierAccess = new IdentifierNode("booleanValue");
            result = PlainType._bool();
        }
        else if(this.annotatedType.equals(VariableType._int())){
            this.identifierAccess = new IdentifierNode("intValue");
            result = PlainType._int();
        }
        else if(this.annotatedType.equals(VariableType._double())){
            this.identifierAccess = new IdentifierNode("doubleValue");
            result = PlainType._double();
        }
        else {
            throw new IllegalStateException();
        }
        this.isSynthetic = true;
        this.callArguments = new ArrayList<>();
        this.annotatedType = result;
    }
    
    public boolean makeAssignableTo(VariableType type, Context context){
        if(type.isAssignableFrom(this.annotatedType))
            return true;
        
        if(this.annotatedType instanceof PlainType && !(type instanceof PlainType)){
            this.wrapPlainAsClass();
            if(context != null) this.annotateTypes(context);
            return type.isAssignableFrom(this.annotatedType);
        }
        else if(!(this.annotatedType instanceof PlainType) && type instanceof PlainType){
            this.unwrapClassToPlain();
            if(context != null) this.annotateTypes(context);
            return this.makeAssignableTo(type, context);
        }
        else if(this.annotatedType.equals(PlainType._int()) && type.equals(PlainType._double())){
            ExprNode op = new ExprNode(this);
            this.clear();
            this.type = ExprType.i2d;
            this.operand = op;
            this.annotatedType = PlainType._double();
            return true;
        }
        return false;
    }
    
    public boolean canBeAssignableTo(VariableType type){
        return new ExprNode(this).makeAssignableTo(type, null);
    }
    
    public void assertNotVoid(){
        if(this.annotatedType != null && this.annotatedType.equals(VariableType._void())){
            printError("Attempting to use 'void' in an expression", this.lineNum);
        }
    }
    
    private boolean getterTransformsAllowed = true;
    public boolean isSynthetic = false;
    
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
            result = VariableType._null();
        }
        else if(this.type == ExprType.int_pr){
            if(this.intValue != (int) this.intValue){
                printError(intValue + " is too big to fit into 4 bytes", this.lineNum);
            }
            if(this.intValue < -32768 || this.intValue > 32767){
                refInfo = new IntegerRefInfo((int) this.intValue, context);
            }
            result = PlainType._int();
        }
        else if(this.type == ExprType.double_pr){
            refInfo = new DoubleRefInfo(this.doubleValue, context);
            result = PlainType._double();
        }
        else if(this.type == ExprType.bool_pr){
            result = PlainType._bool();
        }
        else if(this.type == ExprType.string_pr){
            refInfo = new StringRefInfo(this.stringValue, context);
            result = VariableType._String();
        }
        else if(this.type == ExprType.list_pr){
            VariableType element = null;
            for(ExprNode el: this.listValues){
                el.annotateTypes(context);
                el.assertNotVoid();
                el.makeAssignableTo(VariableType._Object(), context);
                if(element == null){
                    element = el.annotatedType;
                }
                else{
                    element = new ClassType(ClassRecord.lastCommonSuper(element.associatedClass(), el.annotatedType.associatedClass()));
                }
            }

            ExprNode construct = new ExprNode(ExprType.constructNew);
            construct.identifierAccess = new IdentifierNode("List");
            construct.annotatedType = new ListType(element);
            construct.isSynthetic = true;
            ExprNode op = construct;
            for(ExprNode el: this.listValues){
                ExprNode with = new ExprNode(ExprType.methodCall);
                with.operand = op;
                with.identifierAccess = new IdentifierNode("with");
                with.callArguments = List.of(el);
                with.annotatedType = new ListType(element);
                with.isSynthetic = true;
                op = with;
            }

            this.mimic(op);
            this.annotateTypes(context); //FIXME ?
            result = new ListType(element);
        }
        else if(this.type == ExprType.string_interpolation){
            this.operand.annotateTypes(context);
            this.operand2.annotateTypes(context);
            operand2.assertNotVoid();
            operand2.makeAssignableTo(VariableType._Object(), context);
            if(!operand2.annotatedType.equals(VariableType._String())){
                ExprNode toString = new ExprNode(ExprType.methodCall);
                toString.operand = this.operand2;
                toString.identifierAccess = new IdentifierNode("toString");
                this.operand2 = toString;
            }
            
            ExprNode second = new ExprNode(ExprType.string_pr);
            second.stringValue = this.stringValue;
            
            ExprNode concat1 = new ExprNode(ExprType.methodCall);
            concat1.isSynthetic = true;
            concat1.identifierAccess = new IdentifierNode("concat");
            concat1.operand = this.operand;
            concat1.callArguments = List.of(this.operand2);
            ExprNode concat2 = new ExprNode(ExprType.methodCall);
            concat2.isSynthetic = true;
            concat2.identifierAccess = new IdentifierNode("concat");
            concat2.operand = concat1;
            concat2.callArguments = List.of(second);
            this.mimic(concat2);
            return this.annotateTypes(context);
        }
        else if(this.type == ExprType.constructNew || this.type == ExprType.constructConst ||
                this.type == ExprType.constructRedirect || this.type == ExprType.constructSuper){
            
            ClassRecord constructed;
            if(this.type == ExprType.constructRedirect){
                constructed = context.currentClass();
            } else if (this.type == ExprType.constructSuper) {
                constructed = context.currentClass()._super;
            }else {
                constructed = context.lookupClass(this.identifierAccess.stringVal);
                if(constructed == null){
                    printError("Unknown class '"+ this.identifierAccess.stringVal +"'.", this.identifierAccess.lineNum);
                }
                if(constructed.isAbstract() || (constructed.isEnum() && !this.isSynthetic)){
                    printError("Can't instantiate '"+ this.identifierAccess.stringVal +"'.", this.identifierAccess.lineNum);
                }
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
            //this.annotatedRecord = constructor;

            if(context instanceof MethodContext){ //Заменить конструкторы на вызовы методов
                if(this.type == ExprType.constructRedirect){
                    this.operand = new ExprNode(ExprType.this_pr);
                    this.type = ExprType.methodCall;
                    this.identifierAccess = new IdentifierNode(constructor.associatedMethod().name);
                    return this.annotateTypes(context);
                }else if (this.type == ExprType.constructSuper) {
                    if(constructed.equals(RTLClassRecord.object)){
                        this.mimic(new ExprNode(ExprType.this_pr)); //FIXME? Здесь как бы удаление экспра
                        return this.annotateTypes(context);
                    }
                    this.operand = new ExprNode(ExprType.super_pr);
                    this.type = ExprType.methodCall;
                    this.identifierAccess = new IdentifierNode(constructor.associatedMethod().name);
                    return this.annotateTypes(context);
                }else {
                    if(constructed instanceof RTLClassRecord){ //Если заранее скомпилированный класс то просто вызвать джава-конструктор
                        this.type = ExprType.javaConstructCall;
                        this.identifierAccess = new IdentifierNode(constructed.name);
                        return this.annotateTypes(context);
                    }
                    else { //Если пользовательский класс - то вызвать джава конструктор и затем метод, представляющий дарт-конструктор
                        this.operand = new ExprNode(ExprType.javaConstructCall);
                        this.operand.identifierAccess = new IdentifierNode(constructed.name);
                        this.operand.callArguments = new ArrayList<>();
                        this.type = ExprType.methodCall;
                        this.identifierAccess = new IdentifierNode(constructor.associatedMethod().name);
                        return this.annotateTypes(context);
                    }
                }
            }
            else {
                result = this.type == ExprType.constructNew || this.type == ExprType.constructConst ? new ClassType(constructed) : VariableType._void();
            }
        }
        else if(this.type == ExprType.javaConstructSuper){
            refInfo = MethodRefInfo.invokeSpecial(context.currentClass()._super.methods.get("<init>"), context.currentClass()._super, context);
            result = VariableType._void();
        }
        else if(this.type == ExprType.javaConstructCall){
            ClassRecord constructed = context.lookupClass(this.identifierAccess.stringVal); //FIXME? тут проверки не нужны тк создается нами уже после
            refInfo = MethodRefInfo.invokeSpecial(constructed.methods.get("<init>"), constructed, context);
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
            else if(foundRecord instanceof FieldRecord){
                FieldRecord field = (FieldRecord) foundRecord;
                if(context instanceof ClassInitContext){
                    if(field.containerClass.equals(((ClassInitContext) context).classRecord) && !field.isStatic()){
                        printError("The instance member '" + field.name() + "' can't be accessed in an initializer.", this.lineNum);
                    }
                    field.inferType((ClassInitContext) context);
                }
                if((context instanceof MethodContext && ((MethodContext) context).isSytheticGetterOrSetter()) || !getterTransformsAllowed){
                    this.refInfo = new FieldRefInfo(field, context);
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
                this.refInfo = new LocalVarRefInfo((LocalVarRecord) foundRecord);
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
                this.refInfo = method.isStatic() ? MethodRefInfo.invokeStatic(method, context) : MethodRefInfo.invokeVirtual(method, method.containerClass, context);
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
                operand.annotateTypes(context);
                operand.assertNotVoid();
                operand.makeAssignableTo(VariableType._Object(), context);
                classRecord = operand.annotatedType.associatedClass();
                field = classRecord.nonStaticFields().get(this.identifierAccess.stringVal);
            }
            if(field == null){
                printError("Cannot find field '"+ this.identifierAccess.stringVal +"' in '"+ classRecord.name() +"'.", this.lineNum);
            }
            if(context instanceof ClassInitContext){
                //FIXED instance member-ы недоступны  при инициализации только если они принадлежат this, а не просто тому же классу
                if(operand.type == ExprType.this_pr && !field.isStatic()){
                    printError("The instance member '" + field.name() + "' can't be accessed in an initializer.", this.lineNum);
                }
                field.inferType((ClassInitContext) context);
            }
            if((context instanceof MethodContext && ((MethodContext) context).isSytheticGetterOrSetter()) || !getterTransformsAllowed){
                this.refInfo = new FieldRefInfo(field, classRecord, context);
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
            boolean isSpecial = false;
            ClassRecord classRecord = null;
            if(operand.type == ExprType.identifier && context.lookupClass(operand.identifierAccess.stringVal) != null){
                classRecord = context.lookupClass(operand.identifierAccess.stringVal);
                method = classRecord.staticMethods().get(this.identifierAccess.stringVal);
                if(method == null && classRecord.constructors.containsKey(this.identifierAccess.stringVal)){
                    //Именованный конструктор вызван без new или const
                    this.type = ExprType.constructNew;
                    this.constructName = this.identifierAccess;
                    this.identifierAccess = this.operand.identifierAccess;
                    this.operand = null;
                    return this.annotateTypes(context);
                }
            }
            else {
                if(operand.type == ExprType.super_pr){
                    if(context instanceof MethodContext){
                        classRecord = context.currentClass()._super;
                        isSpecial = true;
                    }
                    else {
                        printError("Invalid context for ‘super’ invocation.", operand.lineNum);
                    }
                }
                else {
                    operand.annotateTypes(context);
                    operand.assertNotVoid();
                    operand.makeAssignableTo(VariableType._Object(), context);
                    classRecord = operand.annotatedType.associatedClass();
                }
                method = classRecord.nonStaticMethods().get(this.identifierAccess.stringVal);
            }
            if(method == null || (!method.visible && !this.isSynthetic)){
                printError("Cannot find method '"+ this.identifierAccess.stringVal +"' in '"+ classRecord.name() +"'.", this.lineNum);
            }
            if(context instanceof ClassInitContext && operand.type == ExprType.this_pr && !method.isStatic()){
                printError("The instance member '" + method.name() + "' can't be accessed in an initializer.", this.lineNum);
            }
            checkCallArgumentsTyping(method, context);
            isSpecial = isSpecial && method.isSyntheticConstructor();
            this.refInfo = method.isStatic() ?
                    MethodRefInfo.invokeStatic(method, context) :
                    isSpecial ?
                            MethodRefInfo.invokeSpecial(method, classRecord, context) :
                            MethodRefInfo.invokeVirtual(method, classRecord, context);
            result = method.returnType;
        }
        else if(this.type == ExprType.type_cast){
            operand.annotateTypes(context);
            operand.assertNotVoid();
            operand.makeAssignableTo(VariableType._Object(), context);
            result = VariableType.from(context.classTable(), this.typeForCheckOrCast);
        }
        else if (this.type == ExprType.type_check || this.type == ExprType.neg_type_check) {
            operand.annotateTypes(context);
            operand.assertNotVoid();
            operand.makeAssignableTo(VariableType._Object(), context);
            result = VariableType._bool(); //TODO ? доп преобразования?
        }
        else if(this.isAssign()){
            if(this.type == ExprType.assign){
                if(operand.type != ExprType.identifier && operand.type != ExprType.fieldAccess && operand.type != ExprType.brackets){
                    printError("Value must be assignable.", operand.lineNum);
                }
    
                //замена присвоения на вызов сеттера
                if(!(context instanceof MethodContext && ((MethodContext) context).isSytheticGetterOrSetter()) &&
                        ((operand.type == ExprType.identifier && context.lookupField(operand.identifierAccess.stringVal) != null) ||
                        (operand.type == ExprType.fieldAccess))){
                    operand.getterTransformsAllowed = false;
                    operand.annotateTypes(context);
                    FieldRecord field = ((FieldRefInfo) operand.refInfo).field;
                    this.type = operand.type == ExprType.identifier ? ExprType.call : ExprType.methodCall;
                    this.identifierAccess = new IdentifierNode(field.associatedSetter().name());
                    this.callArguments = List.of(this.operand2);
                    this.operand = this.operand.operand;
                    this.operand2 = null;
                    this.isSynthetic = true;
                    return this.annotateTypes(context);
                }
                
                //Замена скобок на вызов метода set
                if(operand.type == ExprType.brackets){
                    operand.getterTransformsAllowed = false;
                    operand.annotateTypes(context);
                    this.type = ExprType.methodCall;
                    this.identifierAccess = new IdentifierNode("set");
                    this.callArguments = List.of(operand.operand2, operand2);
                    this.operand = operand.operand;
                    this.operand2 = null;
                    this.isSynthetic = true;
                    return this.annotateTypes(context);
                }
                
                operand.annotateTypes(context); //Здесь штуки типа makeAssignableTo(Object) не нужны, так как слева все равно либо переменная либо []
                operand2.annotateTypes(context);
                if(!operand2.makeAssignableTo(operand.annotatedType, context)){
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
            operand.assertNotVoid();
            operand2.annotateTypes(context);
            operand2.assertNotVoid();
            if(this.type == ExprType.add || this.type == ExprType.sub || this.type == ExprType.mul || this.type == ExprType._div){
                if(this.type == ExprType.add &&
                        operand.annotatedType.equals(VariableType._String()) &&
                        operand2.annotatedType.equals(VariableType._String())){ //сложение строк в конкатенацию
                    ExprNode concat = new ExprNode(ExprType.methodCall);
                    concat.isSynthetic = true;
                    concat.identifierAccess = new IdentifierNode("concat");
                    concat.operand = this.operand;
                    concat.callArguments = List.of(this.operand2);
                    this.mimic(concat);
                    return this.annotateTypes(context);
                }
                
                if(!operand.canBeAssignableTo(PlainType._double())){
                    printError("Cannot perform arithmetic on type '"+ operand.annotatedType.toString() +"'.", operand.lineNum);
                }
                if(!operand2.canBeAssignableTo(PlainType._double())){
                    printError("Cannot perform arithmetic on type '"+ operand2.annotatedType.toString() +"'.", operand2.lineNum);
                }
                
                if(operand.canBeAssignableTo(PlainType._int()) && operand2.canBeAssignableTo(PlainType._int()) && this.type != ExprType._div){
                    operand.makeAssignableTo(PlainType._int(), context);
                    operand2.makeAssignableTo(PlainType._int(), context);
                    result = PlainType._int();
                }
                else {
                    operand.makeAssignableTo(PlainType._double(), context);
                    operand2.makeAssignableTo(PlainType._double(), context);
                    result = PlainType._double();
                }
            }
            else if(this.type == ExprType.brackets) {
                if(!(operand.annotatedType instanceof ListType)){
                    printError("Cannot perform brackets op on type '"+ operand.annotatedType.toString() +"' .", operand.lineNum);
                }
                if(getterTransformsAllowed){
                    this.type = ExprType.methodCall;
                    this.identifierAccess = new IdentifierNode("elementAt");
                    this.callArguments = List.of(this.operand2);
                    this.operand2 = null;
                    this.isSynthetic = true;
                    return this.annotateTypes(context);
                }
                else {
                    if(!operand2.makeAssignableTo(VariableType._int(), context)) {
                        printError("The value type '" + operand2.annotatedType.toString() + "' can't be assigned to the expected type 'int'.", operand2.lineNum);
                    }
                    result = ((ListType) operand.annotatedType).valueType;
                }
            }
            else if(this.type == ExprType.ifnull){
                operand.makeAssignableTo(VariableType._Object(), context);
                operand2.makeAssignableTo(VariableType._Object(), context);
                if(!operand.annotatedType.equals(operand2.annotatedType)){
                    printError("Types of operands in ?? operator must be equal for the expression to be statically typed.", this.lineNum);
                }
                result = operand.annotatedType;
            }
            else if(this.type == ExprType.eq || this.type == ExprType.neq){
                if(operand.annotatedType.equals(VariableType._String())){
                    this.type = ExprType.methodCall;
                    this.identifierAccess = new IdentifierNode("equals");
                    this.callArguments = List.of(operand2);
                    this.operand2 = null;
                    this.isSynthetic = true;
                    if(this.type == ExprType.neq){
                        ExprNode op = new ExprNode(this);
                        this.type = ExprType._not;
                        this.operand = op;
                        this.identifierAccess = null;
                        this.callArguments= null;
                    }
                    return this.annotateTypes(context);
                }
                result = PlainType._bool();
            }
            else if(this.type == ExprType.greater || this.type == ExprType.greater_eq || this.type == ExprType.less || this.type == ExprType.less_eq){
                if(!operand.canBeAssignableTo(PlainType._double())){
                    printError("Cannot perform comparisons on type '"+ operand.annotatedType.toString() +"'.", operand.lineNum);
                }
                if(!operand2.canBeAssignableTo(PlainType._double())){
                    printError("Cannot perform comparisons on type '"+ operand2.annotatedType.toString() +"'.", operand2.lineNum);
                }
    
                if(operand.canBeAssignableTo(PlainType._int()) && operand2.canBeAssignableTo(PlainType._int())){
                    operand.makeAssignableTo(PlainType._int(), context);
                    operand2.makeAssignableTo(PlainType._int(), context);
                }
                else {
                    operand.makeAssignableTo(PlainType._double(), context);
                    operand2.makeAssignableTo(PlainType._double(), context);
                }
                result = PlainType._bool();
            }
            else if(this.type == ExprType._or || this.type == ExprType._and){
                if(!operand.makeAssignableTo(PlainType._bool(), context)){
                    printError("Cannot perform logic on type '"+ operand.annotatedType.toString() +"'.", operand.lineNum);
                }
                if(!operand2.makeAssignableTo(PlainType._bool(), context)){
                    printError("Cannot perform logic on type '"+ operand2.annotatedType.toString() +"'.", operand2.lineNum);
                }
                result = PlainType._bool();
            }
            
        }
        else {
            //Унарные операторы
            if(this.type == ExprType.u_minus){
                if(this.operand.type == ExprType.int_pr){
                    this.mimic(this.operand);
                    this.intValue = -this.intValue;
                    return this.annotateTypes(context);
                }
                if(this.operand.type == ExprType.double_pr){
                    this.mimic(this.operand);
                    this.doubleValue = -this.doubleValue;
                    return this.annotateTypes(context);
                }
            }
            
            operand.annotateTypes(context);
            operand.assertNotVoid();
            if(this.type == ExprType._not){
                if(!operand.makeAssignableTo(PlainType._bool(), context)){
                    printError("Cannot perform logic on type '"+ operand.annotatedType.toString() +"'.", operand.lineNum);
                }
                result = PlainType._bool();
            }
            else if(this.type == ExprType.bang){
                operand.makeAssignableTo(VariableType._Object(), context);
                result = operand.annotatedType.clone(); //TODO проверить
                result.isNullable = false;
            }
            else if(this.type == ExprType.i2d){
                operand.makeAssignableTo(PlainType._int(), context); //Как бы не нужно??
                result = PlainType._double();
            }
            else {
                //Арифметические унарные
                if(!operand.canBeAssignableTo(PlainType._double())){
                    printError("Cannot perform arithmetic on type '"+ operand.annotatedType.toString() +"'.", operand.lineNum);
                }
    
                if(operand.canBeAssignableTo(PlainType._int())){
                    operand.makeAssignableTo(PlainType._int(), context);
                }
                else {
                    operand.makeAssignableTo(PlainType._double(), context);
                }
                result = operand.annotatedType;
            }
        
        }

        if(result == null){
            throw new IllegalStateException();
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
            ExprNode arg = this.callArguments.get(i);
            arg.assertNotVoid();
            if(!arg.makeAssignableTo(paramType, context)){
                printError("The argument type '" + argType.toString() + "' can't be assigned to the parameter type '"+ paramType.toString()+"'.", this.lineNum);
            }
        }
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);

        if(this.type == ExprType.this_pr){
            bytes.write(BytecodeUtils.loadThis());
        }
        else if(this.type == ExprType.null_pr){
            bytes.write(BytecodeUtils.loadNull());
        }
        else if(this.type == ExprType.int_pr){
            if(this.intValue >= -1 && this.intValue <= 5){
                bytes.write(BytecodeUtils.loadInt(this.intValue));
            } else if(this.intValue >= -128 && this.intValue <= 127){
                bytes.write(BytecodeUtils.bipushInt(this.intValue));
            } else if(this.intValue >= -32768 && this.intValue <= 32767){
                bytes.write(BytecodeUtils.sipushInt(this.intValue));
            } else {
                bytes.write(BytecodeUtils.loadConstant(((IntegerRefInfo) this.refInfo).constant));
            }
        }
        else if(this.type == ExprType.double_pr){
            bytes.write(BytecodeUtils.loadConstant(((DoubleRefInfo) this.refInfo).constant));
        }
        else if(this.type == ExprType.bool_pr){
            bytes.write(BytecodeUtils.loadBoolean(this.boolValue));
        }
        else if(this.type == ExprType.string_pr){
            bytes.write(BytecodeUtils.loadConstant(((StringRefInfo) this.refInfo).constant));
        }
        else if(this.type == ExprType.javaConstructSuper){
            MethodRefInfo methodRefInfo = ((MethodRefInfo) this.refInfo);
            bytes.write(BytecodeUtils.loadThis());
            bytes.write(BytecodeUtils.invokeMethod(methodRefInfo));
        }
        else if(this.type == ExprType.javaConstructCall){
            MethodRefInfo methodRefInfo = ((MethodRefInfo) this.refInfo);
            bytes.write(BytecodeUtils._new(methodRefInfo.constant.classConst.number));
            bytes.write(BytecodeUtils.dup());
            bytes.write(BytecodeUtils.invokeMethod(methodRefInfo));
        }
        else if(this.type == ExprType.identifier){
            if(this.refInfo instanceof LocalVarRefInfo){
                bytes.write(BytecodeUtils.aload(((LocalVarRefInfo) this.refInfo).localVar.number));
            }
            else {
                FieldRefInfo fieldRefInfo = ((FieldRefInfo) this.refInfo);
                if(!fieldRefInfo.field.isStatic()){
                    bytes.write(BytecodeUtils.loadThis());
                }
                bytes.write(BytecodeUtils.loadField(fieldRefInfo));
            }
        }
        else if(this.type == ExprType.call){
            MethodRefInfo methodRefInfo = ((MethodRefInfo) this.refInfo);
            if(!methodRefInfo.method.isStatic()){
                bytes.write(BytecodeUtils.loadThis());
            }
            for (ExprNode arg : callArguments) {
                bytes.write(arg.toBytes());
            }
            bytes.write(BytecodeUtils.invokeMethod(methodRefInfo));
        }
        else if(this.type == ExprType.fieldAccess){
            FieldRefInfo fieldRefInfo = ((FieldRefInfo) this.refInfo);
            if(!fieldRefInfo.field.isStatic()){
                bytes.write(this.operand.toBytes());
            }
            bytes.write(BytecodeUtils.loadField(fieldRefInfo));
        }
        else if (this.type == ExprType.methodCall) {
            MethodRefInfo methodRefInfo = ((MethodRefInfo) this.refInfo);
            if(!methodRefInfo.method.isStatic()){
                bytes.write(this.operand.toBytes());
            }
            for (ExprNode arg : callArguments) {
                bytes.write(arg.toBytes());
            }
            bytes.write(BytecodeUtils.invokeMethod(methodRefInfo));
        }
        else if(this.type == ExprType.type_cast){
        }
        else if (this.type == ExprType.type_check || this.type == ExprType.neg_type_check) {
        }
        else if(this.isAssign()){
            if(this.type == ExprType.assign){
                if(this.operand.refInfo instanceof LocalVarRefInfo){
                    bytes.write(this.operand2.toBytes());
                    bytes.write(BytecodeUtils.dup());
                    bytes.write(BytecodeUtils.astore(((LocalVarRefInfo) this.operand.refInfo).localVar.number));
                }
                else {
                    FieldRefInfo fieldRefInfo = ((FieldRefInfo) this.operand.refInfo);
                    if(!fieldRefInfo.field.isStatic()){
                        if(this.operand.type == ExprType.fieldAccess){
                            bytes.write(this.operand.operand.toBytes());
                        }
                        else{
                            bytes.write(BytecodeUtils.loadThis());
                        }
                        bytes.write(BytecodeUtils.dup());
                    }
                    bytes.write(this.operand2.toBytes());
                    bytes.write(BytecodeUtils.storeField(fieldRefInfo));
                    bytes.write(BytecodeUtils.loadField(fieldRefInfo));
                }
            }
            else {
                throw new IllegalStateException();
            }
        }
        else if(this.isBinaryOp()){
            bytes.write(this.operand.toBytes());
            bytes.write(this.operand2.toBytes());

            if(this.type == ExprType.add || this.type == ExprType.sub || this.type == ExprType.mul || this.type == ExprType._div){
                boolean isInt = PlainType._int().isAssignableFrom(this.operand.annotatedType);
                if(this.type == ExprType.add)
                    bytes.write(isInt ? BytecodeUtils.Instruction.iadd.code : BytecodeUtils.Instruction.dadd.code);
                else if(this.type == ExprType.sub)
                    bytes.write(isInt ? BytecodeUtils.Instruction.isub.code : BytecodeUtils.Instruction.dsub.code);
                else if(this.type == ExprType.mul)
                    bytes.write(isInt ? BytecodeUtils.Instruction.imul.code : BytecodeUtils.Instruction.dmul.code);
                else if(this.type == ExprType._div)
                    bytes.write(BytecodeUtils.Instruction.ddiv.code);
            
            }
            else if(this.type == ExprType.ifnull){

            }
            else if(this.type == ExprType.eq || this.type == ExprType.neq){

            }
            else if(this.type == ExprType.greater || this.type == ExprType.greater_eq || this.type == ExprType.less || this.type == ExprType.less_eq){
                if(PlainType._int().isAssignableFrom(this.operand.annotatedType)){
                    if(this.type == ExprType.greater)
                        bytes.write(BytecodeUtils.Instruction.if_icmple.code);
                    else if(this.type == ExprType.greater_eq)
                        bytes.write(BytecodeUtils.Instruction.if_icmplt.code);
                    else if(this.type == ExprType.less)
                        bytes.write(BytecodeUtils.Instruction.if_icmpge.code);
                    else if(this.type == ExprType.less_eq)
                        bytes.write(BytecodeUtils.Instruction.if_icmpgt.code);
    
                    bytes.writeShort(7);
                    bytes.write(BytecodeUtils.iconst_1());
                    bytes.write(BytecodeUtils._goto(4));
                    bytes.write(BytecodeUtils.iconst_0());

                }
                else{
                    //TODO double
                }

            }
            else if(this.type == ExprType._or || this.type == ExprType._and){

            }
            else
                throw new IllegalStateException();

        }
        else {
            //Унарные операторы
            bytes.write(this.operand.toBytes());
            if(this.type == ExprType._not){

            }
            else if(this.type == ExprType.bang){

            }
            else {
                //Арифметические унарные
                if(this.type == ExprType.i2d){
                    bytes.write(BytecodeUtils.Instruction.i2d.code);
                }
                else
                    throw new IllegalStateException();
            }
        }

        if(_bytes.size() == 0){
            throw new IllegalStateException();
        }

        return _bytes.toByteArray();
    }

}
