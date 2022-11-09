package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

enum InitializerType {
    superConstructor,
    superNamedConstructor,
    thisAssign,
}

public class InitializerNode extends Node{

    InitializerType type;

    IdentifierNode superConstructorName;
    List<ExprNode> args = new ArrayList<>();

    IdentifierNode thisFieldId;
    ExprNode value;

    public InitializerNode(Element element) {
        super(element);
        type = InitializerType.valueOf(element.getAttribute("type"));
        if(type == InitializerType.thisAssign){
            thisFieldId = new IdentifierNode(unlink(element, "thisFieldId"));
            value = new ExprNode(unlink(element, "value"));
        } else {
            if(type == InitializerType.superNamedConstructor) {
                superConstructorName = new IdentifierNode(unlink(element, "superConstructorName"));
            }

            if(element.getElementsByTagName("args").getLength() > 0){
                unlinkList(element, "args").forEach(e->args.add(new ExprNode(e)));
            }
        }
    }
}
