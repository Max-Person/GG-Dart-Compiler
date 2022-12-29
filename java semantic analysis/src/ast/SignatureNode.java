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
            if(Node.getImmediateChildByName(element, "initializers") != null){
                unlinkList(element, "initializers").forEach(e -> initializers.add(new InitializerNode(e)));
            }
            if(Node.getImmediateChildByName(element, "redirection") != null){
                redirection = new RedirectionNode(unlink(element, "redirection"));
            }
        } else {
            this.isStatic = Boolean.parseBoolean(element.getAttribute("isStatic"));
            this.returnType = new TypeNode(unlink(element, "returnType"));
        }
        name = new IdentifierNode(unlink(element, "name"));

        if(Node.getImmediateChildByName(element, "parameters") != null){
            unlinkList(element, "parameters").forEach(e -> parameters.add(new FormalParameterNode(e)));
        }
    }
    
    public SignatureNode(){
        this.isStatic = false;
        this.isConstruct = false;
        this.returnType = null;
        this.name = null;
        this.isNamed = false;
        this.constructName = null;
    }
}
