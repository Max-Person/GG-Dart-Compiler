import ast.RootNode;
import ast.semantic.SemanticCrawler;
import ast.semantic.SemanticError;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if(args.length == 0){
            System.err.println("Java Segment (semantic) ERR: provide path to parser's xmlOutput file.");
            return;
        }
        
        Document document = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(args[0]));
            document.normalize();
        } catch (Exception e) {
            System.out.println("Java Segment (semantic) ERR: " + e.getLocalizedMessage());
            e.printStackTrace();
            return;
        }
        
        RootNode root;
        try{
            root = new RootNode(document.getDocumentElement());
        } catch (Exception e) {
            System.out.println("Java Segment (semantic) ERR: " + e.getLocalizedMessage());
            e.printStackTrace();
            return;
        }
    
        System.out.println("Java Segment (semantic): tree building SUCCESS!");
    
        SemanticCrawler crawler = new SemanticCrawler();
        try{
            crawler.analyze(root);
        }
        catch (SemanticError e){
            //System.out.println(e.getLocalizedMessage());
            return;
        }
        catch (Exception e) {
            System.out.println("Java Segment (semantic) ERR: " + e.getLocalizedMessage());
            e.printStackTrace();
            return;
        }
    
        System.out.println("Java Segment (semantic): semantic analysis SUCCESS!");
        System.out.println("Java Segment (semantic): Class Table description:\n");
        System.out.println(crawler.describe());
    
        crawler.writeClassesAsBytecode();
    
    }
}
