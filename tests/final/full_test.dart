enum digit{
  digit_0, digit_1, digit_2, digit_3, digit_4, digit_5, digit_6, digit_7, digit_8, digit_9
}

void main() {
 
  List<int> numbers = [1, 2, 3];
  print("Цикл for(;;)");
  for(int i = 0; i < numbers.length; i++){ //цикл for(;;)
    print(numbers[i]);
  }    
  
  print("\n");
  print("Массив до вставки нового элемента");
  print(numbers);
  
  print("\n");
  numbers.insert(2, 10);
  print("Массив после вставки нового элемента");
  print(numbers);
  
  print("\n");
  numbers.add(5);
  print("Массив после вставки нового элемента в конец массива");
  print(numbers);
  
  print("\n");
  numbers.remove(5);
  print("Массив после удаления последнего элемента");
  print(numbers);
 
  
  print("\n");
  print("Цикл for(in)");
  for(int number in numbers){
    print(number);
  }   
  
  List<List<String>> strings = [
    ["fisrt", "second", "third"],
    ["apple", "peach", "tomato"],                           
    ["pen", "pencil", "bunch"]];
  
  print("\n");
  for(int i = 0; i < strings.length; i++){
    for(int j = 0; j < strings[i].length; j++){
      print(strings[i][j]);
    }
  }
  
  print("\n");
  numbers = [];
  print("Пустой массив");
  print(numbers);
  
  print("\n");
  int j = 5;
  print("Цикл while");
  while(j>0){
    j--;
    if(j == 5){
      print("j = $j");
      continue;
    } else if(j == 1){
      print("j = $j, break in while");
      break;
    }
  }
  
  print("\n");
  j = 5;
  print("Цикл do while");
  do{
    j--;
    if(j == 4){
      print("j = $j, continue stmt in do while");
      continue;
      print("continue");
    } else if(j == 1){
      print("break stmt");
      break;
      print("break");
    }
  } while(j > 0);
  
  print(digit.digit_0);
  
  print("\n");
  // Testing local variables
  print('Input a: ');
  var a = readInt();
  print('Input b: ');
  var b = readDouble();
  print('a = $a, b  = $b');
  
  print('Input boolean c: ');
  var c = readBool();
  print(c);

  print('Input str: ');
  var str = readString();
  print("THIS IS INPUT $str");
  
  print("");

  var customer = Customer("bezkoder", 26, "US"); // Customer [name=bezkoder,age=26,location=US]
  print("customer name = ${customer.name}, age = ${customer.age}, location = ${customer.location}");

  var customer1 = Customer.withoutLocation("zkoder", 26); // Customer [name=zkoder,age=26,location=null]
  print("customer1 name = ${customer1.name}, age = ${customer1.age}, location = ${customer1.location}");
  
  var customer2 = Customer.empty();
  print("customer2 name = ${customer2.name}, age = ${customer2.age}, location = ${customer2.location}");
  
  var customer3 = Customer.withoutLocation2("zkode", 27);
  print("customer3 name = ${customer3.name}, age = ${customer3.age}, location = ${customer3.location}");

  print("");

  var car = new Car("a", "2002");
  print("car model = ${car.model}, yearMade = ${car.yearMade}");
  var car2 = new Car.withoutYearMade("car");
  print("car2 model = ${car2.model}, yearMade = ${car2.yearMade}");
  var car3 = Car.without();
  print("car3 model = ${car3.model}, yearMade = ${car3.yearMade}");
  var car4 = Car.cool("cool", "3000");

  digit first = digit.digit_1;
  digit second = digit.digit_2;

  print("");

  if(first == digit.digit_1){
    print("equals!");
  }
  if(first != second){
    print("not equal!");
  }

  print("");

  bool t = true;
  if(t){
	  print("a = $t");
  }
  if(!t){
	  print("!a = $t");
  }
  if(t && getTrue()){
	  print("a AND getTrue()");
  }
  if(t || getTrue()){
	  print("a OR getTrue()");
  }

  print("");

  hello();

  print("");

  int s = 0;
  s = sum(1, 1);
  print("s = " + s.toString());

}

class Customer {
  String? name;
  int? age;
  String? location;

  // constructor
  Customer(String name, int age, String location) {
    this.name = name;
    this.age = age;
    this.location = location;
  }

  // Named constructor - for multiple constructors
  Customer.withoutLocation(this.name, this.age) {
    this.name = name;
    this.age = age;
  }

  Customer.empty() {
    name = "";
    age = 0;
    location = "";
  }

  Customer.withoutLocation2(String name, int age) : this(name, age, "");
}

class Car {
	String? model;
  String? yearMade;
   
  Car(this.model, this.yearMade);
  Car.withoutYearMade(this.model): yearMade = "2005";
  Car.without() : this.withoutYearMade("hgh");
  Car.cool(String model, String yearMade) : model = model, yearMade = yearMade{
    print("car created!");
    print("car model = $model, yearMade = $yearMade");
  }
}

bool getTrue(){
	print("getTrue called");
	return true;
}

bool getFalse(){
	print("getFalse called");
	return false;
}

void hello(){
  print("Hello!");
}

int sum(int a, int b){
    return a + b;
}