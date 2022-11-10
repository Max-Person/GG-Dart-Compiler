package ast;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {

    public static Element unlink(Element element, String container){
        NodeList list = element.getElementsByTagName(container).item(0).getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            org.w3c.dom.Node node = list.item(i);
            if(node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                return (Element) node;
            }
        }
        return null;
    }
    public static List<Element> unlinkList(Element element, String container){
        NodeList list = element.getElementsByTagName(container).item(0).getChildNodes();
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            org.w3c.dom.Node node = list.item(i);
            if(node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                elements.add((Element) node);
            }
        }
        return elements;
    }

    int lineNum;
    
    protected Node(Element element){
        lineNum = Integer.parseInt(element.getAttribute("line"));
    }
    
    protected Node(){
        lineNum = -1;
    }
}
