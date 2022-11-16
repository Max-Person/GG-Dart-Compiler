package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class SignatureNode extends Node{
    
    public boolean isStatic;
    public boolean isConstruct;
    
    public TypeNode returnType;
    public IdentifierNode name;
    public List<FormalParameterNode> parameters = new ArrayList<>();
    
    public boolean isNamed;
    public boolean isConst;
    public IdentifierNode constructName;
    public List<InitializerNode> initializers = new ArrayList<>();
    public RedirectionNode redirection;

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
                unlinkList(element, "initializers").forEach(e -> initializers.add(new InitializerNode(e))); //TODO сделать
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
            unlinkList(element, "parameters").forEach(e -> parameters.add(new FormalParameterNode(e))); //TODO доделать
        }
    }
}
