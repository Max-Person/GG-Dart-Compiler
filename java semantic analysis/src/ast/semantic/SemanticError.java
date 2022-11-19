package ast.semantic;

public class SemanticError extends IllegalStateException{
    public SemanticError(String s) {
        super(s);
    }
}
