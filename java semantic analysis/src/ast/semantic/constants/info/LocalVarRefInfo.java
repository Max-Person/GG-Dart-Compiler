package ast.semantic.constants.info;

import ast.semantic.LocalVarRecord;

public class LocalVarRefInfo implements RefInfo{
    public final LocalVarRecord localVar;
    
    public LocalVarRefInfo(LocalVarRecord localVar) {
        this.localVar = localVar;
    }
}
