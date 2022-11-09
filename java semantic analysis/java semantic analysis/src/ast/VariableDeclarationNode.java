package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class VariableDeclarationNode extends Node{

    DeclaratorNode declarator;
    List<IdInitNode> idInitList = new ArrayList<>();

    public VariableDeclarationNode(Element element) {
        super(element);
        declarator = new DeclaratorNode(unlink(element, "declarator"));
        unlinkList(element, "idInitList").forEach(e->idInitList.add(new IdInitNode(e)));
    }
}
