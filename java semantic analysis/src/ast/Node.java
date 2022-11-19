package ast;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {

    public static Element unlink(Element element, String container){
        org.w3c.dom.Node child = element.getFirstChild();
        org.w3c.dom.Node containerElement = null;
        while (child != null){
            if(child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && ((Element) child).getTagName().equals(container)){
                containerElement = child;
            }
            child = child.getNextSibling();
        }
        if(containerElement == null) throw new IllegalStateException();
        
        child = containerElement.getFirstChild();
        while (child != null){
            if(child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                return (Element) child;
            }
            child = child.getNextSibling();
        }
        return null;
    }
    public static List<Element> unlinkList(Element element, String container){
        org.w3c.dom.Node child = element.getFirstChild();
        org.w3c.dom.Node containerElement = null;
        while (child != null){
            if(child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && ((Element) child).getTagName().equals(container)){
                containerElement = child;
            }
            child = child.getNextSibling();
        }
        if(containerElement == null) throw new IllegalStateException();
    
        child = containerElement.getFirstChild();
        List<Element> elements = new ArrayList<>();
        while (child != null){
            if(child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                elements.add((Element)child);
            }
            child = child.getNextSibling();
        }
        return elements;
    }
    
    public int lineNum;
    
    protected Node(Element element){
        lineNum = Integer.parseInt(element.getAttribute("line"));
    }
    
    protected Node(){
        lineNum = -1;
    }
}
