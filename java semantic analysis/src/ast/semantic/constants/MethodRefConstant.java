package ast.semantic.constants;

import ast.semantic.ConstantRecord;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class MethodRefConstant extends ConstantRecord {
    public final ClassConstant classConst;
    public final NameAndTypeConstant nameAndTypeConst;
    
    public MethodRefConstant(ClassConstant classConst, NameAndTypeConstant nameAndTypeConst) {
        this.classConst = classConst;
        this.nameAndTypeConst = nameAndTypeConst;
    }
    
    @Override
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(10);
        bytes.writeShort(classConst.number);
        bytes.writeShort(nameAndTypeConst.number);
        
        return _bytes.toByteArray();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodRefConstant)) return false;
        MethodRefConstant that = (MethodRefConstant) o;
        return classConst.equals(that.classConst) && nameAndTypeConst.equals(that.nameAndTypeConst);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(classConst, nameAndTypeConst);
    }
}
