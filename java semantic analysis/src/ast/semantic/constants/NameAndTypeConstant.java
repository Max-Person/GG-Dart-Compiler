package ast.semantic.constants;

import ast.semantic.ConstantRecord;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class NameAndTypeConstant extends ConstantRecord {
    public final UTF8Constant nameConst;
    public final UTF8Constant typeConst;
    
    public NameAndTypeConstant(UTF8Constant nameConst, UTF8Constant typeConst) {
        this.nameConst = nameConst;
        this.typeConst = typeConst;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NameAndTypeConstant)) return false;
        NameAndTypeConstant that = (NameAndTypeConstant) o;
        return nameConst.equals(that.nameConst) && typeConst.equals(that.typeConst);
    }
    
    @Override
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(12);
        bytes.writeShort(nameConst.number);
        bytes.writeShort(typeConst.number);
        
        return _bytes.toByteArray();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nameConst, typeConst);
    }
}
