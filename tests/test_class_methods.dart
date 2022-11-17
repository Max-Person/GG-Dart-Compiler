class Cat {


  void sayMeow(){ // void без параметров
    print("Meow!");
  }

  void sayHello(String name) { // void с параметрами
    print("Hello $name");
  }

  static int sum(int a, int b)  { // static с параметрами и возвращаемым значенеием
    return a + b;
  }

  static void name(){ // static без параметров и возвращаемого значения
    print("Cat");
  }

  static void name(String name,){
    print("Cat");
  }

  static void name(String name,) => print("cat $name");

}

void main() {
  Cat tom = new Cat(); // Create a Cat object.  
  tom.sayHello("Jerry"); // Call the method through the object.
}