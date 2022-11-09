import ast.Node;
import ast.TopLevelDeclarationNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("xmlOutput.xml")); // путь к файлу
        document.normalize();

        Node.unlinkRoot(document.getDocumentElement(), "topLevelDeclaration_node").forEach(e->Node.topLevelDeclarationNodes.add(new TopLevelDeclarationNode(e)));

    }
}
