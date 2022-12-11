package ast.semantic.constants;

import ast.semantic.ConstantRecord;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class UTF8Constant extends ConstantRecord {
    public final String value;
    
    public UTF8Constant(String stringval) {
        this.value = stringval;
    }
    
    @Override
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(1);
        bytes.writeUTF(value);
        
        return _bytes.toByteArray();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UTF8Constant)) return false;
        UTF8Constant that = (UTF8Constant) o;
        return value.equals(that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
