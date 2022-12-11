package ast.semantic.constants;

import ast.semantic.ConstantRecord;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class StringConstant extends ConstantRecord {
    public final UTF8Constant valueConst;
    
    public StringConstant(UTF8Constant valueConst) {
        this.valueConst = valueConst;
    }
    
    @Override
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream _bytes = new ByteArrayOutputStream();
        DataOutputStream bytes = new DataOutputStream(_bytes);
        bytes.write(8);
        bytes.writeShort(valueConst.number);
        
        return _bytes.toByteArray();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StringConstant)) return false;
        StringConstant that = (StringConstant) o;
        return valueConst.equals(that.valueConst);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(valueConst);
    }
}
