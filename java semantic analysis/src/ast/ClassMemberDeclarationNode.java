package ast;

import org.w3c.dom.Element;

enum ClassMemberDeclarationType {
    field,
    constructSignature,
    methodSignature,
    methodDefinition,
}

public class ClassMemberDeclarationNode extends Node{

    ClassMemberDeclarationType type;

    VariableDeclarationNode fieldDecl;

    SignatureNode signature;
    StmtNode body;

    public ClassMemberDeclarationNode(Element element) {
        super(element);
        type = ClassMemberDeclarationType.valueOf(element.getAttribute("type"));

        if(type == ClassMemberDeclarationType.field){
            fieldDecl = new VariableDeclarationNode(unlink(element, "fieldDecl"));
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
