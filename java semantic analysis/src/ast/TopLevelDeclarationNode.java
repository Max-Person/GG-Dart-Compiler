package ast;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class TopLevelDeclarationNode extends Node{
    
    public TopLevelDeclarationType type;
    public ClassDeclarationNode classDecl;
    public FunctionDefinitionNode functionDecl;
    public EnumNode enumDecl;
    public List<VariableDeclarationNode> variableDecl = new ArrayList<>();

    public TopLevelDeclarationNode(Element element) {
        super(element);
        type = TopLevelDeclarationType.valueOf(element.getAttribute("type"));

        if(type == TopLevelDeclarationType._enum){
            enumDecl = new EnumNode(unlink(element, "enumDecl"));
        } else if(type == TopLevelDeclarationType._function){
            functionDecl = new FunctionDefinitionNode(unlink(element, "functionDecl"));
        } else if(type == TopLevelDeclarationType._class){
            classDecl = new ClassDeclarationNode(unlink(element, "classDecl"));
        } else {
            unlinkList(element, "variableDecl").forEach(e -> variableDecl.add(new VariableDeclarationNode(e)));
        }
    }
}
