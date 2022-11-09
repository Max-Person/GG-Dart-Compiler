package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class EnumNode extends Node {

    IdentifierNode name;
    List<IdentifierNode> values = new ArrayList<>();

    public EnumNode(Element element) {
        super(element);
        name = new IdentifierNode(unlink(element, "name"));
        unlinkList(element, "values").forEach(e -> values.add(new IdentifierNode(e)));
    }
}
