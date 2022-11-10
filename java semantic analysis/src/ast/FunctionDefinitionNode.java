package ast;

import org.w3c.dom.Element;

public class FunctionDefinitionNode extends Node {

    SignatureNode signature;
    StmtNode body;

    public FunctionDefinitionNode(Element element) {
        super(element);
        signature = new SignatureNode(unlink(element, "signature"));
        body = new StmtNode(unlink(element, "body"));
    }
}
