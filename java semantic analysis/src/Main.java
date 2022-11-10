import ast.Node;
import ast.RootNode;
import ast.TopLevelDeclarationNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("S:\\engRoute\\Dart Compiler\\Debug\\xmlOutput.xml")); // путь к файлу
        document.normalize();
    
        RootNode root = new RootNode(document.getDocumentElement());

    }
}
