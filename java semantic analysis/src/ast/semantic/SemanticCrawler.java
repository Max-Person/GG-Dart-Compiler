package ast.semantic;

import ast.*;
import ast.semantic.typization.StandartType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemanticCrawler {
    
    public Map<String, ClassRecord> classTable = new HashMap<>();
    
    public SemanticCrawler() {
        classTable.put(ClassRecord.globalName, ClassRecord.globalClass());
    }
    
    public static void printError(String msg, int line) {
        String err = "Java Segment (semantic) ERR at line " + line + ": " + msg;
        System.err.println(err);
        throw new IllegalStateException(err);
    }
    
    public void analyze(RootNode root) {
        //Собрать информацию о названиях классов
        for (TopLevelDeclarationNode decl : root.topLevelDeclarationNodes) {
            switch (decl.type) {
                case _enum -> {
                    addClass(decl.enumDecl);
                }
                case _class -> {
                    addClass(decl.classDecl);
                }
                case _function -> {
                    addGlobalFunction(decl.functionDecl);
                }
                case _variable -> {
                    decl.variableDecl.forEach(v -> addGlobalVariable(v));
                }
            }
        }
        
        //Проверить правильность объявлений классов (наследование + повторы).
        for(ClassRecord record : classTable.values()){
            resolveClass(new ArrayList<>(), record);
        }
        
        //Собрать информацию о названиях полей и методов классов (+ информация об их типах, если она доступна сразу) - формирование соответствующих таблиц
        //+ проверить на различия имен, и тп. (проверить правильность ВНУТРИ класса, без учета наследования)
        
        //Выявить типы для var полей (ну и проверить их соответственно)
        
        //Проверить правильность наследований/имплементаций/миксинов + перенести методы для миксинов
        
        //Провести преобразования имплементаций/миксинов
        //(разделение классов на интерфейсы и классы-реализации + создание геттеров/сеттеров)
        
        //обработать методы (преобразовать к нужному виду - у конструкторов например), сформировать таблицы локалок
    }
    
    public void addClass(ClasslikeDeclaration clazz) {
        checkInGlobalNamespace(clazz.name(),
                clazz instanceof ClassDeclarationNode ? ((ClassDeclarationNode) clazz).lineNum : ((EnumNode) clazz).lineNum);
        ClassRecord classRecord = new ClassRecord(clazz);
        classTable.put(classRecord.name(), classRecord);
    }
    
    public void addGlobalFunction(FunctionDefinitionNode func) {
        checkInGlobalNamespace(func.name(), func.lineNum);
        func.signature.isStatic = true;
        classTable.get(ClassRecord.globalName).addMethod(classTable, func.signature, func.body);
    }
    
    public void addGlobalVariable(VariableDeclarationNode var) {
        checkInGlobalNamespace(var.name(), var.lineNum);
        var.declarator.isStatic = true;
        classTable.get(ClassRecord.globalName).addField(classTable, var);
    }
    
    public void checkInGlobalNamespace(String name, int lineNum){
        if (StandartType.isStandartName(name) ||
                classTable.containsKey(name) ||
                classTable.get(ClassRecord.globalName).methods.containsKey(name) ||
                classTable.get(ClassRecord.globalName).fields.containsKey(name)) {
            printError("'" + name + "' is already declared in this scope.", lineNum);
        }
    }
    
    public void resolveClass(List<ClassRecord> children, ClassRecord classRecord){
        if(classRecord.isDeclResolved) return;
        if(classRecord.isEnum()){
            //TODO ?
        }
        else {
            ClassDeclarationNode clazz = (ClassDeclarationNode) classRecord.declaration;
            if(children.contains(classRecord)){
                printError("'" + clazz.name() + "' can't be a supertype of itself.", clazz._super.lineNum); //TODO выписать список рекурсии
            }
            children.add(classRecord);
            if(clazz._super != null){ // Если класс от кого-то наследутеся
                ClassRecord potentialSuper = checkInheritable(clazz._super, "extend");
                
                resolveClass(children, potentialSuper); //FIXME не уверен в этом..
                classRecord._super = potentialSuper;
            }
            for(TypeNode iinterface: clazz.interfaces){
                if(clazz.interfaces.subList(0, clazz.interfaces.indexOf(iinterface)).stream().anyMatch(i -> i.name.stringVal.equals(iinterface.name.stringVal))){
                    printError("'" + iinterface.name.stringVal + "' can only be implemented once.", iinterface.lineNum);
                }
                if(classRecord._super != null && iinterface.name.stringVal.equals(classRecord._super.name())){
                    printError("'" + iinterface.name.stringVal + "' can't be used in both the 'extends' and 'implements' clauses.", iinterface.lineNum);
                }
                ClassRecord potentialInterface = checkInheritable(iinterface, "implement");
                resolveClass(children, potentialInterface); //FIXME не уверен в этом..
                classRecord._interfaces.add(potentialInterface);
            }
            for(TypeNode mixin: clazz.mixins){
                if(classRecord._super != null && mixin.name.stringVal.equals(classRecord._super.name())){
                    printError("'" + mixin.name.stringVal + "' can't be used in both the 'extends' and 'with' clauses.", mixin.lineNum);
                }
                ClassRecord potentialMixin = checkInheritable(mixin, "mixin");
                resolveClass(children, potentialMixin); //FIXME не уверен в этом..
                if(potentialMixin._super != null){
                    printError("The class '"+ potentialMixin.name()+"' can't be used as a mixin because it extends a class other than 'Object'.", mixin.lineNum);
                }
                for(ClassMemberDeclarationNode decl : ((ClassDeclarationNode)potentialMixin.declaration).classMembers){
                    if((decl.type == ClassMemberDeclarationType.methodDefinition || decl.type == ClassMemberDeclarationType.methodSignature) && decl.signature.isConstruct){
                        printError("The class '"+ potentialMixin.name()+"' can't be used as a mixin because it declares a constructor.", mixin.lineNum);
                    }
                }
                classRecord._mixins.add(potentialMixin);
                
            }
        }
        classRecord.isDeclResolved = true;
    }
    
    private ClassRecord checkInheritable(TypeNode node, String action) {
        if (node.type == TypeType._list) {
            printError("a class can't " + action + " a List type", node.lineNum);
        }
        if (node.isNullable) {
            printError("a class can't " + action + " a nullable type", node.lineNum);
        }
        if(StandartType.isStandartName(node.name.stringVal)){
            printError("classes can't' " + action + " '"+node.name+"'.", node.lineNum);
        }
        ClassRecord potentialInheritance = classTable.get(node.name.stringVal);
        if (potentialInheritance == null || potentialInheritance.isEnum()) {
            printError("classes can only " + action + " other classes.", node.lineNum);
        }
        return potentialInheritance;
    }
    
    public void resolveClassMembers(ClassRecord classRecord){
        if(classRecord.isEnum()){
            //TODO ?
        }
        else {
            ClassDeclarationNode clazz = (ClassDeclarationNode) classRecord.declaration;
            for(ClassMemberDeclarationNode classMember : clazz.classMembers){
                if(classMember.type == ClassMemberDeclarationType.field){
                    for(VariableDeclarationNode var: classMember.fieldDecl){
                        classRecord.addField(classTable, var);
                    }
                }
                else{
                    StmtNode body = null;
                    if(classMember.type == ClassMemberDeclarationType.methodDefinition) body = classMember.body;
                    classRecord.addMethod(classTable, classMember.signature, body);
                }
            }
        }
    }
    
    public void inferValueTypes(ClassRecord classRecord){
        if(classRecord.isEnum()){
            return; //TODO ?
        }
        ClassDeclarationNode clazz = (ClassDeclarationNode) classRecord.declaration;
        for(FieldRecord fieldRecord : classRecord.fields.values()){
            if(fieldRecord.type() == null){
                fieldRecord.declaration.declarator.valueType = annotateTypes(fieldRecord.declaration.value, classRecord, fieldRecord.isStatic());
                //TODO
            }
        }
    }
    
    //TODO преобразовать в resolveExpr - запихнуть всю логику статической типизации и преобразований выражений сюда.
    public TypeNode annotateTypes(ExprNode node, ClassRecord classRecord, boolean isStatic){
        TypeNode result = null;
        if(node.type == ExprType.this_pr){
            if(isStatic){
                printError("invoking 'this' in a static context", node.lineNum);
            }
            result = new TypeNode(classRecord.name(), false);
        }
        else if(node.type == ExprType.super_pr){ //FIXME По идее это невозможно?
            if(classRecord._super == null){
                printError("invoking 'super' when no superclass is defined", node.lineNum);
            }
            result = new TypeNode(classRecord._super.name(), false); //FIXME или все таки classRecord._super.name() ?
        }
        else if(node.type == ExprType.null_pr){
            result = new TypeNode("NULL", true);
        }
        else if(node.type == ExprType.int_pr){
            result = new TypeNode("int", false);
        }
        else if(node.type == ExprType.double_pr){
            result = new TypeNode("double", false);
        }
        else if(node.type == ExprType.bool_pr){
            result = new TypeNode("bool", false);
        }
        else if(node.type == ExprType.string_pr){
            result = new TypeNode("String", false);
        }
        else if(node.type == ExprType.list_pr){
            TypeNode element = null;
            for(ExprNode el: node.listValues){
                TypeNode cur = annotateTypes(el, classRecord, isStatic);
                if(element == null){
                    element = cur;
                }
                if(!element.equals(cur)){
                    printError("elements of a List must be of the same type.", el.lineNum);
                }
            }
            result = new TypeNode(element, false);
        }
        else if(node.type == ExprType.string_interpolation){
            annotateTypes(node.operand, classRecord, isStatic);
            annotateTypes(node.operand2, classRecord, isStatic); //TODO если не стринг то вызвать .toString()
            result = new TypeNode("String", false);
        }
        else if(node.type == ExprType.constructNew){
            ClassRecord constructed = classTable.get(node.identifierAccess.stringVal);
            if(constructed == null){
                printError("Unknown class '"+ node.identifierAccess.stringVal +"'.", node.identifierAccess.lineNum);
            }
            String constructorName = node.constructName != null ? node.constructName.stringVal : null;
            List<TypeNode> argumentTypes = new ArrayList<>();
            result = new TypeNode("String", false);
        }
        
        node.annotatedType = result;
        return result;
    }
}
