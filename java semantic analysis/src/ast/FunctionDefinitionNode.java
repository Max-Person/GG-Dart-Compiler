package ast;

import org.w3c.dom.Element;

public class FunctionDefinitionNode extends Node implements Declaration{

    SignatureNode signature;
    StmtNode body;

    public FunctionDefinitionNode(Element element) {
        super(element);
        signature = new SignatureNode(unlink(element, "signature"));
        body = new StmtNode(unlink(element, "body"));
    }
    
    @Override
    public String name() {
        return signature.name.stringVal; //TODO что делать с конструкторами и статическими методами
    }
}
