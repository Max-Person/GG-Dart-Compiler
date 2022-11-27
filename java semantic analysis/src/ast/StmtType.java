package ast;

public enum StmtType {
    block,
    expr_statement,
    variable_declaration_statement,
    forN_statement,
    forEach_statement,
    while_statement,
    do_statement,
    switch_statement,
    if_statement,
    break_statement,
    continue_statement,
    return_statement,
    local_function_declaration;

    @Override
    public String toString() {
        if(this == block){
            return "block";
        }
        if(this == expr_statement){
            return "expr statement";
        }
        if(this == variable_declaration_statement){
            return "variable declaration statement";
        }
        if(this == forN_statement){
            return "forN statement";
        }
        if(this == forEach_statement){
            return "forEach statement";
        }
        if(this == while_statement){
            return "while statement";
        }
        if(this == do_statement){
            return "do statement";
        }
        if(this == switch_statement){
            return "switch statement";
        }
        if(this == if_statement){
            return "if statement";
        }
        if(this == break_statement){
            return "break statement";
        }
        if(this == continue_statement){
            return "continue statement";
        }
        if(this == return_statement){
            return "return statement";
        }
        if(this == local_function_declaration){
            return "local function declaration";
        }
        return "";
    }
}
