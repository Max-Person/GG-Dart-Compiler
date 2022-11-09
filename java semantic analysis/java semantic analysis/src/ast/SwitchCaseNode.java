package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class SwitchCaseNode extends Node{

    List<StmtNode> actions = new ArrayList<>();
    ExprNode condition;

    public SwitchCaseNode(Element element) {
        super(element);
        condition = new ExprNode(unlink(element, "condition"));
        if(element.getElementsByTagName("actions").getLength() > 0){
            unlinkList(element, "actions").forEach(e->actions.add(new StmtNode(e)));
        }
    }
}
