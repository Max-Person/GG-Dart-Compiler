package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SwitchCaseNode extends Node{
    
    public List<StmtNode> actions = new ArrayList<>();
    public ExprNode condition;

    public SwitchCaseNode(Element element) {
        super(element);
        condition = new ExprNode(unlink(element, "condition"));
        if(Node.getImmediateChildByName(element, "actions") != null){
            unlinkList(element, "actions").forEach(e->actions.add(new StmtNode(e)));
        }
    }

    public SwitchCaseNode(){

    }

    public SwitchCaseNode deepCopy(){
        SwitchCaseNode copy = new SwitchCaseNode();
        copy.actions = actions.stream().map(StmtNode::deepCopy).collect(Collectors.toList());
        copy.condition = condition == null ? null : condition.deepCopy();
        return copy;
    }
}
