package ast.semantic;

import ast.semantic.constants.DoubleConstant;
import ast.semantic.constants.info.MethodRefInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class BytecodeUtils {
    public enum Instruction{
        ldc(0x12),
        ldc_w(0x13),
        ldc2_w(0x14),


        invokespecial(0xb7),
        invokestatic(0xb8),
        invokevirtual(0xb6),


        aload(0x19),


        _return(0xb1),
        areturn(0xb0),

        ;

        public final int code;

        Instruction(int code) {
            this.code = code;
        }
    }

    public static byte[] ldc(int index) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.ldc.code);
        bytes.write(index);
        return _bytes.toByteArray();
    }

    public static byte[] ldc_w(int index) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.ldc_w.code);
        bytes.writeShort(index);
        return _bytes.toByteArray();
    }

    public static byte[] ldc2_w(int index) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.ldc2_w.code);
        bytes.writeShort(index);
        return _bytes.toByteArray();
    }

    public static byte[] loadConstant(ConstantRecord constant) throws IOException {
        if(constant instanceof DoubleConstant){
            return ldc2_w(constant.number);
        }
        else {
            if(constant.number < 256){
                return ldc(constant.number);
            } else {
                return ldc_w(constant.number);
            }
        }
    }


    public static byte[] invokespecial(int index) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.invokespecial.code);
        bytes.writeShort(index);
        return _bytes.toByteArray();
    }

    public static byte[] invokestatic(int index) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.invokestatic.code);
        bytes.writeShort(index);
        return _bytes.toByteArray();
    }

    public static byte[] invokevirtual(int index) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.invokevirtual.code);
        bytes.writeShort(index);
        return _bytes.toByteArray();
    }

    public static byte[] invokeMethod(MethodRefInfo methodRefInfo) throws IOException {
        if(methodRefInfo.type == MethodRefInfo.MethodRefType.invokeStatic){
            return invokestatic(methodRefInfo.constant.number);
        } else if(methodRefInfo.type == MethodRefInfo.MethodRefType.invokeSpecial){
            return invokespecial(methodRefInfo.constant.number);
        } else {
            return invokevirtual(methodRefInfo.constant.number);
        }
    }

    public static byte[] aload(int index) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.aload.code);
        bytes.write(index);
        return _bytes.toByteArray();
    }

    public static byte[] loadThis() throws IOException {
        return aload(0);
    }
}
