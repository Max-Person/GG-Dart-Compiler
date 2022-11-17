void main (){
     
  Employee bob = Employee("Bob", "Google");
  bob.display();  // Name: Bob
  bob.work();     // Works in Google
} 
 
class Person{
  
  String name;
  Person(this.name);
  void display(){
    print("Name: $name");
  }
}

class Worker{
  String company = "";
  void work(){
    print("Work in $company");
  }
}
class Employee extends Person with Worker{
     
  Employee(int name, int comp) : super(name){
    company = comp;
  }
}