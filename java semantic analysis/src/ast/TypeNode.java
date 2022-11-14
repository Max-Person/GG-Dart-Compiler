package ast;

import org.w3c.dom.Element;

import java.util.Objects;

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

    public TypeNode(String name, boolean isNullable) {
        super();
        this.type = TypeType._named;
        this.name = new IdentifierNode(name);
        this.isNullable = isNullable;
    }

    public TypeNode(TypeNode listValueType, boolean isNullable) {
        super();
        this.type = TypeType._list;
        this.listValueType = listValueType;
        this.isNullable = isNullable;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeNode typeNode = (TypeNode) o;
        return isNullable == typeNode.isNullable && type == typeNode.type && Objects.equals(listValueType, typeNode.listValueType) && Objects.equals(name.stringVal, typeNode.name.stringVal);
    }
}
