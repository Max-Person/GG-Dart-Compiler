package ast;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class RootNode extends Node{
    
    public List<TopLevelDeclarationNode> topLevelDeclarationNodes = new ArrayList<>();
    
    public RootNode(Element element) {
        super();
        NodeList list = element.getChildNodes();
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            org.w3c.dom.Node node = list.item(i);
            if(node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                elements.add((Element) node);
            }
        }
        elements.forEach(e->topLevelDeclarationNodes.add(new TopLevelDeclarationNode(e)));
    }
}
