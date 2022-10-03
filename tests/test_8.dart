enum Operation{
  add, 
  subtract, 
  multiply
}
void main (){
  runOperation(11, 5, Operation.multiply);
}
 
void runOperation(int x, int y, Operation op)
{
    int result = 0;
  
        switch (op){
            case Operation.add:
                result = x + y;
                break;
            case Operation.subtract:
                result = x - y;
                break;
            case Operation.multiply:
                result = x * y;
                break;
    }
  
    print("Result $result");
}