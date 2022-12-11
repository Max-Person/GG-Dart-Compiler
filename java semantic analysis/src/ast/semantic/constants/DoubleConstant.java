package ast.semantic.constants;

import ast.semantic.ConstantRecord;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class DoubleConstant extends ConstantRecord {
    public final double value;
    
    public DoubleConstant(double value) {
        this.value = value;
    }
    
    @Override
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(6);
        bytes.writeDouble(value);
        
        return _bytes.toByteArray();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoubleConstant)) return false;
        DoubleConstant that = (DoubleConstant) o;
        return Double.compare(that.value, value) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
