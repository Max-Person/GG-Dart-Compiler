import ast.Node;
import ast.RootNode;
import ast.TopLevelDeclarationNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args){
        if(args.length == 0){
            System.err.println("Java Segment (semantic) ERR: provide path to parser's xmlOutput file.");
            return;
        }
        
        Document document = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(args[0]));
            document.normalize();
        } catch (Exception e) {
            System.err.println("Java Segment (semantic) ERR: " + e.getLocalizedMessage().toUpperCase());
            return;
        }
        
        try{
            RootNode root = new RootNode(document.getDocumentElement());
        } catch (Exception e) {
            System.err.println("Java Segment (semantic) ERR: " + e.getLocalizedMessage().toUpperCase());
            return;
        }
        
        System.out.println("Java Segment (semantic): tree building SUCCESS!");

    }
}
