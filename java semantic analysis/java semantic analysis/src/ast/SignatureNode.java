package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class SignatureNode extends Node{

    boolean isStatic;
    boolean isConstruct;

    TypeNode returnType;
    IdentifierNode name;
    List<FormalParameterNode> parameters;

    boolean isNamed;
    boolean isConst;
    IdentifierNode constructName;
    List<InitializerNode> initializers;
    RedirectionNode redirection;

    public SignatureNode(Element element) {
        super(element);
        this.isConstruct = Boolean.parseBoolean(element.getAttribute("isConstruct"));
        if(isConstruct){
            this.isConst = Boolean.parseBoolean(element.getAttribute("isConst"));
            this.isNamed = Boolean.parseBoolean(element.getAttribute("isNamed"));
            if(this.isNamed){
                constructName = new IdentifierNode(unlink(element, "constructName"));
            }
            if(element.getElementsByTagName("initializers").getLength() > 0){
                initializers = new ArrayList<>(); //TODO сделать
            }
            if(element.getElementsByTagName("redirection").getLength() > 0){
                redirection = new RedirectionNode(unlink(element, "redirection"));
            }
        } else {
            this.isStatic = Boolean.parseBoolean(element.getAttribute("isStatic"));
            this.returnType = new TypeNode(unlink(element, "returnType"));
        }
        name = new IdentifierNode(unlink(element, "name"));

        if(element.getElementsByTagName("parameters").getLength() > 0){
            parameters = new ArrayList<>(); //TODO доделать
        }
    }
}
