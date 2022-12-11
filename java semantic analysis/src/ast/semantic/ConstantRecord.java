package ast.semantic;

import java.io.IOException;

public abstract class ConstantRecord {
    public int number;
    
    public abstract byte[] toBytes() throws IOException;
    
}
