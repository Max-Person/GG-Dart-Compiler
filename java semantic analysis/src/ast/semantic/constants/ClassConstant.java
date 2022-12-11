package ast.semantic.constants;

import ast.semantic.ConstantRecord;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ClassConstant extends ConstantRecord {
    public final UTF8Constant nameConst;
    
    public ClassConstant(UTF8Constant nameConst) {
        this.nameConst = nameConst;
    }
    
    @Override
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(7);
        bytes.writeShort(nameConst.number);
        
        return _bytes.toByteArray();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassConstant)) return false;
        ClassConstant that = (ClassConstant) o;
        return nameConst.equals(that.nameConst);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nameConst);
    }
}
