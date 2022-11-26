package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class EnumNode extends Node implements ClasslikeDeclaration {
    
    public IdentifierNode name;
    public List<IdentifierNode> values = new ArrayList<>();

    public EnumNode(Element element) {
        super(element);
        name = new IdentifierNode(unlink(element, "name"));
        unlinkList(element, "values").forEach(e -> values.add(new IdentifierNode(e)));
    }
    
    @Override
    public String name() {
        return name.stringVal;
    }
    
    @Override
    public int lineNum() {
        return lineNum;
    }
}
