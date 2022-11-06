enum Operation{
  
    add, 
    subtract, 
    multiply
}
void main (){

    // создаем переменную перечисления Operation
    Operation op;
    // присваиваем значение
    op = Operation.add; 
      
    print(Operation.multiply);          
    print(Operation.multiply.index);    
}