package ast.semantic.constants.info;

import ast.semantic.ClassRecord;
import ast.semantic.MethodRecord;
import ast.semantic.constants.MethodRefConstant;
import ast.semantic.context.Context;

public class MethodRefInfo implements ConstantRefInfo<MethodRefConstant> {
    public enum MethodRefType{
        invokeStatic,
        invokeSpecial,
        invokeVirtual,
        invokeInterface,
    }
    
    public final MethodRefConstant constant;
    public final MethodRefType type;
    public final MethodRecord method;
    
    private MethodRefInfo(MethodRefType type, MethodRecord method, ClassRecord invoker, Context context){
        if(method.isStatic() && (type != MethodRefType.invokeStatic || invoker != method.containerClass)){
            throw new IllegalStateException();
        }
        if(type == MethodRefType.invokeInterface && !invoker.isJavaInterface ||
                type != MethodRefType.invokeInterface && invoker.isJavaInterface)
            throw new IllegalStateException();
        
        this.type = type;
        this.constant = context.currentClass().addMethodRefConstant(invoker, method);
        this.method = method;
    }
    
    public static MethodRefInfo invokeStatic(MethodRecord method, Context context){
        return new MethodRefInfo(MethodRefType.invokeStatic, method, method.containerClass, context);
    }
    
    public static MethodRefInfo invokeSpecial(MethodRecord method, ClassRecord invoker, Context context){
        return new MethodRefInfo(MethodRefType.invokeSpecial, method, invoker, context);
    }
    
    public static MethodRefInfo invokeVirtual(MethodRecord method, ClassRecord invoker, Context context){
        return new MethodRefInfo(invoker.isJavaInterface? MethodRefType.invokeInterface : MethodRefType.invokeVirtual, method, invoker, context);
    }
    
    @Override
    public MethodRefConstant constant() {
        return constant;
    }
}
