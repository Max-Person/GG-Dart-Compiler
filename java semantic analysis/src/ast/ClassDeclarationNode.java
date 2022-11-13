package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class ClassDeclarationNode extends Node implements ClasslikeDeclaration{

    boolean isAlias;
    boolean isAbstract;
    TypeNode _super;
    List<TypeNode> mixins = new ArrayList<>();
    List<TypeNode> interfaces = new ArrayList<>();
    IdentifierNode name;

    List<ClassMemberDeclarationNode> classMembers = new ArrayList<>();

    public ClassDeclarationNode(Element element) {
        super(element);
        isAbstract = Boolean.parseBoolean(element.getAttribute("isAbstract"));
        isAlias = Boolean.parseBoolean(element.getAttribute("isAlias"));

        if(element.getElementsByTagName("super").getLength() > 0){
            _super = new TypeNode(unlink(element, "super"));
        }

        if(element.getElementsByTagName("mixins").getLength() > 0){
            unlinkList(element, "mixins").forEach(e -> mixins.add(new TypeNode(e)));
        }

        if(element.getElementsByTagName("interfaces").getLength() > 0){
            unlinkList(element, "interfaces").forEach(e -> interfaces.add(new TypeNode(e)));
        }

        if(!isAlias && element.getElementsByTagName("classMembers").getLength() > 0){
            unlinkList(element, "classMembers").forEach(e -> classMembers.add(new ClassMemberDeclarationNode(e)));
        }

        name = new IdentifierNode(unlink(element, "name"));

    }
    
    
    
    @Override
    public String name() {
        return name.stringVal;
    }
}
