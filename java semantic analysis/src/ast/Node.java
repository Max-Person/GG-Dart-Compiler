package ast;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {

    public static List<Element> getImmediateChildren(Element element){
        org.w3c.dom.Node child = element.getFirstChild();
        List<Element> elements = new ArrayList<>();
        while (child != null){
            if(child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                elements.add((Element)child);
            }
            child = child.getNextSibling();
        }
        return elements;
    }

    public static Element getImmediateChildByName(Element element, String name){
        return  getImmediateChildren(element).stream().filter(el -> el.getTagName().equals(name)).findFirst().orElse(null);
    }

    public static Element unlink(Element element, String container){
        Element containerElement = Node.getImmediateChildByName(element, container);
        if(containerElement == null) throw new IllegalStateException();
        List<Element> children = Node.getImmediateChildren(containerElement);
        if(children.size() != 1) throw new IllegalStateException();
        return children.get(0);
    }
    public static List<Element> unlinkList(Element element, String container){
        Element containerElement = Node.getImmediateChildByName(element, container);
        if(containerElement == null) throw new IllegalStateException();
        return Node.getImmediateChildren(containerElement);
    }
    
    public int lineNum;
    
    protected Node(Element element){
        lineNum = Integer.parseInt(element.getAttribute("line"));
    }
    
    protected Node(){
        lineNum = -1;
    }
}
