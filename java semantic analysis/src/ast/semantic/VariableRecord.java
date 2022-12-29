package ast.semantic;

import ast.DeclaratorNode;
import ast.ExprNode;
import ast.VariableDeclarationNode;
import ast.semantic.typization.VariableType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ast.semantic.SemanticCrawler.printError;

public abstract class VariableRecord implements NamedRecord, Cloneable{
    protected boolean isLate, isStatic, isConst, isFinal;
    public VariableType varType;
    public String name;
    
    public ExprNode initValue = null;
    public int lineNum = -1;
    
    protected VariableRecord(boolean isLate, boolean isStatic, boolean isConst, boolean isFinal, VariableType varType, String name) {
        this.isLate = isLate;
        this.isStatic = isStatic;
        this.isConst = isConst;
        this.isFinal = isFinal;
        this.varType = varType;
        this.name = name;
    }
    
    protected VariableRecord(DeclaratorNode declarator, VariableType varType, String name) {
        this(declarator != null && declarator.isLate,
                declarator != null && declarator.isStatic,
                declarator != null && declarator.isConst,
                declarator != null && declarator.isFinal,
                varType,
                name);
    }
    
    protected VariableRecord(Map<String, ClassRecord> classTable, VariableDeclarationNode declarationNode){
        this(declarationNode.declarator.isLate,
                declarationNode.declarator.isStatic,
                declarationNode.declarator.isConst,
                declarationNode.declarator.isFinal,
                declarationNode.declarator.isTyped ? VariableType.from(classTable, declarationNode.declarator.valueType) : null,
                declarationNode.identifier.stringVal);
        
        if(this.isFinal || this.isConst || this.isLate){
            printError("GG-Dart does not support variable modifiers such as 'late', 'final', or 'const'.", declarationNode.lineNum);
        }
        
        if(declarationNode.isAssign)
            this.initValue = declarationNode.value;
        
        this.lineNum = declarationNode.lineNum;
    }
    
    public String name(){
        return name;
    }
    public boolean isLate(){
        return isLate;
    }
    public boolean isStatic(){
        return isStatic;
    }
    public boolean isConst(){
        return isConst;
    }
    public boolean isFinal(){
        return isFinal;
    }
    public VariableType type(){return varType;}
    public String descriptor(){return varType.descriptor();}
}
