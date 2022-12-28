class A{
  
}


void main(){
  
  List<A> arr = [new A(), A(), new A()];
  
  for(A a in arr){
    print("hi");
  }

  List<int> numbers = [1, 2 , 3 , 4];

  for(int number in numbers){
    print(number);
  }
  
}