package ast;

import org.w3c.dom.Element;

enum TypeType{
    _void,
    _named,
    _list,
}
public class TypeNode extends Node{

    TypeType type;

    boolean isNullable;
    TypeNode listValueType;

    IdentifierNode name;

    public TypeNode(Element element) {
        super(element);
        type = TypeType.valueOf(element.getAttribute("type"));
        if(type == TypeType._void){
            return;
        }
        if(type == TypeType._named){
            name = new IdentifierNode(unlink(element, "name"));
        }
        if (type == TypeType._list){
            listValueType = new TypeNode(unlink(element, "listValueType"));
        }

        isNullable = Boolean.parseBoolean(element.getAttribute("isNullable"));
    }
}
