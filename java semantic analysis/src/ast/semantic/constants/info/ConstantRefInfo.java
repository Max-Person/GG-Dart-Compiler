package ast.semantic.constants.info;

import ast.semantic.ConstantRecord;

public interface ConstantRefInfo<ConsntType extends ConstantRecord> extends RefInfo{
    ConsntType constant();
}
