package ast.semantic;

import ast.semantic.constants.DoubleConstant;
import ast.semantic.constants.info.FieldRefInfo;
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
        astore(0x3a),
        getfield(0xb4),
        getstatic(0xb2),
        putfield(0xb5),
        putstatic(0xb3),


        _return(0xb1),
        areturn(0xb0),


        _new(0xbb),
        dup(0x59),

        iconst_m1(0x2),
        iconst_0(0x3),
        iconst_1(0x4),
        iconst_2(0x5),
        iconst_3(0x6),
        iconst_4(0x7),
        iconst_5(0x8),
        bipush(0x10),
        sipush(0x11),
        aconst_null(0x1), 

        if_icmpeq(0x9f),
        if_icmpne(0xa0),
        if_icmplt(0xa1),
        if_icmpge(0xa2),
        if_icmpgt(0xa3),
        if_icmple(0xa4),

        _goto(0xa7),
        _goto_w(0xc8),

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
    
    public static byte[] astore(int index) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.astore.code);
        bytes.write(index);
        return _bytes.toByteArray();
    }

    public static byte[] loadThis() throws IOException {
        return aload(0);
    }
    
    public static byte[] getfield(int index) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.getfield.code);
        bytes.writeShort(index);
        return _bytes.toByteArray();
    }
    
    public static byte[] getstatic(int index) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.getstatic.code);
        bytes.writeShort(index);
        return _bytes.toByteArray();
    }
    
    public static byte[] loadField(FieldRefInfo fieldRefInfo) throws IOException {
        if(fieldRefInfo.isStatic()) return getstatic(fieldRefInfo.constant.number);
        else return getfield(fieldRefInfo.constant.number);
    }
    
    public static byte[] putfield(int index) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.putfield.code);
        bytes.writeShort(index);
        return _bytes.toByteArray();
    }
    
    public static byte[] putstatic(int index) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.putstatic.code);
        bytes.writeShort(index);
        return _bytes.toByteArray();
    }
    
    public static byte[] storeField(FieldRefInfo fieldRefInfo) throws IOException {
        if(fieldRefInfo.isStatic()) return putstatic(fieldRefInfo.constant.number);
        else return putfield(fieldRefInfo.constant.number);
    }
    
    public static byte[] _new(int index) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction._new.code);
        bytes.write(index);
        return _bytes.toByteArray();
    }

    public static byte[] dup() throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.dup.code);
        return _bytes.toByteArray();
    }

    public static byte[] iconst_1() throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.iconst_1.code);
        return _bytes.toByteArray();
    }

    public static byte[] iconst_0() throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.iconst_0.code);
        return _bytes.toByteArray();
    }

    public static byte[] loadBoolean(boolean value) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        if(value) {
            bytes.write(iconst_1());    
        } else {
            bytes.write(iconst_0());
        }
        
        return _bytes.toByteArray();
    }

    public static byte[] loadInt(long value) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        if(value == -1){
            bytes.write(Instruction.iconst_m1.code);
            return _bytes.toByteArray();
        }
            
        if(value == 0){
            bytes.write(Instruction.iconst_0.code);
            return _bytes.toByteArray();
        }
            
        if(value == 1){
            bytes.write(Instruction.iconst_1.code);
            return _bytes.toByteArray();
        }
            
        if(value == 2){
            bytes.write(Instruction.iconst_2.code);
            return _bytes.toByteArray();
        }
            
        if(value == 3){
            bytes.write(Instruction.iconst_3.code);
            return _bytes.toByteArray();
        }
            
        if(value == 4){
            bytes.write(Instruction.iconst_4.code);
            return _bytes.toByteArray();
        }

        if(value == 5){
            bytes.write(Instruction.iconst_5.code);
            return _bytes.toByteArray();
        }

        return _bytes.toByteArray();
    }

    public static byte[] bipushInt(long value) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.bipush.code);
        bytes.write((int)value);

        return _bytes.toByteArray();
    }

    public static byte[] sipushInt(long value) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.sipush.code);
        bytes.writeShort((int)value);

        return _bytes.toByteArray();
    }

    public static byte[] loadNull() throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.aconst_null.code);
        return _bytes.toByteArray();
    }

    public static byte[] _goto(int offset) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        if(offset >= -32768 && offset <= 32767){
            bytes.write(Instruction._goto.code);
            bytes.writeShort(offset);    
        }
        else{
            bytes.write(Instruction._goto_w.code);
            bytes.writeInt(offset);
        }

        return _bytes.toByteArray();
    }
}
