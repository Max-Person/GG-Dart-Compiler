package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class RedirectionNode extends Node{

    boolean isNamed;
    IdentifierNode name;
    List<ExprNode> args = new ArrayList<>();

    public RedirectionNode(Element element) {
        super(element);
        isNamed = Boolean.parseBoolean(element.getAttribute("isNamed"));
        if(isNamed){
            name = new IdentifierNode(unlink(element, "name"));
        }

        if(element.getElementsByTagName("args").getLength() > 0){
            unlinkList(element, "args").forEach(e->args.add(new ExprNode(e)));
        }
    }
}
