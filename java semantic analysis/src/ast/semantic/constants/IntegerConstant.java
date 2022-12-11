package ast.semantic.constants;

import ast.semantic.ConstantRecord;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class IntegerConstant extends ConstantRecord {
    public final int value;
    
    public IntegerConstant(int value) {
        this.value = value;
    }
    
    @Override
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(3);
        bytes.writeInt(value);
        
        return _bytes.toByteArray();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntegerConstant)) return false;
        IntegerConstant that = (IntegerConstant) o;
        return value == that.value;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
