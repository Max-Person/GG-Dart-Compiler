package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

enum ClassMemberDeclarationType {
    field,
    constructSignature,
    methodSignature,
    methodDefinition,
}

public class ClassMemberDeclarationNode extends Node{

    ClassMemberDeclarationType type;

    List<VariableDeclarationNode> fieldDecl = new ArrayList<>();

    SignatureNode signature;
    StmtNode body;

    public ClassMemberDeclarationNode(Element element) {
        super(element);
        type = ClassMemberDeclarationType.valueOf(element.getAttribute("type"));

        if(type == ClassMemberDeclarationType.field){
            unlinkList(element, "fieldDecl").forEach(e -> fieldDecl.add(new VariableDeclarationNode(e)));
            return;
        }
        if(type == ClassMemberDeclarationType.methodSignature){
            signature = new SignatureNode(unlink(element, "signature"));
            return;
        }
        if(type == ClassMemberDeclarationType.methodDefinition){
            signature = new SignatureNode(unlink(element, "signature"));
            body = new StmtNode(unlink(element, "body"));
        }
    }
}
