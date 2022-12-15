package ast.semantic;

import ast.*;
import ast.semantic.typization.VariableType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SemanticCrawler {
    
    public Map<String, ClassRecord> classTable = new ConcurrentHashMap<>();
    
    public SemanticCrawler() {
        //RTLClassRecord.init(classTable);
        classTable.put(ClassRecord.globalName, new ClassRecord(classTable, ClassRecord.globalName, false));
    }
    
    public static void printError(String msg, int line) {
        String err = "Java Segment (semantic) ERR at line " + line + ": " + msg;
        System.err.println(err);
        throw new SemanticError(err);
    }
    
    public String describe(){
        StringBuilder description = new StringBuilder();
        for(ClassRecord classRecord : classTable.values()){
            description.append(classRecord.describe()).append("\n\n");
        }
        return description.toString();
    }
    
    public void analyze(RootNode root) {
        //1. Собрать информацию о названиях классов
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
        
        //2. Проверить правильность объявлений классов (наследование + повторы).
        for(ClassRecord record : classTable.values()){
            record.resolveDeclaration(new ArrayList<>());
        }
        
        //3. Собрать информацию о названиях полей и методов классов (+ информация об их типах, если она доступна сразу) - формирование соответствующих таблиц
        //+ проверить на различия имен, и тп. (проверить правильность ВНУТРИ класса, без учета наследования)
        for(ClassRecord record : classTable.values()){
            record.resolveClassMembers();
        }
        
        //4. Выявить типы для var полей (ну и проверить их соответственно)  + создание геттеров/сеттеров
        for(ClassRecord record : classTable.values()){
            record.inferTypes();
        }
        
        //5. Проверить правильность наследований/имплементаций/миксинов + перенести методы для миксинов
        for(ClassRecord record : classTable.values()){
            record.checkInheritance();
        }
        
        //6. Провести преобразования имплементаций/миксинов
        //(разделение классов на интерфейсы и классы-реализации
        classTable.values().forEach(c -> c.resolveInterfaces());
        classTable.values().forEach(c -> {
            c.finalizeTypes();
            c.addInheritanceConstants();
        });
        
        //7. обработать методы (преобразовать к нужному виду - у конструкторов например), сформировать таблицы локалок
        classTable.values().forEach(c -> c.normalizeConstructors());
        classTable.values().forEach(c -> c.checkMethods());
    }
    
    public static final String bytecodeDir = "GG_OUT/";
    private void clearDir(File file) throws IOException {
        if(!file.exists())
            return;
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    clearDir(entry);
                }
            }
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete " + file);
        }
    }
    public void writeClassesAsBytecode() throws IOException {
        clearDir(new File(bytecodeDir));
    
        File targetIO = new File(bytecodeDir + RTLClassRecord.io.qualifiedName() + ".class");
        targetIO.getParentFile().mkdirs();
        Files.copy(Path.of("InputOutput.class"), targetIO.toPath());
    
        File targetList = new File(bytecodeDir + RTLListClassRecord.basic.qualifiedName() + ".class");
        targetList.getParentFile().mkdirs();
        Files.copy(Path.of("List.class"), targetList.toPath());
        
        for(ClassRecord clazz : classTable.values()){
            File target = new File(bytecodeDir + clazz.qualifiedName() + ".class");
            target.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(clazz.toBytes());
                //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
            }
        }
    }
    
    public void addClass(ClasslikeDeclaration clazz) {
        checkInGlobalNamespace(clazz.name(),
                clazz instanceof ClassDeclarationNode ? ((ClassDeclarationNode) clazz).lineNum : ((EnumNode) clazz).lineNum);
        ClassRecord classRecord = new ClassRecord(classTable, clazz);
        classTable.put(classRecord.name(), classRecord);
    }
    
    public void addGlobalFunction(FunctionDefinitionNode func) {
        checkInGlobalNamespace(func.name(), func.lineNum);
        func.signature.isStatic = true;
        classTable.get(ClassRecord.globalName).addMethod(func.signature, func.body);
    }
    
    public void addGlobalVariable(VariableDeclarationNode var) {
        checkInGlobalNamespace(var.name(), var.lineNum);
        var.declarator.isStatic = true;
        classTable.get(ClassRecord.globalName).addField(var);
    }
    
    public void checkInGlobalNamespace(String name, int lineNum){
        if (VariableType.isStandartName(name) ||
                classTable.containsKey(name) ||
                classTable.get(ClassRecord.globalName).methods.containsKey(name) ||
                classTable.get(ClassRecord.globalName).fields.containsKey(name)) {
            printError("'" + name + "' is already declared in this scope.", lineNum);
        }
    }
    
    
}
