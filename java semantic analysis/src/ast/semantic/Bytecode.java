package ast.semantic;

import ast.semantic.constants.DoubleConstant;
import ast.semantic.constants.info.ClassRefInfo;
import ast.semantic.constants.info.FieldRefInfo;
import ast.semantic.constants.info.MethodRefInfo;
import ast.semantic.typization.PlainType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Bytecode {
    private ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
    private DataOutputStream bytes = new DataOutputStream(_bytes);
    
    public byte[] toBytes(){
        return _bytes.toByteArray();
    }
    public void write(byte[] code) throws IOException {
        bytes.write(code);
    }
    public void writeSimple(Instruction b) throws IOException {
        bytes.write(b.code);
    }
    public int currentOffset(){
        return bytes.size();
    }
    private static byte[] insertReplace(byte[] first, byte[] second, int insertAt, int replaceLength){
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        _bytes.write(first, 0, insertAt);
        _bytes.write(second, 0, second.length);
        _bytes.write(first, insertAt + replaceLength, first.length - (insertAt + replaceLength));
        return _bytes.toByteArray();
    }
    
    private List<Integer> markedBreaks = new ArrayList<>();
    public void markBreak() throws IOException {
        markedBreaks.add(currentOffset());
        bytes.write(jump(Instruction._goto, 3));
    }
    public void resolveBreaks() throws IOException {
        bytes.flush(); //FIXME ? не уверен
        byte[] newBytes = _bytes.toByteArray();
        while(!markedBreaks.isEmpty()){
            int breakOffset = markedBreaks.get(0);
            markedBreaks.remove(0);
            byte[] jump = jump(Instruction._goto, breakOffset - currentOffset());
            newBytes = insertReplace(newBytes, jump, breakOffset, 3);
            if(jump.length > 3){
                markedBreaks.replaceAll(offset -> offset > breakOffset ? offset+2 : offset);
                markedContinues.replaceAll(offset -> offset > breakOffset ? offset+2 : offset);
            }
        }
        _bytes.reset();
        _bytes.write(newBytes);
    }
    private List<Integer> markedContinues = new ArrayList<>();
    public void markContinue() throws IOException {
        markedContinues.add(currentOffset());
        bytes.write(jump(Instruction._goto,3));
    }
    public void resolveContinues() throws IOException {
        bytes.flush(); //FIXME ? не уверен
        byte[] newBytes = _bytes.toByteArray();
        while(!markedContinues.isEmpty()){
            int continueOffset = markedContinues.get(0);
            markedContinues.remove(0);
            byte[] jump = jump(Instruction._goto, continueOffset - currentOffset());
            newBytes = insertReplace(newBytes, jump, continueOffset, 3);
            if(jump.length > 3){
                markedBreaks.replaceAll(offset -> offset > continueOffset ? offset+2 : offset);
                markedContinues.replaceAll(offset -> offset > continueOffset ? offset+2 : offset);
            }
        }
        _bytes.reset();
        _bytes.write(newBytes);
    }
    
    public enum Instruction{
        ldc(0x12),
        ldc_w(0x13),
        ldc2_w(0x14),


        invokespecial(0xb7),
        invokestatic(0xb8),
        invokevirtual(0xb6),
        invokeinterface(0xb9),


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
    
        dcmpg(0x98),
        dcmpl(0x97),

        if_acmpeq(0xa5),
        if_acmpne(0xa6),
    
        ifnonnull(0xc7),
        ifnull(0xc6),
    
        ifeq(0x99),
        ifne(0x9a),
        iflt(0x9b),
        ifge(0x9c),
        ifgt(0x9d),
        ifle(0x9e),

        _goto(0xa7),
        _goto_w(0xc8),
    
        pop(0x57),
        pop2(0x58),
    
        iadd(0x60),
        isub(0x64),
        imul(0x68),
        ineg(0x74),
    
        i2d(0x87),
    
        dadd(0x63),
        dsub(0x67),
        dmul(0x6b),
        ddiv(0x6f),
        dneg(0x77),
    
        checkcast(0xc0),
        _instanceof(0xc1),

        ;

        public final int code;

        Instruction(int code) {
            this.code = code;
        }
        
        public boolean isJump(){
            return this == if_icmpeq || this == if_icmpne || this == if_icmplt || this == if_icmpge || this == if_icmpgt || this == if_icmple ||
                    this == _goto || this == _goto_w ||
                    this == ifeq || this == ifne || this == iflt || this == ifge || this == ifgt || this == ifle ||
                    this == if_acmpeq || this == if_acmpne ||
                    this == ifnull || this == ifnonnull;
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

    public static byte[] invokeMethod(MethodRefInfo methodRefInfo) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        if(methodRefInfo.type == MethodRefInfo.MethodRefType.invokeStatic){
            bytes.write(Instruction.invokestatic.code);
        } else if(methodRefInfo.type == MethodRefInfo.MethodRefType.invokeSpecial){
            bytes.write(Instruction.invokespecial.code);
        } else if(methodRefInfo.type == MethodRefInfo.MethodRefType.invokeVirtual) {
            bytes.write(Instruction.invokevirtual.code);
        } else if(methodRefInfo.type == MethodRefInfo.MethodRefType.invokeInterface) {
            bytes.write(Instruction.invokeinterface.code);
        } else throw new IllegalStateException();

        bytes.writeShort(methodRefInfo.constant.number);

        if(methodRefInfo.type == MethodRefInfo.MethodRefType.invokeInterface){
            int size = 1;
            for(ParameterRecord p : methodRefInfo.method.parameters){
                if(p.varType.equals(PlainType._double()))
                    size+=2;
                else
                    size+=1;
            }
            bytes.write(size);
            bytes.write(0);
        }
        return _bytes.toByteArray();
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
        bytes.writeShort(index);
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

    public static byte[] jump(Instruction jump, int offset) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        if(!jump.isJump())
            throw new IllegalArgumentException();
        if(offset >= -32768 && offset <= 32767){
            bytes.write(jump != Instruction._goto_w ? jump.code : Instruction._goto.code);
            bytes.writeShort(offset);
        }
        else if(jump == Instruction._goto || jump == Instruction._goto_w){
            bytes.write(Instruction._goto_w.code);
            bytes.writeInt(offset);
        }
        else
            throw new IllegalArgumentException();

        return _bytes.toByteArray();
    }
    
    public static byte[] checkcast(ClassRefInfo classRefInfo) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction.checkcast.code);
        bytes.writeShort(classRefInfo.constant.number);
        return _bytes.toByteArray();
    }
    
    public static byte[] _instanceof(ClassRefInfo classRefInfo) throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(Instruction._instanceof.code);
        bytes.writeShort(classRefInfo.constant.number);
        return _bytes.toByteArray();
    }
}
