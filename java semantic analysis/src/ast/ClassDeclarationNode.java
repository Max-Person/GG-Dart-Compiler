package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class ClassDeclarationNode extends Node implements ClasslikeDeclaration{

    public boolean isAlias;
    public boolean isAbstract;
    public TypeNode _super;
    public List<TypeNode> mixins = new ArrayList<>();
    public List<TypeNode> interfaces = new ArrayList<>();
    public IdentifierNode name;
    
    public List<ClassMemberDeclarationNode> classMembers = new ArrayList<>();

    public ClassDeclarationNode(Element element) {
        super(element);
        isAbstract = Boolean.parseBoolean(element.getAttribute("isAbstract"));
        isAlias = Boolean.parseBoolean(element.getAttribute("isAlias"));

        if(Node.getImmediateChildByName(element, "super") != null){
            _super = new TypeNode(unlink(element, "super"));
        }

        if(Node.getImmediateChildByName(element, "mixins") != null){
            unlinkList(element, "mixins").forEach(e -> mixins.add(new TypeNode(e)));
        }

        if(Node.getImmediateChildByName(element, "interfaces") != null){
            unlinkList(element, "interfaces").forEach(e -> interfaces.add(new TypeNode(e)));
        }

        if(!isAlias && Node.getImmediateChildByName(element, "classMembers") != null){
            unlinkList(element, "classMembers").forEach(e -> classMembers.add(new ClassMemberDeclarationNode(e)));
        }

        name = new IdentifierNode(unlink(element, "name"));

    }
    
    
    
    @Override
    public String name() {
        return name.stringVal;
    }
    
    @Override
    public int lineNum() {
        return lineNum;
    }
}
